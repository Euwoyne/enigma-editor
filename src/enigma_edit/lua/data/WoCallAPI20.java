
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

package enigma_edit.lua.data;

import org.luaj.vm2.LuaValue;

import enigma_edit.error.IllegalKeyLength;
import enigma_edit.error.LevelLuaException;

/**
 * A call to the lua API function {@code wo()} creating the world.
 * This class features a constructor for each overloaded signature of the
 * {@code wo} function. On construction each if the arguments will be checked
 * for the correct type and the width and height of the world should be
 * determined.
 */
public class WoCallAPI20 extends SourceData implements WoCall
{
	/**
	 * Map information including dimensions and default tile key.
	 * If a table is given as source for the map (by means of the call {@code wo(resolver,
	 * defaultkey, map)}), it is converted into an array of {@link String}s for easier
	 * access within Java and the level dimensions are determined. 
	 */
	private static class Map
	{
		/** mode this map is defined for */
		public final Mode     mode;
		
		/** width of the map (in tiles) */
		public final int      width;
		
		/** height of the map (in tiles) */
		public final int      height;
		
		/** default key given with the {@code wo}-call */
		public final String   defaultkey;
		
		/** default tile as indicated by the {@code defaultkey} argument */
		public final Tile     defaulttile;
		
		/** map source table */
		public final Table    map;
		
		/** analysed map data */
		private      String[] data;
		
		/**
		 * Create an empty map.
		 * 
		 * @param width       Width of the new world (should be an integer).
		 * @param height      Height of the new world (should be an integer).
		 * @param defaultkey  Default tile key (should be a string).
		 * @param mode        Mode that {@code wo} was called in.
		 */
		Map(Resolver resolver, SimpleValue width, SimpleValue height, SimpleValue defaultkey, Mode mode)
		{
			this.mode = mode;
			this.map  = null;
			this.data = null;
			
			// check width
			if (!width.value.isinttype())
				throw new LevelLuaException.Runtime("IllegalWoWidth", mode, width.typename(mode), width.code);
			this.width = width.value.checkint();
			
			// check height
			if (!height.value.isinttype())
				throw new LevelLuaException.Runtime("IllegalWoHeight", mode, height.typename(mode), height.code);
			this.height = height.value.checkint();
			
			// check default key
			this.defaultkey = defaultkey.value.checkjstring();
			if (this.defaultkey == null)
				throw new LevelLuaException.Runtime("IllegalWoDefaultKey", mode, defaultkey.typename(mode), defaultkey.code);
			
			// get default tile
			this.defaulttile = resolver.resolve(this.defaultkey, Mode.NORMAL);
		}
		
		
		/**
		 * Create a map from a lua table.
		 * 
		 * @param source      World map (should be a table of strings).
		 * @param defaultkey  Default tile key (should be a string).
		 * @param mode        Mode that {@code wo} was called in.
		 * @param instmode    Mode, this instance is created for
		 */
		Map(Resolver resolver, Table source, SimpleValue defaultkey, Mode mode, Mode2 instmode) throws LevelLuaException.Runtime
		{
			this.mode = instmode.mode();
			this.map = source;
			
			// check default key
			this.defaultkey = defaultkey.value.checkjstring();
			if (this.defaultkey == null)
				throw new LevelLuaException.Runtime("IllegalWoDefaultKey", mode, defaultkey.typename(mode), defaultkey.code);
			
			// get default tile
			this.defaulttile = resolver.resolve(this.defaultkey, mode);
			
			// check height
			int height = 0;
			int width  = 0;
			while (source.exist(height+1))
				++height;
			
			// read lines
			SimpleValue line;
			data = new String[height];
			for (int i = 0; i < height; ++i)
			{
				line = source.get(i+1).checkSimple(instmode);
				if (!line.value.isstring())
					throw new LevelLuaException.Runtime("IllegalWoMapEntry", mode, line.typename(mode), Integer.toString(i+1), line.code);
				data[i] = line.value.checkjstring();
				if (data[i].length() % this.defaultkey.length() != 0)
					throw new LevelLuaException.Runtime("IllegalWoMapWidth", mode, Integer.toString(i+1), line.code);
				if (width < data[i].length()) width = data[i].length();
			}
			
			// assign dimensions
			this.width = width / this.defaultkey.length();
			this.height = height;
		}
		
