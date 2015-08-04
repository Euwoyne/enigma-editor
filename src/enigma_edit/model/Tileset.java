
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Map.Entry;

import enigma_edit.error.MissingGroupIconException;
import enigma_edit.error.MissingImageException;
import enigma_edit.error.MissingStringException;
import enigma_edit.lua.data.MMSimpleValue;
import enigma_edit.lua.data.Mode;
import enigma_edit.lua.data.SimpleValue;
import enigma_edit.lua.data.Table;
import enigma_edit.lua.data.TilePart;
import enigma_edit.model.I18N.KeyString;

/**
 * Collection of all available kinds of objects.
 * This class provides the data, necessary for the correct
 * rendering of all kinds of Enigma-objects, as well as the
 * necessary data for attribute and message editing.
 * The actual data will normally be provided by an XML-
 * description file, that is parsed by an instance of
 * {@link TilesetReader}.
 */
public class Tileset implements Iterable<Tileset.Group>
{
	/**
	 * Description of an attribute, that is available for a kind.
	 * This metadata includes type and name of the attribute as well as
	 * information regarding the display in the user interface. 
	 */
	public static class Attr
	{
		/**
		 * Attribute data types.
		 */
		public static enum Type
		{
			/** corresponds to lua type {@code boolean}. */                    BOOLEAN,
			/** corresponds to lua type {@code integer}. */                    INTEGER,
			/** corresponds to lua type {@code float}.   */                    FLOAT,
			/** corresponds to lua type {@code string}.  */                    STRING,
			
			/** is a {@code float} with a special value literal,
			 *  that indicates automatic choice of the value by the engine. */ AUTOFLOAT,
			
			/** is a string that can serve as an identifier in Lua.         */ TOKEN,
			
			/** is either a Lua table with two numbers or an identifier of a
			 *  positioned object or an instance of the Enigma API type
			 *  {@code po} (which can be created by one of the previous).   */ POSITION,
		    
			/** is essentially an {@code integer} but is represented by
			 *  an enumerated list of integer constants for the four main
			 *  directions (see {@link Direction}).                         */ DIRECTION,
			
			/** is an {@code integer} at the core with a specific set of
			 *  value constants (see {@link Direction8}).                   */ DIRECTION8,
			
			/**  provides an additional value <code>NODIR</code>.
			 *   This is currently unused.                                  */ DIRECTION_NODIR,
			
			/** provides an additional value <code>NODIR</code>.
			 *  This is used (by the floor kind <code>fl_slope</code>).     */ DIRECTION8_NODIR,
			
			/** has the additional value <code>RANDOMDIR</code> and is
			 *   currently in use for the four-valued <code>DIRECTION</code>
			 *   type only (by the stone <code>st_mirror</code>).           */ DIRECTION_RND,
			
			/** has the additional value <code>RANDOMDIR</code> and is
			 *  currently unused.                                           */ DIRECTION8_RND,
			
			
			/** is a string with any combination of {@code nesw}.
			 *  This is currently only used by the item {@code it_stip}.    */ CONNECTIONS,
			
			/** is a type, that is essentially an integer, but the
			 *   meaningful values are represented by summed (or rather
			 *   bitwise or'd) predefined values.
			 *   The UI will show them as check-boxes.                      */ FLAGS,
			
			/** is a string containing the name of a stone.                 */ STONE,
			/** is a string containing the name of an item.                 */ ITEM,
			/** is a string containing the name of a floor.                 */ FLOOR,
			/** is a string containing the name of any kind.                */ KIND,
			/** is a lua table of strings representing kinds.               */ SELECTION
		};
		
		/**
		 * Attribute user interface types.
		 */
		public static enum Ui
		{
			/** Show on attribute page. */       ATTR,
			/** Change by clicking the cell. */  SWITCH,
			/** Choose by graphical menu. */     VISUAL,
			/** Choose automatically by path. */ CONNECTIONS,
			/** Choose by neighbors. */          CLUSTER,
			/** Do not show at all. */           READONLY
		}
		
		String            name;
		Type              type;
		ArrayList<String> enums;
		String            defaultval;
		String            i18n;
		Ui                ui;
		
