
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.euwoyne.enigma_edit.Resources;

public class FileOpenDialog extends JFileChooser
{
	private static final long serialVersionUID = 1L;
	
	private class Thumbnail extends JComponent implements PropertyChangeListener
	{
		private static final long serialVersionUID = 1L;
		
		private Image border;
		private Image error;
		private Image thumbnail;
		
		public Thumbnail(Path enigmaPath)
		{
			Path gfxPath = enigmaPath.resolve("gfx");
			File img;
			
			// load border image
			img = gfxPath.resolve(String.format("thumbborder-%dx%d.png", thumbsWidth, thumbsHeight)).toFile();
			if (img.isFile())
				try {border = ImageIO.read(img);} catch (IOException e) {border = null;}
			
			// load error image
			img = gfxPath.resolve(String.format("error-%dx%d.png", thumbsWidth, thumbsHeight)).toFile();
			if (img.isFile())
				try {error = ImageIO.read(img);} catch (IOException e) {error = null;}
			
			// set size (+4px border on each side)
			this.setPreferredSize(new Dimension(thumbsWidth + 8, thumbsHeight + 8));
		}
		
		@Override
		protected void paintComponent(Graphics g)
		{
			if (thumbnail != null)
			{
				Graphics2D g2d = (Graphics2D)g;
				if (border != null)
					g2d.drawImage(border, 0, 0, null);
				g2d.drawImage(thumbnail, 4, 4, null);
			}
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent e)
		{
			thumbnail = null;
			if (e.getNewValue() != null && e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
			{
				try
				{
					File file = (File)e.getNewValue();
					Path path = file.toPath().normalize().toAbsolutePath();
					
					if (!file.isFile()) return;
					
					final String filename = 
							(path.getFileName().toString().endsWith(".xml")) ?
									path.getFileName().toString().substring(0, path.getFileName().toString().length() - 4) :
									path.getFileName().toString();
					
					path = path.getParent();
					
					if (enigmaLevelPath != null && path.startsWith(enigmaLevelPath))
						path = thumbsPath.resolve(enigmaLevelPath.relativize(path));
					else if (userLevelPath != null && path.startsWith(userLevelPath))
						path = thumbsPath.resolve(userLevelPath.relativize(path));
					else throw new IOException();
					
					file = path.toFile();
					if (!file.isDirectory()) throw new IOException();
					final File[] files = file.listFiles(new java.io.FilenameFilter() {public boolean accept(File dir, String name) {return name.startsWith(filename);}});
					if (files.length == 0 || !files[0].isFile()) throw new IOException();
					file = files[0];
					
					thumbnail = ImageIO.read(file);
				}
				catch (IOException err)
				{
					thumbnail = error;
				}
			}
			this.repaint();
		}
	}
	
	private class LevelInfo extends JPanel implements PropertyChangeListener
	{
		private static final long serialVersionUID = 1L;
		
		private class StopParsing extends SAXException {private static final long serialVersionUID = 1L;};
		
		private Thumbnail thumbnail;
		private JLabel    titleLabel,    titleValue;
		private JLabel    subtitleLabel, subtitleValue;
		private JLabel    authorLabel,   authorValue;
		
		SAXParser parser;
		
		public LevelInfo(Path enigmaPath)
		{
			thumbnail     = new Thumbnail(enigmaPath);
			titleLabel    = new JLabel(STR_TITLE + ": ");
			subtitleLabel = new JLabel(STR_SUBTITLE + ": ");
			authorLabel   = new JLabel(STR_AUTHOR + ": ");
			titleValue    = new JLabel(STR_NONE);
			subtitleValue = new JLabel(STR_NONE);
			authorValue   = new JLabel(STR_UNKNOWN);
			
			titleLabel.setHorizontalAlignment(JLabel.RIGHT);
			titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
			subtitleLabel.setHorizontalAlignment(JLabel.RIGHT);
			subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.BOLD));
			authorLabel.setHorizontalAlignment(JLabel.RIGHT);
			authorLabel.setFont(authorLabel.getFont().deriveFont(Font.BOLD));
			titleValue.setHorizontalAlignment(JLabel.LEFT);
			titleValue.setFont(titleValue.getFont().deriveFont(Font.PLAIN));
			subtitleValue.setHorizontalAlignment(JLabel.LEFT);
			subtitleValue.setFont(subtitleValue.getFont().deriveFont(Font.PLAIN));
			authorValue.setHorizontalAlignment(JLabel.LEFT);
			authorValue.setFont(authorValue.getFont().deriveFont(Font.PLAIN));
			
			this.setBorder(
					BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(STR_PREVIEW),
                        BorderFactory.createEmptyBorder(5,5,5,5)));
			
			this.setLayout(new GridBagLayout());
			
			GridBagConstraints c = new GridBagConstraints();
			c.gridwidth = 2;
			c.gridx = 0; c.gridy = 0; this.add(thumbnail, c);
			
			c.gridwidth = 1;
			c.insets = new Insets(20,0,0,0);
			c.gridx = 0; c.gridy = 1; c.anchor = GridBagConstraints.EAST; this.add(titleLabel, c);
			c.gridx = 1; c.gridy = 1; c.anchor = GridBagConstraints.WEST; this.add(titleValue, c);
			c.insets = new Insets(2,0,0,0);
			c.gridx = 0; c.gridy = 2; c.anchor = GridBagConstraints.EAST; this.add(subtitleLabel, c);
			c.gridx = 1; c.gridy = 2; c.anchor = GridBagConstraints.WEST; this.add(subtitleValue, c);
			c.gridx = 0; c.gridy = 3; c.anchor = GridBagConstraints.EAST; this.add(authorLabel, c);
			c.gridx = 1; c.gridy = 3; c.anchor = GridBagConstraints.WEST; this.add(authorValue, c);
			
