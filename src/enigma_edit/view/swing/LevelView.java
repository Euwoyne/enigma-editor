
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

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import enigma_edit.error.MissingImageException;
import enigma_edit.lua.data.Mode;
import enigma_edit.lua.data.Mode2;
import enigma_edit.lua.data.Tile;
import enigma_edit.model.RenderingAgent;
import enigma_edit.model.ImageTile;
import enigma_edit.model.Tileset;
import enigma_edit.model.World;

public class LevelView extends JPanel implements MouseListener
{
	private static final long serialVersionUID = 1L;
	private static final byte FLAG_FLOORS      = 0x1;
	private static final byte FLAG_ITEMS       = 0x2;
	private static final byte FLAG_ACTORS      = 0x4;
	private static final byte FLAG_STONES      = 0x8;
	
	World         world;
	Tileset       tileset;
	int           displaySize;
	BufferedImage buffer;
	
	Mode          mode;
	byte          visibility;
	
	public void setFloorVisibility(boolean b) {if (b) visibility |= FLAG_FLOORS; else visibility &= ~FLAG_FLOORS; drawBuffer(); this.repaint();}
	public void setItemVisibility (boolean b) {if (b) visibility |= FLAG_ITEMS;  else visibility &= ~FLAG_ITEMS;  drawBuffer(); this.repaint();}
	public void setActorVisibility(boolean b) {if (b) visibility |= FLAG_ACTORS; else visibility &= ~FLAG_ACTORS; drawBuffer(); this.repaint();}
	public void setStoneVisibility(boolean b) {if (b) visibility |= FLAG_STONES; else visibility &= ~FLAG_STONES; drawBuffer(); this.repaint();}
	public void setMode           (Mode mode) {this.mode = mode; drawBuffer(); this.repaint();}
	
	LevelView(Tileset tileset, int size)
	{
		this.world       = null;
		this.tileset     = tileset;
		this.displaySize = size;
		this.buffer      = null;
		this.mode        = Mode.DIFFICULT;
		this.visibility  = 0xf;
		this.addMouseListener(this);
	}
	
	void load(World world)
	{
		this.world = world;
		if (world == null || !world.isAnalysed()) return;
		this.setPreferredSize(new java.awt.Dimension(world.getWidth() * displaySize, world.getHeight() * displaySize));
		this.buffer = null;
		this.repaint();
	}
	
	private void drawBuffer()
	{
		final Graphics g = buffer.getGraphics();
		
		RenderingAgent render = new RenderingAgent()
		{
			@Override
			public void draw(enigma_edit.model.Sprite sprite, int x, int y)
			{
				try
				{
					g.drawImage(((Sprite)sprite).getImage(displaySize), x * displaySize, y * displaySize, null);
				}
				catch(MissingImageException e)
				{
					System.err.println(e);
				}
			}
		};
		
		ImageTile tile;
		for (int x = 1; x <= world.getWidth(); ++x)
		{
			for (int y = 1; y <= world.getHeight(); ++y)
			{
				tile = world.getTile(x, y);
				if (tile == null) continue;
				if ((visibility & FLAG_FLOORS) != 0) tile.draw_fl(render, x-1, y-1, mode);
				if ((visibility & FLAG_ITEMS)  != 0) tile.draw_it(render, x-1, y-1, mode);
				if ((visibility & FLAG_ACTORS) != 0) tile.draw_ac(render, x-1, y-1, mode);
				if ((visibility & FLAG_STONES) != 0) tile.draw_st(render, x-1, y-1, mode);
			}
		}
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (world == null) return;
		if (buffer == null)
		{
			buffer = new BufferedImage(world.getWidth() * displaySize, world.getHeight() * displaySize, BufferedImage.TYPE_INT_ARGB);
			drawBuffer();
		}
		g.drawImage(buffer, 0, 0, null);
	}

	private static void print(String label, Tile.Part part)
	{
		if (part.hasNormal() && part.get(Mode.NORMAL).isNormal())
		{
			System.out.println(label + ": " + part.get(Mode.NORMAL).checkTable(Mode2.EASY));
		}
		else
		{
			System.out.println(label + "[easy]: " + part.get(Mode.EASY).checkTable(Mode2.EASY));
			System.out.println(label + "[diff]: " + part.get(Mode.DIFFICULT).checkTable(Mode2.DIFFICULT));
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		final int x = e.getX() / displaySize + 1;
		final int y = e.getY() / displaySize + 1;
		if (x > 0 && y > 0 && x <= world.getWidth() && y <= world.getHeight())
		{
			final ImageTile tile = world.getTile(x, y);
			if (tile.has_fl()) print(String.format("%d:%d:floor", x, y), tile.fl());
			if (tile.has_st()) print(String.format("%d:%d:stone", x, y), tile.st());
			if (tile.has_it()) print(String.format("%d:%d:item",  x, y), tile.it());
			if (tile.has_ac()) print(String.format("%d:%d:actor", x, y), tile.ac());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
}

