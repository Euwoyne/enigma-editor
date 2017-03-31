package com.github.euwoyne.enigma_edit.control;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

class ToggleAction extends Action
{
	private static final long serialVersionUID = 1L;
	
	public ToggleAction(int id, String name)            {this(id, name, false);}
	public ToggleAction(int id, String name, Icon icon) {this(id, name, icon, false);}
	
	public ToggleAction(int id, String name, boolean init)
	{
		super(id, name);
		this.putValue(Action.SELECTED_KEY, init);
	}
	
	public ToggleAction(int id, String name, Icon icon, boolean init)
	{
		super(id, name, icon);
		this.putValue(Action.SELECTED_KEY, init);
	}
	
	public boolean isSelected()
	{
		return (boolean)this.getValue(Action.SELECTED_KEY);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		this.setSelected((boolean)this.getValue(Action.SELECTED_KEY));
	}
}