		/**
		 * Create a new attribute.
		 * 
		 * @param name  Name of the attribute.
		 * @param type  Type of the attribute.
		 * @param ui    User interface type.
		 */
		Attr(String name, Type type, Ui ui)
		{
			this.name       = name;
			this.type       = type;
			this.enums      = new ArrayList<String>(); 
			this.defaultval = null;
			this.i18n       = name;
			this.ui         = ui;
		}
		
		/**
		 * Create a new integer attribute with default user interface.
		 * 
		 * @param name  Name of the attribute.
		 */
		Attr(String name) {this(name, Type.INTEGER, Ui.ATTR);}
		
		public String  getName()       {return name;}
		public Type    getType()       {return type;}
		public int     enumSize()      {return enums.size();}
		public String  getEnum(int i)  {return enums.get(i);}
		public String  getDefault()    {return defaultval;}
		public String  getI18n()       {return i18n;}
		public Ui      getUi()         {return ui;}
	}
	
	/**
	 * A map of named attributes.
	 */
	public static class Attributes implements Iterable<Attr>
	{
		/** internal attribute map. */
		private HashMap<String, Attr> attrs;
		
		/** Default constructor. */
		Attributes() {attrs = new HashMap<String, Attr>();}
		
		/**
		 * Check attribute existence.
		 * @param name  Attribute to check for.
		 * @return      {@code true}, if this set of attributes features the given one.
		 */
		public boolean hasAttr(String name) {return attrs.containsKey(name);}
		
		/**
		 * Attribute access.
		 * @param name  Attribute to check.
		 * @return      Definition of the given attribute.
		 */
		public Attr getAttr(String name) {return attrs.get(name);}
		
		/**
		 * Add new attribute to this group.
		 * @param attr  New attribute instance.
		 */
		void putAttr(Attr attr) {attrs.put(attr.name, attr);}
		
		/**
		 * Return the attribute with the given name, and create a new one, if it does not exist.
		 * If the given attribute is already declared, this declaration will be returned.
		 * Otherwise, the attribute is prepared with default values and returned for modification.
		 * 
		 * @param name  Attribute name.
		 * @return      The already existent or a new declaration of the requested attribute.
		 */
		Attr createAttr(String name)
		{
			Attr out = attrs.get(name);
			if (out == null)
			{
				out = new Attr(name);
				attrs.put(name, out);
			}
			return out;
		}
		
		/**
		 * Returns an iterator over the attributes declared here.
		 */
		@Override
		public Iterator<Attr> iterator()
		{
			return attrs.values().iterator();
		}
	}
	
	/**
	 * A group of common attributes.
	 * This group of attributes is shared by any members of its parent group.
	 */
	public static class AttrGroup extends Attributes
	{
		/** Attribute group id. */
		String id;
		
		/** User interface label. */
		String i18n;
		
		/**
		 * Creates a new attribute group with the given identifier.
		 * @param id  Identifier of the new attribute group.
		 */
		AttrGroup(String id) {super(); this.id = this.i18n = id;}
		
		public String  getId()   {return id;}
		public String  getI18n() {return i18n;}
	}
	
	public static class Image
	{
		String file;
		String text;
		int    x, y;
		Image  stack;
		Sprite sprite;
		
		public Image()                                       {this("",   "",   0, 0, null);}
		public Image(String file)                            {this(file, "",   0, 0, null);}
		public Image(String file, String text)               {this(file, text, 0, 0, null);}
		public Image(String file, String text, int x, int y) {this(file, text, x, y, null);}
		public Image(String file, String text, int x, int y, Image stack)
		{
			this.file  = file;
			this.text  = text;
			this.x     = x;
			this.y     = y;
			this.stack = stack;
		}
		
		public String getFile()   {return file;}
		public String getText()   {return text;}
		public int    getX()      {return x;}
		public int    getY()      {return y;}
		public Image  getStack()  {return stack;}
		public Sprite getSprite() {return sprite;}
	}
	
	public static class Name
	{
		String name;
		String oldname;
		
