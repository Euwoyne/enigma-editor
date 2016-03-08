
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
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.euwoyne.enigma_edit.error.MissingStringException;
import com.github.euwoyne.enigma_edit.error.TilesetXMLException;
import com.github.euwoyne.enigma_edit.model.Tileset.Kind;

public class TilesetReader
{
	private SAXParserFactory factory;
	private SAXParser        parser;
	
    private static String convertToFileURL(String filename)
    {
        String path = new File(filename).getAbsolutePath();
        
        if (File.separatorChar != '/')
            path = path.replace(File.separatorChar, '/');
        
        if (!path.startsWith("/"))
            path = "/" + path;
        
        return "file:" + path;
    }
    
	public TilesetReader() throws ParserConfigurationException, SAXException
	{
		factory = SAXParserFactory.newInstance();
		parser  = factory.newSAXParser();
	}
	
	public Tileset parse(String filename) throws IOException, SAXException, MissingStringException
	{
		parser.getXMLReader().setContentHandler(new TilesetParser());
		parser.getXMLReader().parse(convertToFileURL(filename));
		((TilesetParser)parser.getXMLReader().getContentHandler()).target.registerNames();
		((TilesetParser)parser.getXMLReader().getContentHandler()).target.check();
		return ((TilesetParser)parser.getXMLReader().getContentHandler()).target;
	}
	
	public void addI18n(String filename, Tileset target) throws IOException, SAXException, MissingStringException
	{
		parser.getXMLReader().setContentHandler(new TilesetParser(target));
		parser.getXMLReader().parse(convertToFileURL(filename));
	}
	
	private static class TilesetParser extends DefaultHandler
	{
		private static enum ReaderType {FULL_TILESET, ADD_I18N}
		
		private static enum State {ROOT, TILESET, GROUP, ATTRGROUP,  GATTR,
			                                             PAGE, KIND, ATTR,
			                                                         ALIAS, AATTR,
			                                                         MESSAGE,
                                                                     IMAGE, ICON,
			                                                         CLUSTER, CONNECT,
			                                                         
			                                                         STACK,
			                                                         NVARIANTS, NVARIANT, VATTR,
			                                                         AVARIANTS, AVARIANT,
			                                      
			                                      I18N, STRING, ENGLISH,
			                                                    TRANSLATION}
		
		private class StateStack extends LinkedList<State>
		{
			private static final long serialVersionUID = 1L;
			public State   current()       {return this.getLast();}
			public void    to(State state) {this.add(state);}
			public void    back()          {this.removeLast();}
			public boolean is(State state) {return this.getLast() == state;}
		}
		
		public  Tileset                   target;
		private ReaderType                type;
		private Locator                   locator;
		private StateStack                state;
		
		private Tileset.Group             group;
		private Tileset.Page              page;
		private Tileset.AttrGroup         attrgroup;
		private Tileset.Attribute         attr;
		private Tileset.Kind              kind;
		private Tileset.Alias             alias;
		private Tileset.NamedVariants     nvariants;
		private Tileset.AttributeVariants avariants;
		private Tileset.Variant           variant;
		private Tileset.NamedVariant      nvariant;
		private Tileset.AttributeVariant  avariant;
		private Tileset.VariantImage      imagetarget;
		private Tileset.ClusterImage      cluster;
		private I18N.KeyString            string;
		
		private int                       dirs;
		private String                    id;
		private String                    temp;
		private StringBuffer              buf;
		
		public TilesetParser()
		{
			this.target = null;
			this.type   = ReaderType.FULL_TILESET;
			this.buf    = new StringBuffer();
		}
		
		public TilesetParser(Tileset target)
		{
			this.target = target;
			this.type   = ReaderType.ADD_I18N;
			this.buf    = new StringBuffer();
		}
		
		@Override
		public void warning(SAXParseException e)
		{
			System.err.println(e.getLineNumber() + "|" + e.getColumnNumber() + "| WARNING: " + e.getMessage());
		}
		
		@Override
		public void error(SAXParseException e) throws TilesetXMLException
		{
			System.err.println(e.getLineNumber() + "|" + e.getColumnNumber() + "| ERROR: " + e.getMessage());
			throw (TilesetXMLException)e;
		}
		
		@Override
		public void setDocumentLocator(Locator locator)
		{
			this.locator = locator;
		}
		
