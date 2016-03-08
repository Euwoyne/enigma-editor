
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

package com.github.euwoyne.enigma_edit.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.euwoyne.enigma_edit.error.LevelXMLException;

public class LevelReader
{
	private SAXParser        parser;
	private Level            target;
	
    private static String convertToFileURL(String filename)
    {
        String path = new File(filename).getAbsolutePath();
        
        if (File.separatorChar != '/')
            path = path.replace(File.separatorChar, '/');
        
        if (!path.startsWith("/"))
            path = "/" + path;
        
        return "file:" + path;
    }
    
	public LevelReader() throws ParserConfigurationException, SAXException
	{
		parser = SAXParserFactory.newInstance().newSAXParser();
	}
	
	public void setTarget(Level target) throws SAXException
	{
		parser.getXMLReader().setContentHandler(new LevelParser(target));
		this.target = target;
	}
	
	public void parse(String filename) throws IOException, SAXException
	{
		target.reset();
		target.path = Paths.get(filename);
		parser.getXMLReader().parse(convertToFileURL(filename));
	}
	
	private static class LevelParser extends DefaultHandler
	{
		private enum State {ROOT, LEVEL, PROTECTED, INFO, IDENTITY,
			                                              VERSION,
			                                              AUTHOR,
			                                              COPYRIGHT,
			                                              LICENSE,
			                                              COMPATIBILITY, DEPENDENCY, EXTERNALDATA, EDITORCOMPAT,
			                                              MODES,
			                                              COMMENTS, CREDITS, DEDICATION, CODECOMMENT,
			                                              SCORE,
			                                              UPDATE,
			                                        LUAMAIN,
			                                        ELEMENTS,
			                                        EDITOR,
			                             PUBLIC, UPGRADE,
			                                     I18N, STRING, ENGLISH, TRANSLATION}
		
		private Locator        locator;
		private State          state;
		private Level          target;
		private String         i18nLang;
		private I18N.KeyString i18nString;
		private boolean        i18nProtect;
		
		private StringBuffer   buf;
		
		public LevelParser(Level target)
		{
			this.target = target;
			this.buf    = new StringBuffer();
		}
		
		@Override
		public void warning(SAXParseException e)
		{
			System.err.println(e.getLineNumber() + ":" + e.getColumnNumber() + ": WARNING: " + e.getMessage());
		}
		
		@Override
		public void error(SAXParseException e) throws LevelXMLException
		{
			System.err.println(e.getLineNumber() + ":" + e.getColumnNumber() + ": ERROR: " + e.getMessage());
			throw (LevelXMLException)e;
		}
		
		@Override
		public void setDocumentLocator(Locator locator)
		{
			this.locator = locator;
		}
		
		@Override
		public void startDocument() throws SAXException
		{
			state = State.ROOT;
		}
		
		private int parseVersion(String attrName, String attrVal, int minVal)
		{
			try
			{
				int ver;
				if (attrVal == null)
				{
					this.warning(new SAXParseException("Illegal version number: value of \"" + attrName + "\" is missing.", locator));
					return 1;
				}
				else if ((ver = Integer.parseInt(attrVal)) < minVal)
				{
					this.warning(new SAXParseException("Illegal version number: value of \"" + attrName + "\" is not positive (" + attrName + "=\"" + attrVal + "\").", locator));
					return 1;
				}
				return ver;
			}
			catch (NumberFormatException e)
			{
				this.warning(new SAXParseException("Illegal version number: value of \"" + attrName + "\" is no integer (" + attrName + "=\"" + attrVal + "\").", locator));
				return 1;
			}
		}
		
