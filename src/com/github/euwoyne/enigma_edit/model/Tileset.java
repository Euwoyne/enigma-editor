
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.github.euwoyne.enigma_edit.error.MissingImageException;
import com.github.euwoyne.enigma_edit.error.MissingStringException;
import com.github.euwoyne.enigma_edit.lua.ReverseIDProvider;
import com.github.euwoyne.enigma_edit.lua.data.Mode2;
import com.github.euwoyne.enigma_edit.lua.data.ObjectDecl;
import com.github.euwoyne.enigma_edit.model.I18N.KeyString;

/**
 * Collection of all available kinds of objects.
 * This class provides the data, necessary for the correct
 * rendering of all kinds of Enigma-objects, as well as the
 * necessary data for attribute and message editing.
 * The actual data will normally be provided by an XML-
 * description file, that is parsed by an instance of
 * {@link TilesetReader}.
 */
public class Tileset implements ReverseIDProvider, Iterable<Tileset.Group>
{
	/**
	 * Interface for named entities, i.e. kinds, named variants and aliases.
	 * Each kind has a unique name and optionally several aliases. A variant
	 * <i>might</i> have a name.
	 * And anything that has a name, may have an additional deprecated name
	 * which will be called the {@code oldname}.
	 */
	public static interface Named
	{
		/**
		 * Check, if this named entity has a name.
		 * @return  {@code true}, if the name is set.
		 */
		boolean hasName();
		
		/**
		 * This entity's name.
		 * @return  Associated name string.
		 */
		String  getName();
		
		/**
		 * Keyword for name translation.
		 * @return  Key within the tileset's {@link I18n} dictionary.
		 */
		String  getI18n();
		
		/**
		 * Check, if this named entity has a deprecated (i.e. pre-1.0) name.
		 * @return  {@code true}, if the old name is set.
		 */
		boolean hasOldName();
		
		/**
		 * Deprecated (i.e. pre-1.0) name of this entity.
		 * @return  Associated deprecated name.
		 */
		String  getOldName();
	}
	
	/**
	 * Interface for any entity that defines attribute values (i.e. kinds,
	 * variants and aliases).
	 * Often different entities declare different default values for certain
	 * attributes, that are used, when the user does not specify a value. These
	 * share this interface.
	 */
	public static interface AttributeProvider
	{
		/**
		 * Get the value provided for the given attribute.
		 * @param attrName  Attribute name.
		 * @return          The attribute's (most often default) value.
		 */
		String  getAttribute(String attrName);
		
		/**
		 * Check, if this instance provides any value for the given attribute.
		 * This returns {@code true}, if (and only if) {@link getAttribute}
		 * returns a value.
		 * 
		 * @param attrName  Attribute name.
		 * @return          {@code true}, if a default value is declared.
		 */
		boolean hasAttribute(String attrName);
	}
	
	/**
	 * An object is defined by its kind and its attributes.
	 * This interface makes those easily accessible.
	 */
	public static interface ObjectProvider extends AttributeProvider
	{
		/**
		 * Get the parent kind.
		 * @return  The kind that is parent to this attribute providing structure.
		 */
		Kind getKind();
		
		/**
		 * Get the name of the object's kind.
		 * @return  Name of the kind.
		 */
		String getKindName();
		
		List<Variant>      getVariant();
		List<VariantImage> getImage();
	}
	
	/**
	 * Identifier data for kinds, named variants and aliases.
	 * This simply implements the {@link Named} interface.
	 */
	public static class Name implements Named
	{
		/** name */
		String name;
		
		/** pre-1.0 name */
		String oldname;
		
		/** keyword for i18n data */
		String i18n;
		
		Name(String name)           {this.name = name; this.oldname = null; this.i18n = name;}
		public boolean hasName()    {return name != null;}
		public String  getName()    {return name != null ? name : "";}
		public String  getI18n()    {return i18n;}
		public boolean hasOldName() {return oldname != null;}
		public String  getOldName() {return oldname != null ? oldname : "";}
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
		
		public Image()                         {this("",   "",   0, 0);}
		public Image(String file)              {this(file, "",   0, 0);}
		public Image(String file, String text) {this(file, text, 0, 0);}
		
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
	 * Stack of {@link Image images}.
	 * This is, what a resolved variant will be represented as. 
	 */
	public static class VariantImage implements ResizeRenderable, Iterable<Image>
	{
		ArrayList<Image> images;
		Sprite           sprite;
		
