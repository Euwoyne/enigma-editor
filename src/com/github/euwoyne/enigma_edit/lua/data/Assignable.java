
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

package com.github.euwoyne.enigma_edit.lua.data;

/**
 * A reference that can be redirected by assigning a different value.
 * This interface provides an additional method for the assignment.
 * Any {@link Dereferencable} value, that can also be assigned to, should implement this interface instead.
 */
public interface Assignable extends Dereferencable
{
	/**
	 * Assign a new value for this non-value to reference.
	 * 
	 * @param value   The new underlying value.
	 * @param assign  Assignment source code.
	 * @param mode    Mode to use the value for.
	 */
	public void assign(Source value, CodeSnippet assign, Mode mode);
}