		/**
		 * Return the specified field of the map.
		 * The returned value has the same length as the default key.
		 * 
		 * @param x  X coordinate of the field ({@code 1 <= x <= width})
		 * @param y  Y coordinate of the field ({@code 1 <= y <= height})
		 * @return   Key at the specified position {@code (x,y)} on the map;
		 *           or {@code null}, if the position is outside of the map.
		 */
		String getKey(int x, int y)
		{
			if (y < 1 || y > height) return null;
			if (x < 1 || x > width)  return null;
			--y;
			if (x * defaultkey.length() > data[y].length()) return defaultkey;
			--x;
			x *= defaultkey.length();
			return data[y].substring(x, x + defaultkey.length());
		}
		
		private String updateRow(int x, String row, String newkey)
		{
			if (x < row.length())
			{
				final String temp = row.substring(0, x).concat(newkey);
				x += newkey.length();
				if (x < row.length())
					row = temp.concat(row.substring(x));
				else
					row = temp;
			}
			else
			{
				while (x > row.length())
					row = row.concat(defaultkey);
				row = row.concat(newkey);
			}
			return row;
		}
		
		/**
		 * Set the key at the specified position in the map.
		 * The key must have the same length as the default key. Note, that this returns
		 * a new {@link String} instance containing the new code. Since many code
		 * snippets (i.e. all snippets that start at the same position or after the
		 * modified line in the map definition within the source code) will be
		 * invalidated, the source code should be completely reanalysed after this
		 * operation.
		 * 
		 * @param x       X coordinate of the field ({@code 1 <= x <= width})
		 * @param y       Y coordinate of the field ({@code 1 <= y <= height})
		 * @param newkey  Key to replace the old value with.
		 * @param code    Level source code.
		 * @return        The changed source code.
		 */
		String setKey(int x, int y, String newkey, String code) throws Exception
		{
			if (y < 1 || y > height) return null;
			if (x < 1 || x > width)  return null;
			if (newkey.length() != defaultkey.length()) throw new IllegalKeyLength(newkey.length(), defaultkey.length());
			final Variable row = map.get(y);
			x = (x - 1) * newkey.length();
			
			switch (mode)
			{
			case EASY:
				if (row.hasEasy())
				{
					final SimpleValue val = row.checkSimple(Mode2.EASY);
					assert(val != null);
					code = val.getCode().change(code, updateRow(x, val.toString_noquote(), newkey));
				}
				break;
				
			case DIFFICULT:
				if (row.hasDifficult())
				{
					final SimpleValue val = row.checkSimple(Mode2.DIFFICULT);
					assert(val != null);
					code = val.getCode().change(code, updateRow(x, val.toString_noquote(), newkey));
				}
				break;
				
			default:
				final SimpleValue easy = row.checkSimple(Mode2.EASY);
				final SimpleValue diff = row.checkSimple(Mode2.DIFFICULT);
				assert(easy != null && diff != null);
				
				if (easy.getCode().equals(diff.getCode()))
				{
					code = easy.getCode().change(code, updateRow(x, easy.toString_noquote(), newkey));
				}
				else if (easy.getCode().isBehind(diff.getCode()))
				{
					code = easy.getCode().change(code, updateRow(x, easy.toString_noquote(), newkey));
					code = diff.getCode().change(code, updateRow(x, diff.toString_noquote(), newkey));
				}
				else if (diff.getCode().isBehind(easy.getCode()))
				{
					code = diff.getCode().change(code, updateRow(x, diff.toString_noquote(), newkey));
					code = easy.getCode().change(code, updateRow(x, easy.toString_noquote(), newkey));
				}
				else throw new Exception("Map string code locations for easy and difficult mode overlap. (It is quite impossible, that this occurs!)");
				
				break;
			}
			
			return code;
		}
	}
	
