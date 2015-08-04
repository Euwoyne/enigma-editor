
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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import enigma_edit.lua.data.Mode2;
import enigma_edit.lua.data.SimpleValue;
import enigma_edit.lua.data.Table;
import enigma_edit.lua.data.TilePart;
import enigma_edit.model.I18N.KeyString;
import enigma_edit.error.MissingImageException;
import enigma_edit.error.MissingStringException;

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
	 * Identifier for object kinds and variants.
	 * Each kind has a unique name. A variant <i>may</i> have a name.
	 * And anything that has a name, may have an additional deprecated name
	 * which will be called the {@code oldname}.
	 */
	public static class Name
	{
		String name;
		String oldname;
		String i18n;
		
		Name(String name)           {this.name = name; this.oldname = null; this.i18n = name;}
		public boolean hasName()    {return name != null;}
		public String  getName()    {return name != null ? name : "";}
		public String  getI18n()    {return i18n;}
		public boolean hasOldName() {return oldname != null;}
		public String  getOldName() {return oldname != null ? oldname : "";}
	}
	
	/**
	 * General variant interface.
	 */
	public static interface Variant
	{
		Kind                getKind();
		String              getDefaultValue(String attrName);
		ArrayList<VarImage> getImage();
		ArrayList<VarImage> getImage(Table table, Mode2 mode);
	}
	
	/**
	 * Alias for a kind.
	 * An alias may provide different attribute default values.
	 */
	public static class Alias extends Name implements Variant
	{
		/** parent kind */
		final Kind kind;
		
		/** Alias dependent attribute default-values */
		TreeMap<String, String> attributes;
		
		Alias(String name, Kind parent)
		{
			super(name);
			this.kind = parent;
			this.attributes = new TreeMap<String, String>();
		}
		
		public Kind getKind() {return kind;}
		
		public String getDefaultValue(String attrName)
		{
			String out = attributes.get(attrName);
			return out == null ? kind.getDefaultValue(attrName) : out;
		}
		
		@Override
		public ArrayList<VarImage> getImage()
		{
			return kind.getImage(this);
		}
		
		@Override
		public ArrayList<VarImage> getImage(Table table, Mode2 mode)
		{
			final ArrayList<VarImage> stack = new ArrayList<VarImage>(kind.stack.size());
			for (Variants variants : kind)
			{
				VarImage var = variants.getImage(table, this, mode);
				if (var != null) stack.add(var);
			}
			return stack;
		}
	}
	
	/**
	 * Image location and sprite-data.
	 */
	public static class Image
	{
		String file;
		String text;
		int    x;
		int    y;
		
		public Image()                                       {this("",   "",   0, 0);}
		public Image(String file)                            {this(file, "",   0, 0);}
		public Image(String file, String text)               {this(file, text, 0, 0);}
		
		public Image(String file, String text, int x, int y)
		{
			this.file = file;
			this.text = text;
			this.x    = x;
			this.y    = y;
		}
		
		public String getFile()   {return file;}
		public String getText()   {return text;}
		public int    getX()      {return x;}
		public int    getY()      {return y;}
	}
	
	/**
	 * A stack of {@link Image images} with a {@link Name name}. 
	 */
	public static class NamedImage extends Name implements Renderable, Iterable<Image>
	{
		ArrayList<Image> images;
		Sprite           sprite;
		
		NamedImage(String name)
		{
			super(name);
			images = new ArrayList<Image>();
		}
		
		public Sprite getSprite() {return sprite;}
		
		@Override public void draw(RenderingAgent renderer, int x, int y)
		{
			renderer.draw(sprite, x, y);
		}
		
		@Override public Iterator<Image> iterator()
		{
			return images.iterator();
		}
	}
	
	/**
	 * A named image, that is mapped to a list of string values.
	 */
	public static class VarImage extends NamedImage implements Variant
	{
		/** kind kind */
		final Kind kind;
		
		/** Attribute value list, that is mapped to this variant */
		ArrayList<String> val;
		
		/** Existence of comparators (such as {@code <}, {@code >} or {@code =})
		 *  within the {@link #val variant values}. */
		boolean hasCompare;
		
		/** Special variant dependent attribute default values */
		TreeMap<String, String> attributes;
		
		VarImage(String name, Kind kind)
		{
			super(name);
			this.kind = kind;
			this.val = new ArrayList<String>();
			this.hasCompare = false;
			this.attributes = new TreeMap<String, String>();
		}
		
		void checkCompare()
		{
			hasCompare = false;
			for (String v : val)
			{
				if (v.isEmpty()) continue;
				switch (v.charAt(0))
				{
				case '>':
				case '<':
				case '=': hasCompare = true; break;
				}
			}
		}
		
		public Kind    getKind()       {return kind;}
		public boolean isNamed()       {return hasName();}
		public boolean hasComparison() {return hasCompare;}
		
		public String getDefaultValue(String attrName)
		{
			String out = attributes.get(attrName);
			return out == null ? kind.getDefaultValue(attrName) : out;
		}
		
		@Override
		public ArrayList<VarImage> getImage()
		{
			final ArrayList<VarImage> stack = new ArrayList<VarImage>(1);
			stack.add(this);
			return stack;
		}
		
		@Override
		public ArrayList<VarImage> getImage(Table table, Mode2 mode)
		{
			final ArrayList<VarImage> stack = new ArrayList<VarImage>(kind.stack.size());
			for (Variants variants : kind)
			{
				VarImage var = variants.getImage(table, this, mode);
				if (var != null) stack.add(var);
			}
			return stack;
		}
	}
	
	/**
	 * Variant with cluster data.
	 * This provides an image for each connection configuration.
	 */
	public static class Cluster extends VarImage
	{
		public static final int WEST  = 1;
		public static final int SOUTH = 2;
		public static final int EAST  = 4;
		public static final int NORTH = 8;
		
		/**
		 * Calculate the image index from a {@code connections} string.
		 * @param connections  value of the {@code connections} attribute.
		 * @return             index of the corresponding image in the {@link connect} array.
		 */
		public static int getIndex(String connections)
		{
			int idx = 0;
			for (int i = 0; i < connections.length(); ++i)
			{
				switch (connections.charAt(i))
				{
				case 'w': idx |= WEST;  break;
				case 's': idx |= SOUTH; break;
				case 'e': idx |= EAST;  break;
				case 'n': idx |= NORTH; break;
				default:  return -1;
				}
			}
			return idx;
		}
		
		/**
		 * Get the {@code connections} string from the image index.
		 * @param index  index of an image in the {@link connect} array.
		 * @return       connections attribute value for the corresponding object.
		 */
		public static String getConnections(int index)
		{
			String out = "";
			if ((index & WEST)  != 0) out = out + 'w';
			if ((index & SOUTH) != 0) out = out + 's';
			if ((index & EAST)  != 0) out = out + 'e';
			if ((index & NORTH) != 0) out = out + 'n';
			return out;
		}
		
		/** one image for each possible connection configuration */
		NamedImage[] connect;
		
		Cluster(String name, Kind parent)
		{
			super(name, parent);
			connect = new NamedImage[16];
			connect[0] = this;
		}
		
		void set(String connections, NamedImage image)
		{
			connect[getIndex(connections)] = image;
		}
		
		NamedImage get(String connections)
		{
			return connect[getIndex(connections)];
		}
	}
	
	/**
	 * Variant list (with attribute dependency information).
	 */
	public static class Variants extends ArrayList<VarImage>
	{
		private static final long serialVersionUID = 1L;
		
		ArrayList<String>         attrs;
		TreeMap<String, VarImage> valmap;
		TreeMap<String, VarImage> namemap;
		String                    defaultName;
		boolean                   showAll;
		boolean                   hasCompare;
		
		/**
		 * Constructs a variant set for the specified kind.
		 * @param parent  The variants parent kind. The default variant name will be initialized to the kind's name.
		 */
		public Variants(Kind parent)
		{
			attrs = new ArrayList<String>();
			valmap = new TreeMap<String, VarImage>();
			namemap = new TreeMap<String, VarImage>();
			defaultName = null;
			showAll = false;
			hasCompare = false;
		}
		
		/**
		 * Check, if this kind's variants depend on attribute values.
		 * If they do not, they will always be used by means of their names.
		 * 
		 * @return  {@code true}, if these variants do not depend on attribute values.
		 */
		public boolean isAttributeIndependent() {return attrs.isEmpty();}
		
		@Override
		public boolean add(VarImage variant)
		{
			if (!this.isEmpty() && variant.kind != this.get(0).kind)
				throw new IllegalArgumentException("Unable to add variant for kind '" + variant.kind.getName() + "' to kind '" + this.get(0).kind.getName() + "'");
			
			if (!attrs.isEmpty())
				valmap.put(String.join(",", variant.val), variant);
			
			if (variant instanceof Name)
			{
				if (((Name)variant).hasName())
					namemap.put(((Name)variant).name, variant);
				if (((Name)variant).hasOldName())
					namemap.put(((Name)variant).oldname, variant);
			}
			
			hasCompare |= variant.hasCompare;
			
			return super.add(variant);
		}
		
		/**
		 * Get the variant corresponding to the given attribute value list.
		 * 
		 * @param values  List of values of the attributes declared in {@link attrs}.
		 * @return        Variant, corresponding to the given values.
		 */
		private VarImage getImage(ArrayList<String> values)
		{
			if (hasCompare)
			{
				boolean found;
				for (VarImage variant : this)
				{
					found = true;
					for (int i = 0; i < variant.val.size() && found; ++i)
					{
						try
						{
							switch (variant.val.get(i).charAt(0))
							{
							case '<': found &= Double.parseDouble(variant.val.get(i).substring(1)) > Double.parseDouble(values.get(i)); break;
							case '>': found &= Double.parseDouble(variant.val.get(i).substring(1)) < Double.parseDouble(values.get(i)); break;
							case '=': found &= variant.val.get(i).regionMatches(1, values.get(i), 0, values.get(i).length()); break;
							default:  found &= variant.val.get(i).equals(values.get(i)); break;
							}
						}
						catch (NumberFormatException e)
						{
							found = false;
							break;
						}
					}
					if (found) return variant;
				}
				System.err.println("Unable to resolve variant '" + String.join(",", values) + "' for kind '" + this.get(0).kind.getName() + "'");
				return null;
			}
			else
			{
				final VarImage var = valmap.get(String.join(",", values));
				if (var == null)
					System.err.println("Unable to resolve variant '" + String.join(",", values) + "' for kind '" + this.get(0).kind.getName() + "'");
				return var;
			}
		}
		
		
		/**
		 * Get the default variant.
		 * For attribute independent variants, the default variant is determined
		 * by the {@code defaultName} field, if present. If this field is empty,
		 * the default variant is the one, that is named like the parent kind.
		 * In case of attribute dependency, the default variant is defined by
		 * the attributes default values.
		 * 
		 * @return The default variant.
		 */
		public VarImage getImage()
		{
			if (this.isEmpty())      return null;
			if (this.size() == 1)    return this.get(0);
			if (defaultName != null) return namemap.get(defaultName);
			if (attrs.isEmpty())     return namemap.get(this.get(0).kind.name);
			
			final TreeMap<String, Attribute> kindattrs = this.get(0).kind.attributes;
			final ArrayList<String>          values    = new ArrayList<String>(attrs.size());
			
			for (String attrname : attrs)
			{
				final Attribute attr = kindattrs.get(attrname);
				if      (attr == null)                  values.add("nil");
				else if (attr.defaultValue.equals("*")) values.add(attr.getEnum((int)(Math.random() * attr.enumSize())));
				else                                    values.add(attr.defaultValue);
			}
			
			return this.getImage(values);
		}
		
		/**
		 * Get the kind variant for the given alias.
		 * 
		 * @return The default variant.
		 */
		public VarImage getImage(Alias alias)
		{
			if (this.isEmpty())      return null;
			if (this.size() == 1)    return this.get(0);
			if (defaultName != null) return namemap.get(defaultName);
			
			final TreeMap<String, Attribute> kindattrs = this.get(0).kind.attributes;
			final ArrayList<String>          values    = new ArrayList<String>(attrs.size());
			
			for (String attrname : attrs)
			{
				String attrval = alias.getDefaultValue(attrname);
				if (attrval == null)
				{
					final Attribute attr = kindattrs.get(attrname);
					if      (attr == null)                  values.add("nil");
					else if (attr.defaultValue.equals("*")) values.add(attr.getEnum((int)(Math.random() * attr.enumSize())));
					else                                    values.add(attr.defaultValue);
				}
				else if (attrval.equals("*"))
				{
					final Attribute attr = kindattrs.get(attrname);
					values.add(attr == null ? "*" : attr.getEnum((int)(Math.random() * attr.enumSize())));
				}
				else values.add(attrval);
			}
			
			return this.getImage(values);
		}
		
		/**
		 * Get the kind variant as declared by the given table.
		 * 
		 * @param table  Lua object declaration.
		 * @param mode   Mode to resolve the attributes for.
		 * @return       The variant specified by the object declaration.
		 */
		public VarImage getImage(Table table, Variant base, Mode2 mode)
		{
			if (this.isEmpty())      return null;
			if (this.size() == 1)    return this.get(0);
			if (defaultName != null) return namemap.get(defaultName);
			
			final TreeMap<String, Attribute> kindattrs = this.get(0).kind.attributes;
			final ArrayList<String>          values    = new ArrayList<String>(attrs.size());
			
			for (String attrname : attrs)
			{
				final SimpleValue attr = (table.exist(attrname)) ? table.get(attrname).checkSimple(mode) : null;
				final String attrval = (attr != null ? attr.toString_noquote() : base.getDefaultValue(attrname));
				if (attrval == null)
				{
					final Attribute kindattr = kindattrs.get(attrname);
					if      (kindattr == null)                  values.add("nil");
					else if (kindattr.defaultValue.equals("*")) values.add(kindattr.getEnum((int)(Math.random() * kindattr.enumSize())));
					else                                        values.add(kindattr.defaultValue);
				}
				else if (attrval.equals("*"))
				{
					final Attribute kindattr = kindattrs.get(attrname);
					values.add(kindattr == null ? "*" : kindattr.getEnum((int)(Math.random() * kindattr.enumSize())));
				}
				else values.add(attrval);
			}
			
			return this.getImage(values);
		}
	}
	
	/**
	 * Description of an attribute, that is available for a kind.
	 * This metadata includes type and name of the attribute as well as
	 * information regarding the display in the user interface. 
	 */
	public static class Attribute
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
			
			/** is a string that can serve as an identifier in lua.         */ TOKEN,
			
			/** is either a Lua table with two numbers or an identifier of a
			 *  positioned object or an instance of the Enigma API type
			 *  {@code po} (which can be created by one of the previous).   */ POSITION,
		    
			/** is essentially an {@code integer} but is represented by
			 *  an enumerated list of integer constants for the four main
			 *  directions.                                                 */ DIRECTION,
			
			/** is an {@code integer} at the core with a specific set of
			 *  value constants.                                            */ DIRECTION8,
			
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
		String            defaultValue;
		Double            min, max;
		String            i18n;
		Ui                ui;
		
		/**
		 * Create a new attribute.
		 * 
		 * @param name  Name of the attribute.
		 * @param type  Type of the attribute.
		 * @param ui    User interface type.
		 */
		Attribute(String name, Type type, Ui ui)
		{
			this.name         = name;
			this.type         = type;
			this.enums        = new ArrayList<String>(); 
			this.defaultValue = null;
			this.min          = null;
			this.max          = null;
			this.i18n         = name;
			this.ui           = ui;
		}
		
		/**
		 * Create a new integer attribute with default user interface.
		 * 
		 * @param name  Name of the attribute.
		 */
		Attribute(String name) {this(name, Type.INTEGER, Ui.ATTR);}
		
		public String  getName()       {return name;}
		public Type    getType()       {return type;}
		public int     enumSize()      {return enums.size();}
		public String  getEnum(int i)  {return enums.get(i);}
		public String  getDefault()    {return defaultValue;}
		public String  getI18n()       {return i18n;}
		public Ui      getUi()         {return ui;}
	}
	
	/**
	 * Enigma object kind.
	 */
	public static class Kind extends Name implements Iterable<Variants>, Variant
	{
		/** object type */
		public enum Type {AC, FL, IT, ST};
		
		/** user access mode */
		public enum Access {KIND, ALIAS, HIDDEN};
		
		/** object type */
		Type                       type;
		
		/** all aliases */
		ArrayList<Alias>           alias;
		
		/** variant stack */
		ArrayList<Variants>        stack;
		
		/** attribute declarations */
		TreeMap<String, Attribute> attributes;
		
		/** message names */
		TreeSet<String>            messages;
		
		/** special sprite-set icon */
		NamedImage                 icon;
		
		/** user accessibility */
		Access                     access;
		
		/** framed floor existence */
		boolean                    framed;
		
		Kind(String name, Type type)
		{
			super(name);
			this.type       = type;
			this.alias      = new ArrayList<Alias>();
			this.stack      = new ArrayList<Variants>();
			this.attributes = new TreeMap<String, Attribute>();
			this.messages   = new TreeSet<String>();
			this.icon       = null;
			this.access     = Access.KIND;
			this.framed     = false;
		}
		
		@Override public Iterator<Variants> iterator() {return stack.iterator();}
		
		public List<Alias> getAliases()  {return Collections.unmodifiableList(alias);}
		public Type        getType()     {return type;}
		public boolean     isHidden()    {return access == Access.HIDDEN;}
		public boolean     showAliases() {return access == Access.ALIAS;}
		public boolean     hasIcon()     {return icon != null;}
		public NamedImage  getIcon()     {return icon;}

		@Override
		public Kind getKind()
		{
			return this;
		}
		
		@Override
		public String getDefaultValue(String attrName)
		{
			final Attribute attr = attributes.get(attrName);
			if (attr == null) return "nil";
			return attr.defaultValue;
		}
		
		public ArrayList<VarImage> getImage()
		{
			final ArrayList<VarImage> stack = new ArrayList<VarImage>(this.stack.size());
			for (Variants variants : this)
			{
				VarImage var = variants.getImage();
				if (var != null) stack.add(var);
			}
			return stack;
		}
		
		public ArrayList<VarImage> getImage(Alias alias)
		{
			final ArrayList<VarImage> stack = new ArrayList<VarImage>(this.stack.size());
			for (Variants variants : this)
			{
				VarImage var = variants.getImage(alias);
				if (var != null) stack.add(var);
			}
			return stack;
		}
		
		public ArrayList<VarImage> getImage(Table table, Variant base, Mode2 mode)
		{
			final ArrayList<VarImage> stack = new ArrayList<VarImage>(this.stack.size());
			for (Variants variants : this)
			{
				VarImage var = variants.getImage(table, base, mode);
				if (var != null) stack.add(var);
			}
			return stack;
		}
		
		@Override
		public ArrayList<VarImage> getImage(Table table, Mode2 mode)
		{
			final ArrayList<VarImage> stack = new ArrayList<VarImage>(this.stack.size());
			for (Variants variants : this)
			{
				VarImage var = variants.getImage(table, this, mode);
				if (var != null) stack.add(var);
			}
			return stack;
		}
	}
	
	/**
	 * Page of kinds (as parts of a group).
	 */
	public static class Page extends LinkedList<Kind>
	{
		private static final long serialVersionUID = 1L;
		String i18n;
	}
	
	/**
	 * Kind group.
	 * As collection of pages.
	 */
	public static class Group extends LinkedList<Page>
	{
		/**
		 * Attribute group.
		 * This is a set of attributes shared by all objects of this group.
		 */
		public static class Attributes extends ArrayList<Attribute>
		{
			private static final long serialVersionUID = 1L;
			String i18n;
		}
		
		private static final long serialVersionUID = 1L;
		
		String                i18n;
		String                icon;
		ArrayList<Attributes> attributeGroups;
		
		public Group()
		{
			attributeGroups = new ArrayList<Attributes>();
		}
		
		public String getI18n() {return i18n;}
		public String getIcon() {return icon;}
	}
	
	/**
	 * Map of all named objects.
	 * The interface makes sure, that only {@link Kind Kinds} and {@link Variant Variants}
	 * are registered. Thus either {@link NameMap#isKind(String)} or {@link NameMap#isVariant(String)}
	 * returns {@code true}.
	 */
	private static class NameMap
	{
		private TreeMap<String, Kind>     kindMap;
		private TreeMap<String, Alias>    aliasMap;
		private TreeMap<String, VarImage> variantMap;
		
		NameMap()
		{
			kindMap    = new TreeMap<String, Kind>();
			aliasMap   = new TreeMap<String, Alias>();
			variantMap = new TreeMap<String, VarImage>();
		}
		
		/**
		 * Register miscellaneous named object.
		 * This links the {@link Name#name name} and the {@link Name#oldname old name}
		 * to the given object.
		 * 
		 * @param obj  Object to be registered.
		 */
		void add(VarImage obj)
		{
			if (obj.hasName())
				variantMap.put(((Name)obj).name, obj);
			if (obj.hasOldName())
				variantMap.put(obj.oldname, obj);
		}
		
		/**
		 * Register a variant with an alias name.
		 * Links the {@link Name#name name} and the {@link Name#oldname old name} given
		 * by {@code alias} to the object {@code obj}. This is primarily used to link
		 * cluster-parts to the respective cluster instead of itself.
		 * 
		 * @param obj    Object to be registered.
		 * @param alias  Name the object is registered with.
		 */
		void add(VarImage obj, Name alias)
		{
			if (alias.hasName())
				variantMap.put(alias.name, obj);
			if (alias.hasOldName())
				variantMap.put(alias.oldname, obj);
		}
		
		/**
		 * Register kind.
		 * This links the {@link Name#name name} and the {@link Name#oldname old name}
		 * of the given kind, as well as all its {@link Kind#alias aliases} to the given
		 * object.
		 * 
		 * @param kind  Kind to be registered.
		 */
		void add(Kind kind)
		{
			if (kind.hasName())
				kindMap.put(kind.name, kind);
			if (kind.hasOldName())
				kindMap.put(kind.oldname, kind);
			if (kind.framed)
				kindMap.put(kind.name + "_framed", kind);
			
			for (Alias alias : kind.alias)
			{
				if (alias.attributes.isEmpty())
				{
					if (alias.hasName())
						kindMap.put(alias.name, kind);
					if (alias.hasOldName())
						kindMap.put(alias.oldname, kind);
				}
				else
				{
					if (alias.hasName())
						aliasMap.put(alias.name, alias);
					if (alias.hasOldName())
						aliasMap.put(alias.oldname, alias);
				}
			}
		}
		
		boolean  has     (String name) {return kindMap.containsKey(name) || aliasMap.containsKey(name) || variantMap.containsKey(name);}
		Variant  get     (String name) {return kindMap.containsKey(name) ? kindMap.get(name) : (aliasMap.containsKey(name) ? aliasMap.get(name) : variantMap.get(name));}
		
		boolean  isKind  (String name) {return kindMap.containsKey(name);}
		Kind     getKind (String name) {return kindMap.get(name);}
		
		boolean  isAlias (String name) {return aliasMap.containsKey(name);}
		Alias    getAlias(String name) {return aliasMap.get(name);}
		
		boolean  isImage (String name) {return variantMap.containsKey(name);}
		VarImage getImage(String name) {return variantMap.get(name);}
	}
	
	
	/*
	 * Tileset Members.
	 */
	
	/** target editor version */
	private final String     editorVer;
	
	/** target Enigma version */
	private final String     enigmaVer;
	
	/** Group list. */
	private ArrayList<Group> groups;
	
	/** Name map of all named objects */
	private NameMap          names;
	
	/** internationalization strings */
	private I18N             i18n;
	
	/**
	 * Default constructor.
	 */
	Tileset(String editorVer, String enigmaVer)
	{
		this.editorVer = editorVer;
		this.enigmaVer = enigmaVer;
		this.groups = new ArrayList<Group>();
		this.names  = new NameMap();
		this.i18n   = new I18N();
	}
	
	public String     getEditorVersion()    {return editorVer;}
	public String     getEnigmaVersion()    {return enigmaVer;}
	public I18N       getI18n()             {return i18n;}
	
	public boolean    has(String id)        {return names.has(id);}
	public Variant    get(String id)        {return names.get(id);}
	
	public boolean    hasKind(String id)    {return names.isKind(id);}
	public Kind       getKind(String id)    {return names.getKind(id);}
	
	public boolean    hasAlias(String id)   {return names.isAlias(id);}
	public Alias      getAlias(String id)   {return names.getAlias(id);}
	
	public boolean    hasImage(String id)   {return names.isImage(id);}
	public VarImage   getImage(String id)   {return names.getImage(id);}
	
	public boolean    hasString(String id)  {return i18n.exists(id);}
	public KeyString  getString(String id)  {return i18n.get(id);}
	
	/**
	 * Add a new group to the tile-set.
	 * @return  The newly created group instance.
	 */
	Group createGroup()
	{
		Group group = new Group();
		groups.add(group);
		return group;
	}
	
	@Override
	public Iterator<Group> iterator()
	{
		return groups.iterator();
	}
	
	/**
	 * Set up the name map.
	 */
	void registerNames()
	{
		this.names = new NameMap();
		for (Group group : groups)
		{
			for (Page page : group)
			{
				for (Kind kind : page)
				{
					names.add(kind);
					for (Variants variants : kind.stack)
					{
						for (VarImage variant : variants)
						{
							names.add(variant);
							if (variant instanceof Cluster)
							{
								for (NamedImage connect : ((Cluster)variant).connect)
								{
									names.add(variant, connect);	// cluster resolving takes place elsewhere
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Creates a sprite for each {@link Image} instance through the given
	 * {@link SpriteSet}. 
	 * 
	 * @param spriteset  the {@link SpriteSet} implementation to use for image loading
	 * @throws MissingImageException  Indicates that an image file is missing.
	 */
	public void loadSprites(SpriteSet spriteset) throws MissingImageException
	{
		for (Group group : groups)
		{
			for (Page page : group)
			{
				for (Kind kind : page)
				{
					for (Variants variants : kind.stack)
					{
						for (VarImage variant : variants)
						{
							variant.sprite = spriteset.get(variant);
							
							if (variant instanceof Cluster)
							{
								for (NamedImage connect : ((Cluster)variant).connect)
								{
									connect.sprite = spriteset.get(connect);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Check, if the {@code <i18n>} section contains all referenced
	 * strings.
	 * Everything that has a textual representation within the user interface
	 * will be checked for a corresponding entry in the {@code <i18n>}
	 * section. On failure an instance of {@link MissingStringException} is thrown.
	 * 
	 * @throws MissingStringException  The value of an {@code i18n}
	 *             attribute or an implicit {@code i18n}-identifier is
	 *             missing from the {@code <i18n>} section of the
	 *             tile-set description file.
	 */
	public void check() throws MissingStringException
	{
		for (Group group : groups)
		{
			if (!i18n.exists(group.i18n))
				throw new MissingStringException(group.i18n, "group");
			
			for (Group.Attributes attrgroup : group.attributeGroups)
			{
				if (!i18n.exists(attrgroup.i18n))
					throw new MissingStringException(attrgroup.i18n, "attrgroup");
				
				for (Attribute attribute : attrgroup)
				{
					if (!i18n.exists(attribute.i18n))
						throw new MissingStringException(attribute.i18n, attribute.name, "attribute");
				}
			}
			
			for (Page page : group)
			{
				if (page.i18n != null && !i18n.exists(page.i18n))
					throw new MissingStringException(page.i18n, "page");
				
				for (Kind kind : page)
				{
					if (!kind.isHidden() && !i18n.exists(kind.i18n))
						throw new MissingStringException(kind.i18n, kind.name, "kind");
					
					for (Attribute attribute : kind.attributes.values())
					{
						if (attribute.ui == Attribute.Ui.ATTR && !i18n.exists(attribute.i18n))
							throw new MissingStringException(attribute.i18n, attribute.name, kind.name, "kindattr");
					}
				}
			}
		}
	}
	
	/**
	 * Resolves a {@link Table lua table} to the {@link Variant} as declared by this tileset.
	 * 
	 * @param construct  Tile constructor to be resolved.
	 * @param mode       Mode to use for resolving.
	 * @return           The corresponding variant (or stack of variants).
	 */
	public ArrayList<VarImage> resolve(TilePart.Construct construct, Mode2 mode)
	{
		final Table table = construct.checkTable(mode);
		final SimpleValue kind = table.exist(1) ? table.get(1).checkSimple(mode) : null;
		if (kind == null) return null;
		
		String name = kind.toString_noquote();
		if (name.startsWith("#")) name = name.substring(1);
		Variant obj = names.get(name);
		
		if (obj == null)
		{
			System.err.println("Error: unable to find kind '" + kind.toString_noquote() + "'");
			return new ArrayList<VarImage>();
		}
		return obj.getImage(table, mode);
	}
	
	/**
	 * Dump tile-set contents (for debugging purposes).
	 * 
	 * @throws MissingStringException  The value of an {@code i18n}
	 *             attribute or an implicit {@code i18n}-identifier is
	 *             missing from the {@code <i18n>} section of the
	 *             tile-set description file.
	 */
	public void dump() throws MissingStringException
	{
		for (Group group : this)
		{
			if (!i18n.exists(group.i18n))
				throw new MissingStringException(group.i18n, "group");
			System.out.println("GROUP " + group.i18n + " (" + i18n.english(group.i18n) + ")");
			
			for (Group.Attributes attrgroup : group.attributeGroups)
			{
				if (!i18n.exists(attrgroup.i18n))
					throw new MissingStringException(attrgroup.i18n, "attrgroup");
				System.out.println("\tATTRGROUP " + attrgroup.i18n + " (" + i18n.english(attrgroup.i18n) + ")");
				
				for (Attribute attribute : attrgroup)
				{
					if (!i18n.exists(attribute.i18n))
						throw new MissingStringException(attribute.i18n, attribute.name, "attribute");
					System.out.print("\t\tATTR " + attribute.name + " (" + i18n.english(attribute.i18n) + ") : " + attribute.type);
					if (attribute.defaultValue != null)
						System.out.print(" = " + attribute.defaultValue);
					System.out.println();
				}
			}
			
			for (Page page : group)
			{
				if (page.i18n != null)
				{
					if (!i18n.exists(page.i18n))
						throw new MissingStringException(page.i18n, "page");
					System.out.println("\tPAGE " + page.i18n + " (" + i18n.english(page.i18n) + ")");
				}
				
				for (Kind kind : page)
				{
					if (!kind.isHidden() && !i18n.exists(kind.i18n))
						throw new MissingStringException(kind.i18n, kind.name, "kind");
					System.out.print("\t\tKIND " + kind.name + (kind.oldname != null ? "[" + kind.oldname + "] " : " ") + "(" + i18n.english(kind.i18n) + ")");
					System.out.println(kind.isHidden() ? " HIDDEN" : "");
					
					for (Attribute attribute : kind.attributes.values())
					{
						if (attribute.ui == Attribute.Ui.ATTR)
						{
							if (!i18n.exists(attribute.i18n))
								throw new MissingStringException(attribute.i18n, attribute.name, kind.name, "kindattr");
							System.out.print("\t\t\tATTR " + attribute.name + " (" + i18n.english(attribute.i18n) + "): " + attribute.type);
						}
						else System.out.print("\t\t\tATTR " + attribute.name + ": " + attribute.type);
						if (attribute.defaultValue != null)
							System.out.print(" = " + attribute.defaultValue);
						System.out.println();
					}
					
					for (Variants variants : kind.stack)
					{
						for (VarImage variant : variants)
						{
							if (variant instanceof Cluster)
							{
								final Cluster cluster = (Cluster)variant;
								System.out.print("\t\t\t\tCLUSTER " + cluster.name);
								if (variant.oldname != null) System.out.println("[" + cluster.oldname + "]:"); else System.out.println(":");
								for (int c = 0; c < cluster.connect.length; ++c)
								{
									System.out.print("\t\t\t\t\t{" + Cluster.getConnections(c) + "}: " + cluster.connect[c].name);
									if (cluster.connect[c].oldname != null) System.out.print("[" + cluster.connect[c].oldname + "]: "); else System.out.print(": ");
									for (Image image : cluster.connect[c])
									{
										System.out.print(" " + image.file);
									}
									if (cluster.connect[c].sprite == null) System.out.print("!");
									System.out.println();
								}
							}
							else
							{
								System.out.print("\t\t\t\tVAR " + variant.name);
								if (variant.oldname != null) System.out.print("[" + variant.oldname + "]:"); else System.out.print(":");
								for (Image image : variant)
								{
									System.out.print(" " + image.file);
								}
								if (variant.sprite == null) System.out.print("!");
								System.out.println();
							}
						}
					}
				}
			}
		}
	}
}

