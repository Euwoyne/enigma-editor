
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

public class KindList extends JTabbedPane implements MouseListener 
{
	private static final long serialVersionUID = 1L;
	private static final int  ICONSIZE         = 16;
	
	private class Kind
	{
		I18N.KeyString           label;
		ArrayList<Tileset.Image> image;
		
		Kind(Tileset.Kind kind) throws MissingAttributeException
		{
			label = tileset.getString(kind.getI18n());
			image = new ArrayList<Tileset.Image>();
			if (kind.hasStack())
			{
				for (Tileset.Variants v : kind.getStack())
				{
					if (!v.isVisual(kind)) continue;
					image.add(v.getDefaultVariant(kind).getImage());
				}
			}
			else image.add(kind.getDefaultImage());
			
			while (image.get(image.size() - 1).getStack() != null)
				image.add(image.get(image.size() - 1).getStack());
		}
		
		Kind(Tileset.Variant variant)
		{
			label = tileset.getString(variant.getName());
			image = new ArrayList<Tileset.Image>();
			image.add(variant.getImage());
			
			while (image.get(image.size() - 1).getStack() != null)
				image.add(image.get(image.size() - 1).getStack());
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
			icon = new ImageIcon(((Sprite)group.getIconImage().getSprite()).getImage(ICONSIZE));
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
					if (!kind.hasStack() && kind.hasVariants() && kind.getVariants().getShowAll())
					{
						for (Tileset.Variant variant : kind.getVariants())
							icongroup.kinds.addElement(new Kind(variant));
					}
					else
						icongroup.kinds.addElement(new Kind(kind));
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
	
	private class TileRenderer extends JLabel implements ListCellRenderer<Kind>
	{
		private static final long serialVersionUID = 1L;
		
		private String            lang;
		private Map<Kind, Sprite> sprites;
		
		public TileRenderer(Locale locale)
		{
			this.lang = locale.getLanguage();
			this.sprites = new HashMap<Kind, Sprite>();
			
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
						sprites.put(kind, (Sprite)kind.image.get(0).getSprite());
					}
					else
					{
						Sprite sprite = null;
						for (Tileset.Image image : kind.image)
						{
							if (sprite == null)
								sprite = new Sprite(image, ((Sprite)image.getSprite()).getGfxPath(), ((Sprite)image.getSprite()).getFont());
							else
								sprite.stack(image);
						}
						sprites.put(kind, sprite);
					}
				}
				
				this.setText(kind.label.get(lang));
				this.setIcon(new ImageIcon(sprites.get(kind).getImage(KindList.this.displaySize)));
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

