
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

package enigma_edit.model;

import java.util.ArrayList;

import enigma_edit.lua.data.Mode2;
import enigma_edit.lua.data.SimpleValue;
import enigma_edit.lua.data.Table;
import enigma_edit.model.Tileset.Alias;
import enigma_edit.model.Tileset.AttributeProvider;
import enigma_edit.model.Tileset.Kind;
import enigma_edit.model.Tileset.ObjectProvider;
import enigma_edit.model.Tileset.Variant;
import enigma_edit.model.Tileset.VariantImage;

public class TilePart implements ObjectProvider
{
	final Mode2             mode;
	final Table             table;
	final String            kindName;
	final AttributeProvider objdef;
	
	TilePart(Table table, Tileset tileset, Mode2 mode)
	{
		final SimpleValue kindVal = (table != null && table.exist(1)) ? table.get(1).checkSimple(mode) : null;
		final String      name    = kindVal != null ? kindVal.toString_noquote() : null;
		final String      kind    = kindVal != null ? (name.startsWith("#") ? name.substring(1) : name) : null;
		
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
		if (!table.exist(attrName)) return objdef.getAttribute(attrName);
		final SimpleValue attrval = table.get(attrName).checkSimple(mode);
		if (attrval == null) return "";
		return attrval.toString_noquote();
	}
	
	@Override
	public boolean hasAttribute(String attrName)
	{
		return table.exist(attrName) || objdef.hasAttribute(attrName);
	}
}

