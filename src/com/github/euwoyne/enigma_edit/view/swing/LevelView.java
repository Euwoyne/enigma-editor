
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

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

import com.github.euwoyne.enigma_edit.control.LevelClickListener;
import com.github.euwoyne.enigma_edit.control.Updateable;
import com.github.euwoyne.enigma_edit.error.MissingImageException;
import com.github.euwoyne.enigma_edit.lua.data.Mode;
import com.github.euwoyne.enigma_edit.lua.data.Tile;
import com.github.euwoyne.enigma_edit.model.ImageTile;
import com.github.euwoyne.enigma_edit.model.RenderingAgent;
import com.github.euwoyne.enigma_edit.model.Sprite;
import com.github.euwoyne.enigma_edit.model.World;

public class LevelView extends JPanel implements MouseListener, Updateable
{
	private static final long serialVersionUID = 1L;
	private static final byte FLAG_FLOORS      = 0x1;
	private static final byte FLAG_ITEMS       = 0x2;
	private static final byte FLAG_ACTORS      = 0x4;
	private static final byte FLAG_STONES      = 0x8;
	
	private World         world;
	private int           displaySize;
	private BufferedImage buffer;
	private boolean       dirty;
	
	private Mode          mode;
	private byte          visibility;
	
	private ArrayList<LevelClickListener> listeners;
	
	public void setFloorVisibility(boolean b) {if (b) visibility |= FLAG_FLOORS; else visibility &= ~FLAG_FLOORS; dirty = true;}
	public void setItemVisibility (boolean b) {if (b) visibility |= FLAG_ITEMS;  else visibility &= ~FLAG_ITEMS;  dirty = true;}
	public void setActorVisibility(boolean b) {if (b) visibility |= FLAG_ACTORS; else visibility &= ~FLAG_ACTORS; dirty = true;}
	public void setStoneVisibility(boolean b) {if (b) visibility |= FLAG_STONES; else visibility &= ~FLAG_STONES; dirty = true;}
	public void setMode           (Mode mode) {this.mode = mode; dirty = true;}
	
	LevelView(int size)
	{
		this.world       = null;
		this.displaySize = size;
		this.buffer      = null;
		this.dirty       = false;
		this.mode        = Mode.DIFFICULT;
		this.visibility  = 0xf;
		this.listeners   = new ArrayList<LevelClickListener>();
		this.addMouseListener(this);
	}
	
	void load(World world)
	{
		this.world = world;
		if (world == null || !world.isAnalysed()) return;
		this.setPreferredSize(new java.awt.Dimension(world.getWidth() * displaySize, world.getHeight() * displaySize));
		this.buffer = null;
	}
	
	private void drawBuffer()
	{
		final Graphics g = buffer.getGraphics();
		
		RenderingAgent render = new RenderingAgent()
		{
			@Override
			public void draw(Sprite.Image sprite, int x, int y)
			{
				g.drawImage((AwtSprite.AwtImage)sprite, x * displaySize, y * displaySize, null);
			}
		};
		
		ImageTile tile;
		for (int x = 1; x <= world.getWidth(); ++x)
		{
			for (int y = 1; y <= world.getHeight(); ++y)
			{
				tile = world.getTile(x, y);
				if (tile == null) continue;
				if ((visibility & FLAG_FLOORS) != 0) try
				{
					tile.draw_fl(render, x-1, y-1, displaySize, mode);
				}
				catch (MissingImageException e) {System.err.println(e.getLocalizedMessage());}
				
				if ((visibility & FLAG_ITEMS)  != 0) try
				{
					tile.draw_it(render, x-1, y-1, displaySize, mode);
				}
				catch (MissingImageException e) {System.err.println(e.getLocalizedMessage());}
				
				if ((visibility & FLAG_ACTORS) != 0) try
				{
					tile.draw_ac(render, x-1, y-1, displaySize, mode);
				}
				catch (MissingImageException e) {System.err.println(e.getLocalizedMessage());}
				
				if ((visibility & FLAG_STONES) != 0) try
				{
					tile.draw_st(render, x-1, y-1, displaySize, mode);
				}
				catch (MissingImageException e) {System.err.println(e.getLocalizedMessage());};
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
		else if (dirty)
		{
			drawBuffer();
		}
		g.drawImage(buffer, 0, 0, null);
	}
	
	@Override
	public void update()
	{
		dirty = true;
		this.invalidate();
		this.repaint();
	}
	
	public void addLevelClickListener(LevelClickListener l)
	{
		listeners.add(l);
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		final int x = e.getX() / displaySize + 1;
		final int y = e.getY() / displaySize + 1;
		if (x > 0 && y > 0 && x <= world.getWidth() && y <= world.getHeight())
		{
			final Tile tile = world.getTile(x, y).tile();
			for (LevelClickListener listener : listeners)
				listener.levelClicked(x, y, tile);
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

