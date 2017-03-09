package com.github.euwoyne.enigma_edit.view.swing;

import java.awt.Color;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.euwoyne.enigma_edit.control.Updateable;
import com.github.euwoyne.enigma_edit.error.MissingImageException;
import com.github.euwoyne.enigma_edit.model.ImageTile;
import com.github.euwoyne.enigma_edit.model.RenderingAgent;
import com.github.euwoyne.enigma_edit.model.Sprite;
import com.github.euwoyne.enigma_edit.model.TilePart;
import com.github.euwoyne.enigma_edit.model.Tileset;

public class ObjectPanel extends JPanel implements Updateable 
{
	private static final long serialVersionUID = 1L;
	private static final int ICONSIZE = 48;
	
	private static class IconPanel extends JPanel implements RenderingAgent
	{
		private static final long serialVersionUID = 1L;
		
		public java.awt.Image image;
		
		IconPanel()
		{
			final java.awt.Dimension iconSize = new java.awt.Dimension(ICONSIZE + 8, ICONSIZE + 8);
			this.setMaximumSize(iconSize);
			this.setPreferredSize(iconSize);
			this.setMinimumSize(iconSize);
			this.setVisible(true);
			this.setBackground(Color.WHITE);
		}
		
		@Override
		public void paintComponent(java.awt.Graphics g)
		{
			g.drawImage(image, 4, 4, null);
		}
		
		@Override
		public void draw(Sprite.Image image, int x, int y)
		{
			this.image = ((AwtSprite.Image)image);
		}
	}
	
	private final BoxLayout   layout;
	private final JTextField  txtCanon;
	private final IconPanel   imgIcon;
	
	public ObjectPanel()
	{
		txtCanon = new JTextField("blubb");
		imgIcon = new IconPanel();
		
		layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		this.add(imgIcon);
		this.add(Box.createVerticalGlue());
		this.setBackground(Color.BLUE);
		this.setVisible(true);
	}
	
	public void show(TilePart part)
	{
		txtCanon.setText(part.canonical());
		ImageTile.Part imgTile = new ImageTile.Part(part);
		try {
			imgTile.draw(imgIcon, 0, 0, ICONSIZE);
		} catch (MissingImageException e) {
			imgIcon.image = new java.awt.image.BufferedImage(ICONSIZE,ICONSIZE,java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
		}
	}
	
	public void show(Tileset.Kind kind)
	{
		txtCanon.setText(kind.getKindName());
		try {
			kind.getIcon().draw(imgIcon, 0, 0, ICONSIZE);
		} catch (MissingImageException e) {
			imgIcon.image = new java.awt.image.BufferedImage(ICONSIZE,ICONSIZE,java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
		}
	}
	
	@Override
	public void update()
	{
		imgIcon.repaint();
		this.repaint();
	}
}

