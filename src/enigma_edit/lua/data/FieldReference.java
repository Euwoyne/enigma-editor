
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

/**
 * This is a special {@link Reference} that points to a member of a table.
 * It can even reference non existing table entries, to allow the creation
 * of them through the {@link #assign(Source, CodeSnippet, Mode)}
 * method. 
 */
public class FieldReference extends SourceData implements Assignable
{
	protected final Indexed table;
	protected final String  key;
	
	/**
	 * Constructor.
	 * 
	 * @param table  Table hosting the referenced value.
	 * @param key    Key corresponding to the referenced value.
	 * @param code   Code location of this reference.
	 */
	public FieldReference(Indexed table, String key, CodeSnippet code)
	{
		super(code);
		this.table = table;
		this.key = key;
	}
	
	@Override public void assign(Source value, CodeSnippet assign, Mode mode)
	{
		table.assignI(key, value, assign, mode);
	}
	
	@Override public Source         deref(Mode2 mode)          {return table.derefI(key, mode);}
	@Override public String         typename(Mode2 mode)       {final Source val = table.getValue(key, mode); return val != null ? val.typename(mode) : "nil";}
	
	@Override public String         typename()                 {return "<field-reference>";}
	@Override public FieldReference snapshot()                 {return new FieldReference(table.snapshot(), key, code);}
	
	/*
	 * type checking
	 */
	@Override public Value          checkValue(Mode2 mode)     {final Source val = table.derefI(key, mode); return val != null ? val.checkValue(mode)    : null;}
	@Override public Nil            checkNil(Mode2 mode)       {final Source val = table.derefI(key, mode); return val != null ? val.checkNil(mode)      : new Nil(code);}
	@Override public SimpleValue    checkSimple(Mode2 mode)    {final Source val = table.derefI(key, mode); return val != null ? val.checkSimple(mode)   : null;}
	@Override public Table          checkTable(Mode2 mode)     {final Source val = table.derefI(key, mode); return val != null ? val.checkTable(mode)    : null;}
	@Override public TilePart       checkTilePart(Mode2 mode)  {final Source val = table.derefI(key, mode); return val != null ? val.checkTilePart(mode) : null;}
	@Override public TileDecl       checkTile(Mode2 mode)      {final Source val = table.derefI(key, mode); return val != null ? val.checkTile(mode)     : null;}
	@Override public Resolver       checkResolver(Mode2 mode)  {final Source val = table.derefI(key, mode); return val != null ? val.checkResolver(mode) : null;}
	
	/*
	 * mode checking
	 */
	@Override public boolean isNormal()      {return table.isNormalI(key);}
	@Override public boolean isMixed()       {return table.isMixedI(key);}
	@Override public boolean isComplete()    {return table.isCompleteI(key);}
	@Override public boolean isNull()        {return table.isNullI(key);}
	@Override public boolean onlyEasy()      {return table.onlyEasyI(key);}
	@Override public boolean onlyDifficult() {return table.onlyDifficultI(key);}
	@Override public boolean hasEasy()       {return table.hasEasyI(key);}
	@Override public boolean hasDifficult()  {return table.hasDifficult(key);}
	@Override public boolean hasNormal()     {return table.hasNormal(key);}
}

