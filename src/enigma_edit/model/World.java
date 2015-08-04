
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

import org.luaj.vm2.parser.ParseException;

import enigma_edit.lua.CodeAnalyser;
import enigma_edit.lua.CodeData;
import enigma_edit.error.LevelLuaException;
import enigma_edit.lua.data.Mode;
import enigma_edit.lua.data.Mode2;
import enigma_edit.lua.data.Tile;
import enigma_edit.lua.data.TileDecl;
import enigma_edit.lua.data.WoCall;

/**
 * A world as it is constructed from lua code.
 * This class takes only a {@code String} of lua code from which all
 * necessary data is constructed. First a {@link CodeAnalyser} will be used
 * to analyse the given source code. Then each field of the level will be
 * computed by calling {@link WoCall#getTile} which in turn uses the given
 * resolver instance. At last the thus determined {@link TileDecl tile declaration}
 * is converted to a {@link ImageTile} by the constructor of tis class.
 */
public class World
{
	/** {@code <luamain>} content */
	private String code;
	
	/** Analysed code. This is the result of a {@link CodeAnalyser#analyse} call. */
	private CodeData data;
	
	/** World grid (normal) */
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
		this.code  = code;
		this.data  = null;
		this.world = null;
	}
	
	/**
	 * Check, if the given source code has been analysed.
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
	 * @return      ImageTile at the specified position {@code (x,y)}.
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
	/*
	private boolean isPuzzle(Table decl, Mode2 mode)
	{
		if (!decl.exist(1)) return false;
		final SimpleValue kind = decl.get(1).checkSimple(mode);
		if (kind == null) return false;
		return kind.value.checkjstring().startsWith("st_puzzle");
	}
	
	private boolean isPuzzle(Tile.Part stone, Mode2 mode)
	{
		if (stone.isNull())
			return false;
		
		if (stone.hasNormal())
			return isPuzzle(stone.get(Mode.NORMAL).checkTable(Mode.NORMAL).easy, mode);
		
		switch (mode)
		{
		case EASY:      return (stone.hasEasy()) ? isPuzzle(stone.get(Mode.EASY).checkTable(Mode2.EASY), Mode2.EASY) : false;
		case DIFFICULT: return (stone.hasDifficult()) ? isPuzzle(stone.get(Mode.DIFFICULT).checkTable(Mode2.DIFFICULT), Mode2.DIFFICULT) : false;
		default:        return false;
		}
	}
	
	private void connectPuzzle(Table decl, int x, int y, Mode mode)
	{
		
	}
	
	private void checkPuzzle(Tile.Part stone, int x, int y)
	{
		if (stone.isNull()) return;
		
		if (isPuzzle(stone, Mode2.EASY))
		{
			if (isPuzzle(stone, Mode2.DIFFICULT))
			{
				if (stone.hasNormal())
					connectPuzzle(stone.get(Mode.NORMAL).checkTable(Mode.NORMAL).easy, x, y, Mode.NORMAL);
				else
				{
					connectPuzzle(stone.get(Mode.EASY).checkTable(Mode2.EASY), x, y, Mode.EASY);
					connectPuzzle(stone.get(Mode.DIFFICULT).checkTable(Mode2.DIFFICULT), x, y, Mode.DIFFICULT);
				}
			}
			else
				connectPuzzle(stone.get(Mode.EASY).checkTable(Mode2.EASY), x, y, Mode.EASY);
		}
		else if (isPuzzle(stone, Mode2.DIFFICULT))
			connectPuzzle(stone.get(Mode.DIFFICULT).checkTable(Mode2.DIFFICULT), x, y, Mode.DIFFICULT);
	}
	*/
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
		data = CodeAnalyser.analyse(this.code);
		
		final WoCall    easyCall    = data.getWorldCall(Mode2.EASY);
		final WoCall    diffCall    = data.getWorldCall(Mode2.DIFFICULT);
		final ImageTile defaultTile = new ImageTile(Tile.composeMode(easyCall.getDefaultTile(Mode2.EASY), diffCall.getDefaultTile(Mode2.DIFFICULT)), tileset);
		final int       easyWidth   = easyCall.getWidth(Mode.EASY);
		final int       diffWidth   = diffCall.getWidth(Mode.DIFFICULT);
		final int       easyHeight  = easyCall.getHeight(Mode.EASY);
		final int       diffHeight  = diffCall.getHeight(Mode.DIFFICULT);
		final int       width       = easyWidth  >= diffWidth  ? easyWidth  : diffWidth;
		final int       height      = easyHeight >= diffHeight ? easyHeight : diffHeight;
		
		world = new ImageTile[width][height];
			
		if (easyCall == diffCall)
		{
			for (int x = 0; x < width; ++x)
			{
				for (int y = 0; y < height; ++y)
				{
					world[x][y] = new ImageTile(easyCall.getTile(x+1, y+1), tileset, defaultTile);
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
					easyTile = easyCall.getTile(x+1, y+1);
					diffTile = diffCall.getTile(x+1, y+1);
					if (easyTile == null) easyTile = new Tile();
					if (diffTile == null) diffTile = new Tile();
					world[x][y] = new ImageTile(Tile.composeMode(easyTile, diffTile), tileset, defaultTile);
				}
			}
		}
	}
}

