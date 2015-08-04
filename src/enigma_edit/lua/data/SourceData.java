
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
 * Base class for any data, that is defined by a piece of code.
 * This class contains a {@link CodeSnippet} pointing to the defining code.
 */
public abstract class SourceData extends Data implements Source
{
	protected CodeSnippet code;
	public SourceData(CodeSnippet code) {this.code = code;}
	
	@Override public CodeSnippet getCode()            {return code;}
	@Override public String      typename()           {return "<source>";}
	@Override public String      typename(Mode2 mode) {return "<source>";}
	@Override public SourceData  snapshot()           {return this;}

	@Override public Value       checkValue(Mode2 mode)     {return null;}
	@Override public Nil         checkNil(Mode2 mode)       {return null;}
	@Override public SimpleValue checkSimple(Mode2 mode)    {return null;}
	@Override public Table       checkTable(Mode2 mode)     {return null;}
	@Override public TilePart    checkTilePart(Mode2 mode)  {return null;}
	@Override public TileDecl    checkTile(Mode2 mode)      {return null;}
	@Override public Resolver    checkResolver(Mode2 mode)  {return null;}
}

