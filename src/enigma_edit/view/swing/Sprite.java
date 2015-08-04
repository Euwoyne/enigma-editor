
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import enigma_edit.error.MissingImageException;
import enigma_edit.model.RenderingAgent;
import enigma_edit.model.Tileset;

public class Sprite implements enigma_edit.model.Sprite
{
	public class Image extends BufferedImage implements enigma_edit.model.Sprite.Image
	{
		final int size;
		
		Image(int size)
		{
			super(size, size, Image.TYPE_INT_ARGB);
			this.size   = size;
		}
		
		void draw(Tileset.NamedImage image) throws MissingImageException
		{
			Graphics2D     g;
			java.awt.Image file;
			TextLayout     layout;
			
			g = this.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			for (Tileset.Image data : image)
			{
				try
				{
					file = ImageIO.read(new File(gfxPath.resolve("gfx" + size).resolve(data.getFile()).toString() + ".png"));
					if (data.getFile().startsWith("ac"))
						g.drawImage(file, -(int)((data.getX() * 1.25 + 0.125) * size),
								          -(int)((data.getY() * 1.25 + 0.125) * size), null);
					else
						g.drawImage(file, -data.getX() * size, -data.getY() * size, null);
					if (!data.getText().isEmpty())
					{
						int textwidth = g.getFontMetrics().stringWidth(data.getText());
						if (textwidth > size)
							g.setFont(font.deriveFont(((float)size / textwidth) * font.getSize()));
						else
							g.setFont(font);
						int textheight = g.getFontMetrics().getHeight();
						layout = new TextLayout(data.getText(), g.getFont(), g.getFontRenderContext());
						g.setPaint(Color.WHITE);
						layout.draw(g, 3, textheight);
						g.setPaint(Color.RED);
						layout.draw(g, 2, textheight);
					}
				}
				catch (java.io.IOException e)
				{
					throw new MissingImageException(data.getFile());
				}
			}
			g.dispose();
		}
		
		@Override
		public int getSize() {return size;}
	}
	
	private Path                     gfxPath;
	private Font                     font;
	private Tileset.NamedImage       data;
	private Map<Integer, Image>      sizes;
	
	public Sprite(Tileset.NamedImage image, Path gfxPath, Font font)
	{
		this.gfxPath = gfxPath;
		this.font    = font;
		this.data    = image;
		this.sizes   = new HashMap<Integer, Image>();
	}
	
	public Path getGfxPath()          {return gfxPath;} 
	public void setGfxPath(Path path) {gfxPath = path;} 
	
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
	public void draw(RenderingAgent renderer, int x, int y) throws MissingImageException, IIOException
	{
		renderer.draw(this, x, y);
	}
}

