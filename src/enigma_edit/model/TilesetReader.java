
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

package enigma_edit.model;

import java.io.File;
import java.io.IOException;
import java.util.AbstractCollection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import enigma_edit.error.MissingStringException;
import enigma_edit.error.TilesetXMLException;

public class TilesetReader
{
	private SAXParserFactory factory;
	private SAXParser        parser;
	private Tileset          target;
	
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
	
	public void setTarget(Tileset target) throws SAXException
	{
		parser.getXMLReader().setContentHandler(new TilesetParser(target));
		this.target = target;
	}
	
	public void parse(String filename) throws IOException, SAXException, MissingStringException
	{
		parser.getXMLReader().parse(convertToFileURL(filename));
		target.check();
	}
	
	private static class TilesetParser extends DefaultHandler
	{
		private static enum State {ROOT, TILESET, GROUP, ATTRGROUP, GATTR,
			                                             PAGE, KIND, KALIAS,
                                                                     KIMAGE,
			                                                         KATTR,
			                                                         MESSAGE,
			                                                         STACK,
			                                                         VARIANTS, CLUSTERS,
			                                                         	VARIANT, CLUSTER,
			                                                         		VIMAGE, CIMAGE, VATTR, VALIAS,
			                                      I18N, STRING, ENGLISH,
			                                                    TRANSLATION}
		
		private Locator           locator;
		private State             state;
		private Tileset           target;
		private Tileset.Group     group;
		private Tileset.Page      page;
		private Tileset.AttrGroup attrgroup;
		private Tileset.Attr      attr;
		private Tileset.Kind      kind;
		private Tileset.Variant   variant;
		private I18N.KeyString    string;
		
		private boolean           varstack;
		private int               imgstack;
		private String            id;
		private String            temp;
		private StringBuffer      buf;
		
