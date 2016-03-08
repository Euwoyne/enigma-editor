
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

package com.github.euwoyne.enigma_edit.lua.res;

import java.util.Map.Entry;

import com.github.euwoyne.enigma_edit.error.LevelLuaException;
import com.github.euwoyne.enigma_edit.lua.RevId;
import com.github.euwoyne.enigma_edit.lua.ReverseInfo;
import com.github.euwoyne.enigma_edit.lua.data.CodeSnippet;
import com.github.euwoyne.enigma_edit.lua.data.Mode;
import com.github.euwoyne.enigma_edit.lua.data.Mode2;
import com.github.euwoyne.enigma_edit.lua.data.MultiMode;
import com.github.euwoyne.enigma_edit.lua.data.ObjectDecl;
import com.github.euwoyne.enigma_edit.lua.data.Resolver;
import com.github.euwoyne.enigma_edit.lua.data.Source;
import com.github.euwoyne.enigma_edit.lua.data.Table;
import com.github.euwoyne.enigma_edit.lua.data.Tile;
import com.github.euwoyne.enigma_edit.lua.data.TileDecl;
import com.github.euwoyne.enigma_edit.lua.data.TileDeclPart;
import com.github.euwoyne.enigma_edit.lua.data.TileReference;
import com.github.euwoyne.enigma_edit.lua.data.Variable;

import java.util.TreeMap;

/**
 * Default resolver (by tile repository).
 * This resolver simply looks up the tile definition in a table provided to the
 * constructor. Note that this is not a resolver in the sense of the lua API,
 * but represents the lua table given as the final sub-resolver.
 * As a consequence, this resolver does not have a {@link Constructor}. 
 */
public class Tiles extends Table implements Resolver
{
	/** reverse id cache */
	private final TreeMap<String, String> idToKey;
	
	/**
	 * Default constructor.
	 */
	public Tiles()
	{
		super(null);
		idToKey = new TreeMap<String, String>();
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
		if (this.exist(key)) tile.add(key, new TileDecl(new TileReference(this, '"' + key + '"', CodeSnippet.NONE)), mode);
		return tile;
	}
	
	@Override
	public int reverse(ReverseInfo info)
	{
		final Mode mode = info.getMode();
		
		// check if the tile exists
		String key = idToKey.get(mode.toString() + info.reverseId());
		if (key != null)
		{
			info.setKey(key);
			return info.typeMask();
		}
		
		// search for tile parts
		int typeMask = 0;
		
		// check each declared tile
		for (Entry<String, Variable> entry : this)
		{
			// get relevant tile declaration
			final Variable tile = entry.getValue();
			final TileDecl decl = (TileDecl)tile.get(mode);
			
			if (decl == null) continue;
			if (!tile.isDefined(mode)) continue;
			
			// get the declaration's reverse id
			final RevId declRevId = decl.reverseID(info);
			if (!idToKey.containsKey("KEY" + entry.getKey()))
			{
				idToKey.put("KEY" + entry.getKey(), null);
				if (declRevId.easy != null)
					idToKey.put("EASY"      + declRevId.easy,      entry.getKey());
				if (declRevId.difficult != null)
					idToKey.put("DIFFICULT" + declRevId.difficult, entry.getKey());
				if (declRevId.normal != null)
					idToKey.put("NORMAL"    + declRevId.normal,    entry.getKey());
			}
			
			// check, if the declaration fits the request
			if (decl.typeMask(mode) == info.typeMask())
			{
				info.setKey(entry.getKey());
				return info.typeMask();
			}
			
			// if the declaration defines part of the tile...
			if ((decl.typeMask() & info.typeMask()) == decl.typeMask())
			{
				// ...and the part declarations match...
				if (info.reverseId(decl.typeMask()).equals(declRevId))
				{
					// ...and the declaration contains more parts than a previously found one...
					if (Integer.bitCount(typeMask) < Integer.bitCount(decl.typeMask(mode)))	
					{
						typeMask = decl.typeMask(mode);
						key = entry.getKey();
					}
				}
			}
		}
		
		// return best approximation
		info.setKey(key, typeMask);
		return typeMask;
	}
	
	@Override public Tiles    snapshot()                 {return this;}
	@Override public Tiles    getTiles(Mode2 mode)       {return this;}
	@Override public Resolver getSubresolver(Mode2 mode) {return null;}
	@Override public String   typename()                 {return "res.ti";}
	@Override public String   toString()                 {return "ti";}
	@Override public Resolver checkResolver(Mode2 mode)  {return this;}
	
	/**
	 * Assigns a tile to the given field.
	 * This overrides {@link Table#assignI(String, Source, CodeSnippet, Mode)}
	 * to add conversion of the {@code value} to {@link TileDeclPart}. 
	 * 
	 * @param key     Name of the field.
	 * @param value   Value to be assigned.
	 * @param assign  Assignment source code.
	 * @param mode    Mode to use the value for.
	 */
	@Override
	public Variable assignI(String key, Source value, CodeSnippet assign, Mode mode)
	{
		if (this.exist(key))
			throw new LevelLuaException.Runtime("IllegalTileRedefinition", key, assign);
		
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
			final Variable ret;
			
			if (value instanceof TileDecl)
				ret = super.assignI(key, value, assign, mode);
			else if (value instanceof TileDeclPart)
				ret = super.assignI(key, new TileDecl((TileDeclPart)value), assign, mode);
			else
				ret = super.assignI(key, new TileDecl(new ObjectDecl(value)), assign, mode);
			
			return ret;
		}
	}
	
	@Override
	public TileDecl getValueI(String idx, Mode2 mode)
	{
		final Variable var = super.getI(idx);
		if (var == null) return null;
		return var.checkTile(mode);
	}
	
	@Override
	public TileDecl getValue(String key, Mode2 mode) {return getValueI('"' + key + '"', mode);}
	
	@Override
	public TileDecl getValue(int idx, Mode2 mode) {return getValueI(Integer.toString(idx), mode);}
	
	/**
	 * Get a reference to the given field (raw index).
	 * This overrides {@link Table#getReferenceI(String, CodeSnippet)}
	 * to return an instance of {@link TileReference}. 
	 * 
	 * @param key   Name of the field.
	 * @param code  The referencing code part.
	 * @return      A new {@link TileReference}.
	 */
	@Override
	public TileReference getReferenceI(String key, CodeSnippet code)
	{
		return new TileReference(this, key, code);
	}
	
	/**
	 * Get a reference to the given field.
	 * This overrides {@link Table#getReference(String, CodeSnippet)}
	 * to return an instance of {@link TileReference}. 
	 * 
	 * @param key   Name of the field.
	 * @param code  The referencing code part.
	 * @return      A new {@link TileReference}.
	 */
	@Override
	public TileReference getReference(String key, CodeSnippet code)
	{
		return new TileReference(this, '"' + key + '"', code);
	}
	
	/**
	 * Get a reference to the given indexed field.
	 * This overrides {@link Table#getReference(String, CodeSnippet)}
	 * to return an instance of {@link TileReference}. 
	 * 
	 * @param key   Index of the field.
	 * @param code  The referencing code part.
	 * @return      A new {@link TileReference}.
	 */
	@Override
	public TileReference getReference(int key, CodeSnippet code)
	{
		return new TileReference(this, Integer.toString(key), code);
	}
}

