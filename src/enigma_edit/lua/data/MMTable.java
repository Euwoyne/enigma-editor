
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
 * Multi-mode lua table.
 * @see Table
 */
public class MMTable extends MM<Table> implements Indexed
{
	public MMTable(Table normal)                    {super(normal);}
	public MMTable(Table easy, Table difficult)     {super(easy, difficult);}
	public MMTable(MMTable easy, MMTable difficult) {super(easy.easy, difficult.difficult);}
	
	@Override public String typename() {return "table";}

	@Override
	public MMTable snapshot()
	{
		return new MMTable(easy != null ? easy.snapshot() : null, difficult != null ? difficult.snapshot() : null);
	}
	
	
	/*
	 *  ELEMENT ASSIGNMENT
	 * ====================
	 */
	
	@Override
	public Variable assignI(String idx, Source value, CodeSnippet assign, Mode mode)
	{
		if (this.hasNormal())
			return this.easy.assignI(idx, value, assign, mode);
		if (this.hasEasy() && mode != Mode.DIFFICULT)
			return this.easy.assignI(idx, value, assign, mode);
		if (this.hasDifficult() && mode != Mode.EASY)
			return this.difficult.assignI(idx, value, assign, mode);
		return null;
	}
	
	@Override
	public Variable assign(String key, Source value, CodeSnippet assign, Mode mode)
	{
		return assignI('"' + key + '"', value, assign, mode);
	}
	
	@Override
	public Variable assign(int idx, Source value, CodeSnippet assign, Mode mode)
	{
		return assignI(Integer.toString(idx), value, assign, mode);
	}
	
	
	/*
	 *  ELEMENT ACCESS
	 * ================
	 */
	
	@Override
	public Value getValueI(String idx, Mode2 mode)
	{
		switch (mode)
		{
		case EASY:      return this.hasEasy()      ? this.easy.getValueI(idx, mode)      : null;
		case DIFFICULT: return this.hasDifficult() ? this.difficult.getValueI(idx, mode) : null;
		default:        return null;
		}
	}
	
	@Override
	public Value getValue(String key, Mode2 mode) {return getValueI('"' + key + '"', mode);}
	
	@Override
	public Value getValue(int idx, Mode2 mode) {return getValueI(Integer.toString(idx), mode);}
	
	@Override
	public MMValue getValueI(String idx)
	{
		if (this.hasNormal())
			return this.easy.getValueI(idx);
		return new MMValue(
				this.hasEasy()      ? this.easy.getValueI(idx, Mode2.EASY)           : null,
				this.hasDifficult() ? this.difficult.getValueI(idx, Mode2.DIFFICULT) : null);
	}
	
	@Override
	public MMValue getValue(String key) {return getValueI('"' + key + '"');}
	
	@Override
	public MMValue getValue(int idx) {return getValueI(Integer.toString(idx));}
	
	@Override
	public Source derefI(String key, Mode2 mode)
	{
		switch (mode)
		{
		case EASY:      return this.hasEasy()      ? this.easy.derefI(key, mode)      : null;
		case DIFFICULT: return this.hasDifficult() ? this.difficult.derefI(key, mode) : null;
		default:        return null;
		}
	}
	
	@Override
	public Source deref(String key, Mode2 mode) {return derefI('"' + key + '"', mode);}
	
	@Override
	public Source deref(int idx, Mode2 mode) {return derefI(Integer.toString(idx), mode);}
	
	/**
	 * Get a reference to the given field.
	 * 
	 * @param key   Name of the field.
	 * @param code  The referencing code part.
	 * @return      A new {@link FieldReference}.
	 */
	public FieldReference getReferenceI(String key, CodeSnippet code)
	{
		return new FieldReference(this, key, code);
	}
	
	/**
	 * Get a reference to the given field.
	 * 
	 * @param key   Name of the field.
	 * @param code  The referencing code part.
	 * @return      A new {@link FieldReference}.
	 */
	public FieldReference getReference(String key, CodeSnippet code)
	{
		return new FieldReference(this, '"' + key + '"', code);
	}
	
	/**
	 * Get a reference to the given indexed field.
	 * 
	 * @param key   Index of the field.
	 * @param code  The referencing code part.
	 * @return      A new {@link FieldReference}.
	 */
	public FieldReference getReference(int key, CodeSnippet code)
	{
		return new FieldReference(this, Integer.toString(key), code);
	}
	
	
	/*
	 *  MISCELLANEOUS
	 * ===============
	 */
	
