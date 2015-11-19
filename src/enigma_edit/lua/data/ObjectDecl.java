
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
	private final int     type;
	
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
		this.type = parent.type;
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
		if (!table.hasEasy())
			throw new LevelLuaException.Runtime("IllegalTilePart", table.isNormal() ? Mode.NORMAL : Mode.EASY, source.typename(Mode2.EASY), code);
		else
			Constants.checkTable(table.easy);
		if (!table.hasDifficult())
			throw new LevelLuaException.Runtime("IllegalTilePart", table.isNormal() ? Mode.NORMAL : Mode.DIFFICULT, source.typename(Mode2.DIFFICULT), code);
		else
			Constants.checkTable(table.difficult);
		
		byte type = 0;
		if (table.easy.hasEasy(1))
		{
			final SimpleValue easy = table.easy.get(1).checkSimple(Mode2.EASY);
			if (easy == null) throw new LevelLuaException.Runtime("IllegalTilePart", table.isNormal() ? Mode.NORMAL : Mode.EASY, source.typename(Mode2.EASY), code);
			this.easyKind = easy.toString();
			if (easyKind.length() > 3)
			{
				switch (easyKind.substring(0, 3))
				{
				case "fl_": type = FL; break;
				case "it_": type = IT; break;
				case "ac_": type = AC; break;
				case "st_": type = ST; break;
				case "ot_": type = OT; break;
				}
			}
		}
		else this.easyKind = null;
		
		if (table.difficult.hasDifficult(1))
		{
			final SimpleValue diff = table.easy.get(1).checkSimple(Mode2.DIFFICULT);
			if (diff == null) throw new LevelLuaException.Runtime("IllegalTilePart", table.isNormal() ? Mode.NORMAL : Mode.DIFFICULT, source.typename(Mode2.DIFFICULT), code);
			this.diffKind = diff.toString();
			if (diffKind.length() > 3)
			{
				switch (diffKind.substring(0, 3))
				{
				case "fl_": type |= FL << T_SIZE; break;
				case "it_": type |= IT << T_SIZE; break;
				case "ac_": type |= AC << T_SIZE; break;
				case "st_": type |= ST << T_SIZE; break;
				case "ot_": type |= OT << T_SIZE; break;
				}
			}
		}
		else this.diffKind = null;
		
		this.type = type;
	}
	
	          public String          getKind(Mode2 mode)            {return mode == Mode2.EASY ? easyKind : diffKind;}
	@Override public ObjectDecl      getObject(int idx, Mode2 mode) {return this;}
	@Override public MMObjectDecl getObject(int idx)                {return new MMObjectDecl(this);}
	@Override public int             objectCount(Mode2 mode)        {return 1;}
	@Override public String          typename()                     {return "<tile-constructor>";}
	@Override public String          toString()                     {return source.toString();}
	          public String          toString(Mode mode)            {return table.toString(mode);}
	@Override public Table           checkTable(Mode2 mode)         {return mode == Mode2.EASY ? table.easy : table.difficult;}
	@Override public MMTable         checkTable()                   {return table;}
	@Override public TileDeclPart    checkTilePart(Mode2 mode)      {return this;}
	@Override public MMTilePart      checkTilePart()                {return new MMTilePart(this);}
	@Override public ObjectDecl      snapshot()                     {return new ObjectDecl(source.snapshot());}
	@Override public int             objtype(Mode2 mode)            {return mode == Mode2.EASY ? type & T_MASK : type >> T_SIZE;}
	@Override public int             typeMask()                     {return type;}
	
	@Override public int typeMask(Mode mode)
	{
		switch(mode)
		{
		case EASY:      return type & T_MASK;
		case DIFFICULT: return type & ~T_MASK;
		default:        return type;
		}
	}
}

