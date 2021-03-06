
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
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.euwoyne.enigma_edit.model.Sprite;
import com.github.euwoyne.enigma_edit.model.SpriteFolder;
import com.github.euwoyne.enigma_edit.model.SpriteSet;
import com.github.euwoyne.enigma_edit.model.Tileset.VariantImage;

public class AwtSpriteSet extends AwtSpriteBase implements SpriteSet
{
	private final Map<VariantImage, AwtSprite> sprites;
	
	public AwtSpriteSet(SpriteFolder gfxPath, Font font)
	{
		super(gfxPath, font);
		this.sprites = new LinkedHashMap<VariantImage, AwtSprite>();
	}
	
	@Override
	public Sprite get(VariantImage image)
	{
		AwtSprite sprite = sprites.get(image);
		if (sprite == null)
		{
			sprite = new AwtSprite(image, gfxPath, font);
			sprites.put(image, sprite);
		}
		return sprite;
	}
	
	@Override
	public void free(int size)
	{
		for (Map.Entry<VariantImage, AwtSprite> i : sprites.entrySet())
			i.getValue().freeImage(size);
	}
	
	@Override
	public void free()
	{
		sprites.clear();
	}
}

