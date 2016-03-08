
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

import org.luaj.vm2.parser.ParseException;

import com.github.euwoyne.enigma_edit.error.LevelLuaException;
import com.github.euwoyne.enigma_edit.lua.CodeAnalyser;
import com.github.euwoyne.enigma_edit.lua.CodeData;
import com.github.euwoyne.enigma_edit.lua.data.Mode;
import com.github.euwoyne.enigma_edit.lua.data.Mode2;
import com.github.euwoyne.enigma_edit.lua.data.Resolver;
import com.github.euwoyne.enigma_edit.lua.data.SimpleValue;
import com.github.euwoyne.enigma_edit.lua.data.Table;
import com.github.euwoyne.enigma_edit.lua.data.Tile;
import com.github.euwoyne.enigma_edit.lua.data.TileDecl;
import com.github.euwoyne.enigma_edit.lua.data.WoCall;
import com.github.euwoyne.enigma_edit.model.Tileset.ClusterImage;
import com.github.euwoyne.enigma_edit.model.Tileset.VariantImage;

/**
 * A world as it is constructed from lua code.
 * This class takes only a {@code String} of lua code from which all
 * necessary data is constructed. First a {@link CodeAnalyser} will be used
 * to analyse the given source code. Then each field of the level will be
 * computed by calling {@link WoCall#getTile} which in turn uses the given
 * resolver instance. At last the thus determined {@link TileDecl tile declaration}
 * is converted to a {@link ImageTile} by the constructor of this class.
 */
public class World
{
	/** {@code <luamain>} content */
	private String code;
	
	/** Analysed code. This is the result of a {@link CodeAnalyser#analyse} call. */
	private CodeData data;
	
	/** Default tile */
	private Tile defaultTile;
	
	/** World grid */
	private ImageTile[][] world;
	
	/**
	 * Creates a world from lua code.
	 * The constructor does not analyse the code yet. You will have to call {@link #analyse}
	 * to manually start the analysis.
	 * 
	 * @param code  The world's source code. Typically this is the content of the {@code <luamain>} tag.
	 */
	public World(String code)
	{
		this.code = code;
	}
	
	/**
	 * Check, if the given source code has been analysed.
	 * @return {@code true}, if {@link analyse} has been called.
	 */
	public boolean isAnalysed()
	{
		return world != null;
	}
	
	/**
	 * Get the world's width (in tiles).
	 */
	public int getWidth()
	{
		return world.length;
	}
	
	/**
	 * Get the world's height (in tiles).
	 */
	public int getHeight()
	{
		return world[0].length;
	}
	
	/**
	 * Get tile from position.
	 * 
	 * @param x     X coordinate of the field ({@code 1 <= x <= width})
	 * @param y     Y coordinate of the field ({@code 1 <= y <= height})
	 * @return      Tile at the specified position {@code (x,y)}.
	 */
	public ImageTile getTile(int x, int y)
	{
		return world[x-1][y-1];
	}
	
	/**
	 * Change the world source code to the given string (without re-analysis).
	 * For the analysis of the new code an explicit call to {@link #analyse} is necessary.
	 * 
	 * @param code                New lua source code.
	 * 
	 * @throws ParseException     This indicates a lua syntax error (thrown by the parser).
	 * @throws LevelLuaException  This indicates special runtime errors or constructs, that are illegal in Enigma levels (thrown by the analyser).
	 */
	public void resetCode(String code) throws ParseException, LevelLuaException
	{
		this.code = code;
		data = null;
	}
	
	/**
	 * Change the world source code to the given string.
	 * If the previously given code was already analysed, the new code will be
	 * analysed as well. Otherwise an explicit call to {@link #analyse} is necessary.
	 * 
	 * @param code                New lua source code.
	 * @param tileset             ImageTile set used during analysis for sprite caching.  
	 * @return                    {@code true}, if the new code was analysed.
	 * 
	 * @throws ParseException     This indicates a lua syntax error (thrown by the parser).
	 * @throws LevelLuaException  This indicates special runtime errors or constructs, that are illegal in Enigma levels (thrown by the analyser).
	 */
	public boolean resetCode(String code, Tileset tileset) throws ParseException, LevelLuaException
	{
		this.code = code;
		if (data == null) return false;
		analyse(tileset);
		return true;
	}
	
	private boolean checkCluster(SimpleValue kind, SimpleValue cluster, Tile.Part neighbor, Mode2 mode)
	{
		if (!neighbor.has(mode)) return false;
		final Table table = neighbor.get(mode).checkTable(mode);
		if (!table.exist(1, mode) || !table.exist("cluster", mode)) return false;
		final SimpleValue nkind    = table.get(1).checkSimple(mode);
		final SimpleValue ncluster = table.get("cluster").checkSimple(mode);
		return (nkind != null && ncluster != null && cluster.value.eq_b(ncluster.value) && kind.value.eq_b(nkind.value));
	}
	
