package com.github.euwoyne.enigma_edit.control;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.xml.parsers.ParserConfigurationException;

import org.luaj.vm2.parser.ParseException;
import org.xml.sax.SAXException;

import com.github.euwoyne.enigma_edit.Options;
import com.github.euwoyne.enigma_edit.error.LevelLuaException;
import com.github.euwoyne.enigma_edit.error.LevelXMLException;
import com.github.euwoyne.enigma_edit.error.MissingAttributeException;
import com.github.euwoyne.enigma_edit.error.MissingImageException;
import com.github.euwoyne.enigma_edit.error.WrongSpriteDirException;
import com.github.euwoyne.enigma_edit.lua.data.Mode;
import com.github.euwoyne.enigma_edit.lua.data.Tile;
import com.github.euwoyne.enigma_edit.model.Level;
import com.github.euwoyne.enigma_edit.model.LevelReader;
import com.github.euwoyne.enigma_edit.model.SpriteFolder;
import com.github.euwoyne.enigma_edit.model.Tileset;
import com.github.euwoyne.enigma_edit.model.TilesetReader;
import com.github.euwoyne.enigma_edit.view.swing.AwtSpriteSet;
import com.github.euwoyne.enigma_edit.view.swing.FileOpenDialog;
import com.github.euwoyne.enigma_edit.view.swing.MainWnd;

import jsyntaxpane.syntaxkits.EnigmaSyntaxKit;

public class Controller implements CodeChangeListener, LevelClickListener, KindSelectionListener
{
	private Options options;
	private Tileset tileset;
	private Level   level;
	private MainWnd mainWnd;
	
	private UpdateThread updater;
	
	public Controller(Options options)
	{
			this.options = options;
			loadTileset();
			loadDefaultLevel();
			setupLookAndFeel();
			startUpdater();
			setupUI();
	}
	
	private void loadTileset()
	{
		AwtSpriteSet spriteset = null;
		
		// setup sprites
		try
		{
			System.out.print("Setup sprites...");
			spriteset = new AwtSpriteSet(new SpriteFolder(options.enigmaPath),
			                             new Font("normal", Font.PLAIN, 10));
			System.out.println("DONE");
		}
		catch (WrongSpriteDirException e)
		{
			System.out.println();
			System.err.println("FATAL ERROR: " + e.getMessage());
			System.exit(1);
		}
		
		// setup tileset
		try {
			System.out.print("Setup tileset...");
			TilesetReader reader;
			reader = new TilesetReader();
			tileset = reader.parse("data/tileset.xml");
			reader.addI18n("data/tileset_de.xml", tileset);
			tileset.loadSprites(spriteset);
			System.out.println("DONE");
		}
		catch (ParserConfigurationException e)
		{
			System.out.println("FAIL");
			System.err.println("FATAL ERROR: " + e.getMessage());
			System.exit(1);
		}
		catch (SAXException e)
		{
			System.out.println("FAIL");
			System.err.println("FATAL ERROR: " + e.getMessage());
			System.exit(1);
		}
		catch (IOException e)
		{
			System.out.println("FAIL");
			System.err.println("FATAL ERROR: " + e.getMessage());
			System.exit(1);
		}
	}
	