		@Override
		public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws LevelXMLException
		{
			String temp;
			switch (state)
			{
			case ROOT:
				if (qName.equals("el:level"))
					state = State.LEVEL;
				else this.error(new LevelXMLException("IllegalRoot", qName, locator));
				break;
				
			case LEVEL:
				if (qName.equals("el:protected"))
					state = State.PROTECTED;
				else if (qName.equals("el:public"))
					state = State.PUBLIC;
				else this.error(new LevelXMLException("UnexpectedTagLevel", qName, locator));
				break;
			
			case PROTECTED:
				if (qName.equals("el:info"))
				{
					if ((temp = attrs.getValue("el:type")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:type", qName, locator));
					
					if (temp.equals("level"))
						target.info.type = LevelInfo.Type.LEVEL;
					else if (temp.equals("multilevel"))
						target.info.type = LevelInfo.Type.MULTILEVEL;
					else if (temp.equals("library"))
						target.info.type = LevelInfo.Type.LIBRARY;
					else
						this.error(new LevelXMLException("UnexpectedValueTag", "el:type", qName, locator));
					
					// TODO: attribute "el:quantity" for el:type="multilevel"
					
					state = State.INFO;
				}
				else if (qName.equals("el:luamain"))
				{
					state = State.LUAMAIN;
				}
				else if (qName.equals("el:elements"))
				{
					state = State.ELEMENTS;
				}
				else if (qName.equals("ee:editor"))
				{
					state = State.EDITOR;
				}
				else if (qName.equals("el:i18n"))
				{
					i18nProtect = true;
					state = State.I18N;
				}
				else this.error(new LevelXMLException("UnexpectedTagProtected", qName, locator));
				break;
				
			case INFO:
				if (qName.equals("el:identity"))
				{
					if ((target.info.identity.title = attrs.getValue("el:title")) == null)
						target.info.identity.title = "";
					if ((target.info.identity.subtitle = attrs.getValue("el:subtitle")) == null)
						target.info.identity.subtitle = "";
					if ((target.info.identity.id = attrs.getValue("el:id")) == null)
						target.info.identity.id = "";	// will be corrected by "Level::check"
					state = State.IDENTITY;
				}
				else if (qName.equals("el:version"))
				{
					target.info.version.score    = parseVersion("el:score",    attrs.getValue("el:score"),    1);
					target.info.version.release  = parseVersion("el:release",  attrs.getValue("el:release"),  1);
					target.info.version.revision = parseVersion("el:revision", attrs.getValue("el:revision"), 0);
					
					if ((temp = attrs.getValue("el:status")) == null)
					{
						System.err.println("WANING: Illegal version status: value of \"el:status\" is missing.");
						target.info.version.status = LevelInfo.Status.EXPERIMENTAL;
					}
					else if (temp.equals("released"))
						target.info.version.status = LevelInfo.Status.RELEASED;
					else if (temp.equals("stable"))
						target.info.version.status = LevelInfo.Status.STABLE;
					else if (temp.equals("test"))
						target.info.version.status = LevelInfo.Status.TEST;
					else if (temp.equals("experimental"))
						target.info.version.status = LevelInfo.Status.EXPERIMENTAL;
					else
					{
						System.err.println("WARNING: Illegal version status: el:status=\"" + temp + "\".");
						target.info.version.status = LevelInfo.Status.EXPERIMENTAL;
					}
					
					state = State.VERSION;
				}
				else if (qName.equals("el:author"))
				{
					if ((target.info.author.name = attrs.getValue("el:name")) == null)
						target.info.author.name = "";
					if ((target.info.author.email = attrs.getValue("el:email")) == null)
						target.info.author.email = "";
					if ((target.info.author.homepage = attrs.getValue("el:homepage")) == null)
						target.info.author.homepage = "";
					state = State.AUTHOR;
				}
				else if (qName.equals("el:copyright"))
				{
					state = State.COPYRIGHT;
				}
				else if (qName.equals("el:license"))
				{
					if ((temp = attrs.getValue("el:type")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:type", qName, locator));
					target.info.license.type = temp;
					
					if ((temp = attrs.getValue("el:open")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:open", qName, locator));
					
					if (temp.equalsIgnoreCase("true"))
						target.info.license.open = true;
					else if (temp.equalsIgnoreCase("false"))
						target.info.license.open = false;
					else
						this.error(new LevelXMLException("UnexpectedValueTag", "el:type", qName, locator));
					
					state = State.LICENSE;
				}
				else if (qName.equals("el:compatibility"))
				{
					if ((temp = attrs.getValue("el:enigma")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:enigma", qName, locator));
					target.info.compat.enigma = temp;
					
					if ((temp = attrs.getValue("el:engine")) == null)
						target.info.compat.engine = LevelInfo.Engine.DEFAULT;
					else if (temp.equals("enigma"))
						target.info.compat.engine = LevelInfo.Engine.ENIGMA;
					else if (temp.equals("oxyd1"))
						target.info.compat.engine = LevelInfo.Engine.OXYD1;
					else if (temp.equals("per.oxyd"))
						target.info.compat.engine = LevelInfo.Engine.PEROXYD;
					else if (temp.equals("oxyd.extra"))
						target.info.compat.engine = LevelInfo.Engine.OXYDEXTRA;
					else if (temp.equals("oxyd.magnum"))
						target.info.compat.engine = LevelInfo.Engine.OXYDMAGNUM;
					else
					{
						System.err.println("WARNING: Illegal engine: el:engine=\"" + temp + "\".");
						target.info.compat.engine = LevelInfo.Engine.DEFAULT;
					}
					
					state = State.COMPATIBILITY;
				}
				else if (qName.equals("el:modes"))
				{
					if ((temp = attrs.getValue("el:easy")) == null)
						target.info.modes.easy = false;
					else if (temp.equalsIgnoreCase("true"))
						target.info.modes.easy = true;
					else if (temp.equalsIgnoreCase("false"))
						target.info.modes.easy = false;
					else
						this.error(new LevelXMLException("UnexpectedValueTag", "el:easy", qName, locator));
					
					if ((temp = attrs.getValue("el:single")) == null)
						target.info.modes.single = false;
					else if (temp.equalsIgnoreCase("true"))
						target.info.modes.single = true;
					else if (temp.equalsIgnoreCase("false"))
						target.info.modes.single = false;
					else
						this.error(new LevelXMLException("UnexpectedValueTag", "el:single", qName, locator));
					
					if ((temp = attrs.getValue("el:network")) == null)
						target.info.modes.network = false;
					else if (temp.equalsIgnoreCase("true"))
						target.info.modes.network = true;
					else if (temp.equalsIgnoreCase("false"))
						target.info.modes.network = false;
					else
						this.error(new LevelXMLException("UnexpectedValueTag", "el:network", qName, locator));
					
					if ((temp = attrs.getValue("el:control")) == null)
						target.info.modes.control = LevelInfo.Control.DEFAULT;
					else if (temp.equalsIgnoreCase("force"))
						target.info.modes.control = LevelInfo.Control.FORCE;
					else if (temp.equalsIgnoreCase("balance"))
						target.info.modes.control = LevelInfo.Control.BALANCE;
					else if (temp.equalsIgnoreCase("key"))
						target.info.modes.control = LevelInfo.Control.KEY;
					else if (temp.equalsIgnoreCase("other"))
						target.info.modes.control = LevelInfo.Control.OTHER;
					else
					{
						System.err.println("WARNING: Illegal control type: el:control=\"" + temp + "\".");
						target.info.modes.control = LevelInfo.Control.DEFAULT;
					}
					
					if ((temp = attrs.getValue("el:scoreunit")) == null)
						target.info.modes.scoreunit = LevelInfo.ScoreUnit.DEFAULT;
					else if (temp.equalsIgnoreCase("duration"))
						target.info.modes.scoreunit = LevelInfo.ScoreUnit.DURATION;
					else if (temp.equalsIgnoreCase("number"))
						target.info.modes.scoreunit = LevelInfo.ScoreUnit.NUMBER;
					else
					{
						System.err.println("WARNING: Illegal score unit: el:scoreunit=\"" + temp + "\".");
						target.info.modes.control = LevelInfo.Control.DEFAULT;
					}
					
					if ((temp = attrs.getValue("el:scoretarget")) == null)
						target.info.modes.scoretarget = LevelInfo.ScoreTarget.DEFAULT;
					else if (temp.equalsIgnoreCase("time"))
						target.info.modes.scoretarget = LevelInfo.ScoreTarget.TIME;
					else if (temp.equalsIgnoreCase("pushes"))
						target.info.modes.scoretarget = LevelInfo.ScoreTarget.PUSHES;
					else if (temp.equalsIgnoreCase("moves"))
						target.info.modes.scoretarget = LevelInfo.ScoreTarget.MOVES;
					else
					{
						target.info.modes.scoretarget = LevelInfo.ScoreTarget.LUA;
						target.info.modes.scoretargetlua = temp;
					}
					
					state = State.MODES;
				}
				else if (qName.equals("el:comments"))
				{
					state = State.COMMENTS;
				}
				else if (qName.equals("el:score"))
				{
					if ((temp = attrs.getValue("el:easy")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:easy", qName, locator));
					target.info.score.easy = Score.parseScore(temp);
					
					if ((temp = attrs.getValue("el:difficult")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:difficult", qName, locator));
					target.info.score.difficult = Score.parseScore(temp);
					
					state = State.SCORE;
				}
				else if (qName.equals("el:update"))
				{
					if ((target.info.updateUrl = attrs.getValue("el:url")) == null)
						target.info.updateUrl = "";
					
					state = State.UPDATE;
				}
				else this.error(new LevelXMLException("UnexpectedTagInfo", qName, locator));
				break;
				
			case IDENTITY:  this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:identity", locator));
			case VERSION:   this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:version", locator));
			case AUTHOR:    this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:author", locator));
			case COPYRIGHT: this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:copyright", locator));
			case LICENSE:   this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:license", locator));
			
			case COMPATIBILITY:
				if (qName.equals("el:dependency"))
				{
					LevelInfo.Compatibility.Dependency dep = new LevelInfo.Compatibility.Dependency();
					if ((dep.path = attrs.getValue("el:path")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:path", qName, locator));
					if ((dep.id = attrs.getValue("el:id")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:id", qName, locator));
					if ((temp = attrs.getValue("el:release")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:release", qName, locator));
					dep.release = Integer.parseUnsignedInt(temp);
					
					if ((temp = attrs.getValue("el:preload")) == null)
						dep.preload = true;
					else if (temp.equalsIgnoreCase("true"))
						dep.preload = true;
					else if (temp.equalsIgnoreCase("false"))
						dep.preload = false;
					else
						this.error(new LevelXMLException("UnexpectedValueTag", "el:preload", qName, locator));
					
					if ((dep.url = attrs.getValue("el:url")) == null)
						dep.url = "";
					
					target.info.compat.dependencies.add(dep);
					state = State.DEPENDENCY;
				}
				else if (qName.equals("el:externaldata"))
				{
					LevelInfo.Compatibility.External ext = new LevelInfo.Compatibility.External();
					if ((ext.path = attrs.getValue("el:path")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:path", qName, locator));
					if ((ext.url = attrs.getValue("el:url")) == null)
						ext.url = "";
					target.info.compat.externaldata.add(ext);
					state = State.EXTERNALDATA;
				}
				else if (qName.equals("el:editor"))
				{
					if ((target.info.compat.editor.name = attrs.getValue("el:name")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:name", qName, locator));
					if ((target.info.compat.editor.version = attrs.getValue("el:version")) == null)
						target.info.compat.editor.version = "";
					state = State.EDITORCOMPAT;
				}
				else this.error(new LevelXMLException("UnexpectedTagCompat", qName, locator));
				break;
				
			case DEPENDENCY:   this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:dependency", locator));
			case EXTERNALDATA: this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:externaldata", locator));
			case EDITORCOMPAT: this.error(new LevelXMLException("UnexpectedTag1", qName, "/ee:editor", locator));
			case MODES:        this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:modes", locator));
			
			case COMMENTS:
				if (qName.equals("el:credits") || qName.equals("el:dedication"))
				{
					LevelInfo.Comments.CommentShow tgt = qName.equals("el:credits")
							? target.info.comments.credits
							: target.info.comments.dedication;
					
					if ((temp = attrs.getValue("el:showinfo")) == null)
						tgt.showinfo = false;
					else if (temp.equalsIgnoreCase("true"))
						tgt.showinfo = true;
					else if (temp.equalsIgnoreCase("false"))
						tgt.showinfo = false;
					else
						this.error(new LevelXMLException("UnexpectedValueTag", "el:showinfo", qName, locator));
					
					if ((temp = attrs.getValue("el:showstart")) == null)
						tgt.showstart = false;
					else if (temp.equalsIgnoreCase("true"))
						tgt.showstart = true;
					else if (temp.equalsIgnoreCase("false"))
						tgt.showstart = false;
					else
						this.error(new LevelXMLException("UnexpectedValueTag", "el:showstart", qName, locator));
					
					state = qName.equals("el:credits") ? State.CREDITS : State.DEDICATION;
				}
				else if (qName.equals("el:code"))
				{
					state = State.CODECOMMENT;
				}
				else this.error(new LevelXMLException("UnexpectedTagComments", qName, locator));
				break;
			
			case CREDITS:     this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:credits", locator));
			case DEDICATION:  this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:dedication", locator));
			case CODECOMMENT: this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:code", locator));
			case SCORE:       this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:score", locator));
			case UPDATE:      this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:update", locator));
			case UPGRADE:     this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:upgrade", locator));
			
			case LUAMAIN:     this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:luamain", locator));
			case ELEMENTS:
			case EDITOR:
				
			case PUBLIC:
				if (qName.equals("el:upgrade"))
				{
					if ((temp = attrs.getValue("el:url")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:url", qName, locator));
					target.upgrade.url = temp;
					
					try
					{
						if ((temp = attrs.getValue("el:release")) == null)
							this.error(new LevelXMLException("MissingAttribute", "el:release", qName, locator));
						target.upgrade.release = Integer.parseUnsignedInt(temp);
					}
					catch (NumberFormatException e)
					{
						this.error(new LevelXMLException("UnexpectedValueTag", "el:release", qName, locator));
					}
					state = State.UPGRADE;
				}
				else if (qName.equals("el:i18n"))
				{
					i18nProtect = false;
					state = State.I18N;
				}
				else this.error(new LevelXMLException("UnexpectedTagPublic", qName, locator));
				break;
				
			case I18N:
				if (qName.equals("el:string"))
				{
					if ((temp = attrs.getValue("el:key")) == null)
						this.error(new LevelXMLException("MissingAttribute", "el:key", qName, locator));
					i18nString = target.i18n.get_nothrow(temp);
					if (i18nString == null)
						i18nString = target.i18n.create(temp);
					state = State.STRING;
				}
				else this.error(new LevelXMLException("UnexpectedTagI18n", qName, locator));
				break;
				
			case STRING:
				if (qName.equals("el:english"))
				{
					if (!i18nProtect)
						this.error(new LevelXMLException("UnexpectedPublicEnglish", locator));
					
					if ((temp = attrs.getValue("el:translate")) == null)
						i18nString.translate = true;
					else if (temp.equalsIgnoreCase("true"))
						i18nString.translate = true;
					else if (temp.equalsIgnoreCase("false"))
						i18nString.translate = false;
					else
						this.error(new LevelXMLException("UnexpectedValueTag", "el:translate", qName, locator));
					
					if ((temp = attrs.getValue("el:comment")) == null)
						i18nString.comment = temp;
					
					state = State.ENGLISH;
				}
				else if (qName.equals("el:translation"))
				{
					if ((i18nLang = attrs.getValue("el:lang")) == null)
						this.error(new LevelXMLException("MissingAtttribute", "el:lang", qName, locator));
					state = State.TRANSLATION;
				}
				else this.error(new LevelXMLException("UnexpectedTagString", qName, locator));
				break;
			
			case ENGLISH:     this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:english", locator));
			case TRANSLATION: this.error(new LevelXMLException("UnexpectedTag1", qName, "/el:translation", locator));
			}
		}
		
		@Override
		public void characters(char ch[], int start, int length) 
		{
			if (state == State.COPYRIGHT || state == State.CREDITS || state == State.DEDICATION || state == State.CODECOMMENT)
				buf.append(ch, start, length);
			else if (state == State.LICENSE)
				target.info.license.content.append(ch, start, length);
			else if (state == State.LUAMAIN)
				buf.append(ch, start, length);
			else if (state == State.ENGLISH || state == State.TRANSLATION)
				buf.append(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			switch (state)
			{
			case ROOT:
				this.error(new LevelXMLException("Expected EOF.", locator));
			case LEVEL:
				if (!qName.equals("el:level"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:level", locator));
				state = State.ROOT;
				break;
			case PROTECTED:
				if (!qName.equals("el:protected"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:protected", locator));
				state = State.LEVEL;
				break;
			case INFO:
				if (!qName.equals("el:info"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:info", locator));
				state = State.PROTECTED;
				break;
			case IDENTITY:
				if (!qName.equals("el:identity"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:identity", locator));
				state = State.INFO;
				break;
			case VERSION:
				if (!qName.equals("el:version"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:version", locator));
				state = State.INFO;
				break;
			case AUTHOR:
				if (!qName.equals("el:author"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:author", locator));
				state = State.INFO;
				break;
			case COPYRIGHT:
				if (!qName.equals("el:copyright"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:copyright", locator));
				target.info.copyright = buf.toString();
				buf.delete(0, buf.length());
				state = State.INFO;
				break;
			case LICENSE:
				if (!qName.equals("el:license"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:license", locator));
				state = State.INFO;
				break;
			case COMPATIBILITY:
				if (!qName.equals("el:compatibility"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:compatibility", locator));
				state = State.INFO;
				break;
			case DEPENDENCY:
				if (!qName.equals("el:dependency"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:dependency", locator));
				state = State.COMPATIBILITY;
				break;
			case EXTERNALDATA:
				if (!qName.equals("el:externaldata"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:externaldata", locator));
				state = State.COMPATIBILITY;
				break;
			case EDITORCOMPAT:
				if (!qName.equals("el:editor"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:editor", locator));
				state = State.COMPATIBILITY;
				break;
			case MODES:
				if (!qName.equals("el:modes"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:modes", locator));
				state = State.INFO;
				break;
			case COMMENTS:
				if (!qName.equals("el:comments"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:comments", locator));
				state = State.INFO;
				break;
			case CREDITS:
				if (!qName.equals("el:credits"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:credits", locator));
				target.info.comments.credits.comment = buf.toString();
				buf.delete(0, buf.length());
				state = State.COMMENTS;
				break;
			case DEDICATION:
				if (!qName.equals("el:dedication"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:dedication", locator));
				target.info.comments.dedication.comment = buf.toString();
				buf.delete(0, buf.length());
				state = State.COMMENTS;
				break;
			case CODECOMMENT:
				if (!qName.equals("el:code"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:code", locator));
				target.info.comments.code = buf.toString();
				buf.delete(0, buf.length());
				state = State.COMMENTS;
				break;
			case SCORE:
				if (!qName.equals("el:score"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:score", locator));
				state = State.INFO;
				break;
			case UPDATE:
				if (!qName.equals("el:update"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:update", locator));
				state = State.INFO;
				break;
			case LUAMAIN:
				if (!qName.equals("el:luamain"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:luamain", locator));
				target.luamain = buf.toString();
				buf.delete(0, buf.length());
				state = State.PROTECTED;
				break;
			case ELEMENTS:
				if (!qName.equals("el:elements"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:elements", locator));
				state = State.PROTECTED;
				break;
			case EDITOR:
				if (!qName.equals("el:editor"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:editor", locator));
				state = State.PROTECTED;
				break;
			case PUBLIC:
				if (!qName.equals("el:public"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:public", locator));
				state = State.LEVEL;
				break;
			case UPGRADE:
				if (!qName.equals("el:upgrade"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:upgrade", locator));
				state = State.PUBLIC;
				break;
			case I18N:
				if (!qName.equals("el:i18n"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:i18n", locator));
				state = (i18nProtect) ? State.PROTECTED : State.PUBLIC;
				break;
			case STRING:
				if (!qName.equals("el:string"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:string", locator));
				state = State.I18N;
				break;
			case ENGLISH:
				if (!qName.equals("el:english"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:english", locator));
				i18nString.english = buf.toString();
				buf.delete(0, buf.length());
				state = State.STRING;
				break;
			case TRANSLATION:
				if (!qName.equals("el:translation"))
					this.error(new LevelXMLException("UnexpectedEndTag", qName, "el:translation", locator));
				i18nString.put(i18nLang, buf.toString(), i18nProtect);
				buf.delete(0, buf.length());
				state = State.STRING;
				break;
			}
		}
	}
}