		Name(String name)           {this.name = name; this.oldname = null;}
		public String  getName()    {return name;}
		public boolean hasOldName() {return oldname != null;}
		public String  getOldName() {return oldname != null ? oldname : "";}
	}
	
	public static class NamedImage extends Name
	{
		Image         image;
		HashSet<Name> alias;
		
		NamedImage(String name)
		{
			super(name);
			this.image = new Image(name);
			this.alias = new HashSet<Name>();
		}
		
		public Image getImage() {return image;}
		
		void registerAliases(Tileset tileset)
		{
			for (Name a : alias)
				tileset.names.add(a, this);
		}
	}
	
	public static class Variant extends NamedImage
	{
		Kind                    kind;
		ArrayList<String>       val;
		HashMap<String, String> attrs;
		
		Variant(Kind kind)
		{
			super(kind.name);
			this.kind  = kind; 
			this.val   = new ArrayList<String>();
			this.attrs = new HashMap<String, String>(); 
		}
		
		public Kind getKind() {return kind;}
	}
	
	public static class Variants implements Iterable<Variant>
	{
		ArrayList<String>        attrs;
		String                   defaultname;
		boolean                  showall;
		HashMap<String, Variant> list;
		
		Variants()
		{
			attrs       = new ArrayList<String>();
			defaultname = null;
			showall     = false;
			list        = new HashMap<String, Variant>();
		}
		
		void register(Tileset tileset)
		{
			for (Entry<String, Variant> v : list.entrySet())
				tileset.names.add(v.getValue());
		}
		
		public List<String> getAttrs()   {return Collections.unmodifiableList(attrs);}
		public String       getDefault() {return defaultname;}
		public boolean      getShowAll() {return showall;}
		
		public boolean isVisual(Kind kind)
		{
			for (String attr : attrs)
				if (kind.attrs.getAttr(attr).ui == Attr.Ui.VISUAL)
					return true;
			return false;
		}
		
		@Override public Iterator<Variant> iterator() {return list.values().iterator();}
		
		public Variant getByName(String name)    {return list.get(name);}
		public boolean hasVariant(String name)   {return list.containsKey(name);}
		
		public Variant getByOldName(String name)
		{
			for (Entry<String, Variant> v : list.entrySet())
				if (v.getValue().oldname.equals(name))
					return v.getValue();
			return null;
		}
		
		public Variant getByAttrs(List<String> attrs)
		{
			for (Entry<String, Variant> v : list.entrySet())
				if (v.getValue().val.equals(attrs))
					return v.getValue();
			return null;
		}
		
		public ArrayList<String> getDefaultAttrs(Kind kind)
		{
			ArrayList<String> values = new ArrayList<String>();
			for (String attr : attrs)
			{
				if (!kind.attrs.hasAttr(attr))
					values.add("nil");
				if (kind.attrs.getAttr(attr).defaultval.equals("*"))
					values.add(kind.attrs.getAttr(attr).enums.get((int)(Math.random() * kind.attrs.getAttr(attr).enums.size())));
				else
					values.add(kind.attrs.getAttr(attr).defaultval);
			}
			return values;
		}
		
		public Variant getByDefaultAttrs(Kind kind)
		{
			return getByAttrs(getDefaultAttrs(kind));
		}
		
		public Variant getDefaultVariant(Kind kind)
		{
			if (defaultname != null) return list.get(defaultname);
			if (!attrs.isEmpty())    return getByDefaultAttrs(kind);
			return list.get(kind.getName());
		}
	}
	
	public static class Cluster extends Variants
	{
		String connections;
		String faces;
		String cluster;
		
		Cluster() {super(); connections = faces = cluster = "";}
		
		@Override
		public ArrayList<String> getDefaultAttrs(Kind kind)
		{
			ArrayList<String> values = super.getDefaultAttrs(kind);
			values.add(0, "");
			return values;
		}
	}
	
	public static class Stack extends ArrayList<Variants>
	{
		private static final long serialVersionUID = 0L;
		
		Variants add()
		{
			Variants vars = new Variants();
			this.add(vars);
			return vars;
		}
	}
	
	public static class Kind extends NamedImage
	{
		public static enum Type {AC,FL,IT,ST,OT};
		
