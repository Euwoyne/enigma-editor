
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.github.euwoyne.enigma_edit.error.MissingImageException;
import com.github.euwoyne.enigma_edit.model.RenderingAgent;
import com.github.euwoyne.enigma_edit.model.Sprite;
import com.github.euwoyne.enigma_edit.model.SpriteFolder;
import com.github.euwoyne.enigma_edit.model.Tileset;

public class AwtSprite implements Sprite
{
	public class Image extends BufferedImage implements Sprite.Image
	{
		final int size;
		
		Image(int size)
		{
			super(size, size, Image.TYPE_INT_ARGB);
			this.size   = size;
		}
		
		void draw(Tileset.VariantImage image) throws MissingImageException
		{
			Graphics2D     g;
			java.awt.Image file;
			
			g = this.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			for (Tileset.Image data : image)
			{
				final ImageObserver observer = new ImageObserver() {
					@Override
					public boolean imageUpdate(java.awt.Image img, int flags, int x, int y, int w, int h)
					{
						if ((flags & ALLBITS) != 0)
						{
							g.drawImage(img, x, y, w, h, null);
							drawText(g, data);
							return true;
						}
						else return false;
					}
				};
				
				try
				{
					Integer pngSize = size;
					Path pngPath = gfxPath.getPath(data.getFile() + ".png", pngSize);
					if (pngPath == null)
					{
						pngSize = gfxPath.getBestSize(data.getFile() + ".png", pngSize);
						if (pngSize == null) throw new MissingImageException("gfx" + pngSize + "/" + data.getFile() + ".png");
						pngPath = gfxPath.getPath(data.getFile() + ".png", pngSize);
					}
					
					file = ImageIO.read(pngPath.toFile());
					final int x, y;
					if (data.getFile().startsWith("ac"))
					{
						x = (int)((data.getX() * 1.25 + 0.125) * pngSize);
						y = (int)((data.getY() * 1.25 + 0.125) * pngSize);
					}
					else
					{
						x = data.getX() * pngSize;
						y = data.getY() * pngSize;
					}
					if (g.drawImage(file, 0, 0, size, size, x, y, x + pngSize, y + pngSize, observer))
						drawText(g, data);
				}
				catch (java.io.IOException e)
				{
					System.err.println("Missing file: " + data.getFile());
					e.printStackTrace();
					throw new MissingImageException(data.getFile());
				}
				
			}
			g.dispose();
		}
		
		private void drawText(Graphics2D g, Tileset.Image data)
		{
			if (!data.getText().isEmpty())
			{
				int textwidth = g.getFontMetrics().stringWidth(data.getText());
				if (textwidth > size)
					g.setFont(font.deriveFont(((float)size / textwidth) * font.getSize()));
				else
					g.setFont(font);
				int textheight = g.getFontMetrics().getHeight();
				final TextLayout layout = new TextLayout(data.getText(), g.getFont(), g.getFontRenderContext());
				g.setPaint(Color.WHITE);
				layout.draw(g, 3, textheight);
				g.setPaint(Color.RED);
				layout.draw(g, 2, textheight);
			}
		}
		@Override
		public int getSize() {return size;}
		
		@Override
		public void draw(RenderingAgent renderer, int x, int y) throws MissingImageException
		{
			renderer.draw(this, x, y);
		}
	}
	
	private SpriteFolder             gfxPath;
	private Font                     font;
	private Tileset.VariantImage     data;
	private Map<Integer, Image>      sizes;
	
	public AwtSprite(Tileset.VariantImage image, SpriteFolder gfxPath, Font font)
	{
		this.gfxPath = gfxPath;
		this.font    = font;
		this.data    = image;
		this.sizes   = new HashMap<Integer, Image>();
	}
	
	public Font getFont()          {return font;} 
	public void setFont(Font font) {this.font = font;} 
	
	@Override
	public boolean hasImage(int size)
	{
		return sizes.containsKey(size);
	}
	
	@Override
	public Image getImage(int size) throws MissingImageException
	{
		Image image = sizes.get(size);
		if (image == null)
		{
			image = new Image(size);
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
		final Image img = getImage(size);
		renderer.draw(img, x, y);
	}
}

