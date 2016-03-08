
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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * A lua table implemented as map between {@code String}s and {@link Variable}s. 
 */
public class Table extends Value implements Indexed, Iterable<Entry<String, Variable>>
{
	private final TreeMap<String, Variable> fields;
	
	/**
	 * Create an empty table.
	 */
	public Table(CodeSnippet code)
	{
		super(code);
		fields = new TreeMap<String, Variable>();
	}
	
	@Override
	public Variable assignI(String idx, Source value, CodeSnippet assign, Mode mode)
	{
		if (value instanceof MultiMode)
		{
			if (((MultiMode)value).hasNormal() && mode == Mode.NORMAL)
				return this.assignI(idx, ((MultiMode)value).deref(Mode2.EASY), assign, mode);
			else
			{
				if (((MultiMode)value).hasEasy() && mode != Mode.DIFFICULT)
					this.assignI(idx, ((MultiMode)value).deref(Mode2.EASY), assign, Mode.EASY);
				if (((MultiMode)value).hasDifficult() && mode != Mode.EASY)
					this.assignI(idx, ((MultiMode)value).deref(Mode2.DIFFICULT), assign, Mode.DIFFICULT);
				return this.getI(idx);
			}
		}
		else
		{
			Variable var = fields.get(idx);
			if (var == null)
				fields.put(idx, var = new Variable(value, assign, mode));
			else
				var.assign(value, assign, mode);
			return var;
		}
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
	
	@Override
	public Value getValueI(String idx, Mode2 mode)
	{
		final Variable var = fields.get(idx);
		if (var == null) return null;
		return var.checkValue(mode);
	}
	
	@Override
	public Value getValue(String key, Mode2 mode) {return getValueI('"' + key + '"', mode);}
	
	@Override
	public Value getValue(int idx, Mode2 mode) {return getValueI(Integer.toString(idx), mode);}
	
	@Override
	public MMValue getValueI(String idx)
	{
		final Variable var = fields.get(idx);
		if (var == null) return null;
		return new MMValue(var.checkValue(Mode2.EASY), var.checkValue(Mode2.DIFFICULT));
	}
	
	@Override
	public MMValue getValue(String key) {return getValueI('"' + key + '"');}
	
	@Override
	public MMValue getValue(int idx) {return getValueI(Integer.toString(idx));}
	
	@Override
	public Source derefI(String key, Mode2 mode)
	{
		final Variable var = fields.get(key);
		if (var == null) return null;
		return var.deref(mode);
	}
	
	@Override
	public Source deref(String key, Mode2 mode) {return derefI('"' + key + '"', mode);}
	
	@Override
	public Source deref(int idx, Mode2 mode) {return derefI(Integer.toString(idx), mode);}
	
	@Override
	public void clear()
	{
		fields.clear();
	}
	
	/**
	 * Raw direct getter. 
	 */
	public Variable getI(String idx) {return fields.get(idx);}
	
	/**
	 * Get the variable instance assigned to the given field.
	 * 
	 * @param key   Name of the field.
	 * @return      The corresponding {@link Variable} instance.
	 */
	public Variable get(String key)
	{
		return fields.get('"' + key + '"');
	}
	
	/**
	 * Get the variable instance assigned to the given index.
	 * 
	 * @param idx   Index of the field.
	 * @return      The corresponding {@link Variable} instance.
	 */
	public Variable get(int idx)
	{
		return fields.get(Integer.toString(idx));
	}
	
	/**
	 * Get a reference to the given field (raw index).
	 * 
	 * @param key   Raw index of the field.
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
	
	/**
	 * Raw existence check.
	 */
	public boolean existI(String key)
	{
		return fields.containsKey(key);
	}
	
	/**
	 * Check, whether a field has been defined.
	 * 
	 * @param key  Name of the field.
	 * @return     Existence of the given field. 
	 */
	public boolean exist(String key)
	{
		return fields.containsKey('"' + key + '"');
	}
	
	/**
	 * Check, whether an index has been defined.
	 * 
	 * @param key  Index of the field.
	 * @return     Existence of the given indexed field. 
	 */
	public boolean exist(int key)
	{
		return fields.containsKey(Integer.toString(key));
	}
	
	/**
	 * Create an iterator for the underlying Map.
	 * @return  A new iterator instance.
	 */
	public Iterator<Entry<String, Variable>> iterator()
	{
		return fields.entrySet().iterator();
	}

	@Override
	public String typename() {return "table";}
	
	@Override
	public Table snapshot()
	{
		Table copy = new Table(this.code);
		for (Entry<String, Variable> entry : fields.entrySet())
			copy.fields.put(entry.getKey(), entry.getValue().snapshot());
		return copy;
	}
	
	@Override
	public String toString()
	{
		return toString(Mode.NORMAL);
	}
	
	public String toString(Mode mode)
	{
		StringBuilder out = new StringBuilder();
		int           idx = 1, max;
		
		out.append('{');
		while (fields.containsKey(Integer.toString(idx)))
		{
			if (idx > 1) out.append(", ");
			out.append(fields.get(Integer.toString(idx)).toString(mode));
			++idx;
		}
		max = idx;
		
		for (Entry<String,Variable> entry : fields.entrySet())
		{
			if (entry.getValue().isDefined(mode))
			{
				if (entry.getKey().matches("\"\\w+\""))
				{
					if (idx > 1) out.append(", ");
					out.append(entry.getKey().substring(1, entry.getKey().length() - 1));
					out.append('=');
					out.append(entry.getValue() == null ? "nil" : entry.getValue().toString(mode));
					++idx;
					continue;
				}
				
				try {
					if (Integer.parseInt(entry.getKey()) < max) continue;
				} catch (NumberFormatException e) {}
				
				if (idx > 1) out.append(", [");
				out.append(entry.getKey());
				out.append("]=");
				out.append(entry.getValue().toString(mode));
			}
			++idx;
		}
		out.append('}');
		
		return out.toString();
	}
	
	@Override public MMTable checkTable()           {return new MMTable(this);}
	@Override public Table   checkTable(Mode2 mode) {return this;}
	
	/**
	 * Dump the map content to {@code System.out}.
	 * Each entry will be displayed in its own line as {@code map["key"] = value}.
	 * 
	 * @param name  Name of the map.
	 */
	public void dump(String name)
	{
		for (Map.Entry<String, Variable> entry : fields.entrySet())
		{
			if (entry.getValue().getAssign(Mode.EASY) != null || entry.getValue().getAssign(Mode.DIFFICULT) != null)
				System.out.println(name + '[' + entry.getKey() + "] = " + entry.getValue());
		}
	}
	
	/*
	 *  MODE CHECKING
	 * ===============
	 */
	@Override public boolean isNormal()
	{
		for (Map.Entry<String, Variable> entry : fields.entrySet())
			if (!entry.getValue().isNormal())
				return false;
		return true;
	}
	
	@Override public boolean isNormalI(     String key) {return fields.containsKey(key) && fields.get(key).isNormal();} 
	@Override public boolean isNormal(      String key) {return isNormalI('"' + key + '"');}
	@Override public boolean isNormal(      int    idx) {return isNormalI(Integer.toString(idx));}
	@Override public boolean isMixedI(      String key) {return fields.containsKey(key) && fields.get(key).isMixed();} 
	@Override public boolean isMixed(       String key) {return isMixedI('"' + key + '"');}
	@Override public boolean isMixed(       int    idx) {return isMixedI(Integer.toString(idx));}
	@Override public boolean isCompleteI(   String key) {return fields.containsKey(key) && fields.get(key).isComplete();}
	@Override public boolean isComplete(    String key) {return isCompleteI('"' + key + '"');}
	@Override public boolean isComplete(    int    idx) {return isCompleteI(Integer.toString(idx));}
	@Override public boolean isNullI(       String key) {return !fields.containsKey(key) || fields.get(key).isNull();}
	@Override public boolean isNull(        String key) {return isNullI('"' + key + '"');}
	@Override public boolean isNull(        int    idx) {return isNullI(Integer.toString(idx));}
	@Override public boolean onlyEasyI(     String key) {return fields.containsKey(key) && fields.get(key).onlyEasy();}
	@Override public boolean onlyEasy(      String key) {return onlyEasyI('"' + key + '"');}
	@Override public boolean onlyEasy(      int    idx) {return onlyEasyI(Integer.toString(idx));}
	@Override public boolean onlyDifficultI(String key) {return fields.containsKey(key) && fields.get(key).onlyDifficult();}
	@Override public boolean onlyDifficult( String key) {return onlyDifficultI('"' + key + '"');}
	@Override public boolean onlyDifficult( int    idx) {return onlyDifficultI(Integer.toString(idx));}
	@Override public boolean hasEasyI(      String key) {return fields.containsKey(key) && fields.get(key).hasEasy();}
	@Override public boolean hasEasy(       String key) {return hasEasyI('"' + key + '"');}
	@Override public boolean hasEasy(       int    idx) {return hasEasyI(Integer.toString(idx));}
	@Override public boolean hasDifficultI( String key) {return fields.containsKey(key) && fields.get(key).hasDifficult();}
	@Override public boolean hasDifficult(  String key) {return hasDifficultI('"' + key + '"');}
	@Override public boolean hasDifficult(  int    idx) {return hasDifficultI(Integer.toString(idx));}
	@Override public boolean hasNormalI(    String key) {return fields.containsKey(key) && fields.get(key).hasNormal();} 
	@Override public boolean hasNormal(     String key) {return hasNormalI('"' + key + '"');}
	@Override public boolean hasNormal(     int    idx) {return hasNormalI(Integer.toString(idx));}
	
	@Override public boolean existI(String key, Mode2 mode)
	{
		return fields.containsKey(key)
				&& (mode == Mode2.EASY
					? fields.get(key).hasEasy()
					: fields.get(key).hasDifficult());
	}
	
	@Override public boolean exist( String key, Mode2 mode) {return existI('"' + key + '"', mode);}
	@Override public boolean exist( int    idx, Mode2 mode) {return existI(Integer.toString(idx), mode);}
}

