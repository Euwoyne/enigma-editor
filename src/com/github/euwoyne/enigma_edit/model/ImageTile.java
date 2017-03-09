
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
import java.util.Collections;
import java.util.List;

import com.github.euwoyne.enigma_edit.error.MissingImageException;
import com.github.euwoyne.enigma_edit.lua.data.Mode;
import com.github.euwoyne.enigma_edit.lua.data.Mode2;
import com.github.euwoyne.enigma_edit.lua.data.Tile;
import com.github.euwoyne.enigma_edit.model.Tileset.ClusterImage;
import com.github.euwoyne.enigma_edit.model.Tileset.Kind;
import com.github.euwoyne.enigma_edit.model.Tileset.ObjectProvider;
import com.github.euwoyne.enigma_edit.model.Tileset.Variant;
import com.github.euwoyne.enigma_edit.model.Tileset.VariantImage;

public class ImageTile
{
	public static class Part implements Iterable<VariantImage>, ResizeRenderable, ObjectProvider
	{
		TilePart           part;
		List<Variant>      variants;
		List<VariantImage> images;
		List<Integer>      indices;
		
		public Part(TilePart tilepart)
		{
			part     = tilepart;
			variants = tilepart.getVariant();
			images   = tilepart.getImage();
			indices  = new ArrayList<Integer>();
		}
		
		public boolean isEmpty()   {return images.isEmpty();}
		public boolean isCluster() {return !indices.isEmpty();}
		
		private class Iterator implements java.util.Iterator<VariantImage>
		{
			private java.util.ListIterator<VariantImage> vIt;
			private java.util.ListIterator<Integer>  iIt;
			
			private Iterator()
			{
				vIt = images.listIterator();
				iIt = indices.listIterator();
			}
			
			@Override public boolean hasNext()
			{
				return vIt.hasNext();
			}
			
			@Override public VariantImage next()
			{
				final VariantImage v = vIt.next();
				if (v == null) {
					System.err.println("Got null variant");
					return null;}
				if (!(v instanceof ClusterImage) || !iIt.hasNext()) return v;
				return ((ClusterImage)v).connect[iIt.next()];
			}
		}
		
		@Override public Iterator iterator() {return new Iterator();}
		
		public java.util.Iterator<Variant>      varIterator() {return variants.iterator();}
		public java.util.Iterator<VariantImage> imgIterator() {return images.iterator();}
		public java.util.Iterator<Integer>      idxIterator() {return indices.iterator();}
		
		@Override
		public void draw(RenderingAgent renderer, int x, int y, int size) throws MissingImageException
		{
			for (VariantImage image : this)
				image.draw(renderer, x, y, size);
		}
		
		@Override public Kind    getKind()                      {return part.getKind();}
		@Override public String  getAttribute(String attrName)  {return part.getAttribute(attrName);}
		@Override public boolean hasAttribute(String attrName)  {return part.hasAttribute(attrName);}
		@Override public String  getKindName()                  {return part.getKindName();}
		
		@Override public List<Variant>      getVariant() {return Collections.unmodifiableList(variants);}
		@Override public List<VariantImage> getImage()   {return Collections.unmodifiableList(images);}
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
	}
	
	Tile   tile;
	MMPart floor, item, actor, stone;
	
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
		// get variants
		// * floor
		if (tile.has_fl())
		{
			if (tile.fl().hasEasy())
				floor.easy = new Part(tileset.resolve(tile.fl().get(Mode.EASY), Mode2.EASY));
			else
				floor.easy = new Part(tileset.resolve(defaultTile.fl().get(Mode.EASY), Mode2.EASY));
			
			if (tile.fl().hasNormal())
				floor.difficult = floor.easy;
			else if (tile.fl().hasDifficult())
				floor.difficult = new Part(tileset.resolve(tile.fl().get(Mode.DIFFICULT), Mode2.DIFFICULT));
			else
				floor.difficult = new Part(tileset.resolve(defaultTile.fl().get(Mode.DIFFICULT), Mode2.DIFFICULT));
		}
		else
		{
			floor.easy = new Part(tileset.resolve(defaultTile.fl().get(Mode.EASY), Mode2.EASY));
			floor.difficult = new Part(tileset.resolve(defaultTile.fl().get(Mode.DIFFICULT), Mode2.DIFFICULT));
		}
		
		// * item
		if (tile.has_it())
		{
			if (tile.it().hasEasy())
				item.easy = new Part(tileset.resolve(tile.it().get(Mode.EASY), Mode2.EASY));
			if (tile.it().hasNormal())
				item.difficult = item.easy;
			else if (tile.it().hasDifficult())
				item.difficult = new Part(tileset.resolve(tile.it().get(Mode.DIFFICULT), Mode2.DIFFICULT));
		}
		
		// * actor
		if (tile.has_ac())
		{
			if (tile.ac().hasEasy())
				actor.easy = new Part(tileset.resolve(tile.ac().get(Mode.EASY), Mode2.EASY));
			if (tile.ac().hasNormal())
				actor.difficult = actor.easy;
			else if (tile.ac().hasDifficult())
				actor.difficult = new Part(tileset.resolve(tile.ac().get(Mode.DIFFICULT), Mode2.DIFFICULT));
		}
		
		// * stone
		if (tile.has_st())
		{
			if (tile.st().hasEasy())
				stone.easy = new Part(tileset.resolve(tile.st().get(Mode.EASY), Mode2.EASY));
			if (tile.st().hasNormal())
				stone.difficult = stone.easy;
			else if (tile.st().hasDifficult())
				stone.difficult = new Part(tileset.resolve(tile.st().get(Mode.DIFFICULT), Mode2.DIFFICULT));
		}
	}
	
	public void draw_fl(RenderingAgent renderer, int x, int y, int size, Mode mode) throws MissingImageException
	{
		Part part = floor.get(mode);
		if (part == null) return;
		for (VariantImage image : part)
			image.draw(renderer, x, y, size);
	}
	
	public void draw_it(RenderingAgent renderer, int x, int y, int size, Mode mode) throws MissingImageException
	{
		Part part = item.get(mode);
		if (part == null) return;
		for (VariantImage image : part)
			image.draw(renderer, x, y, size);
	}
	
	public void draw_ac(RenderingAgent renderer, int x, int y, int size, Mode mode) throws MissingImageException
	{
		Part part = actor.get(mode);
		if (part == null) return;
		for (VariantImage image : part)
			image.draw(renderer, x, y, size);
	}
	
	public void draw_st(RenderingAgent renderer, int x, int y, int size, Mode mode) throws MissingImageException
	{
		Part part = stone.get(mode);
		if (part == null) return;
		for (VariantImage image : part)
			image.draw(renderer, x, y, size);
	}
}

