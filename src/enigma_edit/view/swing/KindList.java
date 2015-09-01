
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import enigma_edit.error.MissingAttributeException;
import enigma_edit.error.MissingImageException;
import enigma_edit.model.I18N;
import enigma_edit.model.Tileset;
import enigma_edit.model.Tileset.VariantImage;

public class KindList extends JTabbedPane implements MouseListener 
{
	private static final long serialVersionUID = 1L;
	private static final int  ICONSIZE         = 16;
	
	private class Kind
	{
		I18N.KeyString      label;
		ArrayList<VariantImage> image;
		
		Kind(Tileset.Kind kind) throws MissingAttributeException
		{
			label = tileset.getString(kind.getI18n());
			image = kind.getImage();
		}
		
		Kind(Tileset.Alias alias)
		{
			label = tileset.getString(alias.getName());
			image = alias.getImage();
		}
	}
	
	private class Group
	{
		I18N.KeyString          label;
		ImageIcon               icon;
		DefaultListModel<Kind>  kinds;
		
		Group(Tileset.Group group) throws MissingAttributeException, MissingImageException
		{
			label = tileset.getString(group.getI18n());
			
			final Tileset.Kind iconsrc = tileset.getKind(group.getIcon());
			if (iconsrc != null)
				icon = new ImageIcon(((Sprite)iconsrc.getIcon().getSprite()).getImage(ICONSIZE));
			kinds = new DefaultListModel<Kind>();
		}
	}
	
	private Tileset tileset;
	private int     displaySize;
	
	private ArrayList<Group>       groups;
	private ArrayList<JList<Kind>> kindLists;
	private TileRenderer           renderer;
	
	private void setupGroups() throws MissingAttributeException, MissingImageException
	{
		Group icongroup;
		for (Tileset.Group group : tileset)
		{
			groups.add(new Group(group));
			icongroup = groups.get(groups.size() - 1);
			for (Tileset.Page page : group)
			{
				for (Tileset.Kind kind : page)
				{
					if (kind.isHidden()) continue;
					
					if (!kind.showAliases())
						icongroup.kinds.addElement(new Kind(kind));
					else
						for (Tileset.Alias alias : kind.getAliases())
							icongroup.kinds.addElement(new Kind(alias));
				}
			}
		}
	}
	
	private void setupJLists()
	{
		for (Group g : groups)
		{
			JList<Kind> jlist = new JList<Kind>(g.kinds);
			jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jlist.addMouseListener(this);
			jlist.setDoubleBuffered(true);
			jlist.setVisibleRowCount(-1);
			jlist.setCellRenderer(renderer);
			jlist.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			jlist.setOpaque(true);
			kindLists.add(jlist);
		}
	}
	
	public KindList(Tileset tileset, int size) throws MissingAttributeException, MissingImageException
	{
		this(tileset, size, Locale.getDefault());
	}
	
	public KindList(Tileset tileset, int size, Locale locale) throws MissingAttributeException, MissingImageException
	{
		this.tileset     = tileset;
		this.displaySize = size;
		this.groups      = new ArrayList<Group>();
		this.kindLists   = new ArrayList<JList<Kind>>();
		this.renderer    = new TileRenderer(locale);
		
		setupGroups();
		setupJLists();
		
		this.setTabPlacement(JTabbedPane.LEFT);
		for (int i = 0; i < groups.size(); ++i)
		{
			this.addTab(groups.get(i).label.get(locale.getLanguage()), groups.get(i).icon, new JScrollPane(kindLists.get(i)));
			this.getComponent(this.getComponentCount()-1).setPreferredSize(new Dimension(-1, (int)(1.5 * displaySize)));
		}
	}
	
	public int getIndexByLabel(String i18n)
	{
		int i = 0;
		for (Tileset.Group group : tileset)
		{
			if (group.getI18n().equals(i18n))
				return i;
			++i;
		}
		return -1;
	}
	
	private class TileRenderer extends JLabel implements ListCellRenderer<Kind>
	{
		private static final long serialVersionUID = 1L;
		
		private String                   lang;
		private Map<Kind, BufferedImage> sprites;
		
		public TileRenderer(Locale locale)
		{
			this.lang = locale.getLanguage();
			this.sprites = new HashMap<Kind, BufferedImage>();
			
			//this.setOpaque(false);
			//this.setBackground(null);
			this.setHorizontalTextPosition(JLabel.CENTER);
			this.setVerticalTextPosition(JLabel.BOTTOM);
			this.setHorizontalAlignment(JLabel.CENTER);
		}
		
		@Override
		public Component getListCellRendererComponent(JList<? extends Kind> list,
				                                      Kind                  kind,
				                                      int                   index,
				                                      boolean               isSelected,
				                                      boolean               cellHasFocus)
		{
			try
			{
				if (!sprites.containsKey(kind))
				{
					if (kind.image.size() == 1)
					{
						sprites.put(kind, ((Sprite)kind.image.get(0).getSprite()).getImage(KindList.this.displaySize));
					}
					else
					{
						Sprite.Image buffer = null;
						for (VariantImage image : kind.image)
						{
							if (buffer == null)
								buffer = ((Sprite)image.getSprite()).new Image(KindList.this.displaySize);
							buffer.draw(image);
						}
						sprites.put(kind, buffer);
					}
				}
				
				this.setText(kind.label.get(lang));
				this.setIcon(new ImageIcon(sprites.get(kind)));
			}
			catch (MissingImageException err)
			{
				err.showDialog(null, true);
			}
			return this;
			
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		try
		{
			if (!(e.getSource() instanceof JList)) return;
			Kind kind = (Kind)((JList<?>)e.getSource()).getSelectedValue();
			javax.swing.JFrame frame = new javax.swing.JFrame();
			frame.setTitle(kind.label.english);
			frame.add(new JLabel(kind.label.english, new ImageIcon(((Sprite)kind.image.get(0).getSprite()).getImage(64)), JLabel.CENTER));
			frame.pack();
			frame.setVisible(true);
		}
		catch (MissingImageException err)
		{
			err.showDialog(null, false);
		}
	}
	
	@Override public void mousePressed(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
}