		VariantImage()
		{
			images = new ArrayList<Image>();
		}
		
		public Sprite getSprite() {return sprite;}
		
		@Override public Iterator<Image> iterator()
		{
			return images.iterator();
		}
		
		@Override public void draw(RenderingAgent renderer, int x, int y, int size) throws MissingImageException
		{
			sprite.draw(renderer, x, y, size);
		}
	}
	
	/**
	 * Variant image with cluster data.
	 * This provides an image for each connection configuration.
	 */
	public static class ClusterImage extends VariantImage
	{
		// direction bits
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
		VariantImage[] connect;
		
		/** Default constructor. */
		ClusterImage()
		{
			connect = new VariantImage[16];
			connect[0] = this;
		}
		
		void set(String connections, VariantImage image)
		{
			connect[getIndex(connections)] = image;
		}
		
		VariantImage get(String connections)
		{
			return connect[getIndex(connections)];
		}
	}
	
	/**
	 * Abstract base for all variants.
	 * This class provides the variant's image and its parent kind.
	 * Furthermore each variant may alter the default values of the kind's
	 * attributes.
	 */
	public static abstract class Variant implements ObjectProvider
	{
		/** parent kind */
		final Kind kind;
		
		/** variant image */
		VariantImage image; 
		
		/** Special variant dependent attribute default values */
		TreeMap<String, String> attributes;
		
		public Variant(Kind kind)
		{
			this.kind = kind;
			this.image = new VariantImage();
			this.attributes = new TreeMap<String, String>();
		}
		
		/**
		 * Variant type check.
		 * @return  Equivalent to {@code this instanceof NamedVariant}.
		 * @see NamedVariant, AttributeVariant
		 */
		public abstract boolean isNamed();
		
		@Override
		public Kind getKind()
		{
			return kind;
		}
		
		@Override
		public String getAttribute(String attrName)
		{
			String out = attributes.get(attrName);
			return out == null ? kind.getAttribute(attrName) : out;
		}
		
		@Override
		public boolean hasAttribute(String attrName)
		{
			return attributes.containsKey(attrName) || kind.hasAttribute(attrName);
		}
		
		@Override public String                  getKindName() {return kind.getName();}
		@Override public ArrayList<Variant>      getVariant()  {return kind.getVariant(this);}
		@Override public ArrayList<VariantImage> getImage()    {return kind.getImage(this);}
	}
	
	/**
	 * A named variant in an attribute independent variant list.
	 */
	public static class NamedVariant extends Variant implements Named
	{
		/** variant name */
		final Name name;
		
		public NamedVariant(String name, Kind kind)
		{
			super(kind);
			this.name = new Name(name);
		}
		
		@Override public boolean isNamed()    {return true;}
		@Override public boolean hasName()    {return name.hasName();}
		@Override public String  getName()    {return name.getName();}
		@Override public String  getI18n()    {return name.getI18n();}
		@Override public boolean hasOldName() {return name.hasOldName();}
		@Override public String  getOldName() {return name.getOldName();}
	}
	
	/**
	 * An attribute dependent variant.
	 */
	public static class AttributeVariant extends Variant
	{
		/** Attribute value list, that is mapped to this variant */
		ArrayList<String> val;
		
		/** Existence of comparators (such as {@code <}, {@code >} or {@code =})
		 *  within the {@link #val variant values}. */
		boolean hasCompare;
		
		public AttributeVariant(Kind kind)
		{
			super(kind);
			this.val = new ArrayList<String>();
			this.hasCompare = false;
		}
		
		@Override
		public boolean isNamed() {return false;}
		
		/**
		 * Search the {@link val attribute value list} for compare expressions.
		 * That is any value starting with either one of {@code <}, {@code >} or
		 * {@code =}.
		 */
		boolean checkCompare()
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
			return hasCompare;
		}
	}
	
	/**
	 * Variant list base class.
	 * This only contains the {@code showAll} flag, which is shared by both variant types.
	 */
	public static abstract class Variants
	{
		boolean showAll;
		
		Variants()
		{
			showAll = false;
		}
		
		/**
		 * Determines the default variant of this variant set.
		 * @return  The default variant of this variant set.
		 */
		public abstract Variant getDefaultVariant();
		
