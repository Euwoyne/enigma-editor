package com.github.euwoyne.enigma_edit.view.swing;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.github.euwoyne.enigma_edit.control.Action;

class MainMenu extends JMenu
{
	private static final long serialVersionUID = 1L;
	
	public MainMenu(String name) {super(name);}
	
	public JMenuItem addItem(Action action)
	{
		final JMenuItem item = new JMenuItem(action);
		action.addSource(item);
		this.add(item);
		return item;
	}
	
	public JCheckBoxMenuItem addCheckBox(Action action)
	{
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
		action.addSource(item);
		this.add(item);
		return item;
	}
}

