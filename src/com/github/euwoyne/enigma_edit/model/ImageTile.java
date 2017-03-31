
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

import java.util.List;

import com.github.euwoyne.enigma_edit.error.MissingImageException;
import com.github.euwoyne.enigma_edit.lua.data.Mode;
import com.github.euwoyne.enigma_edit.lua.data.Mode2;
import com.github.euwoyne.enigma_edit.lua.data.SimpleValue;
import com.github.euwoyne.enigma_edit.lua.data.Table;
import com.github.euwoyne.enigma_edit.lua.data.Tile;
import com.github.euwoyne.enigma_edit.model.Tileset.*;

public class ImageTile
{
	public static class Part implements Renderable
	{
		TilePart       part;
		List<Variant>  variants;
		SpriteStack    sprites;
		
		public Part(TilePart tilepart)
		{
			part     = tilepart;
			variants = tilepart.getVariant();
			sprites  = tilepart.getImage();
		}
		
		public boolean isEmpty()   {return sprites.isEmpty();}
		
		public boolean checkCluster()
		{
			for (Variant variant : variants)
			{
				if (variant.image.isClusterImage())
					return true;
			}
			return false;
		}
		
		private static boolean checkCluster(SimpleValue kind, SimpleValue cluster, Tile.Part neighbor, Mode2 mode)
		{
			if (!neighbor.has(mode)) return false;
			final Table table = neighbor.get(mode).checkTable(mode);
			if (!table.exist(1, mode) || !table.exist("cluster", mode)) return false;
			final SimpleValue nkind    = table.get(1).checkSimple(mode);
			final SimpleValue ncluster = table.get("cluster").checkSimple(mode);
			return (nkind != null && ncluster != null && cluster.value.eq_b(ncluster.value) && kind.value.eq_b(nkind.value));
		}
		
		public void resolveCluster(Mode2 mode, Tile.Part tilePart, Tile.Part nPart, Tile.Part ePart, Tile.Part sPart, Tile.Part wPart)
		{
			final Table table = tilePart.get(mode).checkTable(mode);
			final SimpleValue cluster = table.exist("cluster", mode) ? table.get("cluster").checkSimple(mode) : null;
			final SimpleValue kind    = table.exist(1,         mode) ? table.get(1).checkSimple(mode)         : null;
			int vIdx = 0;
			for (Variant variant : variants)
			{
				if (variant.image.isClusterImage())
				{
					if (cluster != null)
					{
						final StringBuffer s = new StringBuffer(4);
						if (nPart != null && checkCluster(kind, cluster, nPart, mode))
							s.append('n');
						if (ePart != null && checkCluster(kind, cluster, ePart, mode))
							s.append('e');
						if (sPart != null && checkCluster(kind, cluster, sPart, mode))
							s.append('s');
						if (wPart != null && checkCluster(kind, cluster, wPart, mode))
							s.append('w');
						sprites.set(vIdx, ((Tileset.ClusterImage)variant.image).get(s.toString()).sprite);
					}
					else if (table.exist("connections"))
					{
						final SimpleValue conn = table.get("connections").checkSimple(mode);
						if (conn != null)
							sprites.set(vIdx, ((Tileset.ClusterImage)variant.image).get(conn.toString_noquote()).sprite);
					}
					else if (part.hasAttribute("connections"))
					{
						sprites.set(vIdx, ((Tileset.ClusterImage)variant.image).get(part.getAttribute("connections")).sprite);
					}
				}
				++vIdx;
			}
		}
		
		public java.util.Iterator<Variant> varIterator() {return variants.iterator();}
		public java.util.Iterator<Sprite>  imgIterator() {return sprites.iterator();}
		
		@Override
		public void draw(RenderingAgent renderer, int x, int y, int size) throws MissingImageException
		{
			sprites.draw(renderer, x, y, size);
		}
	}
	
	public static class MMPart
	{
		Part easy, difficult;
		
		MMPart()
		{
			easy = null;
			difficult = null;
		}
		
		public Part get(Mode mode)
		{
			switch (mode)
			{
			case EASY:      return easy;
			case DIFFICULT: return difficult;
			case NORMAL:    return easy == difficult ? easy : null;
			default:        return null;
			}
		}
		
		public Part get(Mode2 mode)
		{
			return mode == Mode2.EASY ? easy : difficult;
		}
		
		public boolean hasEasy()      {return easy      != null;}
		public boolean hasDifficult() {return difficult != null;}
		public boolean hasNormal()    {return easy == difficult && easy != null;}
		
		public boolean has(Mode mode)
		{
			switch (mode)
			{
			case EASY:      return easy != null;
			case DIFFICULT: return difficult != null;
			case NORMAL:    return easy == difficult && easy != null;
			default:        return false;
			}
		}
		
