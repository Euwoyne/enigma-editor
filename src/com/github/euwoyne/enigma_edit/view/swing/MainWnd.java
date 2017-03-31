
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

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.euwoyne.enigma_edit.Options;
import com.github.euwoyne.enigma_edit.Resources;
import com.github.euwoyne.enigma_edit.control.Action;
import com.github.euwoyne.enigma_edit.control.CodeChangeListener;
import com.github.euwoyne.enigma_edit.control.Controller;
import com.github.euwoyne.enigma_edit.control.KindSelectionListener;
import com.github.euwoyne.enigma_edit.control.LevelClickListener;
import com.github.euwoyne.enigma_edit.error.MissingAttributeException;
import com.github.euwoyne.enigma_edit.error.MissingImageException;
import com.github.euwoyne.enigma_edit.lua.data.CodeSnippet;
import com.github.euwoyne.enigma_edit.lua.data.Mode;
import com.github.euwoyne.enigma_edit.model.Level;
import com.github.euwoyne.enigma_edit.model.Tileset;
import com.github.euwoyne.enigma_edit.view.CodeEditor;

public class MainWnd extends JFrame implements WindowListener
{
	private static final long   serialVersionUID = 1L;
	private static final int    LEVELTAB         = 0;
	private static final int    CODETAB          = 1;
	private static final String OBJECTPANEL      = "objectPanel";
	private static final String TILEPANEL        = "tilePanel";
	private static final String METAPANEL        = "metaPanel";
	
	private static final String strTitleLong       = Resources.uiText.getString("MainWnd.title.long");
	private static final String strFileMenu        = Resources.uiText.getString("MainWnd.fileMenu");
	private static final String strFileMenu_new    = Resources.uiText.getString("MainWnd.fileMenu.new");
	private static final String strFileMenu_open   = Resources.uiText.getString("MainWnd.fileMenu.open");
	private static final String strFileMenu_save   = Resources.uiText.getString("MainWnd.fileMenu.save");
	private static final String strFileMenu_saveAs = Resources.uiText.getString("MainWnd.fileMenu.saveAs");
	private static final String strFileMenu_exit   = Resources.uiText.getString("MainWnd.fileMenu.exit");
	private static final String strLevelMenu       = Resources.uiText.getString("MainWnd.levelMenu");
	private static final String strLevelMenu_info  = Resources.uiText.getString("MainWnd.levelMenu.info");
	private static final String strViewMenu        = Resources.uiText.getString("MainWnd.viewMenu");
	private static final String strViewMenu_items  = Resources.uiText.getString("MainWnd.viewMenu.items");
	private static final String strViewMenu_actors = Resources.uiText.getString("MainWnd.viewMenu.actors");
	private static final String strViewMenu_stones = Resources.uiText.getString("MainWnd.viewMenu.stones");
	private static final String strModeMenu_easy      = Resources.uiText.getString("MainWnd.modeMenu.easy");
	private static final String strModeMenu_difficult = Resources.uiText.getString("MainWnd.modeMenu.difficult");
	
	private static final String strLevelTab  = Resources.uiText.getString("MainWnd.levelTab");
	private static final String strCodeTab   = Resources.uiText.getString("MainWnd.codeTab");
	
	private static final Icon   icoFile      = UIManager.getIcon("FileView.fileIcon");
	private static final Icon   icoDirectory = UIManager.getIcon("FileView.directoryIcon");
	private static final Icon   icoFloppy    = UIManager.getIcon("FileView.floppyDriveIcon");
	
	private Controller  controller;
	
	private LevelView   levelView;
	private CardLayout  infoLayout;
	private JPanel      infoPanel;
	private ObjectPanel objectPanel;
	private TilePanel   tilePanel;
	private MetaPanel   metaPanel;
	private CodeEditor  codeEditor;
	private JTabbedPane editTabs;
	private KindList    kindList;
	private JMenuBar    menuBar;
	private ToolBar     toolBar;
	
	private static ImageIcon loadIcon(String path, int size)
	{
		Image img    = new ImageIcon(path).getImage();
		int   height = img.getWidth(null);
		int   width  = img.getHeight(null);
		if (width == height)
		{
			width = height = size;
		}
		if (width > height)
		{
			height = (height * size) / width;
			width  = size;
		}
		else
		{
			width  = (width * size) / height;
			height = size;
		}
		BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics      g2d    = scaled.createGraphics();
		g2d.drawImage(img, 0, 0, width, height, null);
		return new ImageIcon(scaled);
	}
	