	private void loadDefaultLevel()
	{
		try
		{
			System.out.println("Load default level...");
			level = Level.getEmpty(System.getProperty("user.name"));
			level.analyse(tileset);
			System.out.println("Load default level...DONE");
		}
		catch (ParseException | LevelLuaException e)
		{
			// this should never occur
			System.err.println("FATAL ERROR: caught error in default level code");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void setupLookAndFeel()
	{
		UIManager.LookAndFeelInfo lafNimbus = 
			Arrays.stream(UIManager.getInstalledLookAndFeels())
			      .filter(nimbus -> nimbus.getName().equals("Nimbus"))
			      .findAny()
			      .orElse(null);
		
		if (lafNimbus != null) try
		{
			UIManager.setLookAndFeel(lafNimbus.getClassName());
		}
		catch (Exception e1) {lafNimbus = null;}
		
		if (lafNimbus == null) try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e2) {try
		{
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch (Exception e3) {}}
	}
	
	private void startUpdater()
	{
		System.out.print("Start UI Updater...");
		updater = new UpdateThread();
		updater.start();
		System.out.println("DONE");
	}
	
	private void setupUI()
	{
		try
		{
			System.out.print("Setup UI...");
			EnigmaSyntaxKit.initKit();
			mainWnd = new MainWnd(this, tileset, options);
			mainWnd.setWorld(level, 0);
			mainWnd.addCodeChangeListener(this);
			mainWnd.addLevelClickListener(this);
			mainWnd.addKindSelectionListener(this);
			System.out.println("DONE");
		}
		catch (MissingAttributeException e)
		{
			System.out.println("FAIL");
			System.err.println("FATAL ERROR: " + e.getMessage());
			System.exit(1);
		}
		catch (MissingImageException e)
		{
			System.out.println("FAIL");
			System.err.println("FATAL ERROR: " + e.getMessage());
			System.exit(1);
		}
	}
	
	public void scheduleUpdate(Updateable u)
	{
		updater.scheduleUpdate(u);
	}
	
	private void analyseLevel()
	{
		try
		{
			level.analyse(tileset);
		}
		catch (ParseException e)
		{
			JOptionPane.showMessageDialog(
					mainWnd,
					e.currentToken.beginLine + ":" + e.currentToken.beginColumn + ": ERROR: " + e.getLocalizedMessage(),
					"Level XML Error",
					JOptionPane.ERROR_MESSAGE);
			mainWnd.setCode(level, 0);
		}
		catch (LevelLuaException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					mainWnd,
					(e.code.isNone() ? "" : e.code.startString() + ": ")+ "ERROR: " + e.getLocalizedMessage(),
					"Level Lua Error",
					JOptionPane.ERROR_MESSAGE);
			mainWnd.setCode(level, 0);
			System.err.println(e.code.startString() + ": ERROR:" + e.getLocalizedMessage());
			if (!e.code.isNone())
			{
				System.err.println("    " + e.code.getLine(level.luamain));
				for (int i = 0; i < 3 + e.code.getBeginColumn(); ++i)
					System.err.print(' ');
				System.err.println('^');
				mainWnd.moveCursorToSnippet(e.code);
			}
		}
	}
	
	private void onLoadLevel(File file)
	{
		try
		{
			System.out.println("Loading level '" + file.getName() + "'...");
			LevelReader reader = new LevelReader();
			level = new Level();
			reader.setTarget(level);
			reader.parse(file.getCanonicalPath());
			analyseLevel();
			mainWnd.setWorld(level, 0);
			System.out.println("Loading level '" + file.getName() + "'... SUCCESS!");
		}
		catch (LevelXMLException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					mainWnd,
					e.getLineNumber() + ":" + e.getColumnNumber() + ": ERROR: " + e.getLocalizedMessage(),
					"Level XML Error",
					JOptionPane.ERROR_MESSAGE);
		}
		catch (ParserConfigurationException | SAXException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					mainWnd,
					file.getName() + ": ERROR: " + e.getLocalizedMessage(),
					"Parser Configuration Error",
					JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					mainWnd,
					file.getName() + ": ERROR: " + e.getLocalizedMessage(),
					"I/O Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	@Override
	public void codeChanged(String luacode)
	{
		new Thread(new Runnable()
		{
			@Override public void run()
			{
				// TODO: waiting animation in levelview
				level.luamain = luacode;
				analyseLevel();
			}
		}).start();
	}
	
	@Override
	public void levelClicked(int x, int y, Tile tile)
	{
		// TODO: handle click on level view
	}
	
	@Override
	public void kindSelected(Tileset.Kind kind)
	{
		mainWnd.showKindInfo(kind);
	}
	
	public Action newFileOpenAction(String name, Icon icon)
	{
		return new Action(0, name, icon)
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				new Thread(new Runnable()
				{
					final FileOpenDialog dialog = new FileOpenDialog(options.enigmaPath, options.userPath);
					
					@Override public void run()
					{
						if (dialog.showOpenDialog(mainWnd) == JFileChooser.APPROVE_OPTION)
						{
							// TODO: waiting animation in levelview
							onLoadLevel(dialog.getSelectedFile());
						}
					}
				}).start();
			}
		};
	}
	
	public Action newNewLevelAction(String name, Icon icon)
	{
		// TODO: real new level routine
		return new NullAction(name, icon);
	}
	
	public Action newFileSaveAction(String name, Icon icon)
	{
		// TODO: real level save routine
		return new NullAction(name, icon);
	}
	
	public Action newFileSaveAsAction(String name, Icon icon)
	{
		// TODO: real level save routine
		return new NullAction(name, icon);
	}
	
	public Action newExitAction(String name, Icon icon)
	{
		return new Action(0, name, icon)
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mainWnd.dispose();
			}
		};
	}
	
	public Action newShowMetaAction(String name, Icon icon)
	{
		return new Action(0, name,  icon)
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mainWnd.showMetaInfo();
			}
		};
	}
	
	public Action newVisibilityAction(Tileset.Kind.Type type, String name, Icon icon)
	{
		return new ToggleAction(type.ordinal(), name, icon, true)
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent event)
			{
				super.actionPerformed(event);
				mainWnd.setVisibility(Tileset.Kind.Type.values()[this.getId()], isSelected());
			}
		};
	}
	
	public Action newModeAction(Mode mode, String name, Icon icon)
	{
		return new ToggleAction(mode.ordinal(), name, icon, true)
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent event)
			{
				super.actionPerformed(event);
				if (isSelected())
					mainWnd.setMode(Mode.values()[this.getId()]);
			}
		};
	}
}
