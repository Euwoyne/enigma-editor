package com.github.euwoyne.enigma_edit.view.swing;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

public class ObjectPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private final GroupLayout layout;
	
	public ObjectPanel()
	{
		this.setLayout(new GroupLayout(this));
		layout = (GroupLayout)this.getLayout();
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
	}
}