	final private Resolver      resolver;
	
	final private Source        defaultkeySrc;
	final private Source        mapSrc;
	final private Source        widthSrc;
	final private Source        heightSrc;
	
	final private MMSimpleValue defaultkey;
	final private MMTable       map;
	final private MMSimpleValue width;
	final private MMSimpleValue height;
	
	final private Map           easyMap;
	final private Map           difficultMap;
	
	/**
	 * Call to {@code wo(resolver, defaultkey, width, height)} to create an empty level.
	 * 
	 * @param resolver    Resolver instance (already checked by the {@link enigma_edit.lua.CodeAnalyser CodeAnalyser}).
	 * @param defaultkey  Default tile key (should be a string).
	 * @param width       Width of the new world (should be an integer).
	 * @param height      Height of the new world (should be an integer).
	 * @param mode        Mode that {@code wo} was called in.
	 * @param code        Code snippet containing the call.
	 */
	public WoCallAPI20(Resolver resolver, Source defaultkey, Source width, Source height, Mode mode, CodeSnippet code) throws LevelLuaException.Runtime
	{
		super(code);
		this.resolver      = resolver;
		this.defaultkeySrc = defaultkey.snapshot();
		this.mapSrc        = null;
		this.map           = null;
		this.widthSrc      = width.snapshot();
		this.heightSrc     = height.snapshot();
		
		// check default key
		this.defaultkey = defaultkeySrc.checkSimple(mode);
		if (this.defaultkey.isNull())
			throw new LevelLuaException.Runtime("IllegalWoDefaultKey", mode, defaultkeySrc.typename(mode), defaultkeySrc.getCode());
		
		// check dimensions
		this.width  = widthSrc.checkSimple(mode);
		this.height = heightSrc.checkSimple(mode);
		
		// create empty map
		if (this.defaultkey.hasEasy() && this.width.hasEasy() && this.height.hasEasy())
			this.easyMap = new Map(resolver, this.width.easy, this.height.easy, this.defaultkey.easy, Mode.EASY);
		else
			this.easyMap = null;
		
		if (this.defaultkey.hasDifficult() && this.width.hasDifficult() && this.height.hasDifficult())
			this.difficultMap = new Map(resolver, this.width.difficult,  this.height.difficult, this.defaultkey.difficult, Mode.DIFFICULT);
		else
			this.difficultMap = null;
	}
	
	/**
	 * Call to {@code wo(resolver, defaultkey, map)} to create a world from map.
	 * 
	 * @param resolver    Resolver instance (already checked by the {@link enigma_edit.lua.CodeAnalyser CodeAnalyser}).
	 * @param defaultkey  Default tile key (should be a string).
	 * @param map         World map (should be a table of strings).
	 * @param mode        Mode that {@code wo} was called in.
	 * @param code        Code snippet containing the call.
	 */
	public WoCallAPI20(Resolver resolver, Source defaultkey, Source map, Mode mode, CodeSnippet code) throws LevelLuaException.Runtime
	{
		super(code);
		this.resolver      = resolver;
		this.defaultkeySrc = defaultkey.snapshot();
		this.mapSrc        = map.snapshot();
		this.widthSrc      = null;
		this.width         = null;
		this.heightSrc     = null;
		this.height        = null;
		
		// check default key
		this.defaultkey = defaultkeySrc.checkSimple(mode);
		if (this.defaultkey.isNull())
			throw new LevelLuaException.Runtime("IllegalWoDefaultKey", mode, defaultkeySrc.typename(mode), defaultkeySrc.getCode());
		
		// check map
		this.map = mapSrc.checkTable(mode);
		if (this.map.isNull())
			throw new LevelLuaException.Runtime("IllegalWoMap", mode, mapSrc.typename(mode), defaultkeySrc.getCode());
		
		if (this.defaultkey.hasEasy() && this.map.hasEasy())
			this.easyMap = new Map(resolver, this.map.easy, this.defaultkey.easy, mode, Mode2.EASY);
		else
			this.easyMap = null;
		
		if (this.defaultkey.isNormal() && this.map.isNormal())
			this.difficultMap = this.easyMap;
		else if (this.defaultkey.hasDifficult() && this.map.hasDifficult())
			this.difficultMap = new Map(resolver, this.map.difficult, this.defaultkey.difficult, mode, Mode2.DIFFICULT);
		else
			this.difficultMap = null;
	}
	
