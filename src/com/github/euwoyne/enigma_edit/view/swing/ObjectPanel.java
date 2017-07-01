package com.github.euwoyne.enigma_edit.view.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
			final java.awt.Dimension iconSize = new java.awt.Dimension(ICONSIZE + 2, ICONSIZE + 2);
			this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			this.setMaximumSize(iconSize);
			this.setPreferredSize(iconSize);
			this.setMinimumSize(iconSize);
			this.setBackground(Color.WHITE);
			this.setVisible(true);
		}
		
		@Override
		public void paintComponent(java.awt.Graphics g)
		{
			super.paintComponent(g);
			g.drawImage(image, 1, 1, null);
		}
		
		@Override
		public void draw(Sprite.Image image, int x, int y)
		{
			this.image = ((AwtSprite.AwtImage)image);
		}
	}
	
	private final Tileset         tileset;
	private final String          lang;
	private final BoxLayout       layout;
	private final JLabel          lblTitle;
	private final JLabel          lblName;
	private final IconPanel       imgIcon;
	private final AttributePanel  pnlGroups;
	
	public ObjectPanel(Tileset tileset)
	{
		this(tileset, Locale.getDefault());
	}
	
	public ObjectPanel(Tileset tileset, Locale locale)
	{
		this.tileset = tileset;
		this.lang = locale.getLanguage();
		this.lblTitle = new JLabel();
		this.lblName = new JLabel();
		this.imgIcon = new IconPanel();
		this.pnlGroups = new AttributePanel(tileset, locale);
		
		lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 15.0f));
		lblTitle.setHorizontalAlignment(JLabel.RIGHT);
		lblName.setFont(lblTitle.getFont().deriveFont(Font.ITALIC, 12.0f));
		lblName.setHorizontalAlignment(JLabel.RIGHT);
		
		JPanel pnlHeader = new JPanel();
		JPanel pnlNames  = new JPanel(new GridLayout(2,1));
		pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.X_AXIS));
		pnlHeader.add(Box.createRigidArea(new java.awt.Dimension(16,0)));
		pnlNames.add(lblTitle);
		pnlNames.add(lblName);
		pnlHeader.add(pnlNames);
		pnlHeader.add(Box.createRigidArea(new java.awt.Dimension(12,0)));
		pnlHeader.add(imgIcon);
		pnlHeader.add(Box.createRigidArea(new java.awt.Dimension(16,0)));
		
		pnlHeader.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, ICONSIZE + 2));
		pnlHeader.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, ICONSIZE + 2));
		
		pnlGroups.setLayout(new BoxLayout(pnlGroups, BoxLayout.Y_AXIS));
		pnlGroups.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		
		JScrollPane scrolls = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrolls.setViewportView(pnlGroups);
		
		this.layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		this.add(Box.createRigidArea(new java.awt.Dimension(0,16)));
		this.add(pnlHeader);
		this.add(Box.createRigidArea(new java.awt.Dimension(0,16)));
		this.add(scrolls);
		this.add(Box.createVerticalGlue());
		this.setVisible(true);
	}
	
	public void show(TilePart part)
	{
		lblTitle.setText(tileset.getString(part.getKind().getI18n()).get(lang));
		lblName.setText(part.getKindName());
		
		try {
			ImageTile.Part imgTile = new ImageTile.Part(part);
			imgTile.draw(imgIcon, 0, 0, ICONSIZE);
		} catch (MissingImageException e) {
			imgIcon.image = new java.awt.image.BufferedImage(ICONSIZE,ICONSIZE,java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
		}
	}
	
	public void show(Tileset.Kind kind)
	{
		// show icon
		try {
			kind.getIcon().draw(imgIcon, 0, 0, ICONSIZE);
		} catch (MissingImageException e) {
			imgIcon.image = new java.awt.image.BufferedImage(ICONSIZE,ICONSIZE,java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
		}
				
		lblTitle.setText(tileset.getString(kind.getI18n()).get(lang));
		lblName.setText(kind.getName());
		
		// show attributes
		pnlGroups.show(kind);
	}
	
	@Override
	public void update()
	{
		imgIcon.repaint();
		this.repaint();
	}
}

