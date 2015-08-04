
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

import enigma_edit.lua.Constants;

/**
 * A special {@link SimpleValue} of type integer, that holds a predefined constant.
 * In addition to the numerical value stored as SimpleValue, the representation of
 * the underlying value as a Enigma API constant is saved as a, instance of
 * {@link Constants}.
 */
public class ConstValue extends SimpleValue
{
	/** the constant */
	Constants constant;
	
	/**
	 * Creates a {@link SimpleValue} from the numerical value of the given constant
	 * enum. The given {@code constval} will be stored.
	 * 
	 * @param constval  Enigma API constant to be stored.
	 * @param code      Code location of the reference to this constant.
	 */
	public ConstValue(Constants constval, CodeSnippet code)
	{
		super(LuaValue.valueOf(constval.value()), code);
		this.constant = constval;
	}
	
	@Override
	public String toString_noquote() {return constant.toString();}
	
	@Override
	public String toString() {return constant.toString();}
}

