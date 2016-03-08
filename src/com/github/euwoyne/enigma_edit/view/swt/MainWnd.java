package com.github.euwoyne.enigma_edit.view.swt;

import java.io.IOException;

import javax.swing.JScrollPane;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.github.euwoyne.enigma_edit.Options;
import com.github.euwoyne.enigma_edit.Resources;
import com.github.euwoyne.enigma_edit.error.MissingAttributeException;
import com.github.euwoyne.enigma_edit.model.Tileset;
import com.github.euwoyne.enigma_edit.view.CodeEditor;

import org.eclipse.swt.widgets.Label;

public class MainWnd
{
	private Shell      shell;
	//private Level      level;
	private CodeEditor codeEditor;
	private TabFolder  editTabs;
	private KindList   kindList;
	
	public static void main(String[] args) throws MissingAttributeException, IOException {new MainWnd(null, null);}
	
	public MainWnd(Tileset tileset, Options options) throws MissingAttributeException, IOException
	{
		this.open();
	}
	
	/**
	 * Open the window.
	 * @wbp.parser.entryPoint
	 */
	public void open()
	{
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}
	
	/**
	 * Create contents of the window.
	 */
	protected void createContents()
	{
		// setup shell
		shell = new Shell();
		shell.setSize(800, 600);
		shell.setText(Resources.uiText.getString("MainWnd.title.long"));
		shell.setLayout(new FormLayout());
		
		// create components
		editTabs = new TabFolder(shell, SWT.BOTTOM);
		final Sash sash = new Sash(shell, SWT.VERTICAL);
		kindList = new KindList(shell, SWT.BOTTOM);
		
		// setup editor tabs
		TabItem levelTab = new TabItem(editTabs, SWT.NONE);
		levelTab.setText(Resources.uiText.getString("MainWnd.levelTab"));
		Label lblLevel = new Label(editTabs, SWT.NONE);
		levelTab.setControl(lblLevel);
		lblLevel.setText("LEVEL");
		
		codeEditor = new CodeEditor();
		JScrollPane scrollPane = new JScrollPane(codeEditor);
		codeEditor.setup();
		
		Composite codeComposite = new Composite(editTabs, SWT.EMBEDDED | SWT.DOUBLE_BUFFERED);
		java.awt.Frame awtFrame = SWT_AWT.new_Frame(codeComposite);
		java.awt.Panel awtPanel = new java.awt.Panel(new java.awt.BorderLayout());
		awtFrame.add(awtPanel);
		awtPanel.add(scrollPane);
		
		TabItem codeTab = new TabItem(editTabs, SWT.NONE);
		codeTab.setText(Resources.uiText.getString("MainWnd.codeTab"));
		codeTab.setControl(codeComposite);
		
		codeEditor.setText(
			"-- Markov Chain Program in Lua" + "\n"
			+ "" + "\n"
			+ "function allwords ()" + "\n"
			+ "  local line = io.read()    -- current line" + "\n"
			+ "  local pos = 1             -- current position in the line" + "\n"
			+ "  return function ()        -- iterator function" + "\n"
			+ "    while line do           -- repeat while there are lines" + "\n"
			+ "      local s, e = string.find(line, \"%w+\", pos)" + "\n"
			+ "      if s then      -- found a word?" + "\n"
			+ "        pos = e + 1  -- update next position" + "\n"
			+ "        return string.sub(line, s, e)   -- return the word" + "\n"
			+ "      else" + "\n"
			+ "        line = io.read()    -- word not found; try next line" + "\n"
			+ "        pos = 1             -- restart from first position" + "\n"
			+ "      end" + "\n"
			+ "    end" + "\n"
			+ "    return nil            -- no more lines: end of traversal" + "\n"
			+ "  end" + "\n"
			+ "end" + "\n"
			+ "" + "\n"
			+ "function prefix (w1, w2)" + "\n"
			+ "  return w1 .. ' ' .. w2" + "\n"
			+ "end" + "\n"
			+ "" + "\n"
			+ "local statetab" + "\n"
			+ "" + "\n"
			+ "function insert (index, value)" + "\n"
			+ "  if not statetab[index] then" + "\n"
			+ "    statetab[index] = {n=0}" + "\n"
			+ "  end" + "\n"
			+ "  table.insert(statetab[index], value)" + "\n"
			+ "end" + "\n"
			+ "" + "\n"
			+ "local N  = 2" + "\n"
			+ "local MAXGEN = 10000" + "\n"
			+ "local NOWORD = \"\n\"" + "\n"
			+ "" + "\n"
			+ "-- build table" + "\n"
			+ "statetab = {}" + "\n"
			+ "local w1, w2 = NOWORD, NOWORD" + "\n"
			+ "for w in allwords() do" + "\n"
			+ "  insert(prefix(w1, w2), w)" + "\n"
			+ "  w1 = w2; w2 = w;" + "\n"
			+ "end" + "\n"
			+ "insert(prefix(w1, w2), NOWORD)" + "\n"
			+ "" + "\n"
			+ "-- generate text" + "\n"
			+ "w1 = NOWORD; w2 = NOWORD     -- reinitialize" + "\n"
			+ "for i=1,MAXGEN do" + "\n"
			+ "  local list = statetab[prefix(w1, w2)]" + "\n"
			+ "  -- choose a random item from list" + "\n"
			+ "  local r = math.random(table.getn(list))" + "\n"
			+ "  local nextword = list[r]" + "\n"
			+ "  if nextword == NOWORD then return end" + "\n"
			+ "  io.write(nextword, \" \")" + "\n"
			+ "  w1 = w2; w2 = nextword" + "\n"
			+ "end" + "\n"
			+ "\n");
		
		//Label lblCode = new Label(editTabs, SWT.NONE);
		//codeTab.setControl(lblCode);
		//lblCode.setText("CODE");
		
		final FormData editData = new FormData();
		editData.left = new FormAttachment (0, 0);
		editData.right = new FormAttachment (sash, 0);
		editData.top = new FormAttachment (0, 0);
		editData.bottom = new FormAttachment (100, 0);
		editTabs.setLayoutData (editData);
		
		// setup sash
		final int limit = 20, percent = 50;
		final FormData sashData = new FormData();
		sashData.left = new FormAttachment(percent, 0);
		sashData.top = new FormAttachment(0, 0);
		sashData.bottom = new FormAttachment(100, 0);
		sash.setLayoutData(sashData);
		sash.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent (Event e)
			{
				Rectangle sashRect = sash.getBounds ();
				Rectangle shellRect = shell.getClientArea ();
				int right = shellRect.width - sashRect.width - limit;
				e.x = Math.max (Math.min (e.x, right), limit);
				if (e.x != sashRect.x)
				{
					sashData.left = new FormAttachment (0, e.x);
					shell.layout ();
				}
			}
		});
		
		// setup kind list
		final FormData kindData = new FormData();
		kindData.left = new FormAttachment (sash, 0);
		kindData.right = new FormAttachment (100, 0);
		kindData.top = new FormAttachment (0, 0);
		kindData.bottom = new FormAttachment (100, 0);
		kindList.setLayoutData (kindData);
	}
}

