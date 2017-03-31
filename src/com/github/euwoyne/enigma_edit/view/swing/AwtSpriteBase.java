package com.github.euwoyne.enigma_edit.view.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import com.github.euwoyne.enigma_edit.error.InternalError;
import com.github.euwoyne.enigma_edit.error.MissingImageException;
import com.github.euwoyne.enigma_edit.model.RenderingAgent;
import com.github.euwoyne.enigma_edit.model.Sprite;
import com.github.euwoyne.enigma_edit.model.SpriteFolder;
import com.github.euwoyne.enigma_edit.model.Tileset;

public class AwtSpriteBase
{
	protected final SpriteFolder gfxPath;
	protected final Font         font;
	
	protected AwtSpriteBase(SpriteFolder gfxPath, Font font)
	{
		this.gfxPath = gfxPath;
		this.font    = font;
	}
	
	public class AwtImage extends BufferedImage implements Sprite.Image, RenderingAgent
	{
		private final int size;
		
		AwtImage(int size)
		{
			super(size, size, BufferedImage.TYPE_INT_ARGB);
			this.size = size;
		}
		
		protected void draw(Tileset.VariantImage image) throws MissingImageException
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
		public void draw(Sprite.Image image, int x, int y)
		{
			if (image instanceof AwtImage)
			{
				final Graphics2D g = this.createGraphics();
				final ImageObserver observer = new ImageObserver()
				{
					@Override
					public boolean imageUpdate(java.awt.Image img, int flags, int x, int y, int w, int h)
					{
						if ((flags & ALLBITS) != 0)
						{
							g.drawImage(img, x, y, w, h, null);
							return true;
						}
						else return false;
					}
				};
				
				g.drawImage((AwtImage)image, 0, 0, observer);
			}
			else throw new InternalError("FrontendMismatch", this.getClass().getName(), image.getClass().getName());
		}
	}
}

