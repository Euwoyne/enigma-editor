package com.github.euwoyne.enigma_edit.control;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

public class ToggleAction extends Action
{
	private static final long serialVersionUID = 1L;
	
	public ToggleAction(int id, String name)            {super(id, name);}
	public ToggleAction(int id, String name, Icon icon) {super(id, name, icon);}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		this.setSelected((boolean)this.getValue(Action.SELECTED_KEY));
	}
}