	@SuppressWarnings("serial")
	public MainWnd(Controller ctrl, Tileset tileset, Options options) throws MissingAttributeException, MissingImageException
	{
		// setup window
		super(Resources.uiText.getString("MainWnd.title.long"));
		this.getContentPane().setLayout(new java.awt.BorderLayout());
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setSize(1024, 786);
		this.setMinimumSize(new Dimension(640, 480));
		
		// setup widgets
		levelView   = new LevelView(32);
		infoLayout  = new CardLayout();
		infoPanel   = new JPanel(infoLayout);
		objectPanel = new ObjectPanel(tileset);
		tilePanel   = new TilePanel();
		metaPanel   = new MetaPanel();
		codeEditor  = new CodeEditor();
		editTabs    = new JTabbedPane()
		{
			@Override
			public void addTab(String title, Icon icon, java.awt.Component component)
			{
				this.add(component);
				JLabel lbl = new JLabel(title);
				lbl.setIcon(icon);
				lbl.setIconTextGap(5);
				lbl.setHorizontalTextPosition(JLabel.RIGHT);
				this.setTabComponentAt(this.getTabCount() - 1, lbl);
			}
		};
		kindList    = new KindList(tileset, 24);
		menuBar     = new JMenuBar();
		toolBar     = new ToolBar("Editor Tools");
		
		JSplitPane    editorPane  = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editTabs, infoPanel);
		JSplitPane    kindPane    = new JSplitPane(JSplitPane.VERTICAL_SPLIT,   editorPane, kindList);
		JScrollPane   codeScroll  = new JScrollPane(codeEditor);
		JScrollPane   levelScroll = new JScrollPane(levelView);
		
		// prepare icons
		final Icon icoEasy      = loadIcon(options.enigmaPath.resolve("gfx/completed-easy.png").toString(), 19);
		final Icon icoDifficult = loadIcon(options.enigmaPath.resolve("gfx/completed.png").toString(), 19);
		final Icon icoItems     = kindList.getGroupIcon("grp_items");
		final Icon icoActors    = kindList.getGroupIcon("grp_actors");
		final Icon icoStones    = kindList.getGroupIcon("grp_stones");
		
		
		
		final Icon icoLevelTab  = new ImageIcon(AwtSprite.create((AwtSpriteSet)tileset.getSpriteset(), tileset.getKind("fl_lawn").getIcon(), 16));
		final Icon icoCodeTab   = new ImageIcon(AwtSprite.create((AwtSpriteSet)tileset.getSpriteset(), tileset.getKind("it_document").getIcon(), 16));
		
		// setup actions
		controller = ctrl;
		final Action newAction    = ctrl.newNewLevelAction  (strFileMenu_new, icoFile);
		final Action openAction   = ctrl.newFileOpenAction  (strFileMenu_open, icoDirectory);
		final Action saveAction   = ctrl.newFileSaveAction  (strFileMenu_save, icoFloppy);
		final Action saveAsAction = ctrl.newFileSaveAsAction(strFileMenu_saveAs, null);
		final Action exitAction   = ctrl.newExitAction      (strFileMenu_exit, null);
		final Action metaAction   = ctrl.newShowMetaAction  (strLevelMenu_info, null);
		
		final Action easyAction   = ctrl.newModeAction(Mode.EASY,      strModeMenu_easy,      icoEasy);
		final Action diffAction   = ctrl.newModeAction(Mode.DIFFICULT, strModeMenu_difficult, icoDifficult);
		
		final Action itemsAction  = ctrl.newVisibilityAction(Tileset.Kind.Type.IT, strViewMenu_items,  icoItems);
		final Action actorsAction = ctrl.newVisibilityAction(Tileset.Kind.Type.AC, strViewMenu_actors, icoActors);
		final Action stonesAction = ctrl.newVisibilityAction(Tileset.Kind.Type.ST, strViewMenu_stones, icoStones);
		
		// setup toolbar
		toolBar.addButton(newAction);
		toolBar.addButton(openAction);
		toolBar.addButton(saveAction);
		toolBar.addSeparator();
		
		toolBar.addToggle(itemsAction);
		toolBar.addToggle(actorsAction);
		toolBar.addToggle(stonesAction);
		toolBar.addSeparator();
		