		/**
		 * Return the variant as determined by the given object provider.
		 * @param obj  Object declaration.
		 * @return     Variant as declared by the given object.
		 */
		public abstract Variant getVariant(ObjectProvider obj);
		
		/**
		 * Generic variant iterator.
		 * @return  an {@code Iterator} of the variants in this list.
		 */
		public abstract Iterator<Variant> viterator();
	}
	
	/**
	 * Variant list intermediary template.
	 */
	private static abstract class VariantsT<T extends Variant> extends Variants implements Collection<T>
	{
		ArrayList<T>       variants;
		TreeMap<String, T> variantMap;
		
		VariantsT()
		{
			variants = new ArrayList<T>();
			variantMap = new TreeMap<String, T>();
		}
		
		public T get(int    index) {return variants.get(index);}
		public T get(String key)   {return variantMap.get(key);}
		
		@Override public int                size()                            {return variants.size();}
		@Override public boolean            isEmpty()                         {return variants.isEmpty();}
		@Override public boolean            contains(java.lang.Object o)      {return variants.contains(o);}
		@Override public Iterator<T>        iterator()                        {return variants.iterator();}
		@Override public java.lang.Object[] toArray()                         {return variants.toArray();}
		@Override public <U> U[]            toArray(U[] a)                    {return variants.toArray(a);}
		@Override public boolean            add(T e)                          {return variants.add(e);}
		@Override public boolean            remove(java.lang.Object o)        {return variants.remove(o);}
		@Override public boolean            containsAll(Collection<?> c)      {return variants.containsAll(c);}
		@Override public boolean            addAll(Collection<? extends T> c) {return variants.addAll(c);}
		@Override public boolean            removeAll(Collection<?> c)        {return variants.removeAll(c);}
		@Override public boolean            retainAll(Collection<?> c)        {return variants.retainAll(c);}
		@Override public void               clear()                           {variants.clear();}
		
		@Override public Iterator<Variant>  viterator()
		{
			return new Iterator<Variant>()
			{
				private Iterator<T> it = variants.iterator();
				@Override public boolean hasNext() {return it.hasNext();}
				@Override public Variant next()    {return it.next();}
			};
		}
	}
	
	/**
	 * List of named variants.
	 * All contained variants are {@link NamedVariants} and a default variant
	 * name is provided.
	 */
	public static class NamedVariants extends VariantsT<NamedVariant>
	{
		/** name of the default variant */
		String defaultName;
		
		NamedVariants(String defaultName)
		{
			this.defaultName = defaultName;
		}
		
		public boolean add(NamedVariant variant)
		{
			if (!variants.isEmpty() && variant.kind != variants.get(0).kind)
				throw new IllegalArgumentException("Unable to add variant for kind '" + variant.kind.getName() + "' to kind '" + this.get(0).kind.getName() + "'");
			
			if (variant.hasName())
				variantMap.put(variant.getName(), variant);
			if (variant.hasOldName())
				variantMap.put(variant.getOldName(), variant);
			
			return super.add(variant);
		}
		
		/**
		 * Determines the default variant of this variant set.
		 * It is chosen by the given {@link defaultName} attribute.
		 * @return  The default variant of this variant set.
		 */
		@Override
		public Variant getDefaultVariant()
		{
			return variantMap.get(defaultName);
		}
		
		/**
		 * Return the variant as determined by the given object provider.
		 * In this case, only the kind-name is used.
		 * @param obj  Object declaration.
		 * @return     Variant as declared by the given object.
		 */
		@Override
		public Variant getVariant(ObjectProvider obj)
		{
			if (!variantMap.containsKey(obj.getKindName()))
				return variantMap.get(defaultName);
			return variantMap.get(obj.getKindName());
		}
	}
	
	/**
	 * Variant list base class.
	 */
	public static class AttributeVariants extends VariantsT<AttributeVariant>
	{
		ArrayList<String> attrs;
		boolean           hasCompare;
		
		/**
		 * Constructs a variant set for the specified kind.
		 * @param parent  The variants parent kind. The default variant name will be initialized to the kind's name.
		 */
		public AttributeVariants()
		{
			attrs      = new ArrayList<String>();
			hasCompare = false;
		}
		