		String   i18n;
		Type     type;
		boolean  hidden;
		boolean  frame;
		Stack    stack;
		Cluster  cluster;
		
		Attributes      attrs;
		HashSet<String> messages;
		
		Kind(String id, Type type)
		{
			super(id);
			this.i18n     = id;
			this.type     = type;
			this.hidden   = false; 
			this.frame    = (type == Type.FL);
			this.stack    = new Stack();
			this.cluster  = null;
			this.attrs    = new Attributes();
			this.messages = new HashSet<String>();
		}
		
		Variants variants() {return stack.get(stack.size()-1);}
		
		public String   getI18n()     {return i18n;}
		public Type     getType()     {return type;}
		public boolean  isHidden()    {return hidden;}
		public boolean  hasFrame()    {return frame;}
		public boolean  hasVariants() {return stack.size() > 0;}
		public boolean  hasStack()    {return stack.size() > 1;}
		public boolean  isCluster()   {return cluster != null;}
		public Variants getVariants() {return stack.get(stack.size() - 1);}
		public Stack    getStack()    {return stack;}
		
		public Image getDefaultImage()
		{
			if (image != null) return image;
			Variant var = getDefaultVariant();
			if (var != null) return var.image;
			if (cluster.list.containsKey("")) return cluster.list.get("").image;
			return null;
		}
		
		public Variant getDefaultVariant()
		{
			if (cluster != null)
				return cluster.getDefaultVariant(this);
			if (!stack.isEmpty())
				return stack.get(stack.size() - 1).getDefaultVariant(this);
			return null;
		}
		
		@Override
		void registerAliases(Tileset tileset)
		{
			super.registerAliases(tileset);
			if (this.frame)
				tileset.names.add(new Name(this.name + "_framed"), this);
		}
	}
	
	private static class NameMap implements Iterable<Entry<String, NamedImage>> 
	{
		HashMap<String, NamedImage> images;
		
		NameMap() {images = new HashMap<String, NamedImage>();}
		
		void add(NamedImage obj)             {images.put(obj.name,   obj); if (obj.hasOldName())   images.put(obj.oldname,   obj);}
		void add(Name alias, NamedImage obj) {images.put(alias.name, obj); if (alias.hasOldName()) images.put(alias.oldname, obj);}
		
		boolean    has       (String name) {return images.containsKey(name);}
		NamedImage get       (String name) {return images.get(name);}
		
		boolean    isKind    (String name) {return images.get(name).getClass() == Kind.class;}
		Kind       getKind   (String name) {return (Kind)images.get(name);}
		
		boolean    isVariant (String name) {return images.get(name).getClass() == Variant.class;}
		Variant    getVariant(String name) {return (Variant)images.get(name);}
		
		@Override
		public Iterator<Entry<String, NamedImage>> iterator() {return images.entrySet().iterator();}
	}
	
	public class Page implements Iterable<Kind>
	{
		String                id;
		String                i18n;
		HashMap<String, Kind> kinds;
		
		protected Page(String id)
		{
			this.id    = id;
			this.i18n  = id;
			this.kinds = new HashMap<String, Kind>(); 
		}

		public String  getId()            {return id;}
		public String  getI18n()          {return i18n;}
		
		public boolean hasKind(String id) {return kinds.containsKey(id);}
		public Kind    getKind(String id) {return kinds.get(id);}
		
		Kind createKind(String id, Kind.Type type)
		{
			Kind out = kinds.get(id);
			if (out == null)
			{
				if (Tileset.this.names.has(id))
					out = Tileset.this.names.getKind(id);
				else
				{
					out = new Kind(id, type);
					Tileset.this.names.add(out);
				}
				kinds.put(id, out);
			}
			return out;
		}
		
		@Override
		public Iterator<Kind> iterator() {return kinds.values().iterator();}
	}
	
	public class Group implements Iterable<Page>
	{
		String id;
		String i18n;
		String icon;
		
		HashMap<String, AttrGroup> attrgroups;
		HashMap<String, Page>      pages;
		
		protected Group(String id)
		{
			this.id         = id;
			this.i18n       = id;
			this.icon       = "";
			this.attrgroups = new HashMap<String, AttrGroup>();
			this.pages      = new HashMap<String, Page>();
		}
		
