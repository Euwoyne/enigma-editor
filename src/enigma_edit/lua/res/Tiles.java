
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

package enigma_edit.lua.res;

import java.util.TreeMap;

import enigma_edit.lua.data.CodeSnippet;
import enigma_edit.lua.data.Mode;
import enigma_edit.lua.data.Mode2;
import enigma_edit.lua.data.MultiMode;
import enigma_edit.lua.data.Resolver;
import enigma_edit.lua.data.Source;
import enigma_edit.lua.data.Table;
import enigma_edit.lua.data.Tile;
import enigma_edit.lua.data.TileDecl;
import enigma_edit.lua.data.TilePart;
import enigma_edit.lua.data.Variable;

/**
 * Default resolver (by tile repository).
 * This resolver simply looks up the tile definition in a table provided to the
 * constructor. Note that this is not a resolver in the sense of the lua API,
 * but represents the lua table given as the final sub-resolver.
 * As a consequence, this resolver does not have a constructor. 
 */
public class Tiles extends Table implements Resolver
{
	private final TreeMap<String, String> easyKeys;
	private final TreeMap<String, String> difficultKeys;
	
	/**
	 * Default constructor.
	 */
	public Tiles()
	{
		super(null);
		this.easyKeys      = new TreeMap<String, String>();
		this.difficultKeys = new TreeMap<String, String>();
	}
	
	/**
	 * This resolver just looks up the tile in the tile repository.
	 * 
	 * @param key   Key to resolve.
	 * @param mode  Difficulty mode to use.
	 * @return      A tile declaration corresponding to the requested tile.
	 */
	@Override
	public Tile resolve(String key, Mode mode)
	{
		final Tile tile = new Tile();
		if (this.exist(key)) tile.add(key, new TileDecl(new TilePart.Ref(this, '"' + key + '"', CodeSnippet.NONE)), mode);
		return tile;
	}
	
	/**
	 * Reverse tile lookup.
	 * 
	 * @param tile  ImageTile to look for.
	 * @param mode  Difficulty mode to use.
	 * @return      The key of the given tile, if existent. {@code null} otherwise.
	 */
	@Override
	public String reverse(Tile tile, Mode2 mode)
	{
		switch (mode)
		{
		case DIFFICULT: return difficultKeys.get(tile.toString());
		case EASY:      return easyKeys.get(tile.toString());
		default:        return null;
		}
	}
	
	@Override public Tiles    snapshot()                {return this;}
	@Override public Tiles    getTiles(Mode2 mode)      {return this;}
	@Override public String   typename()                {return "res.ti";}
	@Override public String   toString()                {return "ti";}
	@Override public Resolver checkResolver(Mode2 mode) {return this;}
	
	/**
	 * Assigns a tile to the given field.
	 * This overrides {@link Table#assign(String, SourceData, CodeSnippet, Mode)}
	 * to add conversion of the {@code value} to {@link TilePart}. 
	 * 
	 * @param key     Name of the field.
	 * @param value   Value to be assigned.
	 * @param assign  Assignment source code.
	 * @param mode    Mode to use the value for.
	 */
	@Override
	public Variable assignI(String key, Source value, CodeSnippet assign, Mode mode)
	{
		if (value instanceof MultiMode)
		{
			if (((MultiMode)value).hasNormal() && mode == Mode.NORMAL)
				return this.assignI(key, ((MultiMode)value).deref(Mode2.EASY), assign, mode);
			else
			{
				if (((MultiMode)value).hasEasy() && mode != Mode.DIFFICULT)
					this.assignI(key, ((MultiMode)value).deref(Mode2.EASY), assign, Mode.EASY);
				if (((MultiMode)value).hasDifficult() && mode != Mode.EASY)
					this.assignI(key, ((MultiMode)value).deref(Mode2.DIFFICULT), assign, Mode.DIFFICULT);
				return this.getI(key);
			}
		}
		else
		{
			if (mode != Mode.DIFFICULT) easyKeys.put(value.toString(), key);
			if (mode != Mode.EASY)      difficultKeys.put(value.toString(), key);
			
			if (value instanceof TileDecl)
				return super.assignI(key, value, assign, mode);
			
			if (value instanceof TilePart)
				return super.assignI(key, new TileDecl((TilePart)value), assign, mode);
			
			return super.assignI(key, new TileDecl(new TilePart.Construct(value)), assign, mode);
		}
	}
	
	/**
	 * Get a reference to the given field (raw index).
	 * This overrides {@link Table#getReferenceI(String, CodeSnippet)}
	 * to return an instance of {@link TilePart.Ref}. 
	 * 
	 * @param key   Name of the field.
	 * @param code  The referencing code part.
	 * @return      A new {@link TilePart.Ref}.
	 */
	@Override
	public TilePart.Ref getReferenceI(String key, CodeSnippet code)
	{
		return new TilePart.Ref(this, key, code);
	}
	
	/**
	 * Get a reference to the given field.
	 * This overrides {@link Table#getReference(String, CodeSnippet)}
	 * to return an instance of {@link TilePart.Ref}. 
	 * 
	 * @param key   Name of the field.
	 * @param code  The referencing code part.
	 * @return      A new {@link TilePart.Ref}.
	 */
	@Override
	public TilePart.Ref getReference(String key, CodeSnippet code)
	{
		return new TilePart.Ref(this, '"' + key + '"', code);
	}
	
	/**
	 * Get a reference to the given indexed field.
	 * This overrides {@link Table#getReference(String, CodeSnippet)}
	 * to return an instance of {@link TilePart.Ref}. 
	 * 
	 * @param key   Index of the field.
	 * @param code  The referencing code part.
	 * @return      A new {@link TilePart.Ref}.
	 */
	@Override
	public TilePart.Ref getReference(int key, CodeSnippet code)
	{
		return new TilePart.Ref(this, Integer.toString(key), code);
	}
}