		@Override
		public void startDocument() throws SAXException
		{
			state = new StateStack();
			state.to(State.ROOT);
			dirs = 0;
		}
		
		private static void parse_list(String list, AbstractCollection<String> target)
		{
			int pos0 = 0;
			int pos1;
			while ((pos1 = list.indexOf(',', pos0)) >= 0)
			{
				target.add(list.substring(pos0, pos1).trim());
				pos0 = pos1 + 1;
			}
			if (pos0 < list.length())
				target.add(list.substring(pos0).trim());
		}
		
		private Tileset.Attribute parse_attr(Attributes attrs) throws TilesetXMLException
		{
			// name
			if ((temp = attrs.getValue("name")) == null)
				this.error(new TilesetXMLException(state.is(State.KIND) ? "MissingAttributeKind" : "MissingAttributeGroup", "name", "attr", id, locator));
			attr = new Tileset.Attribute(temp);
			
			// type
			if ((temp = attrs.getValue("type")) == null)
				this.error(new TilesetXMLException(state.is(State.KIND) ? "MissingAttributeKind" : "MissingAttributeGroup", "type", "attr", id, locator));
			
			if      (temp.equals("boolean"))          attr.type = Tileset.Attribute.Type.BOOLEAN;
			else if (temp.equals("integer"))          attr.type = Tileset.Attribute.Type.INTEGER;
			else if (temp.equals("float"))            attr.type = Tileset.Attribute.Type.FLOAT;
			else if (temp.equals("autofloat"))        attr.type = Tileset.Attribute.Type.AUTOFLOAT;
			else if (temp.equals("string"))           attr.type = Tileset.Attribute.Type.STRING;
			else if (temp.equals("token"))            attr.type = Tileset.Attribute.Type.TOKEN;
			else if (temp.equals("position"))         attr.type = Tileset.Attribute.Type.POSITION;
			else if (temp.equals("direction"))        attr.type = Tileset.Attribute.Type.DIRECTION;
			else if (temp.equals("direction+rnd"  ))  attr.type = Tileset.Attribute.Type.DIRECTION_RND;
			else if (temp.equals("direction+nodir"))  attr.type = Tileset.Attribute.Type.DIRECTION_NODIR;
			else if (temp.equals("direction4"))       attr.type = Tileset.Attribute.Type.DIRECTION;
			else if (temp.equals("direction4+rnd"  )) attr.type = Tileset.Attribute.Type.DIRECTION_RND;
			else if (temp.equals("direction4+nodir")) attr.type = Tileset.Attribute.Type.DIRECTION_NODIR;
			else if (temp.equals("direction8"))       attr.type = Tileset.Attribute.Type.DIRECTION8;
			else if (temp.equals("direction8+rnd"  )) attr.type = Tileset.Attribute.Type.DIRECTION8_RND;
			else if (temp.equals("direction8+nodir")) attr.type = Tileset.Attribute.Type.DIRECTION8_NODIR;
			else if (temp.equals("connections"))      attr.type = Tileset.Attribute.Type.CONNECTIONS;
			else if (temp.equals("flags"))            attr.type = Tileset.Attribute.Type.FLAGS;
			else if (temp.equals("selection"))        attr.type = Tileset.Attribute.Type.SELECTION;
			else if (temp.equals("stone"))            attr.type = Tileset.Attribute.Type.STONE;
			else if (temp.equals("item"))             attr.type = Tileset.Attribute.Type.ITEM;
			else if (temp.equals("floor"))            attr.type = Tileset.Attribute.Type.FLOOR;
			else this.error(new TilesetXMLException(state.is(State.KIND) ? "IllegalAttrTypeKind" : "IllegalAttrTypeGroup", temp, attr.name, id, locator));
			
			// enum, default, i18n
			if ((temp = attrs.getValue("enum"))    != null) parse_list(temp, attr.enums);
			if ((temp = attrs.getValue("default")) != null) attr.defaultValue = temp;
			if ((temp = attrs.getValue("i18n"))    != null) attr.i18n = temp;
			
			// ui
			if ((temp = attrs.getValue("ui")) != null)
			{
				if      (temp.equals("attr"))        attr.ui = Tileset.Attribute.Ui.ATTR;
				else if (temp.equals("switch"))      attr.ui = Tileset.Attribute.Ui.SWITCH;
				else if (temp.equals("visual"))      attr.ui = Tileset.Attribute.Ui.VISUAL;
				else if (temp.equals("connections")) attr.ui = Tileset.Attribute.Ui.CONNECTIONS;
				else if (temp.equals("cluster"))     attr.ui = Tileset.Attribute.Ui.CLUSTER;
				else if (temp.equals("readonly"))    attr.ui = Tileset.Attribute.Ui.READONLY;
				else this.error(new TilesetXMLException(state.is(State.KIND) ? "IllegalAttrUITypeKind" : "IllegalAttrUITypeGroup", temp, attr.name, id, locator));
			}
			
			// return
			return attr;
		}
		