		public boolean add(AttributeVariant variant)
		{
			if (!this.isEmpty() && variant.kind != this.get(0).kind)
				throw new IllegalArgumentException("Unable to add variant for kind '" + variant.kind.getName() + "' to kind '" + this.get(0).kind.getName() + "'");
			
			variantMap.put(String.join(",", variant.val), variant);
			hasCompare |= variant.hasCompare;
			
			return super.add(variant);
		}
		
		/**
		 * Determines the default variant of this variant set.
		 * It is chosen by the parent kind's attribute default values. For this
		 * type of variants this is the same as calling {@link #getVariant(ObjectProvider)}
		 * on the parent kind.
		 * @return  The default variant of this variant set.
		 */
		@Override
		public Variant getDefaultVariant()
		{
			if (this.isEmpty())   return null;
			if (this.size() == 1) return this.get(0);
			return getVariant(this.get(0).kind);
		}
		
		
		/**
		 * Return the variant as determined by the given object provider.
		 * @param obj  Object declaration.
		 * @return     Variant as declared by the given object.
		 */
		@Override
		public Variant getVariant(ObjectProvider obj)
		{
			return getVariant((AttributeProvider)obj);
		}
		
		/**
		 * Get the variant corresponding to the given attribute source.
		 * 
		 * @param provider  Source of attribute values based on which to choose the variant.
		 * @return          Variant, corresponding to the given values.
		 */
		private Variant getVariant(AttributeProvider provider)
		{
			if (this.isEmpty())   return null;
			if (this.size() == 1) return this.get(0);
			
			final TreeMap<String, Attribute> kindattrs = this.get(0).kind.attributes;
			final ArrayList<String>          values    = new ArrayList<String>(attrs.size());
			
			for (int i = 0; i < attrs.size(); ++i)
			{
				final String attrname = attrs.get(i);
				final String attrval  = provider.getAttribute(attrname);
				
				if (attrval == null)
				{
					values.add("nil");
				}
				else if (attrval.equals("*"))
				{
					final Attribute attr = kindattrs.get(attrname);
					values.add(attr == null ? "*" : attr.getEnum((int)(Math.random() * attr.enumSize())));
				}
				else values.add(attrval);
			}
			
			Variant out = null;
			
			if (!hasCompare)
			{
				out = variantMap.get(String.join(",", values));
			}
			else
			{
				for (Variant variant : this)
				{
					out = variant;
					final ArrayList<String> val = ((AttributeVariant)variant).val;
					for (int i = 0; i < val.size() && out != null; ++i)
					{
						try
						{
							switch (val.get(i).charAt(0))
							{
							case '<': if (Double.parseDouble(values.get(i)) >= Double.parseDouble(val.get(i).substring(1))) out = null; break;
							case '>': if (Double.parseDouble(values.get(i)) <= Double.parseDouble(val.get(i).substring(1))) out = null; break;
							case '=': if (!val.get(i).regionMatches(1, values.get(i), 0, values.get(i).length())) out = null; break;
							default:  if (!val.get(i).equals(values.get(i))) out = null; break;
							}
						}
						catch (NumberFormatException e)
						{
							out = null;
							break;
						}
					}
					if (out != null) break;
				}
			}
			
			if (out == null)
				System.err.println("Unable to resolve variant '" + String.join(",", values) + "' for kind '" + this.get(0).kind.getName() + "'");
			
			return out;
		}
	}
	
	/**
	 * Alias for a kind.
	 * An alias may provide different attribute default values.
	 */
	public static class Alias extends Name implements ObjectProvider
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
		
		@Override
		public Kind getKind()
		{
			return kind;
		}
		
		@Override
		public String getAttribute(String attrName)
		{
			String out = attributes.get(attrName);
			return out == null ? kind.getAttribute(attrName) : out;
		}
		
		@Override
		public boolean hasAttribute(String attrName)
		{
			return attributes.containsKey(attrName) || kind.hasAttribute(attrName);
		}
		
		@Override public String                  getKindName() {return kind.getName();}
		@Override public ArrayList<Variant>      getVariant()  {return kind.getVariant(this);}
		@Override public ArrayList<VariantImage> getImage()    {return kind.getImage(this);}
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
	public static class Kind extends Name implements Iterable<Variants>, ObjectProvider
	{
		/** object type enumeration */
		public enum Type {AC, FL, IT, ST};
		
