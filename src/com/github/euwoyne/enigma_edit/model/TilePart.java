
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

package com.github.euwoyne.enigma_edit.model;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.github.euwoyne.enigma_edit.lua.data.Mode2;
import com.github.euwoyne.enigma_edit.lua.data.SimpleValue;
import com.github.euwoyne.enigma_edit.lua.data.Table;
import com.github.euwoyne.enigma_edit.lua.data.Variable;
import com.github.euwoyne.enigma_edit.model.Tileset.Alias;
import com.github.euwoyne.enigma_edit.model.Tileset.Kind;
import com.github.euwoyne.enigma_edit.model.Tileset.ObjectProvider;
import com.github.euwoyne.enigma_edit.model.Tileset.Variant;
import com.github.euwoyne.enigma_edit.model.Tileset.VariantImage;

public class TilePart implements ObjectProvider
{
	final Mode2          mode;
	final Table          table;
	final String         kindName;
	final ObjectProvider objdef;
	
	TilePart(Table table, Tileset tileset, Mode2 mode)
	{
		final SimpleValue kindVal = (table != null && table.exist(1)) ? table.get(1).checkSimple(mode) : null;
		final String      name    = kindVal != null ? kindVal.toString_noquote() : null;
		final String      kind    = name    != null ? (name.startsWith("#") ? name.substring(1) : name) : null;
		
		this.mode = mode;
		this.table = table;
		this.objdef = tileset.get(kind);
		this.kindName = (objdef instanceof Alias) ? objdef.getKind().name : kind;
	}
	
	@Override
	public ArrayList<Variant> getVariant()
	{
		return objdef != null ? objdef.getKind().getVariant(this) : new ArrayList<Variant>(0);
	}
	
	@Override
	public ArrayList<VariantImage> getImage()
	{
		return objdef != null ? objdef.getKind().getImage(this) : new ArrayList<VariantImage>(0);
	}
	
	@Override public Kind   getKind()     {return objdef.getKind();}
	@Override public String getKindName() {return kindName;}
	
	@Override
	public String getAttribute(String attrName)
	{
		if (!table.exist(attrName)) return objdef != null ? objdef.getAttribute(attrName) : "";
		final SimpleValue attrval = table.get(attrName).checkSimple(mode);
		if (attrval == null) return "";
		return attrval.toString_noquote();
	}
	
	@Override
	public boolean hasAttribute(String attrName)
	{
		return table.exist(attrName) || objdef.hasAttribute(attrName);
	}
	
	/**
	 * Checks, if the given attribute is given with its default value.
	 * Note that for attributes without default values this always returns
	 * {@code false}. On the other hand attributes not present in the
	 * declaration will always yield {@code true}.
	 * 
	 * @param attrName  The attribute to check.
	 * @return          {@code True}, iff this declaration is equivalent to the declaration
	 *                  missing the given attribute (i.e. its value equals the default).
	 */
	public boolean isDefault(String attrName)
	{
		if (!table.exist(attrName)) return true;
		final SimpleValue attrval = table.get(attrName).checkSimple(mode);
		if (attrval == null) return false;
		return objdef.getAttribute(attrName).equals(attrval.toString_noquote());
	}
	
	public String canonical()
	{
		StringBuilder out = new StringBuilder();
		int           idx = 1, max;
		
		out.append('{');
		while (table.exist(idx))
		{
			if (idx > 1) out.append(", ");
			out.append(table.get(idx).toString(mode.mode()));
			++idx;
		}
		max = idx;
		
		final Pattern quoted = Pattern.compile("\"((\\w+))\"");
		
		for (Entry<String,Variable> entry : table)
		{
			if (entry.getValue().isDefined(mode))
			{
				final String val = entry.getValue().toString(mode.mode());
				final String unquotedVal = quoted.matcher(val).matches() ? val.substring(1,  val.length() - 1) : val;
				final String key = entry.getKey();
				final String unquotedKey = quoted.matcher(key).matches() ? key.substring(1,  key.length() - 1) : key;
				if (key != unquotedKey)
				{
					if (idx > 1) out.append(", ");
					if (!objdef.getAttribute(unquotedKey).equals(unquotedVal))
					{
						out.append(entry.getKey().substring(1, entry.getKey().length() - 1));
						out.append('=');
						out.append(entry.getValue() == null ? "nil" : val);
						++idx;
					}
					continue;
				}
				
				try {
					if (Integer.parseInt(entry.getKey()) < max) continue;
				} catch (NumberFormatException e) {}
				
				if (idx > 1) out.append(", [");
				out.append(entry.getKey());
				out.append("]=");
				out.append(val);
			}
			++idx;
		}
		out.append('}');
		
		return out.toString();
	}
}

