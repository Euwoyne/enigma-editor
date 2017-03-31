
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

package com.github.euwoyne.enigma_edit.view.swing;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import com.github.euwoyne.enigma_edit.error.MissingImageException;
import com.github.euwoyne.enigma_edit.model.RenderingAgent;
import com.github.euwoyne.enigma_edit.model.Sprite;
import com.github.euwoyne.enigma_edit.model.SpriteFolder;
import com.github.euwoyne.enigma_edit.model.SpriteStack;
import com.github.euwoyne.enigma_edit.model.Tileset;

public class AwtSprite extends AwtSpriteBase implements Sprite
{
	private final Tileset.VariantImage data;
	private Map<Integer, AwtImage>     sizes;
	
	public AwtSprite(Tileset.VariantImage image, SpriteFolder gfxPath, Font font)
	{
		super(gfxPath, font);
		this.data    = image;
		this.sizes   = new HashMap<Integer, AwtImage>();
	}
	
	@Override
	public boolean hasImage(int size)
	{
		return sizes.containsKey(size);
	}
	
	@Override
	public AwtImage getImage(int size) throws MissingImageException
	{
		AwtImage image = sizes.get(size);
		if (image == null)
		{
			image = new AwtImage(size);
			image.draw(data);
			sizes.put(size,  image);
		}
		return image;
	}
	
	@Override
	public void freeImage(int size)
	{
		sizes.remove(size);
	}
	
	public void freeImages()
	{
		sizes.clear();
	}
	
	@Override
	public void draw(RenderingAgent renderer, int x, int y, int size) throws MissingImageException
	{
		renderer.draw(getImage(size), x, y);
	}
	
	public static AwtImage create(AwtSpriteBase base, SpriteStack stack, int size) throws MissingImageException
	{
		final AwtImage image = base.new AwtImage(size);
		stack.draw(image, 0, 0, size);
		return image;
	}
}