	@Override
	public void clear()
	{
		if (this.easy      != null) easy.clear();
		if (this.difficult != null) easy.clear();
	}
	
	
	/*
	 *  MODE CHECKING
	 * ===============
	 */
	@Override public boolean isNormalI(     String key) {return this.hasNormal() && easy.isNormalI(key);} 
	@Override public boolean isNormal(      String key) {return this.hasNormal() && easy.isNormal (key);}
	@Override public boolean isNormal(      int    idx) {return this.hasNormal() && easy.isNormal (idx);}
	@Override public boolean isMixedI(      String key) {return this.isMixed() || (this.hasNormal() && easy.isMixedI(key));} 
	@Override public boolean isMixed(       String key) {return this.isMixed() || (this.hasNormal() && easy.isMixed (key));}
	@Override public boolean isMixed(       int    idx) {return this.isMixed() || (this.hasNormal() && easy.isMixed (idx));}
	@Override public boolean isCompleteI(   String key) {return this.isComplete() && easy.isCompleteI(key) && (this.isNormal() || difficult.isCompleteI(key));}
	@Override public boolean isComplete(    String key) {return this.isComplete() && easy.isComplete (key) && (this.isNormal() || difficult.isComplete (key));}
	@Override public boolean isComplete(    int    idx) {return this.isComplete() && easy.isComplete (idx) && (this.isNormal() || difficult.isComplete (idx));}
	@Override public boolean isNullI(       String key) {return (easy == null || easy.isNullI(key)) && (difficult == null || difficult == easy || difficult.isNullI(key));}
	@Override public boolean isNull(        String key) {return (easy == null || easy.isNull (key)) && (difficult == null || difficult == easy || difficult.isNull (key));}
	@Override public boolean isNull(        int    idx) {return (easy == null || easy.isNull (idx)) && (difficult == null || difficult == easy || difficult.isNull (idx));}
	@Override public boolean onlyEasyI(     String key) {return (easy != null && easy.onlyEasyI(key) && (difficult == null || !difficult.existI(key) || difficult.onlyEasyI(key)));}
	@Override public boolean onlyEasy(      String key) {return (easy != null && easy.onlyEasy (key) && (difficult == null || !difficult.exist (key) || difficult.onlyEasy (key)));}
	@Override public boolean onlyEasy(      int    idx) {return (easy != null && easy.onlyEasy (idx) && (difficult == null || !difficult.exist (idx) || difficult.onlyEasy (idx)));}
	@Override public boolean onlyDifficultI(String key) {return (difficult != null && easy.onlyDifficultI(key) && (easy == null || !easy.existI(key) || easy.onlyDifficultI(key)));}
	@Override public boolean onlyDifficult( String key) {return (difficult != null && easy.onlyDifficult (key) && (easy == null || !easy.exist (key) || easy.onlyDifficult (key)));}
	@Override public boolean onlyDifficult( int    idx) {return (difficult != null && easy.onlyDifficult (idx) && (easy == null || !easy.exist (idx) || easy.onlyDifficult (idx)));}
	@Override public boolean hasEasyI(      String key) {return this.hasEasy() && easy.hasEasyI(key);}
	@Override public boolean hasEasy(       String key) {return this.hasEasy() && easy.hasEasy (key);}
	@Override public boolean hasEasy(       int    idx) {return this.hasEasy() && easy.hasEasy (idx);}
	@Override public boolean hasDifficultI( String key) {return this.hasDifficult() && difficult.hasDifficultI(key);}
	@Override public boolean hasDifficult(  String key) {return this.hasDifficult() && difficult.hasDifficult (key);}
	@Override public boolean hasDifficult(  int    idx) {return this.hasDifficult() && difficult.hasDifficult (idx);}
	@Override public boolean hasNormalI(    String key) {return this.hasNormal() && easy.hasNormalI(key);} 
	@Override public boolean hasNormal(     String key) {return this.hasNormal() && easy.hasNormal (key);}
	@Override public boolean hasNormal(     int    idx) {return this.hasNormal() && easy.hasNormal (idx);}
}