		public String    getId()                 {return id;}
		public String    getI18n()               {return i18n;}
		public String    getIcon()               {return icon;}
		
		public Image getIconImage() throws MissingGroupIconException
		{
			Tileset.Image image = null;
			if (Tileset.this.hasKind(icon))
				image = Tileset.this.getKind(icon).getDefaultImage();
			else if (Tileset.this.hasVariant(icon))
				image = Tileset.this.getVariant(icon).getImage();
			if (image == null || image.getSprite() == null)
				throw new MissingGroupIconException(image == null ? icon : image.getFile(), id);
			return image;
		}
		
		public boolean   hasAttrGroup(String id) {return attrgroups.containsKey(id);}
		public AttrGroup getAttrGroup(String id) {return attrgroups.get(id);}
		
		public boolean   hasPage(String id)      {return pages.containsKey(id);}
		public Page      getPage(String id)      {return pages.get(id);}
		
		AttrGroup createAttrGroup(String id)
		{
			AttrGroup out = attrgroups.get(id);
			if (out == null)
			{
				out = new AttrGroup(id);
				attrgroups.put(id, out);
			}
			return out;
		}
		
		Page createPage(String id)
		{
			Page out = pages.get(id);
			if (out == null)
			{
				out = new Page(id);
				pages.put(id, out);
			}
			return out;
		}

		@Override
		public Iterator<Page> iterator() {return pages.values().iterator();}
	}
	
	String                 editor_version;
	String                 enigma_version;
	HashMap<String, Group> groups;
	NameMap                names;
	I18N                   i18n;
	
	public Tileset()
	{
		editor_version = "";
		enigma_version = "";
		groups = new HashMap<String, Group>();
		names  = new NameMap();
		i18n   = new I18N();
		i18n.put("", false);
	}
	
	public boolean    hasGroup(String id)   {return groups.containsKey(id);}
	public Group      getGroup(String id)   {return groups.get(id);}
	
	public boolean    has(String id)        {return names.has(id);}
	public NamedImage get(String id)        {return names.get(id);}
	
	public boolean    hasKind(String id)    {return names.has(id) && names.isKind(id);}
	public Kind       getKind(String id)    {return names.getKind(id);}
	
	public boolean    hasVariant(String id) {return names.has(id) && names.isVariant(id);}
	public Variant    getVariant(String id) {return names.getVariant(id);}
	
	public boolean    hasString(String id)  {return i18n.exists(id);}
	public KeyString  getString(String id)  {return i18n.get(id);}
	
	@Override public Iterator<Group> iterator() {return groups.values().iterator();}
	
	Group createGroup(String id)
	{
		Group out = groups.get(id);
		if (out == null)
		{
			out = new Group(id);
			groups.put(id, out);
		}
		return out;
	}
	
	/**
	 * Check, if the {@code <i18n>} section contains all referenced
	 * strings.
	 * Everything that has a textual representation within the user interface
	 * will be checked for a corresponding entry in the <code>&lt;i18n&gt;</code>
	 * section. On failure an instance of {@link MissingStringException} is thrown.
	 * 
	 * @throws MissingStringException  The value of an {@code i18n}
	 *             attribute or an implicit {@code i18n}-identifier is
	 *             missing from the {@code <i18n>} section of the
	 *             tile-set description file.
	 */
	public void check() throws MissingStringException
	{
		for (Group g : this)
		{
			if (!i18n.exists(g.i18n))
				throw new MissingStringException(g.i18n, g.id, "group");
			
			for (AttrGroup ag : g.attrgroups.values())
			{
				if (!i18n.exists(ag.i18n))
					throw new MissingStringException(ag.i18n, ag.id, "attrgroup");
				
				for (Attr a : ag)
				{
					if (!i18n.exists(a.i18n))
						throw new MissingStringException(a.i18n, a.name, "attribute");
				}
			}
			
			for (Page p : g)
			{
				if (!i18n.exists(p.i18n))
					throw new MissingStringException(p.i18n, p.id, "page");
				
				for (Kind k : p)
				{
					if (!k.hidden && !i18n.exists(k.i18n))
						throw new MissingStringException(k.i18n, k.name, "kind");
					
					for (Entry<String, Attr> a : k.attrs.attrs.entrySet())
					{
						if (a.getValue().ui == Attr.Ui.ATTR && !i18n.exists(a.getValue().i18n))
							throw new MissingStringException(a.getValue().i18n, a.getKey(), k.name, "kindattr");
					}
				}
			}
		}
	}
	