		/** user access mode */
		public enum Access {KIND, ALIAS, HIDDEN};
		
		/** parent group */
		final Group                group;
		
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
		VariantImage               icon;
		
		/** user accessibility */
		Access                     access;
		
		/** framed floor existence */
		boolean                    framed;
		
		Kind(String name, Type type, Group group)
		{
			super(name);
			this.group      = group;
			this.type       = type;
			this.alias      = new ArrayList<Alias>();
			this.stack      = new ArrayList<Variants>();
			this.attributes = new TreeMap<String, Attribute>();
			this.messages   = new TreeSet<String>();
			this.icon       = null;
			this.access     = Access.KIND;
			this.framed     = false;
		}
		
		private Kind(String name, Type type)
		{
			this(name, type, null);
		}
		
		@Override public Iterator<Variants> iterator() {return stack.iterator();}
		
		public List<Alias>  getAliases()  {return Collections.unmodifiableList(alias);}
		public Type         getType()     {return type;}
		public boolean      isHidden()    {return access == Access.HIDDEN;}
		public boolean      showAliases() {return access == Access.ALIAS;}
		public boolean      hasIcon()     {return icon != null;}
		public VariantImage getIcon()
		{
			if (icon != null) return icon;
			return stack.get(0).getDefaultVariant().image;
		}
		
		@Override public Kind   getKind()     {return this;}
		@Override public String getKindName() {return name;}
		
		@Override
		public String getAttribute(String attrName)
		{
			final Attribute attr = attributes.get(attrName);
			if (attr == null || attr.defaultValue == null)
			{
				if (group == null) return "nil";
				return group.getAttribute(attrName); 
			}
			return attr.defaultValue;
		}
		
		@Override
		public boolean hasAttribute(String attrName)
		{
			final Attribute attr = attributes.get(attrName);
			if (attr != null && attr.defaultValue != null)
				return true;
			return (group != null && group.hasAttribute(attrName));
		}
		
		@Override
		public ArrayList<Variant> getVariant()
		{
			final ArrayList<Variant> list = new ArrayList<Variant>();
			for (Variants variants : stack)
				list.add(variants.getDefaultVariant());
			return list;
		}
		
		public ArrayList<Variant> getVariant(ObjectProvider obj)
		{
			final ArrayList<Variant> list = new ArrayList<Variant>();
			for (Variants variants : stack)
				list.add(variants.getVariant(obj));
			return list;
		}
		
		@Override
		public ArrayList<VariantImage> getImage()
		{
			final ArrayList<VariantImage> list = new ArrayList<VariantImage>();
			for (Variants variants : stack)
			{
				final Variant variant = variants.getDefaultVariant();
				if (variant != null)
					list.add(variant.image);
			}
			return list;
		}
		
		public ArrayList<VariantImage> getImage(ObjectProvider obj)
		{
			final ArrayList<VariantImage> list = new ArrayList<VariantImage>();
			for (Variants variants : stack)
			{
				final Variant variant = variants.getVariant(obj);
				if (variant != null)
					list.add(variant.image);
			}
			return list;
		}
	}
	
	/**
	 * Page of kinds (as parts of a group).
	 */
	public static class Page extends LinkedList<Kind>
	{
		private static final long serialVersionUID = 1L;
		private final String i18n;
		
		Page(String i18n) {this.i18n = i18n;}
		
		public String getI18n() {return i18n;}
	}
	
	/**
	 * Attribute group.
	 * This is a set of attributes shared by all objects of this group.
	 */
	public static class AttrGroup implements Iterable<Attribute>
	{
		private final ArrayList<Attribute> attributes;
		private final String               i18n;
		
		AttrGroup(String i18n)
		{
			attributes = new ArrayList<Attribute>();
			this.i18n = i18n;
		}
		
		public String getI18n() {return i18n;}
		
		void add(Attribute attr)
		{
			attributes.add(attr);
		}
		
		@Override
		public Iterator<Attribute> iterator()
		{
			return attributes.iterator();
		}
	}
	
	/**
	 * Kind group.
	 * As collection of pages.
	 */
	public static class Group extends LinkedList<Page> implements AttributeProvider
	{
		private static final long serialVersionUID = 1L;
		
