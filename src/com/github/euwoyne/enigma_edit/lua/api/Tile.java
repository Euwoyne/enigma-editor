
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

package com.github.euwoyne.enigma_edit.lua.api;

import java.util.Iterator;
import java.util.LinkedList;

public class Tile implements Iterable<ApiObjectRef>
{
	private String                key;
	private LinkedList<ApiObjectRef> data;
	
	public Tile()
	{
		this.key = null;
		this.data = new LinkedList<ApiObjectRef>();
	}
	
	public Tile(String key)
	{
		this.key = key;
		this.data = new LinkedList<ApiObjectRef>();
	}
	
	public boolean hasKey()           {return key != null;}
	public String  getKey()           {return key;}
	public void    setKey(String key) {this.key = key;}
	
	public void push(ApiObject object)
	{
		data.add(new ApiObjectRef(object, this));
	}
	
	public int stackSize() {return data.size();}
	
	@Override
	public Iterator<ApiObjectRef> iterator()
	{
		return new Iterator<ApiObjectRef>()
		{
			private Iterator<ApiObjectRef> it = data.iterator(); 
			@Override public boolean hasNext() {return it.hasNext();}
			@Override public ApiObjectRef next()  {return it.next();}
		};
	}
	
	public static Tile concat(Tile tile1, Tile tile2)
	{
		Tile out = new Tile();
		out.data.addAll(tile1.data);
		out.data.addAll(tile2.data);
		return out;
	}
	
	public static Tile concat(Tile tile, ApiObject object)
	{
		Tile out = new Tile();
		out.data.addAll(tile.data);
		out.data.add(new ApiObjectRef(object, out));
		return out;
	}
	
	public static Tile concat(ApiObject object, Tile tile)
	{
		Tile out = new Tile();
		out.data.add(new ApiObjectRef(object, out));
		out.data.addAll(tile.data);
		return out;
	}
	
	public static Tile concat(ApiObject object1, ApiObject object2)
	{
		Tile out = new Tile();
		out.data.add(new ApiObjectRef(object1, out));
		out.data.add(new ApiObjectRef(object2, out));
		return out;
	}
	
	public String getUID()
	{
		// TODO tile UID (for API created tile)
		return "";
	}
}

