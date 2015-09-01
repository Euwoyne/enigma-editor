
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

package enigma_edit.view.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.luaj.vm2.parser.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import enigma_edit.Options;
import enigma_edit.Resources;
import enigma_edit.control.Action;
import enigma_edit.control.NullAction;
import enigma_edit.control.ToggleAction;
import enigma_edit.error.LevelLuaException;
import enigma_edit.error.LevelXMLException;
import enigma_edit.error.MissingAttributeException;
import enigma_edit.lua.data.Mode;
import enigma_edit.model.Level;
import enigma_edit.model.LevelReader;
import enigma_edit.model.Tileset;
import enigma_edit.view.CodeEditor;

public class MainWnd extends JFrame implements WindowListener
{
	private static final long serialVersionUID = 1L;
	private static final int  LEVELTAB         = 0;
	private static final int  CODETAB          = 1;
	
	private Tileset     tileset;
	private Level       level;
	private LevelView   levelView;
	private InfoPanel   infoPanel;
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
	
	public MainWnd(Tileset tileset, Options options) throws MissingAttributeException, IOException
	{
		// setup window
		super(Resources.uiText.getString("MainWnd.title.long"));
		this.getContentPane().setLayout(new java.awt.BorderLayout());
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setSize(1024, 786);
		this.setMinimumSize(new Dimension(640, 480));
		
		levelView   = new LevelView(tileset, 32);
		infoPanel   = new InfoPanel();
		codeEditor  = new CodeEditor();
		editTabs    = new JTabbedPane();
		kindList    = new KindList(tileset, 40);
		menuBar     = new JMenuBar();
		toolBar     = new ToolBar("Editor Tools");
		
		JSplitPane    editorPane  = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editTabs, infoPanel);
		JSplitPane    kindPane    = new JSplitPane(JSplitPane.VERTICAL_SPLIT,   editorPane, kindList);
		JScrollPane   codeScroll  = new JScrollPane(codeEditor);
		JScrollPane   levelScroll = new JScrollPane(levelView);
		
		// setup actions
		final Action newAction    = new NullAction(Resources.uiText.getString("MainWnd.fileMenu.new"),  UIManager.getIcon("FileView.fileIcon"));
		final Action openAction   = new FileOpenAction(Resources.uiText.getString("MainWnd.fileMenu.open"), UIManager.getIcon("FileView.directoryIcon"), options.enigmaPath, options.userPath);
		final Action saveAction   = new NullAction(Resources.uiText.getString("MainWnd.fileMenu.save"), UIManager.getIcon("FileView.floppyDriveIcon"));
		final Action saveAsAction = new NullAction(Resources.uiText.getString("MainWnd.fileMenu.saveAs"));
		@SuppressWarnings("serial")
		final Action exitAction   = new Action(0, Resources.uiText.getString("MainWnd.fileMenu.exit")) {
			public void actionPerformed(ActionEvent e) {MainWnd.this.dispose();}};
		final Action easyAction   = new ModeAction(ModeAction.EASY,      loadIcon(options.enigmaPath.resolve("gfx/completed-easy.png").toString(), 19));
		final Action diffAction   = new ModeAction(ModeAction.DIFFICULT, loadIcon(options.enigmaPath.resolve("gfx/completed.png").toString(), 19));
		final Action itemsAction  = new VisibilityAction(VisibilityAction.ITEMS,  kindList.getIconAt(kindList.getIndexByLabel("grp_items")));
		final Action actorsAction = new VisibilityAction(VisibilityAction.ACTORS, kindList.getIconAt(kindList.getIndexByLabel("grp_actors")));
		final Action stonesAction = new VisibilityAction(VisibilityAction.STONES, kindList.getIconAt(kindList.getIndexByLabel("grp_stones")));
		
