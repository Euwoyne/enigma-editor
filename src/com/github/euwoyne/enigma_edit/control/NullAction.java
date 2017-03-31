package com.github.euwoyne.enigma_edit.control;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

class NullAction extends Action
{
	private static final long serialVersionUID = 1L;
	
	NullAction(String text, Icon icon) {super(0, text, icon);}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		System.out.println("Action " + this.getValue(AbstractAction.NAME) + " performed");
	}
}