		private String                     i18n;
		private String                     icon;
		private TreeMap<String, Attribute> attributes;
		private ArrayList<AttrGroup>       attributeGroups;
		
		public Group()
		{
			attributes      = new TreeMap<String, Attribute>();
			attributeGroups = new ArrayList<AttrGroup>();
		}
		
		void setI18n(String i18n) {this.i18n = i18n;}
		void setIcon(String icon) {this.icon = icon;}
		
		public String getI18n() {return i18n;}
		public String getIcon() {return icon;}
		
		Iterator<AttrGroup> getAttrGroups()
		{
			return attributeGroups.iterator();
		}
		
		AttrGroup addAttributeGroup(String i18n)
		{
			final AttrGroup grp = new AttrGroup(i18n);
			attributeGroups.add(grp);
			return grp;
		}
		
		void addAttribute(Attribute attr)
		{
			attributeGroups.get(attributeGroups.size() - 1).add(attr);
			attributes.put(attr.name, attr);
		}
		
		@Override
		public String getAttribute(String attrName)
		{
			final Attribute attr = attributes.get(attrName);
			if (attr == null || attr.defaultValue == null) return "nil";
			return attr.defaultValue;
		}
		
		@Override
		public boolean hasAttribute(String attrName)
		{
			final Attribute attr = attributes.get(attrName);
			return (attr != null && attr.defaultValue != null);
		}
	}
	
	/**
	 * Map of all named objects.
	 * The interface makes sure, that only {@link Kind Kinds} and {@link Variant Variants}
	 * are registered. Thus either {@link NameMap#isKind(String)} or {@link NameMap#isVariant(String)}
	 * returns {@code true}.
	 */
	private static class NameMap
	{
		private TreeMap<String, Kind>         kindMap;
		private TreeMap<String, Alias>        aliasMap;
		private TreeMap<String, NamedVariant> variantMap;
		
		NameMap()
		{
			kindMap    = new TreeMap<String, Kind>();
			aliasMap   = new TreeMap<String, Alias>();
			variantMap = new TreeMap<String, NamedVariant>();
		}
		
		/**
		 * Register miscellaneous named object.
		 * This links the {@link Name#name name} and the {@link Name#oldname old name}
		 * to the given object.
		 * 
		 * @param obj  Variant to be registered.
		 */
		void add(NamedVariant obj)
		{
			if (obj.hasName())
				variantMap.put(obj.getName(), obj);
			if (obj.hasOldName())
				variantMap.put(obj.getOldName(), obj);
		}
		
		/**
		 * Register an alias name for a kind.
		 * Links the {@link Name#name name} and the {@link Name#oldname old name} given
		 * by {@code alias} to the given object.
		 * 
		 * @param alias  Alias to be registered.
		 */
		void add(Alias alias)
		{
			if (alias.hasName())
				aliasMap.put(alias.name, alias);
			if (alias.hasOldName())
				aliasMap.put(alias.oldname, alias);
		}
		
