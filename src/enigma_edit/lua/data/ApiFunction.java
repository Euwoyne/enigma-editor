
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

import java.util.List;

import enigma_edit.error.LevelLuaException;

/**
 * Functions, that can be called from lua code. 
 */
public interface ApiFunction
{
	/**
	 * Calls the API function.
	 * 
	 * @param args  Arguments to the function.
	 * @param mode  Mode that the function is called in.
	 * @param code  Location of the function call.
	 * @return      The functions return value.
	 * 
	 * @throws LevelLuaException.Runtime  Typically thrown, if the given arguments ({@code args}) are invalid.
	 */
	Source call(List<Source> args, Mode mode, CodeSnippet code);
}