	/**
	 * Call to {@code wo(resolver, libmap)} to create a world by the library {@code libmap}.
	 * 
	 * @param resolver  Resolver instance (already checked by the {@link enigma_edit.lua.CodeAnalyser CodeAnalyser}).
	 * @param libmap    World map (should be an instance of {@link LibmapMap}).
	 * @param mode      Mode that {@code wo} was called in.
	 * @param code      Code snippet containing the call.
	 */
	public WoCallAPI20(Resolver resolver, Source libmap, Mode mode, CodeSnippet code) throws LevelLuaException.Runtime
	{
		super(code);
		this.resolver      = resolver;
		this.defaultkeySrc = null;
		this.defaultkey    = null;
		this.mapSrc        = libmap;
		this.widthSrc      = null;
		this.width         = null;
		this.heightSrc     = null;
		this.height        = null;
		
		// TODO: implement libmap classes
		this.map = null;
		this.easyMap = null;
		this.difficultMap = null;
		throw new UnsupportedOperationException("TODO: implement wo(resolver, libmap) call");
	}
	
	/**
	 * Return the specified field of the map.
	 * The returned value has the same length as the default key.
	 * 
	 * @param x     X coordinate of the field ({@code 1 <= x <= width})
	 * @param y     Y coordinate of the field ({@code 1 <= y <= height})
	 * @param mode  Use the map for this mode.
	 * @return      Key at the specified position {@code (x,y)} on the map;
	 *              or {@code null}, if the position is outside of the map.
	 */
	public String getKey(int x, int y, Mode2 mode)
	{
		final Map map = mode == Mode2.EASY ? easyMap : difficultMap;
		return map.getKey(x, y);
	}
	
	/**
	 * Resolve the tile for the given position.
	 * This calls {@link Resolver#resolve} on the value returned by {@link #getKey}.
	 * 
	 * @param x     X coordinate of the field ({@code 1 <= x <= width})
	 * @param y     Y coordinate of the field ({@code 1 <= y <= height})
	 * @return      ImageTile at the specified position {@code (x,y)} on the map;
	 *              or {@code null}, if the position is outside of the map or if the resolver fails.
	 */
	@Override
	public Tile getTile(int x, int y)
	{
		final String easyKey = easyMap      != null ? easyMap.getKey(x, y)      : null;
		final String diffKey = difficultMap != null ? difficultMap.getKey(x, y) : null;
		final Tile tile;
		if (easyKey == diffKey)
			tile = (easyKey != null) ? resolver.resolve(easyKey, Mode.NORMAL) : new Tile();
		else
			tile = Tile.composeMode(easyKey != null ? resolver.resolve(easyKey, Mode.EASY)      : new Tile(),
					                diffKey != null ? resolver.resolve(diffKey, Mode.DIFFICULT) : new Tile());
		return tile;
	}
	
	/**
	 * Resolve the tile for the given position.
	 * This calls {@link Resolver#resolve} on the value returned by {@link #getKey}.
	 * 
	 * @param x     X coordinate of the field ({@code 1 <= x <= width})
	 * @param y     Y coordinate of the field ({@code 1 <= y <= height})
	 * @return      ImageTile at the specified position {@code (x,y)} on the map;
	 *              or {@code null}, if the position is outside of the map or if the resolver fails.
	 */
	@Override
	public Tile getTile(int x, int y, Mode mode)
	{
		final String easyKey = easyMap      != null ? easyMap.getKey(x, y)      : null;
		final String diffKey = difficultMap != null ? difficultMap.getKey(x, y) : null;
		final Tile tile;
		switch (mode)
		{
		case EASY:      tile = (easyKey != null) ? resolver.resolve(easyKey, mode) : new Tile(); break;
		case DIFFICULT: tile = (diffKey != null) ? resolver.resolve(diffKey, mode) : new Tile(); break;
		default:
			if (easyKey == diffKey)
				tile = (easyKey != null) ? resolver.resolve(easyKey, Mode.NORMAL) : new Tile();
			else
				tile = Tile.composeMode(easyKey != null ? resolver.resolve(easyKey, Mode.EASY)      : new Tile(),
						                diffKey != null ? resolver.resolve(diffKey, Mode.DIFFICULT) : new Tile());
		}
		return tile;
	}
	
