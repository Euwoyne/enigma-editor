
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import enigma_edit.error.MissingImageException;
import enigma_edit.model.Tileset;

public class Sprite implements enigma_edit.model.Sprite
{
	public class Image extends BufferedImage implements enigma_edit.model.Sprite.Image
	{
		final int size;
		      int layers, loaded;
		
		Image(int size)
		{
			super(size, size, Image.TYPE_INT_ARGB);
			
			this.size   = size;
			this.layers = 0;
			this.loaded = 0;
		}
		
		void stack(Tileset.Image data) throws MissingImageException
		{
			Graphics2D     g;
			java.awt.Image file;
			
			try
			{
				g = this.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				while (data != null)
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
						TextLayout layout = new TextLayout(data.getText(), g.getFont(), g.getFontRenderContext());
						g.setPaint(Color.WHITE);
						layout.draw(g, 3, textheight);
						g.setPaint(Color.RED);
						layout.draw(g, 2, textheight);
					}
					data = data.getStack();
				}
				g.dispose();
			}
			catch (java.io.IOException e)
			{
				throw new MissingImageException(data.getFile());
			}
		}
		
		@Override
		public boolean isReady() {return layers == loaded;}
		
		@Override
		public int getSize() {return size;}
	}
	
	private Path                     gfxPath;
	private Font                     font;
	private Map<Integer, Image>      sizes;
	private ArrayList<Tileset.Image> data;
	
	public Sprite(Tileset.Image image, Path gfxPath, Font font)
	{
		this.gfxPath = gfxPath;
		this.font    = font;
		this.sizes   = new HashMap<Integer, Image>();
		this.data    = new ArrayList<Tileset.Image>();
		
		data.add(image);
	}

	public void stack(Tileset.Image image) throws MissingImageException
	{
		data.add(image);
		for (Map.Entry<Integer, Image> i : sizes.entrySet())
			i.getValue().stack(image);
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
			for (Tileset.Image source : data)
			{
				image.stack(source);
			}
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
	
	public static void draw(Graphics2D g, int x, int y, enigma_edit.model.Sprite sprite, int size) throws MissingImageException, IIOException
	{
		if (sprite.getImage(size) instanceof Sprite.Image)
			g.drawImage((Sprite.Image)sprite.getImage(size), x, y, null);
		else
			throw new IIOException("Unsupported Image Class.");
	}
}

