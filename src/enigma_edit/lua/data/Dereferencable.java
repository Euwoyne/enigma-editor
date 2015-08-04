
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
 * Any construct that may reference different values when evaluated in different modes.
 * This interface provides mode-dependent dereferencing. 
 */
public interface Dereferencable extends Typed
{
	/**
	 * Return the data that this non-value references.
	 * 
	 * @param mode  Mode to fetch the value for.
	 * @return      The data referenced by this object.
	 */
	public Source deref(Mode2 mode);
	
	/**
	 * Get the type-name of the underlying value.
	 * This should be equivalent to calling {@link Data#typename} on the
	 * result of {@link #deref}. 
	 * 
	 * @param mode  Mode to fetch the type for.
	 * @return      The type-name of the underlying value.
	 */
	public String typename(Mode2 mode);
}