	/**
	 * Returns the tile declaration defined by the {@code defaulttile} argument.
	 * 
	 * @param mode  Mode to resolve the tile for.
	 * @return      Default tile for the given mode.
	 */
	public Tile getDefaultTile(Mode2 mode)
	{
		return mode == Mode2.EASY ? easyMap.defaulttile : difficultMap.defaulttile;
	}
	
	/**
	 * Set the key at the specified position in the map.
	 * The key must have the same length as the default key. Note, that this returns
	 * a new {@link String} instance containing the new code. Since many code
	 * snippets (i.e. all snippets that start at the same position or after the
	 * modified line in the map definition within the source code) will be
	 * invalidated, the source code should be completely reanalysed after this
	 * operation.
	 * 
	 * @param x       X coordinate of the field ({@code 1 <= x <= width})
	 * @param y       Y coordinate of the field ({@code 1 <= y <= height})
	 * @param newkey  Key to replace the old value with.
	 * @param code    Level source code.
	 * @param mode    Mode to change the map for.
	 * @return        The changed source code (or {@code null}, if the operation failed).
	 */
	String setKey(int x, int y, String newkey, String code, Mode2 mode) throws Exception
	{
		switch (mode)
		{
		case EASY:      return easyMap.setKey(x, y, newkey, code);
		case DIFFICULT: return difficultMap.setKey(x, y, newkey, code);
		}
		return null;
	}
	
	@Override
	public int getWidth(Mode mode)
	{
		switch (mode)
		{
		case EASY:      return easyMap.width;
		case DIFFICULT: return difficultMap.width;
		case NORMAL:
		default:		return (easyMap.width == difficultMap.width) ? easyMap.width : -1;
		}
	}
	
	@Override
	public MMSimpleValue getWidth()
	{
		if (easyMap == difficultMap)
			return new MMSimpleValue(new SimpleValue(LuaValue.valueOf(easyMap.width), this.code));
		return new MMSimpleValue(
				easyMap      != null ? new SimpleValue(LuaValue.valueOf(easyMap.width), this.code) : null,
				difficultMap != null ? new SimpleValue(LuaValue.valueOf(difficultMap.width), this.code) : null);
	}
	
	@Override
	public int getHeight(Mode mode)
	{
		switch (mode)
		{
		case EASY:      return easyMap.height;
		case DIFFICULT: return difficultMap.height;
		case NORMAL:
		default:		return (easyMap.height == difficultMap.height) ? easyMap.height : -1;
		}
	}
	
	@Override
	public MMSimpleValue getHeight()
	{
		if (easyMap == difficultMap)
			return new MMSimpleValue(new SimpleValue(LuaValue.valueOf(easyMap.height), this.code));
		return new MMSimpleValue(
				easyMap      != null ? new SimpleValue(LuaValue.valueOf(easyMap.height), this.code) : null,
				difficultMap != null ? new SimpleValue(LuaValue.valueOf(difficultMap.height), this.code) : null);
	}
	
	@Override
	public String toString()
	{
		if (defaultkey != null)
		{
			if (width != null && height != null)
			{
				return "wo(" + resolver + ", " + defaultkey + ", " + width + ", " + height + ")";
			}
			return "wo(" + resolver + ", " + defaultkey + ", " + map + ")";
		}
		return "wo(" + resolver + ", " + map + ")";
	}
}

