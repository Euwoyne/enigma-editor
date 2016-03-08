package com.github.euwoyne.enigma_edit.view.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.github.euwoyne.enigma_edit.Resources;

public class KindList extends TabFolder
{
	private TabItem   levelTab;
	private TabItem   codeTab;
	
	public KindList(Composite parent, int style)
	{
		super(parent, style);
		
		levelTab = new TabItem(this, SWT.NONE);
		levelTab.setText(Resources.uiText.getString("MainWnd.levelTab"));
		Label lblLevel = new Label(this, SWT.NONE);
		levelTab.setControl(lblLevel);
		lblLevel.setText("LEVEL");
		//lblLevel.setBounds(10, 100, 100, 100);
		
		codeTab = new TabItem(this, SWT.NONE);
		codeTab.setText(Resources.uiText.getString("MainWnd.codeTab"));
		Label lblCode = new Label(this, SWT.NONE);
		codeTab.setControl(lblCode);
		lblCode.setText("CODE");
		//lblCode.setBounds(10, 100, 100, 100);
	}

	@Override
	protected void checkSubclass() {}
}
