
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
 * This represents a reference to a {@link Variable} within the code.
 * It just contains a reference to said {@link Variable} and the code
 * location of the reference.
 */
public class Reference extends SourceData implements Assignable
{
	/** referenced variable */
	Variable target;
	
	/**
	 * Create a reference to a variable.
	 * 
	 * @param target  Variable referenced by the code. 
	 * @param code    Code referencing the variable.
	 */
	public Reference(Variable target, CodeSnippet code)
	{
		super(code);
		this.target = target == null ? null : target.snapshot();
	}
	
	@Override public void assign(Source value, CodeSnippet assign, Mode mode)
	{
		target.assign(value, assign, mode);
	}
	
	@Override public Source      deref(Mode2 mode)          {return target != null ? target.deref(mode) : new Nil(code);}
	@Override public String      typename(Mode2 mode)       {return target != null ? target.typename(mode) : "nil";}
	
	@Override public String      typename()                 {return "<reference>";}
	@Override public Reference   snapshot()                 {return target != null ? new Reference(target.snapshot(), code) : this;}
	@Override public String      toString()                 {return target != null ? target.toString() : "nil";}
	
	@Override public Value       checkValue(Mode2 mode)     {return target != null ? target.checkValue(mode) : null;}
	@Override public Nil         checkNil(Mode2 mode)       {return target != null ? target.checkNil(mode) : new Nil(code);}
	@Override public SimpleValue checkSimple(Mode2 mode)    {return target != null ? target.checkSimple(mode) : null;}
	@Override public Table       checkTable(Mode2 mode)     {return target != null ? target.checkTable(mode) : null;}
	@Override public TilePart    checkTilePart(Mode2 mode)  {return target != null ? target.checkTilePart(mode) : null;}
	@Override public TileDecl    checkTile(Mode2 mode)      {return target != null ? target.checkTile(mode) : null;}
	@Override public Resolver    checkResolver(Mode2 mode)  {return target != null ? target.checkResolver(mode) : null;}
	
	/*
	 * mode checking
	 */
	@Override public boolean isNormal()      {return target.isNormal();}
	@Override public boolean isMixed()       {return target.isMixed();}
	@Override public boolean isComplete()    {return target.isComplete();}
	@Override public boolean isNull()        {return target.isNull();}
	@Override public boolean onlyEasy()      {return target.onlyEasy();}
	@Override public boolean onlyDifficult() {return target.onlyDifficult();}
	@Override public boolean hasEasy()       {return target.hasEasy();}
	@Override public boolean hasDifficult()  {return target.hasDifficult();}
	@Override public boolean hasNormal()     {return target.hasNormal();}
}