		public boolean has(Mode2 mode)
		{
			return mode == Mode2.EASY ? easy != null : difficult != null;
		}
		
		
		void resolvePart(Tile.Part tilePart, Tileset tileset, Tile defaultTile)
		{
			if (!tilePart.isNull())
			{
				if (tilePart.hasEasy())
					easy = new Part(tileset.resolve(tilePart.get(Mode.EASY), Mode2.EASY));
				else
					easy = new Part(tileset.resolve(defaultTile.fl().get(Mode.EASY), Mode2.EASY));
				
				if (tilePart.hasNormal())
					difficult = easy;
				else if (tilePart.hasDifficult())
					difficult = new Part(tileset.resolve(tilePart.get(Mode.DIFFICULT), Mode2.DIFFICULT));
				else
					difficult = new Part(tileset.resolve(defaultTile.fl().get(Mode.DIFFICULT), Mode2.DIFFICULT));
			}
			else
			{
				easy = new Part(tileset.resolve(defaultTile.fl().get(Mode.EASY), Mode2.EASY));
				difficult = new Part(tileset.resolve(defaultTile.fl().get(Mode.DIFFICULT), Mode2.DIFFICULT));
			}
		}
		
		void resolvePart(Tile.Part tilePart, Tileset tileset)
		{
			if (!tilePart.isNull())
			{
				if (tilePart.hasEasy())
					easy = new Part(tileset.resolve(tilePart.get(Mode.EASY), Mode2.EASY));
				
				if (tilePart.hasNormal())
					difficult = easy;
				else if (tilePart.hasDifficult())
					difficult = new Part(tileset.resolve(tilePart.get(Mode.DIFFICULT), Mode2.DIFFICULT));
			}
		}
		
		void resolveCluster(Tile.Part tilePart, Tile.Part nPart, Tile.Part ePart, Tile.Part sPart, Tile.Part wPart)
		{
			if (easy != null)
				easy.resolveCluster(Mode2.EASY, tilePart, nPart, ePart, sPart, wPart);
			if (difficult != null)
				difficult.resolveCluster(Mode2.DIFFICULT, tilePart, nPart, ePart, sPart, wPart);
		}
	}
	
	private Tile   tile;
	private MMPart floor, item, actor, stone;
	
	ImageTile(Tile tile)
	{
		this.tile = tile;
		this.floor = new MMPart();
		this.item  = new MMPart();
		this.actor = new MMPart();
		this.stone = new MMPart();
	}
	
	public Tile    tile()  {return tile;}
	public MMPart  fl()    {return floor;}
	public MMPart  it()    {return item;}
	public MMPart  ac()    {return actor;}
	public MMPart  st()    {return stone;}
	
	void resolveTile(Tileset tileset, Tile defaultTile)
	{
		floor.resolvePart(tile.fl(), tileset, defaultTile);
		item .resolvePart(tile.it(), tileset);
		actor.resolvePart(tile.ac(), tileset);
		stone.resolvePart(tile.st(), tileset);
	}
	
	void resolveCluster(ImageTile nTile, ImageTile eTile, ImageTile sTile, ImageTile wTile)
	{
		if (tile.has_fl())
			floor.resolveCluster(tile.fl(),
				nTile != null ? nTile.tile.fl() : null,
				eTile != null ? eTile.tile.fl() : null,
				sTile != null ? sTile.tile.fl() : null,
				wTile != null ? wTile.tile.fl() : null);
		
		if (tile.has_it())
			item .resolveCluster(tile.it(),
				nTile != null ? nTile.tile.it() : null,
				eTile != null ? eTile.tile.it() : null,
				sTile != null ? sTile.tile.it() : null,
				wTile != null ? wTile.tile.it() : null);
		
		if (tile.has_ac())
			actor.resolveCluster(tile.ac(),
				nTile != null ? nTile.tile.ac() : null,
				eTile != null ? eTile.tile.ac() : null,
				sTile != null ? sTile.tile.ac() : null,
				wTile != null ? wTile.tile.ac() : null);
					
		if (tile.has_st())
			stone.resolveCluster(tile.st(),
				nTile != null ? nTile.tile.st() : null,
				eTile != null ? eTile.tile.st() : null,
				sTile != null ? sTile.tile.st() : null,
				wTile != null ? wTile.tile.st() : null);
	}
	
	public void draw_fl(RenderingAgent renderer, int x, int y, int size, Mode mode) throws MissingImageException
	{
		final Part part = floor.get(mode);
		if (part != null)
			part.sprites.draw(renderer, x, y, size);
	}
	
	public void draw_it(RenderingAgent renderer, int x, int y, int size, Mode mode) throws MissingImageException
	{
		final Part part = item.get(mode);
		if (part != null)
			part.sprites.draw(renderer, x, y, size);
	}
	
	public void draw_ac(RenderingAgent renderer, int x, int y, int size, Mode mode) throws MissingImageException
	{
		final Part part = actor.get(mode);
		if (part != null)
			part.sprites.draw(renderer, x, y, size);
	}
	
	public void draw_st(RenderingAgent renderer, int x, int y, int size, Mode mode) throws MissingImageException
	{
		final Part part = stone.get(mode);
		if (part != null)
			part.sprites.draw(renderer, x, y, size);
	}
}