		/**
		 * Register kind.
		 * This links the {@link Name#name name} and the {@link Name#oldname old name}
		 * of the given kind, and registers all its {@link Kind#alias aliases}.
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
				else this.add(alias);
			}
		}
		
		boolean has(String name)
		{
			return kindMap.containsKey(name) || aliasMap.containsKey(name) || variantMap.containsKey(name);
		}
		
		ObjectProvider get(String name)
		{
			return kindMap.containsKey(name) ? kindMap.get(name) : (aliasMap.containsKey(name) ? aliasMap.get(name) : variantMap.get(name));
		}
		
		boolean isKind    (String name) {return kindMap.containsKey(name);}
		Kind    getKind   (String name) {return kindMap.get(name);}
		
		boolean isAlias   (String name) {return aliasMap.containsKey(name);}
		Alias   getAlias  (String name) {return aliasMap.get(name);}
		
		boolean isVariant (String name) {return variantMap.containsKey(name);}
		Variant getVariant(String name) {return variantMap.get(name);}
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
		
		Kind nil; 
		names.add(nil = new Kind("fl_nil", Kind.Type.FL)); nil.access = Kind.Access.HIDDEN;
		names.add(nil = new Kind("st_nil", Kind.Type.ST)); nil.access = Kind.Access.HIDDEN;
		names.add(nil = new Kind("it_nil", Kind.Type.IT)); nil.access = Kind.Access.HIDDEN;
		names.add(nil = new Kind("ac_nil", Kind.Type.AC)); nil.access = Kind.Access.HIDDEN;
	}
	
	public String     getEditorVersion()    {return editorVer;}
	public String     getEnigmaVersion()    {return enigmaVer;}
	public I18N       getI18n()             {return i18n;}
	
	public boolean        has(String id)    {return names.has(id);}
	public ObjectProvider get(String id)    {return names.get(id);}
	
	public boolean    hasKind(String id)    {return names.isKind(id);}
	public Kind       getKind(String id)    {return names.getKind(id);}
	
	public boolean    hasAlias(String id)   {return names.isAlias(id);}
	public Alias      getAlias(String id)   {return names.getAlias(id);}
	
	public boolean    hasVariant(String id) {return names.isVariant(id);}
	public Variant    getVariant(String id) {return names.getVariant(id);}
	
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
					for (Variants variants : kind)
					{
						if (variants instanceof NamedVariants)
						{
							for (NamedVariant variant : (NamedVariants)variants)
							{
								names.add(variant);
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
						for (Iterator<Variant> vIt = variants.viterator(); vIt.hasNext();)
						{
							final Variant variant = vIt.next();
							variant.image.sprite = spriteset.get(variant.image);
							
							if (variant.image instanceof ClusterImage)
							{
								for (VariantImage connect : ((ClusterImage)variant.image).connect)
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
			
			for (AttrGroup attrgroup : group.attributeGroups)
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
	 * Resolves an {@link ObjectDecl object declaration} to the {@link TilePart} as declared by this tileset.
	 * 
	 * @param decl   Object declaration to be resolved.
	 * @param mode   Mode to use for resolving.
	 * @return       The corresponding tile part.
	 */
	public TilePart resolve(ObjectDecl decl, Mode2 mode)
	{
		final TilePart part = new TilePart(decl.checkTable(mode), this, mode);
		if (part.objdef == null)
			System.err.println("Error: unable to resolve kind '" + part.kindName + "'");
		return part;
	}
	
	@Override
	public String getReverseID(ObjectDecl decl, Mode2 mode)
	{
		final TilePart part = new TilePart(decl.checkTable(mode), this, mode);
		if (part.objdef == null)
			return part.table.toString(mode.mode());
		return part.canonical();
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
			
			for (AttrGroup attrgroup : group.attributeGroups)
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
					
					for (Alias alias : kind.alias)
					{
						System.out.print("\t\t\tALIAS " + alias.name);
						if (alias.hasOldName())
							System.out.print(" [" + alias.oldname + "]");
						System.out.println();
					}
					
					for (Variants variants : kind.stack)
					{
						if (variants instanceof NamedVariants)
							System.out.println("\t\t\tVARIANTS" + ((((NamedVariants)variants).defaultName != null) ? (" <" + ((NamedVariants)variants).defaultName + ">:") : ":"));
						else if (variants instanceof AttributeVariants)
							System.out.println("\t\t\tVARIANTS <" + String.join(", ", ((AttributeVariants)variants).attrs) + ">:");
						
						for (Iterator<Variant> vIt = variants.viterator(); vIt.hasNext();)
						{
							final Variant variant = vIt.next();
							System.out.print("\t\t\t\tVAR");
							if (variant.isNamed())
							{
								if (((NamedVariant)variant).hasName())
									System.out.print(" " + ((NamedVariant)variant).getName());
								if (((NamedVariant)variant).hasOldName())
									System.out.print(" [" + ((NamedVariant)variant).getOldName() + "]");
							}
							else
							{
								System.out.print(" <" + String.join(",", ((AttributeVariant)variant).val) + ">");
							}
							if (variant.image instanceof ClusterImage)
							{
								System.out.println(':');
								final ClusterImage cluster = (ClusterImage)variant.image;
								for (int c = 0; c < cluster.connect.length; ++c)
								{
									System.out.print("\t\t\t\t\t{" + ClusterImage.getConnections(c) + "}: ");
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
								System.out.print(':');
								for (Image image : variant.image)
								{
									System.out.print(" " + image.file);
								}
								if (variant.image.sprite == null) System.out.print("!");
								System.out.println();
							}
						}
					}
				}
			}
		}
	}
}