	/**
	 * Creates an instance for each {@link Image#sprite} through the given
	 * {@link SpriteSet} instance. 
	 * 
	 * @param spriteset  the {@link SpriteSet} implementation to use for image loading
	 * @throws MissingImageException  Indicates that an image file is missing.
	 */
	public void loadImages(SpriteSet spriteset) throws MissingImageException
	{
		Image img;
		for (Group g : this)
		{
			for (Page p : g)
			{
				for (Kind k : p)
				{
					img = k.image;
					while (img != null)
					{
						img.sprite = spriteset.get(img);
						if (img.sprite == null) throw new MissingImageException(img.file, k.name);
						img = img.stack;
					}
					
					for (Variants vs : k.stack)
					{
						for (Variant v : vs)
						{
							img = v.image;
							while (img != null)
							{
								img.sprite = spriteset.get(img);
								if (img.sprite == null) throw new MissingImageException(img.file, k.name);
								img = img.stack;
							}
						}
					}
					
					if (k.cluster != null)
					{
						for (Variant v : k.cluster)
						{
							img = v.image;
							while (img != null)
							{
								img.sprite = spriteset.get(img);
								if (img.sprite == null) throw new MissingImageException(img.file, k.name);
								img = img.stack;
							}
						}
					}
				}
			}
		}
	}
	
	private Image getVariantImage(Table table, Kind kind, Variants variants, Mode mode, ArrayList<String> defaultattrs)
	{
		final List<String> values = new ArrayList<String>(variants.getAttrs().size());
		final Iterator<String> def = defaultattrs.iterator();
		for (String attr : variants.getAttrs())
		{
			final String defattr = def.hasNext() ? def.next() : "nil";
			if (table.exist(attr))
			{
				final MMSimpleValue attrval = table.get(attr).checkSimple(mode);
				if (attrval == null || attrval.get(mode) == null)
					values.add("nil");
				else
					values.add(attrval.get(mode).toString_noquote());
			}
			else
			{
				values.add(defattr);
			}
		}
		final Variant variant = variants.getByAttrs(values);
		if (variant != null)
			return variant.image;
		else
			return kind.image;
	}
	
	/**
	 * Return the visual for the given tile-part as stack of images.
	 * 
	 * @param tile  tile-part constructor to gather the images for.
	 * @param mode  mode to use
	 * @return      a list of {@link Image}s used to fraw the given tile.
	 */
	public ArrayList<Image> getImages(TilePart.Construct tile, Mode mode)
	{
		final Table table = tile.checkTable().get(mode);
		if (table == null || !table.exist(1))
			return null;
		final SimpleValue name = table.get(1).checkSimple(mode).get(mode);
		if (name == null)
			return null;
		String namestr = name.value.tojstring();
		if (namestr.startsWith("#")) namestr = namestr.substring(1);
		if (namestr.equals("st_passage"))
			System.out.println("passage");
		final NamedImage image = names.get(namestr);
		if (image == null)
			return null;
		if (image instanceof Variant)
		{
			if (((Variant)image).val.isEmpty())
			{
				final ArrayList<Image> ret = new ArrayList<Image>(1);
				ret.add(((Variant)image).image);
				return ret;
			}
			
			final ArrayList<Image> ret = new ArrayList<Image>(((Variant)image).kind.stack.size());
			for (Variants variants : ((Variant)image).kind.stack)
			{
				ret.add(this.getVariantImage(table, ((Variant)image).kind, variants, mode, ((Variant)image).val));
			}
			return ret;
		}
		if (((Kind)image).stack.isEmpty())
		{
			final ArrayList<Image> ret = new ArrayList<Image>(1);
			ret.add(((Kind)image).getDefaultImage());
			return ret;
		}
		final ArrayList<Image> ret = new ArrayList<Image>(((Kind)image).stack.size());
		for (Variants variants : ((Kind)image).stack)
		{
			if (variants.attrs.isEmpty()) continue;
			ret.add(this.getVariantImage(table, (Kind)image, variants, mode, variants.getDefaultAttrs((Kind)image)));
		}
		if (ret.isEmpty())
			ret.add(((Kind)image).getDefaultImage());
		return ret;
	}
	