	private void resolveCluster(ImageTile.Part target, Tile.Part part, int x, int y, Tileset tileset, Mode2 mode) throws LevelLuaException
	{
		final Table table = part.get(mode).checkTable(mode);
		final SimpleValue cluster = table.exist("cluster", mode) ? table.get("cluster").checkSimple(mode) : null;
		final SimpleValue kind    = table.exist(1,         mode) ? table.get(1).checkSimple(mode)         : null;
		for (VariantImage variant : target)
		{
			if (variant instanceof ClusterImage)
			{
				if (cluster != null)
				{
					final StringBuffer s = new StringBuffer(4);
					if (y > 0                   && checkCluster(kind, cluster, world[x][y-1].tile.st(), mode))
						s.append('n');
					if (x < world.length - 1    && checkCluster(kind, cluster, world[x+1][y].tile.st(), mode))
						s.append('e');
					if (y < world[0].length - 1 && checkCluster(kind, cluster, world[x][y+1].tile.st(), mode))
						s.append('s');
					if (x > 0                   && checkCluster(kind, cluster, world[x-1][y].tile.st(), mode))
						s.append('w');
					target.indices.add(ClusterImage.getIndex(s.toString()));
				}
				else if (table.exist("connections"))
				{
					final SimpleValue conn = table.get("connections").checkSimple(mode);
					target.indices.add(conn == null ? 0 : ClusterImage.getIndex(conn.toString_noquote()));
				}
				else if (target.hasAttribute("connections"))
				{
					target.indices.add(ClusterImage.getIndex(target.getAttribute("connections")));
				}
				else target.indices.add(0);
			}
		}
	}
	
	private static boolean checkCluster(ImageTile.Part data)
	{
		for (VariantImage variant : data)
		{
			if (variant instanceof ClusterImage)
				return true;
		}
		return false;
	}
	
	private void resolveCluster(ImageTile.MMPart target, Tile.Part part, int x, int y, Tileset tileset) throws LevelLuaException
	{
		if (target.hasEasy() && checkCluster(target.easy))
			resolveCluster(target.easy, part, x, y, tileset, Mode2.EASY);
		if (target.hasDifficult() && checkCluster(target.difficult))
			resolveCluster(target.difficult, part, x, y, tileset, Mode2.DIFFICULT);
	}
	
	/**
	 * Execute a code analysis.
	 * The given code will be analysed by a {@link CodeAnalyser}.
	 * Then all cells of the world will be resolved by the {@link Resolver} given
	 * to the {@code wo()} call.
	 * 
	 * @param tileset             ImageTile set used for sprite caching.  
	 * 
	 * @throws ParseException     This indicates a lua syntax error (thrown by the parser).
	 * @throws LevelLuaException  This indicates special runtime errors or constructs, that are illegal in Enigma levels (thrown by the analyser).
	 */
	public void analyse(Tileset tileset) throws ParseException, LevelLuaException
	{
		// analyse level code
		data = CodeAnalyser.analyse(this.code);
		
		// prepare world data
		final WoCall easyCall   = data.getWorldCall(Mode2.EASY);
		final WoCall diffCall   = data.getWorldCall(Mode2.DIFFICULT);
		final int    easyWidth  = easyCall.getWidth(Mode.EASY);
		final int    diffWidth  = diffCall.getWidth(Mode.DIFFICULT);
		final int    easyHeight = easyCall.getHeight(Mode.EASY);
		final int    diffHeight = diffCall.getHeight(Mode.DIFFICULT);
		final int    width      = easyWidth  >= diffWidth  ? easyWidth  : diffWidth;
		final int    height     = easyHeight >= diffHeight ? easyHeight : diffHeight;
		
		world = new ImageTile[width][height];
		defaultTile = Tile.composeMode(easyCall.getDefaultTile(Mode2.EASY), diffCall.getDefaultTile(Mode2.DIFFICULT));
		
		// resolve tiles (declaration -> tile-set reference)
		if (easyCall == diffCall)
		{
			for (int x = 0; x < width; ++x)
			{
				for (int y = 0; y < height; ++y)
				{
					world[x][y] = new ImageTile(easyCall.getTile(x+1, y+1));
					world[x][y].resolveTile(tileset, defaultTile);
				}
			}
		}
		else
		{
			Tile easyTile, diffTile;
			for (int x = 0; x < width; ++x)
			{
				for (int y = 0; y < height; ++y)
				{
					easyTile = easyCall.getTile(x+1, y+1, Mode.EASY);
					diffTile = diffCall.getTile(x+1, y+1, Mode.DIFFICULT);
					if (easyTile == null) easyTile = new Tile();
					if (diffTile == null) diffTile = new Tile();
					world[x][y] = new ImageTile(Tile.composeMode(easyTile, diffTile));
					world[x][y].resolveTile(tileset, defaultTile);
				}
			}
		}
		
		// resolve cluster images
		for (int x = 0; x < width; ++x)
		{
			for (int y = 0; y < height; ++y)
			{
				resolveCluster(world[x][y].floor, world[x][y].tile.fl(), x, y, tileset);
				resolveCluster(world[x][y].item,  world[x][y].tile.it(), x, y, tileset);
				resolveCluster(world[x][y].actor, world[x][y].tile.ac(), x, y, tileset);
				resolveCluster(world[x][y].stone, world[x][y].tile.st(), x, y, tileset);
			}
		}
	}
}