		ButtonGroup group = new ButtonGroup();
		group.add(toolBar.addToggle(easyAction));
		group.add(toolBar.addToggle(diffAction));
		this.add(toolBar, java.awt.BorderLayout.NORTH);
		
		// setup code editor
		codeEditor.setup();
		
		// setup tabs
		editTabs.addTab(strLevelTab, icoLevelTab, levelScroll);
		editTabs.addTab(strCodeTab,  icoCodeTab,  codeScroll);
		editTabs.setTabPlacement(JTabbedPane.TOP);
		
		// setup info panel
		infoPanel.add(objectPanel, OBJECTPANEL);
		infoPanel.add(tilePanel,   TILEPANEL);
		infoPanel.add(metaPanel,   METAPANEL);
		
		// setup main pane
		this.add(kindPane, java.awt.BorderLayout.CENTER);
		
		// setup menu
		MainMenu menu;
		menu = new MainMenu(strFileMenu);
		menu.addItem(newAction)   .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		menu.addItem(openAction)  .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		menu.addItem(saveAction)  .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		menu.addItem(saveAsAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
		menu.addSeparator();
		menu.addItem(exitAction);
		menuBar.add(menu);
		menu = new MainMenu(strLevelMenu);
		menu.addItem(metaAction)  .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
		menuBar.add(menu);
		menu = new MainMenu(strViewMenu);
		menu.addCheckBox(itemsAction) .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK));
		menu.addCheckBox(actorsAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK));
		menu.addCheckBox(stonesAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK));
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
		
		// show window
		kindPane.setResizeWeight(1.0);
		kindPane.setDividerLocation(400);
		kindPane.setDividerSize(4);
		editorPane.setResizeWeight(1.0);
		editorPane.setDividerLocation(800);
		editorPane.setOneTouchExpandable(true);
		editorPane.setDividerSize(10);
		this.pack();
		infoLayout.show(infoPanel, METAPANEL);
		this.setVisible(true);
	}
	
	public void setWorld(Level level, int worldIndex)
	{
		this.setTitle(strTitleLong + " \u2012 " + level.info.identity.title);
		levelView.load(level.worlds.get(worldIndex));
		codeEditor.setText(level.luamain);
		metaPanel.fromLevelInfo(level.info);
		if (editTabs.getSelectedIndex() != LEVELTAB)
			editTabs.setSelectedIndex(LEVELTAB);
		else
			controller.scheduleUpdate(levelView);
	}
	
	public void setCode(Level level, int worldIndex)
	{
		levelView.load(level.worlds.get(worldIndex));
		codeEditor.setText(level.luamain);
		metaPanel.fromLevelInfo(level.info);
		editTabs.setSelectedIndex(CODETAB);
		codeEditor.requestFocus();
	}
	
	public void setVisibility(Tileset.Kind.Type type, boolean visible)
	{
		switch (type)
		{
		case FL: levelView.setFloorVisibility(visible); break;
		case IT: levelView.setItemVisibility(visible);  break;
		case AC: levelView.setActorVisibility(visible); break;
		case ST: levelView.setStoneVisibility(visible); break;
		}
		controller.scheduleUpdate(levelView);
	}
	
	public void setMode(Mode mode)
	{
		levelView.setMode(mode);
		controller.scheduleUpdate(levelView);
	}
	
	public void redrawWorld()
	{
		levelView.revalidate();
		levelView.repaint();
	}
	
	public void moveCursorToSnippet(CodeSnippet code)
	{
		codeEditor.setCaretPosition(code.getBeginPos());
	}
	
	public void showKindInfo(Tileset.Kind kind)
	{
		objectPanel.show(kind);
		infoLayout.show(infoPanel, OBJECTPANEL);
		controller.scheduleUpdate(objectPanel);
	}
	
	public void showMetaInfo()
	{
		infoLayout.show(infoPanel, METAPANEL);
	}
	
	public void addCodeChangeListener(CodeChangeListener l)
	{
		editTabs.addChangeListener(new ChangeListener() {
			@Override public void stateChanged(ChangeEvent e) {
				if (((JTabbedPane)e.getSource()).getSelectedIndex() == LEVELTAB)
					l.codeChanged(codeEditor.getText());
			}
		});
	}
	
	public void addLevelClickListener(LevelClickListener l)
	{
		levelView.addLevelClickListener(l);
	}
	
	public void addKindSelectionListener(KindSelectionListener l)
	{
		kindList.addKindSelectionListener(l);
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
}