	/**
	 * Dump tile-set contents (for debugging purposes).
	 */
	public void dump() throws MissingStringException
	{
		Image img;
		for (Group g : this)
		{
			if (!i18n.exists(g.i18n))
				throw new MissingStringException(g.i18n, g.id, "group");
			System.out.println("GROUP " + g.id + " (" + i18n.english(g.i18n) + ")");
			
			
			for (AttrGroup ag : g.attrgroups.values())
			{
				if (!i18n.exists(ag.i18n))
					throw new MissingStringException(ag.i18n, ag.id, "attrgroup");
				System.out.println("\tATTRGROUP " + ag.id + " (" + i18n.english(ag.i18n) + ")");
				
				for (Attr a : ag)
				{
					if (!i18n.exists(a.i18n))
						throw new MissingStringException(a.i18n, a.name, "attribute");
					System.out.print("\t\tATTR " + a.name + " (" + i18n.english(a.i18n) + ") : " + a.type);
					if (a.defaultval != null)
						System.out.print(" = " + a.defaultval);
					System.out.println();
				}
			}
			
			for (Page p : g)
			{
				if (!i18n.exists(p.i18n))
					throw new MissingStringException(p.i18n, p.id, "page");
				System.out.println("\tPAGE " + p.id + " (" + i18n.english(p.i18n) + ")");
				
				for (Kind k : p)
				{
					if (k.hidden) continue;
					if (!i18n.exists(k.i18n))
						throw new MissingStringException(k.i18n, k.name, "kind");
					System.out.print("\t\tKIND " + k.name + (k.oldname != null ? "[" + k.oldname + "] " : " ") + "(" + i18n.english(k.i18n) + ")");
					if (k.image != null) System.out.print("; image = '" + k.image.file + "' " + (k.image.sprite != null ? "" : "!"));
					System.out.println(k.hidden ? " HIDDEN" : "");
					
					for (Entry<String, Attr> a : k.attrs.attrs.entrySet())
					{
						if (a.getValue().ui == Attr.Ui.ATTR)
						{
							if (!i18n.exists(a.getValue().i18n))
								throw new MissingStringException(a.getValue().i18n, a.getKey(), k.name, "kindattr");
							System.out.print("\t\t\tATTR " + a.getValue().name + " (" + i18n.english(a.getValue().i18n) + "): " + a.getValue().type);
						}
						else System.out.print("\t\t\tATTR " + a.getValue().name + ": " + a.getValue().type);
						if (a.getValue().defaultval != null)
							System.out.print(" = " + a.getValue().defaultval);
						System.out.println();
					}
					
					for (Variants vs : k.stack)
					{
						for (Variant v : vs)
						{
							System.out.print("\t\t\t\tVAR " + v.name);
							if (v.oldname != null) System.out.print("[" + v.oldname + "]:"); else System.out.print(":");
							img = v.image;
							while (img != null)
							{
								System.out.print(" " + img.file);
								if (img.sprite == null) System.out.print("!");
								img = img.stack;
							}
							System.out.println();
						}
					}
					
					if (k.cluster != null)
					{
						System.out.println("\t\t\t\tCLUSTER:");
						for (Variant v : k.cluster)
						{
							System.out.print("\t\t\t\t\tVAR " + v.name);
							if (v.oldname != null) System.out.print("[" + v.oldname + "]:"); else System.out.print(":");
							img = v.image;
							while (img != null)
							{
								System.out.print(" " + img.file);
								if (img.sprite == null) System.out.print("!");
								img = img.stack;
							}
							System.out.println();
						}
					}
				}
			}
		}
	}
}

