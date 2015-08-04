
/*
  Enigma Editor
  Copyright (C) 2015 Dominik Lehmann
  
  Licensed under the EUPL, Version 1.1 or – as soon they
  will be approved by the European Commission - subsequent
  versions of the EUPL (the "Licence");
  You may not use this work except in compliance with the
  Licence.
  You may obtain a copy of the Licence at:
  
  https://joinup.ec.europa.eu/software/page/eupl
  
  Unless required by applicable law or agreed to in
  writing, software distributed under the Licence is
  distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  express or implied.
  See the Licence for the specific language governing
  permissions and limitations under the Licence.
*/

package enigma_edit.lua.data;

import enigma_edit.lua.Constants;
import enigma_edit.error.LevelLuaException;
import enigma_edit.lua.res.Tiles;

/**
 * Part of a tile declaration.
 * There are two types of objects that can make up a tile.
 * <ol><li>A simple tile declaration by a defining lua table (see {@link TilePart.Construct}).</li>
 * <li>A reference to an existing tile (see {@link TilePart.Ref}).</li></ol>
 */
public interface TilePart extends Source 
{
	@Override public TilePart snapshot();
	
	/**
	 * Return the {@code idx}-th part of this declaration.
	 * This is, after dereferencing all tile references, the {@code idx}-th
	 * table in the concatenation expression.
	 * 
	 * @param idx   Part index.
	 * @param mode  Mode to check for.
	 * @return      Requested tile part (as it is wrapped by {@link Construct}).
	 */
	public Construct getObject(int idx, Mode2 mode);
	
	/**
	 * Return the {@code idx}-th part of this declaration.
	 * This is, after dereferencing all tile references, the {@code idx}-th
	 * table in the concatenation expression.
	 * 
	 * @param idx   Part index.
	 * @return      Requested tile part (in both modes).
	 */
	public MMTileConstruct getObject(int idx);
	
	/**
	 * Return the number of tables this part consists of.
	 * 
	 * @param mode  Mode to check for.
	 * @return      Number of tables in this part.
	 */
	public int objectCount(Mode2 mode);
	
	/**
	 * Any part of a tile declaration (that is not a reference to another tile).
	 * Obviously this should be referencing a {@link Table}.
	 */
	public static class Construct extends SourceData implements TilePart
	{
		/** wrapped source instance */
		private final Source source;
		
		/** wrapped table */
		private final MMTable table;
		
		/**
		 * Construct an instance with exactly the same source as a given tile part.
		 * The member will just be transfered, not copied. This constructor is useful
		 * for creating subclasses by extending an existing {@code Construct} instance.
		 * 
		 * @param parent  The source instance.
		 */
		protected Construct(Construct parent)
		{
			super(parent.code);
			this.source = parent.source;
			this.table = parent.table;
		}
		
		/**
		 * Construct a miscellaneous tile part from a {@link Source} object.
		 * On construction the given source is checked, if it really references
		 * a table. Then {@link Constants.checkTable} is applied to the table,
		 * to convert any integers, that can be represented by constants.
		 * 
		 * @param source  source to be referenced by a tile declaration via this object.
		 * @throws LevelLuaException.Runtime  indicates, that the given {@code source} is not a {@link Table}.
		 */
		public Construct(Source source) throws LevelLuaException.Runtime
		{
			super(source.getCode());
			this.source = source;
			this.table = source.checkTable();
			if (!table.hasEasy())
				throw new LevelLuaException.Runtime("IllegalTilePart", table.isNormal() ? Mode.NORMAL : Mode.EASY,      source.typename(Mode2.EASY), code);
			else
				Constants.checkTable(table.easy);
			if (!table.hasDifficult())
				throw new LevelLuaException.Runtime("IllegalTilePart", table.isNormal() ? Mode.NORMAL : Mode.DIFFICULT, source.typename(Mode2.DIFFICULT), code);
			else
				Constants.checkTable(table.difficult);
		}
		
		@Override public Construct       getObject(int idx, Mode2 mode) {return this;}
		@Override public MMTileConstruct getObject(int idx)             {return new MMTileConstruct(this);}
		@Override public int             objectCount(Mode2 mode)        {return 1;}
		@Override public String          typename()                     {return "<tile-constructor>";}
		@Override public String          toString()                     {return source.toString();}
		@Override public Table           checkTable(Mode2 mode)         {return mode == Mode2.EASY ? table.easy : table.difficult;}
		@Override public MMTable         checkTable()                   {return table;}
		@Override public TilePart        checkTilePart(Mode2 mode)      {return this;}
		@Override public MMTilePart      checkTilePart()                {return new MMTilePart(this);}
		@Override public Construct       snapshot()                     {return new Construct(source.snapshot());}
	}
	
	/**
	 * This is a special {@link FieldReference}, that stores a reference to
	 * a member of the {@link Tiles Tiles}-Resolver.
	 */
	public static class Ref extends FieldReference implements TilePart
	{
		/**
		 * Constructor.
		 * 
		 * @param tiles  The tiles resolver (hosting the referenced tile).
		 * @param key    Key corresponding to the referenced tile.
		 * @param code   Code location of this reference.
		 */
		public Ref(Tiles tiles, String key, CodeSnippet code)
		{
			super(tiles, key, code);
		}
		
		@Override public Construct getObject(int idx, Mode2 mode)
		{
			final TileDecl tile = this.checkTile(mode);
			return tile != null ? tile.getObject(idx, mode) : null;
		}
		
		@Override public MMTileConstruct getObject(int idx)
		{
			final MMTileDecl tile = this.checkTile();
			return tile != null ? tile.getObject(idx) : null;
		}
		
		@Override public int         objectCount(Mode2 mode)   {final TileDecl tile = this.checkTile(mode); return tile != null ? tile.objectCount(mode) : 0;}
		@Override public CodeSnippet getCode()                 {return code;}
		@Override public TilePart    checkTilePart(Mode2 mode) {return this;}
		@Override public MMTilePart  checkTilePart()           {return new MMTilePart(this);}
		@Override public String      typename()                {return "<tile-reference>";}
		@Override public String      toString()                {return "ti[" + this.key + "]";}
		@Override public Ref         snapshot()                {return this;}
	}
}