		private Tileset.Image parse_image(Attributes attrs, String defaultfile) throws TilesetXMLException
		{
			Tileset.Image image = new Tileset.Image(defaultfile);
			if ((temp = attrs.getValue("file")) != null) image.file = temp;
			if ((temp = attrs.getValue("text")) != null) image.text = temp;
			if ((temp = attrs.getValue("pos"))  != null)
			{
				try
				{
					int pos = temp.indexOf(',');
					if (pos < 0) throw new NumberFormatException();
					image.x = Integer.parseUnsignedInt(temp.substring(0,pos));
					image.y = Integer.parseUnsignedInt(temp.substring(pos+1));
				}
				catch (NumberFormatException e)
				{
					this.error(new TilesetXMLException("UnexpectedValueKindTag", "pos", "image", kind.name, locator));
				}
			}
			return image;
		}
		
		@Override
		public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws TilesetXMLException
		{
			String temp;
			switch (state.current())
			{
			case ROOT:
				if (type == ReaderType.FULL_TILESET && qName.equals("tileset"))
				{
					String editorVer = attrs.getValue("version");
					if (editorVer == null) editorVer = "";
					String enigmaVer = attrs.getValue("enigma");
					if (enigmaVer == null) enigmaVer = "";
					target = new Tileset(editorVer, enigmaVer);
					state.to(State.TILESET);
				}
				else if (type == ReaderType.ADD_I18N && qName.equals("i18n"))
				{
					state.to(State.I18N);
				}
				else this.error(new TilesetXMLException("IllegalRoot", qName, locator));
				break;
			
			case TILESET:
				if (qName.equals("group"))
				{
					group = target.createGroup();
					temp = attrs.getValue("i18n");
					if (temp != null)
						group.setI18n(temp);
					temp = attrs.getValue("icon");
					if (temp != null)
						group.setIcon(temp);
					state.to(State.GROUP);
				}
				else if (qName.equals("i18n"))
				{
					state.to(State.I18N);
				}
				else this.error(new TilesetXMLException("UnexpectedTagTileset", qName, locator));
				break;
			
			case GROUP:
				if (qName.equals("attrgroup"))
				{
					temp = attrs.getValue("i18n");
					attrgroup = group.addAttributeGroup(temp);
					state.to(State.ATTRGROUP);
				}
				else if (qName.equals("page"))
				{
					page = new Tileset.Page();
					group.add(page);
					if ((temp = attrs.getValue("i18n")) != null) page.i18n = temp;
					state.to(State.PAGE);
				}
				else this.error(new TilesetXMLException("UnexpectedTagGroup", qName, group.getI18n(), locator));
				break;
			
			case ATTRGROUP:
				if (qName.equals("attr"))
				{
					parse_attr(attrs);
					state.to(State.GATTR);
				}
				else this.error(new TilesetXMLException("UnexpectedTagAttrGroup", qName, attrgroup.getI18n(), locator));
				break;
			
			case PAGE:
				if (qName.equals("kind"))
				{
					// id
					id = attrs.getValue("id");
					if (id == null)
						id = attrs.getValue("name");
					if (id == null)
						this.error(new TilesetXMLException("MissingAttributesOr", "id", "name", qName, locator));
					
					// type
					if ((temp = attrs.getValue("type")) == null)
						this.error(new TilesetXMLException("MissingAttribute", "type", qName, locator));
					if (temp.equalsIgnoreCase("ac"))
						kind = new Tileset.Kind(id, Tileset.Kind.Type.AC, group);
					else if (temp.equalsIgnoreCase("fl"))
						kind = new Tileset.Kind(id, Tileset.Kind.Type.FL, group);
					else if (temp.equalsIgnoreCase("it"))
						kind = new Tileset.Kind(id, Tileset.Kind.Type.IT, group);
					else if (temp.equalsIgnoreCase("st"))
						kind = new Tileset.Kind(id, Tileset.Kind.Type.ST, group);
					else
						this.error(new TilesetXMLException("UnexpectedValueKind", "type", id, locator));
					page.add(kind);
					
					// name, oldname, i18n, hidden, show
					if ((temp = attrs.getValue("name"))    != null) kind.name = temp;
					if ((temp = attrs.getValue("oldname")) != null) kind.oldname = temp;
					if ((temp = attrs.getValue("i18n"))    != null) kind.i18n = temp;
					if ((temp = attrs.getValue("access")) != null)
					{
						if      (temp.equalsIgnoreCase("kind"))   kind.access = Kind.Access.KIND;
						else if (temp.equalsIgnoreCase("alias"))  kind.access = Kind.Access.ALIAS;
						else if (temp.equalsIgnoreCase("hidden")) kind.access = Kind.Access.HIDDEN;
						else this.error(new TilesetXMLException("UnexpectedValueKind", "access", id, locator));
					}
					
					// frame (only for floors)
					if (kind.type == Tileset.Kind.Type.FL && (temp = attrs.getValue("framed")) != null)
					{
						if (temp.equals("true")) kind.framed = true;
						else if (!temp.equals("false")) this.error(new TilesetXMLException("UnexpectedValueKind", "framed", id, locator));
					}
					
					state.to(State.KIND);
				}
				else this.error(new TilesetXMLException("UnexpectedTagPage", qName, page.i18n, locator));
				break;
			
			case KIND:
				if (qName.equals("alias"))
				{
					if ((temp = attrs.getValue("name")) == null)
						this.error(new TilesetXMLException("MissingAttributeKind", "name", qName, id, locator));
					alias = new Tileset.Alias(temp, kind);
					if ((temp = attrs.getValue("oldname")) != null) alias.oldname = temp;
					kind.alias.add(alias);
					state.to(State.ALIAS);
				}
				else if (qName.equals("image"))
				{
					if (kind.stack.isEmpty())
						kind.stack.add(nvariants = new Tileset.NamedVariants(kind.name));
					if (kind.stack.size() > 1 || nvariants == null)
						this.error(new TilesetXMLException("UnexpectedImage", id, locator));
					nvariant = new Tileset.NamedVariant(kind.name, kind);
					nvariant.name.oldname = kind.oldname;
					nvariants.add(nvariant);
					nvariant.image.images.add(parse_image(attrs, kind.name));
					imagetarget = nvariant.image;
					avariant = null;
					avariants = null;
					state.to(State.IMAGE);
				}
				else if (qName.equals("cluster"))
				{
					if (kind.stack.isEmpty())
						kind.stack.add(nvariants = new Tileset.NamedVariants(kind.name));
					if (kind.stack.size() > 1 || nvariants == null)
						this.error(new TilesetXMLException("UnexpectedImage", id, locator));
					nvariant = new Tileset.NamedVariant(kind.name, kind);
					nvariant.name.oldname = kind.oldname;
					nvariants.add(nvariant);
					nvariant.image = imagetarget = cluster = new Tileset.ClusterImage();
					avariant = null;
					avariants = null;
					state.to(State.CLUSTER);
				}
				else if (qName.equals("icon"))
				{
					kind.icon = new Tileset.VariantImage();
					kind.icon.images.add(parse_image(attrs, kind.name));
					imagetarget = kind.icon;
					state.to(State.ICON);
				}
				else if (qName.equals("attr"))
				{
					final Tileset.Attribute attr = parse_attr(attrs);
					kind.attributes.put(attr.name, attr);
					state.to(State.ATTR);
				}
				else if (qName.equals("message"))
				{
					if ((temp = attrs.getValue("name")) == null)
						this.error(new TilesetXMLException("MissingAttributeKind", "name", qName, id, locator));
					kind.messages.add(temp);
					state.to(State.MESSAGE);
				}
				else if (qName.equals("variants"))
				{
					if (!kind.stack.isEmpty())
						this.error(new TilesetXMLException("UnexpectedVariants", id, locator));
					nvariants = null;
					avariants = null;
					if ((temp = attrs.getValue("attrs")) != null)
					{
						avariants = new Tileset.AttributeVariants();
						parse_list(temp, avariants.attrs);
						kind.stack.add(avariants);
						state.to(State.AVARIANTS);
					}
					if ((temp = attrs.getValue("default")) != null)
					{
						if (avariants != null)
							this.error(new TilesetXMLException("IllegalVariants", id, locator)); 
						nvariants = new Tileset.NamedVariants(temp);
						kind.stack.add(nvariants);
						state.to(State.NVARIANTS);
					}
					else if (avariants == null)
					{
						nvariants = new Tileset.NamedVariants(kind.name);
						kind.stack.add(nvariants);
						state.to(State.NVARIANTS);
					}
				}
				else if (qName.equals("stack"))
				{
					if (!kind.stack.isEmpty())
						this.error(new TilesetXMLException("UnexpectedStack", id, locator));
					state.to(State.STACK);
				}
				else this.error(new TilesetXMLException("UnexpectedTagKind", qName, id, locator));
				break;
			
			case MESSAGE:
				this.error(new TilesetXMLException("UnexpectedTag1", qName, "/message", locator));
			
			case ALIAS:
				if (qName.equals("attr"))
				{
					final String name = attrs.getValue("name");
					if (name == null)
						this.error(new TilesetXMLException("MissingAttributeAlias", "name", alias.name, id, locator));
					if ((temp = attrs.getValue("val")) == null)
						this.error(new TilesetXMLException("MissingAttributeAlias", "val",  alias.name, id, locator));
					alias.attributes.put(name, temp);
					state.to(State.AATTR);
				}
				else this.error(new TilesetXMLException("UnexpectedTagAlias", qName, alias.name, id, locator));
				break;
			
			case STACK:
				if (qName.equals("variants"))
				{
					avariants = new Tileset.AttributeVariants();
					nvariants = null;
					kind.stack.add(avariants);
					if ((attrs.getValue("default")) != null) 
						this.error(new TilesetXMLException("IllegalVariantStack", id, locator));
					if ((temp = attrs.getValue("attrs")) != null) parse_list(temp, avariants.attrs);
					state.to(State.AVARIANTS);
				}
				else this.error(new TilesetXMLException("UnexpectedTagStack", qName, id, locator));
				break;
				
			case NVARIANTS:
				if (qName.equals("variant"))
				{
					if ((id = attrs.getValue("name")) == null)
						id = kind.name;
					variant = nvariant = new Tileset.NamedVariant(id, kind);
					if ((temp = attrs.getValue("oldname")) != null)
						nvariant.name.oldname = temp;
					nvariants.add(nvariant);
					state.to(State.NVARIANT);
				}
				else this.error(new TilesetXMLException("UnexpectedTagVariants", qName, id, locator));
				break;
				
			case AVARIANTS:
				if (qName.equals("variant"))
				{
					if ((id = attrs.getValue("val")) == null)
						this.error(new TilesetXMLException("MissingAttributeVal", id, locator));
					
					ArrayList<String> val = new ArrayList<String>();
					parse_list(id, val);
					if (val.isEmpty()) val.add("");
					variant = avariant = new Tileset.AttributeVariant(kind);
					avariant.val = val;
					avariants.add(avariant);
					state.to(State.AVARIANT);
				}
				else this.error(new TilesetXMLException("UnexpectedTagVariants", qName, id, locator));
				break;
			
			case NVARIANT:
			case AVARIANT:
				if (qName.equals("image"))
				{
					imagetarget = variant.image;
					imagetarget.images.add(parse_image(attrs, kind.name));
					state.to(State.IMAGE);
				}
				else if (qName.equals("cluster"))
				{
					if (variant.image.images.size() > 0)
						this.error(new TilesetXMLException("UnexpectedClusterVariant", id, kind.name, locator));
					variant.image = imagetarget = cluster = new Tileset.ClusterImage();
					state.to(State.CLUSTER);
				}
				else if (qName.equals("attr"))
				{
					if ((id = attrs.getValue("name")) == null)
						this.error(new TilesetXMLException("MissingAttributeAttrName", kind.name, locator));
					if ((temp = attrs.getValue("val")) == null)
						this.error(new TilesetXMLException("MissingAttributeAttrVal", kind.name, locator));
					variant.attributes.put(id, temp);
					state.to(State.VATTR);
				}
				else this.error(new TilesetXMLException("UnexpectedTagVariant", qName, kind.name, locator));
				break;
			
			case CLUSTER:
				if (qName.equals("connect"))
				{
					if ((temp = attrs.getValue("dirs")) == null)
						this.error(new TilesetXMLException("MissingAttributeKind", "dirs", "connect", kind.name, locator));
					if ((dirs = Tileset.ClusterImage.getIndex(temp)) < 0)
						this.error(new TilesetXMLException("UnexpectedValueKindTag", "dirs", "connect", kind.name, locator));
					if (dirs > 0)
						cluster.connect[dirs] = new Tileset.VariantImage();
					state.to(State.CONNECT);
				}
				else if (qName.equals("image"))
				{
					imagetarget = cluster.connect[0];
					imagetarget.images.add(parse_image(attrs, kind.name));
					state.to(State.IMAGE);
				}
				else this.error(new TilesetXMLException("UnexpectedTagCluster", qName, kind.name, locator));
				break;
			
			case CONNECT:
				if (qName.equals("image"))
				{
					imagetarget = cluster.connect[dirs];
					imagetarget.images.add(parse_image(attrs, kind.name));
					state.to(State.IMAGE);
				}
				else this.error(new TilesetXMLException("UnexpectedTagConnect", qName, kind.name, locator));
				break;
				
			case GATTR:
				this.error(new TilesetXMLException("UnexpectedTagGroupAttr", qName, attrgroup.getI18n(), locator));
				
			case ATTR:
			case VATTR:
				this.error(new TilesetXMLException("UnexpectedTagKindAttr", qName, kind.name, locator));
			
			case AATTR:
				this.error(new TilesetXMLException("UnexpectedTagAliasAttr", qName, alias.name, kind.name, locator));
			
			case IMAGE:
			case ICON:
				if (qName.equals("image"))
				{
					imagetarget.images.add(parse_image(attrs, kind.name));
					state.to(State.IMAGE);
				}
				else this.error(new TilesetXMLException("UnexpectedTagImage", qName, kind.name, locator));
				break;
			
			case I18N:
				if (qName.equals("string"))
				{
					id = attrs.getValue("id");
					if (id == null) this.error(new TilesetXMLException("MissingAttribute", "id", qName, locator));
					string = target.getI18n().get_nothrow(id);
					if (string == null)
					{
						target.getI18n().put(id, true);
						string = target.getI18n().get_nothrow(id);
					}
					state.to(State.STRING);
				}
				else this.error(new TilesetXMLException("UnexpectedTagI18n", qName, locator));
				break;
			
			case STRING:
				if (qName.equals("english"))
				{
					state.to(State.ENGLISH);
				}
				else if (qName.equals("translation"))
				{
					if ((id = attrs.getValue("lang")) == null)
						this.error(new TilesetXMLException("MissingAttributeLang", id, locator));
					state.to(State.TRANSLATION);
				}
				else this.error(new TilesetXMLException("UnexpectedTagString", qName, id, locator));
				break;
			
			case ENGLISH:
				this.error(new TilesetXMLException("UnexpectedTagEnglish", qName, id, locator));
			case TRANSLATION:
				this.error(new TilesetXMLException("UnexpectedTagTranslation", qName, id, locator));
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			if (state.is(State.ENGLISH) || state.is(State.TRANSLATION))
				buf.append(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			switch (state.current())
			{
			case ROOT:
				this.error(new TilesetXMLException("Expected EOF.", locator)); 
			case TILESET:
				if (!qName.equals("tileset"))
					this.error(new TilesetXMLException("UnexpectedEndTagTileset", qName, locator));
				state.back();
				break;
			case GROUP:
				if (!qName.equals("group"))
					this.error(new TilesetXMLException("UnexpectedEndTagGroup", qName, group.getI18n(), locator));
				group = null;
				state.back();
				break;
			case ATTRGROUP:
				if (!qName.equals("attrgroup"))
					this.error(new TilesetXMLException("UnexpectedEndTagAttrGroup", qName, attrgroup.getI18n(), locator));
				attrgroup = null;
				state.back();
				break;
			case GATTR:
				if (!qName.equals("attr"))
					this.error(new TilesetXMLException("UnexpectedEndTagGroupAttr", qName, attrgroup.getI18n(), locator));
				state.back();
				break;
			case PAGE:
				if (!qName.equals("page"))
					this.error(new TilesetXMLException("UnexpectedEndTagPage", qName, page.i18n, locator));
				page = null;
				state.back();
				break;
			case KIND:
				if (!qName.equals("kind"))
					this.error(new TilesetXMLException("UnexpectedEndTagKind", qName, kind.name, locator));
				if (kind.stack.isEmpty())
				{
					nvariants = new Tileset.NamedVariants(kind.name);
					nvariant = new Tileset.NamedVariant(kind.name, kind);
					nvariant.name.oldname = kind.oldname;
					nvariant.image.images.add(new Tileset.Image(kind.name));
					nvariants.add(nvariant);
					kind.stack.add(nvariants);
				}
				kind = null;
				state.back();
				break;
			case ATTR:
				if (!qName.equals("attr"))
					this.error(new TilesetXMLException("UnexpectedEndTagKindAttr", qName, kind.name, locator));
				state.back();
				break;
			case ALIAS:
				if (!qName.equals("alias"))
					this.error(new TilesetXMLException("UnexpectedEndTagAlias", qName, kind.name, locator));
				state.back();
				break;
			case AATTR:
				if (!qName.equals("attr"))
					this.error(new TilesetXMLException("UnexpectedEndTagAliasAttr", qName, alias.name, kind.name, locator));
				state.back();
				break;
			case MESSAGE:
				if (!qName.equals("message"))
					this.error(new TilesetXMLException("UnexpectedEndTagMessage", qName, kind.name, locator));
				state.back();
				break;
			case STACK:
				if (!qName.equals("stack"))
					this.error(new TilesetXMLException("UnexpectedEndTagStack", qName, kind.name, locator));
				state.back();
				break;
			case NVARIANTS:
			case AVARIANTS:
				if (!qName.equals("variants"))
					this.error(new TilesetXMLException("UnexpectedEndTagVariants", qName, kind.name, locator));
				nvariants = null;
				avariants = null;
				state.back();
				break;
			case NVARIANT:
				if (!qName.equals("variant"))
					this.error(new TilesetXMLException("UnexpectedEndTagVariant", qName, kind.name, locator));
				if (variant.image.images.isEmpty())
					variant.image.images.add(new Tileset.Image(nvariant.name.name));
				variant = null;
				nvariant = null;
				state.back();
				break;
			case AVARIANT:
				if (!qName.equals("variant"))
					this.error(new TilesetXMLException("UnexpectedEndTagVariant", qName, kind.name, locator));
				if (variant.image.images.isEmpty())
					variant.image.images.add(new Tileset.Image(kind.name));
				if (avariant.checkCompare())
					avariants.hasCompare = true;
				variant = null;
				avariant = null;
				state.back();
				break;
			case CLUSTER:
				if (!qName.equals("cluster"))
					this.error(new TilesetXMLException("UnexpectedEndTagCluster", qName, kind.name, locator));
				cluster = null;
				state.back();
				break;
			case CONNECT:
				if (!qName.equals("connect"))
					this.error(new TilesetXMLException("UnexpectedEndTagConnect", qName, kind.name, locator));
				state.back();
				break;
			case VATTR:
				if (!qName.equals("attr"))
					this.error(new TilesetXMLException("UnexpectedEndTagVariantAttr", qName, kind.name, locator));
				state.back();
				break;
			case IMAGE:
				if (!qName.equals("image"))
					this.error(new TilesetXMLException("UnexpectedEndTagImage", qName, kind.name, locator));
				imagetarget = null;
				state.back();
				break;
			case ICON:
				if (!qName.equals("icon"))
					this.error(new TilesetXMLException("UnexpectedEndTagIcon", qName, kind.name, locator));
				imagetarget = null;
				state.back();
				break;
			case I18N:
				if (!qName.equals("i18n"))
					this.error(new TilesetXMLException("UnexpectedEndTagI18n", qName, locator));
				state.back();
				break;
			case STRING:
				if (!qName.equals("string"))
					this.error(new TilesetXMLException("UnexpectedEndTagString", qName, id, locator));
				string = null;
				state.back();
				break;
			case ENGLISH:
				if (!qName.equals("english"))
					this.error(new TilesetXMLException("UnexpectedEndTagEnglish", qName, id, locator));
				string.english = buf.toString();
				buf.delete(0, buf.length());
				state.back();
				break;
			case TRANSLATION:
				if (!qName.equals("translation"))
					this.error(new TilesetXMLException("UnexpectedEndTagTranslation", qName, id, locator));
				string.put(id, buf.toString());
				buf.delete(0, buf.length());
				state.back();
				break;
			}
		}
	}
}

