
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
 * Interface to a lua table.
 * This is implemented by the {@link Table} class itself and the
 * multi-mode value {@link MMTable}.
 */
public interface Indexed extends Typed
{
	@Override Indexed snapshot();
	
	/*
	 *  ELEMENT ASSIGNMENT
	 * ====================
	 */
	
	/**
	 * Raw assignment.
	 */
	Variable assignI(String idx, Source value, CodeSnippet assign, Mode mode);
	
	/**
	 * Assigns a value to the given field (string index).
	 * 
	 * @param key     Name of the field.
	 * @param value   Value to be assigned.
	 * @param assign  Assignment source code.
	 * @param mode    Mode to use the value for.
	 * 
	 * @return  The variable that was assigned to.
	 */
	Variable assign(String key, Source value, CodeSnippet assign, Mode mode);
	
	/**
	 * Assigns a value to the given field (numerical index).
	 * 
	 * @param idx     Index of the field.
	 * @param value   Value to be assigned.
	 * @param assign  Assignment source code.
	 * @param mode    Mode to use the value for.
	 * 
	 * @return  The variable that was assigned to.
	 */
	Variable assign(int idx, Source value, CodeSnippet assign, Mode mode);
	
	
	/*
	 *  ELEMENT ACCESS
	 * ================
	 */
	
	/**
	 * Raw value getter.
	 * @see #getValue(String, Mode2)
	 */
	Value getValueI(String idx, Mode2 mode);
	
	/**
	 * Get the value of a given field completely dereferencing all
	 * references. @see NonValue#getValue(Mode)
	 * 
	 * @param key   Name of the field.
	 * @param mode  Mode to fetch the value for.
	 * @return      The {@link Value} instance referenced by the identifier.
	 */
	Value getValue(String key, Mode2 mode);
	
	/**
	 * Get the value with a given index completely dereferencing all
	 * references. @see NonValue#getValue(Mode)
	 * 
	 * @param idx   Index of the field.
	 * @param mode  Mode to fetch the value for.
	 * @return      The {@link Value} instance referenced by the identifier.
	 */
	Value getValue(int idx, Mode2 mode);
	
	/**
	 * Raw value getter.
	 * @see #getValue(String)
	 */
	MMValue getValueI(String idx);
	
	/**
	 * Get the value of a given field completely dereferencing all
	 * references.
	 * 
	 * @param key   Name of the field.
	 * @return      The {@link MM} instance referenced by the identifier.
	 */
	MMValue getValue(String key);
	
	/**
	 * Get the value with a given index completely dereferencing all
	 * references.
	 * 
	 * @param idx   Index of the field.
	 * @return      The {@link MM} instance referenced by the identifier.
	 */
	MMValue getValue(int idx);
	
	/**
	 * Raw dereferencing getter.
	 * @see #deref(String, Mode2)
	 */
	Source derefI(String key, Mode2 mode);
	
	/**
	 * Get the value assigned to the field of the given name.
	 * 
	 * @param key   Name of the field.
	 * @param mode  Mode to fetch the value for.
	 * @return      The data referenced by this object.
	 */
	Source deref(String key, Mode2 mode);
	
	/**
	 * Get the value assigned to the given index.
	 * 
	 * @param idx   Index of the field.
	 * @param mode  Mode to fetch the value for.
	 * @return      The data referenced by this object.
	 */
	Source deref(int idx, Mode2 mode);
	
	
	/*
	 *  MISCELLANEOUS
	 * ===============
	 */
	
	/**
	 * Clear this instance of any data.
	 */
	void clear();
	
	
	/*
	 *  MODE CHECKING
	 * ===============
	 */
	
	/** Raw normal mode checker. */
	boolean isNormalI(String key);
	
	/** Check if a field has the same value in both modes (might be {@code nil}). */
	public boolean isNormal(String key);
	
	/** Check if an index has the same value in both modes (might be {@code nil}). */
	public boolean isNormal(int    idx);
	
	/** Raw mixed mode checker. */
	public boolean isMixedI(String key);
	
	/** Check if a field is defined in both difficult and easy mode but with differing values. */
	public boolean isMixed(String key);
	
	/** Check if an index is defined in both difficult and easy mode but with differing values. */
	public boolean isMixed(int    idx);
	
	/** Raw complete mode checker. */
	public boolean isCompleteI(String key);
	
	/** Check if a field is defined in both difficult and easy mode. */
	public boolean isComplete(String key);
	
	/** Check if an index is defined in both difficult and easy mode. */
	public boolean isComplete(int    idx);
	
	/** Raw {@code null} checker. */
	public boolean isNullI(String key);
	
	/** Check if a field is undefined in any circumstance. */
	public boolean isNull(String key);
	
	/** Check if an index is undefined in any circumstance. */
	public boolean isNull(int    idx);
	
	/** Raw easy mode checker. */
	public boolean onlyEasyI(String key);
	
	/** Check if a field is only defined in easy mode. */
	public boolean onlyEasy(String key);
	
	/** Check if a an index is only defined in easy mode. */
	public boolean onlyEasy(int    idx);
	
	/** Raw difficult mode checker. */
	public boolean onlyDifficultI(String key);
	
	/** Check if a field is only defined in difficult mode. */
	public boolean onlyDifficult(String key);
	
	/** Check if a an index is only defined in difficult mode. */
	public boolean onlyDifficult(int    idx);
	
	/** Raw easy mode checker. */
	public boolean hasEasyI(String key);
	
	/** Check if a field is defined in easy mode. */
	public boolean hasEasy(String key);
	
	/** Check if a an index is defined in easy mode. */
	public boolean hasEasy(int    idx);
	
	/** Raw difficult mode checker. */
	public boolean hasDifficultI(String key);
	
	/** Check if a field is defined in difficult mode. */
	public boolean hasDifficult(String key);
	
	/** Check if a an index is defined in difficult mode. */
	public boolean hasDifficult(int    idx);
	
	/** Raw normal mode checker. */
	public boolean hasNormalI(String key);
	
	/** Check if a field is defined in normal mode. */
	public boolean hasNormal(String key);
	
	/** Check if a an index is defined in normal mode. */
	public boolean hasNormal(int    idx);
	
	/** Raw mode existence checker. */
	public boolean existI(String key, Mode2 mode);
	
	/** Check if a field is defined in a given mode. */
	public boolean exist(String key, Mode2 mode);
	
	/** Check if a an index is defined in a given mode. */
	public boolean exist(int    idx, Mode2 mode);
}

