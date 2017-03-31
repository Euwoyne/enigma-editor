
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.github.euwoyne.enigma_edit.control.KindSelectionListener;
import com.github.euwoyne.enigma_edit.error.InternalError;
import com.github.euwoyne.enigma_edit.error.MissingAttributeException;
import com.github.euwoyne.enigma_edit.error.MissingImageException;
import com.github.euwoyne.enigma_edit.model.SpriteStack;
import com.github.euwoyne.enigma_edit.model.Tileset;

@SuppressWarnings("serial")
public class KindList extends JPanel 
{
	private static final int ICONSIZE = 16;
	
	private class KindAction extends AbstractAction
	{
		private final Tileset.Kind kind;
		
		KindAction(Tileset.Kind kind, String label, Icon icon)
		{
			super(label, icon);
			this.kind = kind;
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			for (KindSelectionListener l : listeners)
				l.kindSelected(kind);
		}
	}
	
	private class PageMenu extends JMenu
	{
		PageMenu(Tileset.Page page, String label)
		{
			super(label);
			super.getPopupMenu().setLayout(new java.awt.GridLayout(0,3));
			
			for (Tileset.Kind kind : page)
			{
				this.add(new JMenuItem(new KindAction(kind, tileset.getString(kind.getI18n()).get(lang), KindList.this.getIcon(kind))));
			}
		}
	}
	
	private class GroupButton extends JButton
	{
		GroupButton(Tileset.Group group, String label, Icon icon)
		{
			super(label, icon);
			
			JPopupMenu popup = new JPopupMenu();
			if (group.size() == 1)
			{
				for (Tileset.Kind kind : group.get(0))
				{
					popup.add(new JMenuItem(new KindAction(kind, tileset.getString(kind.getI18n()).get(lang), KindList.this.getIcon(kind))));
				}
			}
			else
			{
				for (Tileset.Page page : group)
				{
					final String i18n = page.getI18n();
					popup.add(new PageMenu(page, i18n != null ? tileset.getString(page.getI18n()).get(lang) : "..."));
				}
			}
			
			this.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					popup.show(GroupButton.this, getWidth(), 0);
				}
			});
		}
	}
	
	private final Tileset tileset;
	private final String  lang;
	private final int     displaySize;
	
	private ArrayList<KindSelectionListener> listeners;
	
	private ImageIcon getIcon(Tileset.Kind kind, int size)
	{
		try
		{
			final SpriteStack        images = kind.getImage();
			final AwtSprite.AwtImage sprite = (images.size() == 1) ?
					((AwtSprite)images.get(0)).getImage(size) :
					AwtSprite.create((AwtSpriteSet)tileset.getSpriteset(), images, size);
			return new ImageIcon(sprite);
		}
		catch (ClassCastException e)
		{
			throw new InternalError("FrontendMismatch",
					AwtSprite.AwtImage.class.getName(),
					tileset.getSpriteset().getClass().getName());
		}
		catch (MissingImageException e)
		{
			return null;
		}
	}
	
	private ImageIcon getIcon(Tileset.Kind kind)
	{
		return getIcon(kind, displaySize);
	}
	
	private ImageIcon getIcon(Tileset.Group group)
	{
		Tileset.Kind kind = tileset.getKind(group.getIcon());
		return (kind == null) ? null : getIcon(kind, ICONSIZE);
	}
	
	public KindList(Tileset tileset, int size) throws MissingAttributeException, MissingImageException, InternalError
	{
		this(tileset, size, Locale.getDefault());
	}
	
	public KindList(Tileset tileset, int size, Locale locale) throws MissingAttributeException, MissingImageException, InternalError
	{
		this.tileset     = tileset;
		this.lang        = locale.getLanguage();
		this.displaySize = size;
		this.listeners   = new ArrayList<KindSelectionListener>();
		
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		
		for (Tileset.Group group : tileset)
		{
			this.add(new GroupButton(group, tileset.getString(group.getI18n()).get(lang), getIcon(group)));
		}
	}
	
	public void addKindSelectionListener(KindSelectionListener l)
	{
		listeners.add(l);
	}
	
	public Icon getGroupIcon(String i18n)
	{
		int i = 0;
		for (Tileset.Group group : tileset)
		{
			if (group.getI18n().equals(i18n))
				return ((GroupButton)this.getComponents()[i]).getIcon();
			++i;
		}
		return null;
	}
}