			try
			{
				parser = SAXParserFactory.newInstance().newSAXParser();
				parser.getXMLReader().setContentHandler(
					new DefaultHandler()
					{
						private boolean gotId, gotAuthor;
						
						@Override public void startDocument() {gotId = gotAuthor = false;}
						
						@Override
						public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException
						{
							String attr;
							if (!gotId && qName.equals("el:identity"))
							{
								if ((attr = attrs.getValue("el:title")) != null && !attr.isEmpty())
									titleValue.setText(attr);
								if ((attr = attrs.getValue("el:subtitle")) != null && !attr.isEmpty())
									subtitleValue.setText(attr);
								gotId = true;
							}
							else if (!gotAuthor && qName.equals("el:author"))
							{
								if ((attr = attrs.getValue("el:name")) != null && !attr.isEmpty())
									authorValue.setText(attr);
								else
									authorValue.setText(STR_UNKNOWN);
								
								if ((attr = attrs.getValue("el:email")) != null && !attr.isEmpty())
									authorValue.setText(authorValue.getText() + " <" + attr + ">");
								
								gotAuthor = true;
							}
							
							if (gotId && gotAuthor) throw new StopParsing();
						}
					});
			}
			catch (SAXException | ParserConfigurationException e) {}
		}
		
		void resetValues()
		{
			titleValue.setText(STR_NONE);
			subtitleValue.setText(STR_NONE);
			authorValue.setText(STR_UNKNOWN);
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent e)
		{
			resetValues();
			if (parser != null && e.getNewValue() != null && e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
			{
				File file = (File)e.getNewValue();
				if (!file.isFile()) return;
				this.setVisible(true);
				try
				{
					parser.getXMLReader().parse("file://" + file);
				}
				catch (IOException | SAXException ex) {}
			}
			else this.setVisible(false);
			thumbnail.propertyChange(e);
		}
	}
	
	private static final String  STR_PREVIEW   = Resources.uiText.getString("FileDialog.Preview");
	private static final String  STR_TITLE     = Resources.uiText.getString("FileDialog.Title");
	private static final String  STR_SUBTITLE  = Resources.uiText.getString("FileDialog.Subtitle");
	private static final String  STR_AUTHOR    = Resources.uiText.getString("FileDialog.Author");
	private static final String  STR_NONE      = Resources.uiText.getString("FileDialog.none");
	private static final String  STR_UNKNOWN   = Resources.uiText.getString("FileDialog.unknown");

	private static final String  STR_XMLFILTER = Resources.uiText.getString("FileDialog.XMLFilter");
	private static final String  STR_LUAFILTER = Resources.uiText.getString("FileDialog.LUAFilter");
	private static final String  STR_ALLFILTER = Resources.uiText.getString("FileDialog.AllFilter");

	private static final Pattern folderPattern = Pattern.compile("thumbs-(\\d+)x(\\d+)");
	
	Path      enigmaLevelPath;
	Path      userLevelPath;
	Path      thumbsPath;
	int       thumbsWidth;
	int       thumbsHeight;
	LevelInfo info;
	
	public FileOpenDialog(Path enigmaPath, Path userPath)
	{
		// check enigma level path
		enigmaLevelPath = enigmaPath.resolve("levels").toAbsolutePath();
		if (!enigmaLevelPath.toFile().isDirectory())
			enigmaLevelPath = null;
		
		// check user level path
		userLevelPath = userPath.resolve("levels").toAbsolutePath();
		if (!userLevelPath.toFile().isDirectory())
			userLevelPath = null;
		
		// collect possible thumbnail directories
		final File userDir = userPath.toFile();
		final File[] dirs = userDir.listFiles(new java.io.FileFilter() {public boolean accept(File file) {return file.isDirectory() && file.getName().startsWith("thumbs-");}});
		
		// choose correct thumbnail format (if available)
		if (dirs.length > 0)
		{
			Matcher m;
			thumbsWidth  = -1;
			thumbsHeight = -1;
			
			int w, h;
			for (File f : dirs)
			{
				m = folderPattern.matcher(f.getName());
				if (m.matches())
				{
					w = Integer.parseInt(m.group(1));
					h = Integer.parseInt(m.group(2));
					if (w > thumbsWidth && h > thumbsHeight)
					{
						thumbsWidth  = w;
						thumbsHeight = h;
						thumbsPath   = f.toPath();
					}
				}
			}
		}
		
		// setup preview
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				info = new LevelInfo(enigmaPath);
				
				removeChoosableFileFilter(getFileFilter());
				
				setFileFilter(new javax.swing.filechooser.FileFilter() {
					@Override public boolean accept(File pathname) {return pathname.isDirectory() || pathname.getName().endsWith(".xml");}
					@Override public String  getDescription()      {return STR_XMLFILTER;}
				});
				
				addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
					@Override public boolean accept(File pathname) {return pathname.isDirectory() || pathname.getName().endsWith(".lua");}
					@Override public String  getDescription()      {return STR_LUAFILTER;}
				});
				
				addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
					@Override public boolean accept(File pathname) {return true;}
					@Override public String  getDescription()      {return STR_ALLFILTER;}
				});
				
				setCurrentDirectory(enigmaLevelPath.toFile());
				
				setFileHidingEnabled(false);
				setPreferredSize(new Dimension(640, 480));
				setAccessory(info);
				addPropertyChangeListener(info);
			}
		});
	}
}

