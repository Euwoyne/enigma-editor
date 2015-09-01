
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

package enigma_edit.view.swing;

import java.awt.Font;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import enigma_edit.model.Tileset.VariantImage;

public class SpriteSet implements enigma_edit.model.SpriteSet
{
	private Path                      gfxPath;
	private Font                      font;
	private Map<VariantImage, Sprite> sprites;
	
	public SpriteSet(Path gfxPath, Font font)
	{
		this.gfxPath = gfxPath;
		this.font    = font;
		this.sprites = new LinkedHashMap<VariantImage, Sprite>();
	}
	
	@Override
	public enigma_edit.model.Sprite get(VariantImage image)
	{
		Sprite sprite = sprites.get(image);
		if (sprite == null)
		{
			sprite = new Sprite(image, gfxPath, font);
			sprites.put(image,  sprite);
		}
		return sprite;
	}
	
	@Override
	public void free(int size)
	{
		for (Map.Entry<VariantImage, Sprite> i : sprites.entrySet())
			i.getValue().freeImage(size);
	}
	
	@Override
	public void free()
	{
		sprites.clear();
	}
}