		// setup default level
		try
		{
			this.tileset = tileset;
			level = Level.getEmpty(System.getProperty("user.name"));
			level.analyse(tileset);
		}
		catch (ParseException | LevelLuaException e)
		{
			// this should never occur
			System.err.println("FATAL ERROR: caught error in default level code");
			e.printStackTrace();
		}
		
		
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
		this.add(toolBar, java.awt.BorderLayout.PAGE_START);
		
		// setup code editor
		codeEditor.setup();
		codeEditor.setText(level.luamain);
		
		// setup level editor
		if (!level.worlds.isEmpty())
			levelView.load(level.worlds.getFirst());
		
		// setup tabs
		editTabs.addTab(Resources.uiText.getString("MainWnd.levelTab"),
		                new ImageIcon(((Sprite)tileset.getKind("fl_lawn").getIcon().getSprite()).getImage(16)),
		                levelScroll);
		editTabs.addTab(Resources.uiText.getString("MainWnd.codeTab"),
		                new ImageIcon(((Sprite)tileset.getKind("it_document").getIcon().getSprite()).getImage(32).getScaledInstance(16, 16, Image.SCALE_SMOOTH)),
		                codeScroll);
		editTabs.setTabPlacement(JTabbedPane.TOP);
		editTabs.addChangeListener(new TabChangeAction());

		// setup main pane
		this.add(kindPane, java.awt.BorderLayout.CENTER);
		
		// setup menu
		MainMenu menu;
		menu = new MainMenu(Resources.uiText.getString("MainWnd.fileMenu"));
		menu.addItem(newAction)   .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		menu.addItem(openAction)  .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		menu.addItem(saveAction)  .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		menu.addItem(saveAsAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
		menu.addSeparator();
		menu.addItem(exitAction);
		menuBar.add(menu);
		menu = new MainMenu(Resources.uiText.getString("MainWnd.viewMenu"));
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
		this.setVisible(true);
	}
	