		public TilesetParser(Tileset target)
		{
			this.target = target;
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
			state = State.ROOT;
			imgstack = 0;
			varstack = false;
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
		
		private Tileset.Attr parse_attr(Attributes attrs) throws TilesetXMLException
		{
			// name
			if ((temp = attrs.getValue("name")) == null)
				this.error(new TilesetXMLException((state == State.KIND) ? "MissingAttributeKind" : "MissingAttributeGroup", "name", "attr", id, locator));
			attr = new Tileset.Attr(temp);
			
			// type
			if ((temp = attrs.getValue("type")) == null)
				this.error(new TilesetXMLException((state == State.KIND) ? "MissingAttributeKind" : "MissingAttributeGroup", "type", "attr", id, locator));
			
			if      (temp.equals("boolean"))          attr.type = Tileset.Attr.Type.BOOLEAN;
			else if (temp.equals("integer"))          attr.type = Tileset.Attr.Type.INTEGER;
			else if (temp.equals("float"))            attr.type = Tileset.Attr.Type.FLOAT;
			else if (temp.equals("autofloat"))        attr.type = Tileset.Attr.Type.AUTOFLOAT;
			else if (temp.equals("string"))           attr.type = Tileset.Attr.Type.STRING;
			else if (temp.equals("token"))            attr.type = Tileset.Attr.Type.TOKEN;
			else if (temp.equals("position"))         attr.type = Tileset.Attr.Type.POSITION;
			else if (temp.equals("direction"))        attr.type = Tileset.Attr.Type.DIRECTION;
			else if (temp.equals("direction+rnd"  ))  attr.type = Tileset.Attr.Type.DIRECTION_RND;
			else if (temp.equals("direction+nodir"))  attr.type = Tileset.Attr.Type.DIRECTION_NODIR;
			else if (temp.equals("direction4"))       attr.type = Tileset.Attr.Type.DIRECTION;
			else if (temp.equals("direction4+rnd"  )) attr.type = Tileset.Attr.Type.DIRECTION_RND;
			else if (temp.equals("direction4+nodir")) attr.type = Tileset.Attr.Type.DIRECTION_NODIR;
			else if (temp.equals("direction8"))       attr.type = Tileset.Attr.Type.DIRECTION8;
			else if (temp.equals("direction8+rnd"  )) attr.type = Tileset.Attr.Type.DIRECTION8_RND;
			else if (temp.equals("direction8+nodir")) attr.type = Tileset.Attr.Type.DIRECTION8_NODIR;
			else if (temp.equals("connections"))      attr.type = Tileset.Attr.Type.CONNECTIONS;
			else if (temp.equals("flags"))            attr.type = Tileset.Attr.Type.FLAGS;
			else if (temp.equals("selection"))        attr.type = Tileset.Attr.Type.SELECTION;
			else if (temp.equals("stone"))            attr.type = Tileset.Attr.Type.STONE;
			else if (temp.equals("item"))             attr.type = Tileset.Attr.Type.ITEM;
			else if (temp.equals("floor"))            attr.type = Tileset.Attr.Type.FLOOR;
			else this.error(new TilesetXMLException((state == State.KIND) ? "IllegalAttrTypeKind" : "IllegalAttrTypeGroup", temp, attr.name, id, locator));
			
			// enum, default, i18n
			if ((temp = attrs.getValue("enum"))    != null) parse_list(temp, attr.enums);
			if ((temp = attrs.getValue("default")) != null) attr.defaultval = temp;
			if ((temp = attrs.getValue("i18n"))    != null) attr.i18n       = temp;
			
			// ui
			if ((temp = attrs.getValue("ui")) != null)
			{
				if      (temp.equals("attr"))        attr.ui = Tileset.Attr.Ui.ATTR;
				else if (temp.equals("switch"))      attr.ui = Tileset.Attr.Ui.SWITCH;
				else if (temp.equals("visual"))      attr.ui = Tileset.Attr.Ui.VISUAL;
				else if (temp.equals("connections")) attr.ui = Tileset.Attr.Ui.CONNECTIONS;
				else if (temp.equals("cluster"))     attr.ui = Tileset.Attr.Ui.CLUSTER;
				else if (temp.equals("readonly"))    attr.ui = Tileset.Attr.Ui.READONLY;
				else this.error(new TilesetXMLException((state == State.KIND) ? "IllegalAttrUITypeKind" : "IllegalAttrUITypeGroup", temp, attr.name, id, locator));
			}
			
			// return
			return attr;
		}
		
		private void parse_image(Attributes attrs, Tileset.Image image) throws TilesetXMLException
		{
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
		}
		
		@Override
		public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws TilesetXMLException
		{
			String temp;
			switch (state)
			{
			case ROOT:
				if (qName.equals("tileset"))
				{
					target.editor_version = attrs.getValue("version");
					if (target.editor_version == null) target.editor_version = "";
					target.enigma_version = attrs.getValue("enigma");
					if (target.enigma_version == null) target.enigma_version = "";
					state = State.TILESET;
				}
				else this.error(new TilesetXMLException("IllegalRoot", qName, locator));
				break;
			
			case TILESET:
				if (qName.equals("group"))
				{
					id = attrs.getValue("id");
					if (id == null) this.error(new TilesetXMLException("MissingAttribute", "id", "group", locator));
					group = target.createGroup(id);
					temp = attrs.getValue("i18n");
					if (temp != null)
						group.i18n = temp;
					temp = attrs.getValue("icon");
					if (temp != null)
						group.icon = temp;
					state = State.GROUP;
				}
				else if (qName.equals("i18n"))
				{
					state = State.I18N;
				}
				else this.error(new TilesetXMLException("UnexpectedTagTileset", qName, locator));
				break;
			
			case GROUP:
				if (qName.equals("attrgroup"))
				{
					if ((id = attrs.getValue("id")) == null)
						this.error(new TilesetXMLException("MissingAttributeGroup", "id", "attrgroup", group.id, locator));
					attrgroup = group.createAttrGroup(id);
					temp = attrs.getValue("i18n");
					if (temp != null) attrgroup.i18n = temp;
					state = State.ATTRGROUP;
				}
				else if (qName.equals("page"))
				{
					if ((id = attrs.getValue("id")) == null)
						this.error(new TilesetXMLException("MissingAttributeGroup", "id", "page", group.id, locator));
					page = group.createPage(id);
					if ((temp = attrs.getValue("i18n")) != null) page.i18n = temp;
					state = State.PAGE;
				}
				else this.error(new TilesetXMLException("UnexpectedTagGroup", qName, group.id, locator));
				break;
			
			case ATTRGROUP:
				if (qName.equals("attr"))
				{
					parse_attr(attrs);
					state = State.GATTR;
				}
				else this.error(new TilesetXMLException("UnexpectedTagAttrGroup", qName, attrgroup.id, locator));
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
						kind = page.createKind(id, Tileset.Kind.Type.AC);
					else if (temp.equalsIgnoreCase("fl"))
						kind = page.createKind(id, Tileset.Kind.Type.FL);
					else if (temp.equalsIgnoreCase("it"))
						kind = page.createKind(id, Tileset.Kind.Type.IT);
					else if (temp.equalsIgnoreCase("st"))
						kind = page.createKind(id, Tileset.Kind.Type.ST);
					else if (temp.equalsIgnoreCase("ot"))
						kind = page.createKind(id, Tileset.Kind.Type.OT);
					else
						this.error(new TilesetXMLException("UnexpectedValueKind", "type", id, locator));
					kind.image = null;
					
					// name, oldname, i18n, hidden
					if ((temp = attrs.getValue("name"))    != null) kind.name = temp;
					if ((temp = attrs.getValue("oldname")) != null) kind.oldname = temp;
					if ((temp = attrs.getValue("i18n"))    != null) kind.i18n = temp;
					if ((temp = attrs.getValue("hidden"))  != null)
					{
						if      (temp.equalsIgnoreCase("true"))  kind.hidden = true;
						else if (temp.equalsIgnoreCase("false")) kind.hidden = false;
						else this.error(new TilesetXMLException("UnexpectedValueKind", "hidden", id, locator));
					}
					
					// frame (only for floors)
					if (kind.type == Tileset.Kind.Type.FL && (temp = attrs.getValue("frame")) != null)
					{
						if (temp.equals("none")) kind.frame = false;
						else this.error(new TilesetXMLException("UnexpectedValueKind", "frame", id, locator));
					}
					state = State.KIND;
				}
				else this.error(new TilesetXMLException("UnexpectedTagPage", qName, page.id, locator));
				break;
			
			case KIND:
				if (qName.equals("alias"))
				{
					if ((temp = attrs.getValue("name")) == null)
						this.error(new TilesetXMLException("MissingAttributeKind", "name", qName, id, locator));
					Tileset.Name alias = new Tileset.Name(temp);
					if ((temp = attrs.getValue("oldname")) != null) alias.oldname = temp;
					kind.alias.add(alias);
					state = State.KALIAS;
				}
				else if (qName.equals("image"))
				{
					imgstack = 1;
					kind.image = new Tileset.Image(kind.name);
					parse_image(attrs, kind.image);
					state = State.KIMAGE;
				}
				else if (qName.equals("attr"))
				{
					kind.attrs.putAttr(parse_attr(attrs));
					state = State.KATTR;
				}
				else if (qName.equals("message"))
				{
					if ((temp = attrs.getValue("name")) == null)
						this.error(new TilesetXMLException("MissingAttributeKind", "name", qName, id, locator));
					kind.messages.add(temp);
					state = State.MESSAGE;
				}
				else if (qName.equals("variants"))
				{
					if (!kind.stack.isEmpty())
						this.error(new TilesetXMLException("UnexpectedVariants", id, locator));
					kind.stack.add();
					if ((temp = attrs.getValue("attrs"))   != null) parse_list(temp, kind.variants().attrs);
					if ((temp = attrs.getValue("default")) != null) kind.variants().defaultname = temp;
					if ((temp = attrs.getValue("showall")) != null)
					{
						if      (temp.equalsIgnoreCase("true"))  kind.variants().showall = true;
						else if (temp.equalsIgnoreCase("false")) kind.variants().showall = false;
						else this.error(new TilesetXMLException("UnexpectedValueVariants", "showall", id, locator)); 
					}
					state = State.VARIANTS;
				}
				else if (qName.equals("stack"))
				{
					if (!kind.stack.isEmpty())
						this.error(new TilesetXMLException("UnexpectedStack", id, locator));
					state = State.STACK;
				}
				else if (qName.equals("cluster"))
				{
					kind.cluster = new Tileset.Cluster();
					if ((temp = attrs.getValue("connections")) != null) kind.cluster.connections = temp;
					else this.error(new TilesetXMLException("MissingAttributeCluster", "connections", id, locator));
					if ((temp = attrs.getValue("faces")) != null) kind.cluster.faces = temp;
					else this.error(new TilesetXMLException("MissingAttributeCluster", "faces", id, locator));
					if ((temp = attrs.getValue("cluster")) != null) kind.cluster.cluster = temp;
					else this.error(new TilesetXMLException("MissingAttributeCluster", "cluster", id, locator));
					if ((temp = attrs.getValue("attrs"))   != null) parse_list(temp, kind.cluster.attrs);
					if ((temp = attrs.getValue("default")) != null) kind.cluster.defaultname = temp;
					state = State.CLUSTERS;
				}
				else this.error(new TilesetXMLException("UnexpectedTagKind", qName, id, locator));
				break;
			
			case MESSAGE:
				this.error(new TilesetXMLException("UnexpectedTag1", qName, "/message", locator));
			
			case KALIAS:
			case VALIAS:
				this.error(new TilesetXMLException("UnexpectedTag1", qName, "/alias", locator));
			
			case STACK:
				if (qName.equals("variants"))
				{
					kind.stack.add();
					if ((temp = attrs.getValue("attrs"))   != null) parse_list(temp, kind.variants().attrs);
					if ((temp = attrs.getValue("default")) != null) kind.variants().defaultname = temp;
					if ((temp = attrs.getValue("showall")) != null)
					{
						if      (temp.equalsIgnoreCase("true"))  kind.variants().showall = true;
						else if (temp.equalsIgnoreCase("false")) kind.variants().showall = false;
						else this.error(new TilesetXMLException("UnexpectedValueVariants", "showall", id, locator));
					}
					varstack = true;
					state = State.VARIANTS;
				}
				else this.error(new TilesetXMLException("UnexpectedTagStack", qName, id, locator));
				break;
				
			case VARIANTS:
				if (qName.equals("variant"))
				{
					variant = new Tileset.Variant(kind);
					if ((temp = attrs.getValue("val")) != null) parse_list(temp, variant.val);
					if ((temp = attrs.getValue("name")) != null)
						variant.name = variant.image.file = temp;
					else if (!variant.val.isEmpty())
						variant.name = "__" + String.join(",", variant.val) + "__";
					if ((temp = attrs.getValue("oldname")) != null) variant.oldname = temp;
					kind.variants().list.put(variant.name, variant);
					state = State.VARIANT;
				}
				else this.error(new TilesetXMLException("UnexpectedTagVariants", qName, id, locator));
				break;
			
			case CLUSTERS:
				if (qName.equals("variant"))
				{
					variant = new Tileset.Variant(kind);
					if ((temp = attrs.getValue("conn")) == null)
						this.error(new TilesetXMLException("MissingAttributeVariant", "conn", kind.name, locator));
					variant.val.add(temp);
					if ((temp = attrs.getValue("val")) != null) parse_list(temp, variant.val);
					if ((temp = attrs.getValue("name")) != null)
						variant.name = variant.image.file = temp;
					else if (!variant.val.isEmpty())
						variant.name = "__" + String.join(",", variant.val) + "__";
					if ((temp = attrs.getValue("oldname")) != null) variant.oldname = temp;
					kind.cluster.list.put(variant.name, variant);
					state = State.CLUSTER;
				}
				else this.error(new TilesetXMLException("UnexpectedTagVariants", qName, id, locator));
				break;
			
			case VARIANT:
			case CLUSTER:
				if (qName.equals("image"))
				{
					imgstack = 1;
					parse_image(attrs, variant.image);
					state = (state == State.VARIANT) ? State.VIMAGE : State.CIMAGE;
				}
				else if (state == State.CLUSTER)
				{
					this.error(new TilesetXMLException("UnexpectedTagCluster", qName, kind.name, locator));
				}
				else if (qName.equals("attr"))
				{
					if ((id = attrs.getValue("name")) == null)
						this.error(new TilesetXMLException("MissingAttributeName", kind.name, locator));
					if ((temp = attrs.getValue("default")) == null)
						this.error(new TilesetXMLException("MissingAttributeDefault", kind.name, locator));
					variant.attrs.put(id, temp);
					state = State.VATTR;
				}
				else if (qName.equals("alias"))
				{
					if ((temp = attrs.getValue("name")) == null)
						this.error(new TilesetXMLException("MissingAttribute", "name", qName, locator));
					Tileset.Name alias = new Tileset.Name(temp);
					if ((temp = attrs.getValue("oldname")) != null) alias.oldname = temp;
					variant.alias.add(alias);
					state = State.VALIAS;
				}
				else this.error(new TilesetXMLException("UnexpectedTagVariant", qName, kind.name, locator));
				break;
				
			case GATTR:
				this.error(new TilesetXMLException("UnexpectedTagGroupAttr", qName, attrgroup.id, locator));
				
			case KATTR:
			case VATTR:
				this.error(new TilesetXMLException("UnexpectedTagKindAttr", qName, kind.name, locator));
			
			case KIMAGE:
				if (qName.equals("image"))
				{
					++imgstack;
					Tileset.Image image = kind.image;
					while (image.stack != null) image = image.stack;
					image.stack = new Tileset.Image(kind.name);
					parse_image(attrs, image.stack);
				}
				else this.error(new TilesetXMLException("UnexpectedTagImage", qName, kind.name, locator));
				break;
			
			case VIMAGE:
			case CIMAGE:
				if (qName.equals("image"))
				{
					++imgstack;
					Tileset.Image image = variant.image;
					while (image.stack != null) image = image.stack;
					image.stack = new Tileset.Image(variant.name);
					parse_image(attrs, image.stack);
				}
				else this.error(new TilesetXMLException("UnexpectedTagImage", qName, kind.name, locator));
				break;
			
			case I18N:
				if (qName.equals("string"))
				{
					id = attrs.getValue("id");
					if (id == null) this.error(new TilesetXMLException("MissingAttribute", "id", qName, locator));
					string = target.i18n.get_nothrow(id);
					if (string == null)
					{
						target.i18n.put(id, true);
						string = target.i18n.get_nothrow(id);
					}
					state = State.STRING;
				}
				else this.error(new TilesetXMLException("UnexpectedTagI18n", qName, locator));
				break;
			
			case STRING:
				if (qName.equals("english"))
				{
					state = State.ENGLISH;
				}
				else if (qName.equals("translation"))
				{
					if ((id = attrs.getValue("lang")) == null)
						this.error(new TilesetXMLException("MissingAttributeLang", id, locator));
					state = State.TRANSLATION;
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
			if (state == State.ENGLISH || state == State.TRANSLATION)
				buf.append(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			switch (state)
			{
			case ROOT:
				this.error(new TilesetXMLException("Expected EOF.", locator)); 
			case TILESET:
				if (!qName.equals("tileset"))
					this.error(new TilesetXMLException("UnexpectedEndTagTileset", qName, locator));
				state = State.ROOT;
				break;
			case GROUP:
				if (!qName.equals("group"))
					this.error(new TilesetXMLException("UnexpectedEndTagGroup", qName, group.id, locator));
				state = State.TILESET;
				break;
			case ATTRGROUP:
				if (!qName.equals("attrgroup"))
					this.error(new TilesetXMLException("UnexpectedEndTagAttrGroup", qName, attrgroup.id, locator));
				state = State.GROUP;
				break;
			case GATTR:
				if (!qName.equals("attr"))
					this.error(new TilesetXMLException("UnexpectedEndTagGroupAttr", qName, attrgroup.id, locator));
				state = State.ATTRGROUP;
				break;
			case KATTR:
				if (!qName.equals("attr"))
					this.error(new TilesetXMLException("UnexpectedEndTagKindAttr", qName, kind.name, locator));
				state = State.KIND;
				break;
			case PAGE:
				if (!qName.equals("page"))
					this.error(new TilesetXMLException("UnexpectedEndTagPage", qName, page.id, locator));
				state = State.GROUP;
				break;
			case KIND:
				if (!qName.equals("kind"))
					this.error(new TilesetXMLException("UnexpectedEndTagKind", qName, kind.name, locator));
				if (kind.image == null && kind.getDefaultVariant() == null)
					kind.image = new Tileset.Image(kind.name);
				kind.registerAliases(target);
				state = State.PAGE;
				break;
			case MESSAGE:
				if (!qName.equals("message"))
					this.error(new TilesetXMLException("UnexpectedEndTagMessage", qName, kind.name, locator));
				state = State.KIND;
				break;
			case KALIAS:
			case VALIAS:
				if (!qName.equals("alias"))
					this.error(new TilesetXMLException("UnexpectedEndTagAlias", qName, kind.name, locator));
				state = (state == State.KALIAS) ? State.KIND : State.VARIANT;
				break;
			case STACK:
				if (!qName.equals("stack"))
					this.error(new TilesetXMLException("UnexpectedEndTagStack", qName, kind.name, locator));
				varstack = false;
				state = State.KIND;
				break;
			case VARIANTS:
				if (!qName.equals("variants"))
					this.error(new TilesetXMLException("UnexpectedEndTagVariants", qName, kind.name, locator));
				kind.variants().register(target);
				state = varstack ? State.STACK : State.KIND;
				break;
			case CLUSTERS:
				if (!qName.equals("cluster"))
					this.error(new TilesetXMLException("UnexpectedEndTagClusters", qName, kind.name, locator));
				kind.cluster.register(target);
				state = State.KIND;
				break;
			case VARIANT:
				variant.registerAliases(target);
			case CLUSTER:
				if (!qName.equals("variant"))
					this.error(new TilesetXMLException("UnexpectedEndTagVariant", qName, kind.name, locator));
				state = (state == State.VARIANT) ? State.VARIANTS : State.CLUSTERS;
				break;
			case VATTR:
				if (!qName.equals("attr"))
					this.error(new TilesetXMLException("UnexpectedEndTagVariantAttr", qName, kind.name, locator));
				state = State.VARIANT;
				break;
			case KIMAGE:
			case VIMAGE:
			case CIMAGE:
				if (!qName.equals("image"))
					this.error(new TilesetXMLException("UnexpectedEndTagImage", qName, kind.name, locator));
				if (--imgstack == 0) state = (state == State.KIMAGE) ? State.KIND : ((state == State.VIMAGE) ? State.VARIANT : State.CLUSTER);
				break;
			case I18N:
				if (!qName.equals("i18n"))
					this.error(new TilesetXMLException("UnexpectedEndTagI18n", qName, locator));
				state = State.TILESET;
				break;
			case STRING:
				if (!qName.equals("string"))
					this.error(new TilesetXMLException("UnexpectedEndTagString", qName, id, locator));
				state = State.I18N;
				break;
			case ENGLISH:
				if (!qName.equals("english"))
					this.error(new TilesetXMLException("UnexpectedEndTagEnglish", qName, id, locator));
				string.english = buf.toString();
				buf.delete(0, buf.length());
				state = State.STRING;
				break;
			case TRANSLATION:
				if (!qName.equals("translation"))
					this.error(new TilesetXMLException("UnexpectedEndTagTranslation", qName, id, locator));
				string.put(id, buf.toString());
				buf.delete(0, buf.length());
				state = State.STRING;
				break;
			}
		}
	}
}

