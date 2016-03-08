package com.github.euwoyne.enigma_edit.view.swing;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.github.euwoyne.enigma_edit.control.Action;

class ToolBar extends JToolBar
{
	private static final long serialVersionUID = 1L;
	
	public ToolBar(String name)
	{
		super(name, JToolBar.HORIZONTAL);
		this.setFloatable(false);
		this.setRollover(true);
		this.setFocusable(false);
	}
	
	public JButton addButton(Action action)
	{
		JButton button = new JButton(action);
		button.setText("");
		button.setFocusable(false);
		button.setPreferredSize(new Dimension(32, 32));
		button.setMinimumSize(new Dimension(32, 32));
		button.setMaximumSize(new Dimension(32, 32));
		action.addSource(button);
		this.add(button);
		return button;
	}
	
	public JToggleButton addToggle(Action action)
	{
		JToggleButton button = new JToggleButton(action);
		button.setText("");
		button.setFocusable(false);
		button.setPreferredSize(new Dimension(32, 32));
		button.setMinimumSize(new Dimension(32, 32));
		button.setMaximumSize(new Dimension(32, 32));
		action.addSource(button);
		this.add(button);
		return button;
	}
}