	private boolean analyse()
	{
		try
		{
			level.analyse(tileset);
			if (level.worlds.isEmpty()) return false;
			levelView.load(level.worlds.getFirst());
			return true;
		}
		catch (ParseException e)
		{
			editTabs.setSelectedIndex(CODETAB);
			JOptionPane.showMessageDialog(
					MainWnd.this,
					e.currentToken.beginLine + ":" + e.currentToken.beginColumn + ": ERROR: " + e.getLocalizedMessage(),
					"Level XML Error",
					JOptionPane.ERROR_MESSAGE);
			codeEditor.requestFocus();
		}
		catch (LevelLuaException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					MainWnd.this,
					(e.code.isNone() ? "" : e.code.startString() + ": ")+ "ERROR: " + e.getLocalizedMessage(),
					"Level Lua Error",
					JOptionPane.ERROR_MESSAGE);
			editTabs.setSelectedIndex(CODETAB);
			System.err.println(e.code.startString() + ": ERROR:" + e.getLocalizedMessage());
			if (!e.code.isNone())
			{
				System.err.println("    " + e.code.getLine(level.luamain));
				for (int i = 0; i < 3 + e.code.getBeginColumn(); ++i)
					System.err.print(' ');
				System.err.println('^');
				codeEditor.setCaretPosition(e.code.getBeginPos());
			}
			codeEditor.requestFocus();
		}
		return false;
	}
	
	class TabChangeAction implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent event)
		{
			if (((JTabbedPane)event.getSource()).getSelectedIndex() == LEVELTAB)
			{
				level.luamain = codeEditor.getText();
				analyse();
				//levelEditor.view.repaint();
			}
		}
	}
	
	private static final String[] visibilityUiText = {"MainWnd.viewMenu.floors", "MainWnd.viewMenu.items", "MainWnd.viewMenu.actors", "MainWnd.viewMenu.stones"};
	
	private class VisibilityAction extends ToggleAction
	{
		private static final long serialVersionUID = 1L;
		public  static final int  FLOORS = 0;
		public  static final int  ITEMS  = 1;
		public  static final int  ACTORS = 2;
		public  static final int  STONES = 3;
		
		public VisibilityAction(int id, Icon icon)
		{
			super(id, Resources.uiText.getString(visibilityUiText[id]), icon);
			putValue(Action.SELECTED_KEY, true);
		}
		
		@Override
		public void actionPerformed(ActionEvent event)
		{
			super.actionPerformed(event);
			switch (this.getId())
			{
			case FLOORS: MainWnd.this.levelView.setFloorVisibility((boolean)this.getValue(Action.SELECTED_KEY)); break;
			case ITEMS:  MainWnd.this.levelView.setItemVisibility ((boolean)this.getValue(Action.SELECTED_KEY)); break;
			case ACTORS: MainWnd.this.levelView.setActorVisibility((boolean)this.getValue(Action.SELECTED_KEY)); break;
			case STONES: MainWnd.this.levelView.setStoneVisibility((boolean)this.getValue(Action.SELECTED_KEY)); break;
			}
		}
	}
	
	private static final String[] modeUiText = {"MainWnd.modeMenu.easy", "MainWnd.modeMenu.difficult"};
	
	private class ModeAction extends ToggleAction
	{
		private static final long serialVersionUID = 1L;
		public  static final int  EASY = 0;
		public  static final int  DIFFICULT  = 1;
		
		public ModeAction(int id, Icon icon)
		{
			super(id, Resources.uiText.getString(modeUiText[id]), icon);
			putValue(Action.SELECTED_KEY, false);
		}
		
		@Override
		public void actionPerformed(ActionEvent event)
		{
			super.actionPerformed(event);
			switch (this.getId())
			{
			case EASY:      MainWnd.this.levelView.setMode(Mode.EASY); break;
			case DIFFICULT: MainWnd.this.levelView.setMode(Mode.DIFFICULT); break;
			}
		}
	}
	
	class FileOpenAction extends Action
	{
		private static final long serialVersionUID = 1L;
		
		private final FileOpenDialog dialog;
		
		public FileOpenAction(String text, Icon icon, Path enigmaPath, Path userPath)
		{
			super(0, text, icon);
			dialog = new FileOpenDialog(enigmaPath, userPath);
		}
		
		@Override
		public void actionPerformed(ActionEvent event)
		{
			if (dialog.showOpenDialog(MainWnd.this) == JFileChooser.APPROVE_OPTION)
			{
				Level target = new Level();
				try
				{
					System.out.println("Loading level '" + dialog.getSelectedFile().getName() + "'...");
					LevelReader reader = new LevelReader();
					reader.setTarget(target);
					reader.parse(dialog.getSelectedFile().getCanonicalPath());
					MainWnd.this.level = target;
					MainWnd.this.setTitle(Resources.uiText.getString("MainWnd.title.long") + " \u2012 " + level.info.identity.title);
					codeEditor.setText(level.luamain);
					infoPanel.fromLevelInfo(level.info);
					MainWnd.this.analyse();
					System.out.println("Loading level '" + dialog.getSelectedFile().getName() + "'... SUCCESS!");
				}
				catch (LevelXMLException e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							MainWnd.this,
							e.getLineNumber() + ":" + e.getColumnNumber() + ": ERROR: " + e.getLocalizedMessage(),
							"Level XML Error",
							JOptionPane.ERROR_MESSAGE);
				}
				catch (ParserConfigurationException | SAXException e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							MainWnd.this,
							dialog.getSelectedFile().getName() + ": ERROR: " + e.getLocalizedMessage(),
							"Parser Configuration Error",
							JOptionPane.ERROR_MESSAGE);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							MainWnd.this,
							dialog.getSelectedFile().getName() + ": ERROR: " + e.getLocalizedMessage(),
							"I/O Error",
							JOptionPane.ERROR_MESSAGE);
				}
			};
		}		
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

