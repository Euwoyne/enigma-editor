
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

import enigma_edit.error.LevelLuaException;
import enigma_edit.lua.Constants;

/**
 * Any part of a tile declaration (that is not a reference to another tile).
 * Obviously this should be referencing a {@link Table}.
 */
public class ObjectDecl extends SourceData implements TileDeclPart
{
	/** wrapped source instance */
	private final Source source;
	
	/** wrapped table */
	private final MMTable table;
	private final String  easyKind;
	private final String  diffKind;
	
	/**
	 * Construct an instance with exactly the same source as a given tile part.
	 * The member will just be transfered, not copied. This constructor is useful
	 * for creating subclasses by extending an existing {@code Construct} instance.
	 * 
	 * @param parent  The source instance.
	 */
	protected ObjectDecl(ObjectDecl parent)
	{
		super(parent.code);
		this.source = parent.source;
		this.table = parent.table;
		this.easyKind = parent.easyKind;
		this.diffKind = parent.diffKind;
	}
	
	/**
	 * Construct a miscellaneous tile part from a {@link Source} object.
	 * On construction the given source is checked, if it really references
	 * a table. Then {@link Constants#checkTable} is applied to the table,
	 * to convert any integers, that can be represented by constants.
	 * 
	 * @param source  source to be referenced by a tile declaration via this object.
	 * @throws LevelLuaException.Runtime  indicates, that the given {@code source} is not a {@link Table}.
	 */
	public ObjectDecl(Source source) throws LevelLuaException.Runtime
	{
		super(source.getCode());
		this.source = source;
		this.table = source.checkTable();
		if (!table.hasEasy() || !table.easy.hasEasy(1))
			throw new LevelLuaException.Runtime("IllegalTilePart", table.isNormal() ? Mode.NORMAL : Mode.EASY, source.typename(Mode2.EASY), code);
		else
			Constants.checkTable(table.easy);
		if (!table.hasDifficult() || !table.difficult.hasDifficult(1))
			throw new LevelLuaException.Runtime("IllegalTilePart", table.isNormal() ? Mode.NORMAL : Mode.DIFFICULT, source.typename(Mode2.DIFFICULT), code);
		else
			Constants.checkTable(table.difficult);
		final SimpleValue easy = table.easy.get(1).checkSimple(Mode2.EASY);
		final SimpleValue diff = table.easy.get(1).checkSimple(Mode2.DIFFICULT);
		if (easy == null) throw new LevelLuaException.Runtime("IllegalTilePart", table.isNormal() ? Mode.NORMAL : Mode.EASY, source.typename(Mode2.EASY), code);
		if (diff == null) throw new LevelLuaException.Runtime("IllegalTilePart", table.isNormal() ? Mode.NORMAL : Mode.DIFFICULT, source.typename(Mode2.DIFFICULT), code);
		this.easyKind = easy.toString();
		this.diffKind = diff.toString();
	}
	
	          public String          getKind(Mode2 mode)            {return mode == Mode2.EASY ? easyKind : diffKind;}
	@Override public ObjectDecl       getObject(int idx, Mode2 mode) {return this;}
	@Override public MMTileConstruct getObject(int idx)             {return new MMTileConstruct(this);}
	@Override public int             objectCount(Mode2 mode)        {return 1;}
	@Override public String          typename()                     {return "<tile-constructor>";}
	@Override public String          toString()                     {return source.toString();}
	@Override public Table           checkTable(Mode2 mode)         {return mode == Mode2.EASY ? table.easy : table.difficult;}
	@Override public MMTable         checkTable()                   {return table;}
	@Override public TileDeclPart        checkTilePart(Mode2 mode)      {return this;}
	@Override public MMTilePart      checkTilePart()                {return new MMTilePart(this);}
	@Override public ObjectDecl       snapshot()                     {return new ObjectDecl(source.snapshot());}
}

