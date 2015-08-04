
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

package enigma_edit.lua;

import java.util.TreeMap;

public class World
{
	private int width;
	private int height;
	
	private TreeMap<String, Tile> tileset;
	
	public World()
	{
		width  = 0;
		height = 0;
		tileset = new TreeMap<String, Tile>();
	}
	
	public void create(int w, int h)
	{
		width = w;
		height = h;
	}
	
	public int getWidth()  {return width;}
	public int getHeight() {return height;}
	
	public void addTile(Tile tile)
	{
		if (tile.getKey() == null)
			throw new RuntimeException("Try to declare tile without key!");
		System.out.print("added tile \"" + tile.getKey() + "\": ");
		for (ObjectRef obj : tile)
		{
			System.out.print(obj.object().kind);
			if (obj.object().name != null)
				System.out.print(" (named \"" + obj.object().name + "\")");
			System.out.print("  ");
		}
		System.out.println();
		tileset.put(tile.getKey(), tile);
	}
	
	Tile getTile(String key)
	{
		return tileset.get(key);
	}
}

