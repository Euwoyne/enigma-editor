
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

import org.luaj.vm2.LuaValue;

/**
 * A simple value (directly representable by {@link LuaValue}).
 */
public class SimpleValue extends Value
{
	public LuaValue value;
	
	public SimpleValue(LuaValue value, CodeSnippet code)
	{
		super(code);
		this.value = value;
	}
	
	public String toString_noquote()
	{
		return value.toString();
	}
	
	@Override public String typename()           {return value.typename();}
	@Override public String typename(Mode2 mode) {return value.typename();}
	
	@Override
	public String toString()
	{
		if      (value.isnumber()) return value.toString();
		else if (value.isstring()) return '"' + value.toString() + '"';
		return value.toString();
	}
	
	@Override public MMSimpleValue checkSimple()           {return new MMSimpleValue(this);}
	@Override public SimpleValue   checkSimple(Mode2 mode) {return this;}
}

