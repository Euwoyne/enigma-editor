
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

import javax.imageio.IIOException;

import org.luaj.vm2.parser.ParseException;

import enigma_edit.lua.CodeAnalyser;
import enigma_edit.lua.CodeData;
import enigma_edit.lua.data.Mode;
import enigma_edit.lua.data.Mode2;
import enigma_edit.lua.data.Resolver;
import enigma_edit.lua.data.SimpleValue;
import enigma_edit.lua.data.Table;
import enigma_edit.lua.data.Tile;
import enigma_edit.lua.data.TileDecl;
import enigma_edit.lua.data.WoCall;
import enigma_edit.model.Tileset.NamedImage;
import enigma_edit.model.Tileset.VarImage;
import enigma_edit.model.ImageTile;
import enigma_edit.error.LevelLuaException;
import enigma_edit.error.MissingImageException;

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
	private static class TileData implements ImageTile
	{
		private static class Data implements Part
		{
			ArrayList<VarImage> variant;
			ArrayList<Integer>  clusterIndex;
			
			Data()
			{
				variant      = new ArrayList<VarImage>();
				clusterIndex = new ArrayList<Integer>();
			}
			
			public boolean isEmpty()   {return variant.isEmpty();}
			public boolean isCluster() {return !clusterIndex.isEmpty();}
			
			private class Iterator implements java.util.Iterator<NamedImage>
			{
				private java.util.ListIterator<VarImage> vIt;
				private java.util.ListIterator<Integer>  iIt;
				
				private Iterator()
				{
					vIt = variant.listIterator();
					iIt = clusterIndex.listIterator();
				}
				
				@Override public boolean hasNext()
				{
					return vIt.hasNext();
				}
				
				@Override public NamedImage next()
				{
					final VarImage v = vIt.next();
					if (v == null || !(v instanceof Tileset.Cluster) || !iIt.hasNext()) return v;
					return ((Tileset.Cluster)v).connect[iIt.next()];
				}
			}
			
			@Override public Iterator iterator() {return new Iterator();}
			
			@Override
			public void draw(RenderingAgent renderer, int x, int y) throws MissingImageException, IIOException
			{
				for (NamedImage image : this)
					image.draw(renderer, x, y);
			}
		}
		
		private static class Object implements MMPart
		{
			Data easy, difficult;
			
			Object()
			{
				easy = new Data();
				difficult = new Data();
			}
			
			@Override
			public Part get(Mode mode)
			{
				switch (mode)
				{
				case EASY:      return easy;
				case DIFFICULT: return difficult;
				case NORMAL:    return easy == difficult ? easy : null;
				default:        return null;
				}
			}
			
			@Override
			public Part get(Mode2 mode)
			{
				return mode == Mode2.EASY ? easy : difficult;
			}
		}
		
		Tile   tile;
		Object floor, item, actor, stone;
		
		TileData(Tile tile)
		{
			this.tile = tile;
			this.floor = new Object();
			this.item  = new Object();
			this.actor = new Object();
			this.stone = new Object();
		}
		
		public Tile    tile()  {return tile;}
		public MMPart  fl()    {return floor;}
		public MMPart  it()    {return item;}
		public MMPart  ac()    {return actor;}
		public MMPart  st()    {return stone;}
		
		void resolveTile(Tileset tileset, Tile defaultTile)
		{
			// get variants
			// * floor
			if (tile.has_fl())
			{
				if (tile.fl().hasEasy())
					floor.easy.variant = tileset.resolve(tile.fl().get(Mode.EASY), Mode2.EASY);
				else
					floor.easy.variant = tileset.resolve(defaultTile.fl().get(Mode.EASY), Mode2.EASY);
				
				if (tile.fl().hasNormal())
					floor.difficult.variant = floor.easy.variant;
				else if (tile.fl().hasDifficult())
					floor.difficult.variant = tileset.resolve(tile.fl().get(Mode.DIFFICULT), Mode2.DIFFICULT);
				else
					floor.difficult.variant = tileset.resolve(defaultTile.fl().get(Mode.DIFFICULT), Mode2.DIFFICULT);
			}
			else
			{
				floor.easy.variant = tileset.resolve(defaultTile.fl().get(Mode.EASY), Mode2.EASY);
				floor.difficult.variant = tileset.resolve(defaultTile.fl().get(Mode.DIFFICULT), Mode2.DIFFICULT);
			}
			
			// * item
			if (tile.has_it())
			{
				if (tile.it().hasEasy())
					item.easy.variant = tileset.resolve(tile.it().get(Mode.EASY), Mode2.EASY);
				if (tile.it().hasNormal())
					item.difficult.variant = item.easy.variant;
				else if (tile.it().hasDifficult())
					item.difficult.variant = tileset.resolve(tile.it().get(Mode.DIFFICULT), Mode2.DIFFICULT);
			}
			
			// * actor
			if (tile.has_ac())
			{
				if (tile.ac().hasEasy())
					actor.easy.variant = tileset.resolve(tile.ac().get(Mode.EASY), Mode2.EASY);
				if (tile.ac().hasNormal())
					actor.difficult.variant = actor.easy.variant;
				else if (tile.ac().hasDifficult())
					actor.difficult.variant = tileset.resolve(tile.ac().get(Mode.DIFFICULT), Mode2.DIFFICULT);
			}
			
			// * stone
			if (tile.has_st())
			{
				if (tile.st().hasEasy())
					stone.easy.variant = tileset.resolve(tile.st().get(Mode.EASY), Mode2.EASY);
				if (tile.st().hasNormal())
					stone.difficult.variant = stone.easy.variant;
				else if (tile.st().hasDifficult())
					stone.difficult.variant = tileset.resolve(tile.st().get(Mode.DIFFICULT), Mode2.DIFFICULT);
			}
			
			/*
			 * ERROR CHECK
			 */
			if (floor.easy.variant == null)
			{
				if (tile.has_fl(Mode.EASY))
					System.err.println("ERROR: unable to resolve floor " + tile.fl().get(Mode.EASY).checkTable(Mode2.EASY).get(1).toString() + "(EASY mode)");
				else
					System.err.println("ERROR: unable to resolve default-floor " + defaultTile.fl().get(Mode.EASY).checkTable(Mode2.EASY).get(1).toString() + "(EASY mode)");
			}
			
			if (floor.difficult.variant == null)
			{
				if (tile.has_fl(Mode.DIFFICULT))
					System.err.println("ERROR: unable to resolve floor " + tile.fl().get(Mode.DIFFICULT).checkTable(Mode2.DIFFICULT).get(1).toString() + "(DIFFICULT mode)");
				else
					System.err.println("ERROR: unable to resolve default-floor " + defaultTile.fl().get(Mode.DIFFICULT).checkTable(Mode2.DIFFICULT).get(1).toString() + "(DIFFICULT mode)");
			}
			
			if (item.easy.variant == null)
			{
				if (tile.has_it(Mode.EASY))
					System.err.println("ERROR: unable to resolve item " + tile.it().get(Mode.EASY).checkTable(Mode2.EASY).get(1).toString() + "(EASY mode)");
				else
					System.err.println("ERROR: illegal request for deafult-item (EASY mode)");
			}
			
			if (item.difficult.variant == null)
			{
				if (tile.has_it(Mode.DIFFICULT))
					System.err.println("ERROR: unable to resolve item " + tile.it().get(Mode.DIFFICULT).checkTable(Mode2.DIFFICULT).get(1).toString() + "(DIFFICULT mode)");
				else
					System.err.println("ERROR: illegal request for deafult-item (DIFFICULT mode)");
			}
			
			if (actor.easy.variant == null)
			{
				if (tile.has_ac(Mode.EASY))
					System.err.println("ERROR: unable to resolve actor " + tile.ac().get(Mode.EASY).checkTable(Mode2.EASY).get(1).toString() + "(EASY mode)");
				else
					System.err.println("ERROR: illegal request for deafult-actor (EASY mode)");
			}
			
			if (actor.difficult.variant == null)
			{
				if (tile.has_ac(Mode.DIFFICULT))
					System.err.println("ERROR: unable to resolve actor " + tile.ac().get(Mode.DIFFICULT).checkTable(Mode2.DIFFICULT).get(1).toString() + "(DIFFICULT mode)");
				else
					System.err.println("ERROR: illegal request for deafult-actor (DIFFICULT mode)");
			}
			
			if (stone.easy.variant == null)
			{
				if (tile.has_st(Mode.EASY))
					System.err.println("ERROR: unable to resolve stone " + tile.st().get(Mode.EASY).checkTable(Mode2.EASY).get(1).toString() + "(EASY mode)");
				else
					System.err.println("ERROR: illegal request for deafult-stone (EASY mode)");
			}
			
			if (stone.difficult.variant == null)
			{
				if (tile.has_st(Mode.DIFFICULT))
					System.err.println("ERROR: unable to resolve stone " + tile.st().get(Mode.DIFFICULT).checkTable(Mode2.DIFFICULT).get(1).toString() + "(DIFFICULT mode)");
				else
					System.err.println("ERROR: illegal request for deafult-stone (DIFFICULT mode)");
			}
		}
		
		@Override
		public void draw_fl(RenderingAgent renderer, int x, int y, Mode mode)
		{
			Part part = floor.get(mode);
			if (part == null) return;
			for (NamedImage image : part)
				image.draw(renderer, x, y);
		}
		
		@Override
		public void draw_it(RenderingAgent renderer, int x, int y, Mode mode)
		{
			Part part = item.get(mode);
			if (part == null) return;
			for (NamedImage image : part)
				image.draw(renderer, x, y);
		}
		
		@Override
		public void draw_ac(RenderingAgent renderer, int x, int y, Mode mode)
		{
			Part part = actor.get(mode);
			if (part == null) return;
			for (NamedImage image : part)
				image.draw(renderer, x, y);
		}
		
		@Override
		public void draw_st(RenderingAgent renderer, int x, int y, Mode mode)
		{
			Part part = stone.get(mode);
			if (part == null) return;
			for (NamedImage image : part)
				image.draw(renderer, x, y);
		}
	}
	
	/** {@code <luamain>} content */
	private String code;
	
	/** Analysed code. This is the result of a {@link CodeAnalyser#analyse} call. */
	private CodeData data;
	
	/** Default tile */
	private Tile defaultTile;
	
	/** World grid */
	private TileData[][] world;
	
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
	
	private boolean checkCluster(SimpleValue cluster, Tile.Part neighbor, Mode2 mode) throws LevelLuaException
	{
		if (!neighbor.has(mode)) return false;
		final Table table = neighbor.get(mode).checkTable(mode);
		if (table.exist("cluster"))
		{
			final SimpleValue ncluster = table.get("cluster").checkSimple(mode);
			if (ncluster == null)
				throw new LevelLuaException(new LevelLuaException.Runtime("IllegalClusterId", neighbor.getKey(mode), neighbor.get(mode).getCode()));
			if (cluster.value.eq_b(ncluster.value))
				return true;
		}
		return false;
	}
	
	private void resolveCluster(TileData.Data target, Tile.Part part, int x, int y, Tileset tileset, Mode2 mode) throws LevelLuaException
	{
		final Table table = part.get(mode).checkTable(mode);
		SimpleValue value;
		for (VarImage variant : target.variant)
		{
			if (variant instanceof Tileset.Cluster)
			{
				if (table.exist("cluster", mode)
						&& (value = table.get("cluster").checkSimple(mode)) != null)
				{
					final StringBuffer s = new StringBuffer(4);
					if (y > 0                   && checkCluster(value, world[x][y-1].tile.st(), mode))
						s.append('n');
					if (x < world.length - 1    && checkCluster(value, world[x+1][y].tile.st(), mode))
						s.append('e');
					if (y < world[0].length - 1 && checkCluster(value, world[x][y+1].tile.st(), mode))
						s.append('s');
					if (x > 0                   && checkCluster(value, world[x-1][y].tile.st(), mode))
						s.append('w');
					target.clusterIndex.add(Tileset.Cluster.getIndex(s.toString()));
				}
				else if (table.exist("connections"))
				{
					final SimpleValue conn = table.get("connections").checkSimple(mode);
					final int         idx  = conn != null ? Tileset.Cluster.getIndex(conn.toString_noquote()) : -1;
					if (idx < 0)
						throw new LevelLuaException(new LevelLuaException.Runtime("IllegalClusterConnections", part.getKey(mode), table.get("connections").get(mode.mode()).getCode()));
					target.clusterIndex.add(idx);
				}
				else target.clusterIndex.add(0);
			}
		}
	}
	
	private static boolean checkCluster(ArrayList<VarImage> variants)
	{
		for (VarImage variant : variants)
		{
			if (variant instanceof Tileset.Cluster)
				return true;
		}
		return false;
	}
	
	private void resolveCluster(TileData.Object target, Tile.Part part, int x, int y, Tileset tileset) throws LevelLuaException
	{
		if (checkCluster(target.easy.variant))
			resolveCluster(target.easy, part, x, y, tileset, Mode2.EASY);
		
		if (checkCluster(target.difficult.variant))
			resolveCluster(target.difficult, part, x, y, tileset, Mode2.DIFFICULT);
		
		if (target.easy.variant == target.difficult.variant && target.easy.clusterIndex.equals(target.difficult.clusterIndex))
			target.difficult = target.easy;
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
		final WoCall    easyCall    = data.getWorldCall(Mode2.EASY);
		final WoCall    diffCall    = data.getWorldCall(Mode2.DIFFICULT);
		final int       easyWidth   = easyCall.getWidth(Mode.EASY);
		final int       diffWidth   = diffCall.getWidth(Mode.DIFFICULT);
		final int       easyHeight  = easyCall.getHeight(Mode.EASY);
		final int       diffHeight  = diffCall.getHeight(Mode.DIFFICULT);
		final int       width       = easyWidth  >= diffWidth  ? easyWidth  : diffWidth;
		final int       height      = easyHeight >= diffHeight ? easyHeight : diffHeight;
		
		world = new TileData[width][height];
		defaultTile = Tile.composeMode(easyCall.getDefaultTile(Mode2.EASY), diffCall.getDefaultTile(Mode2.DIFFICULT));
		
		// resolve tiles (declaration -> tile-set reference)
		if (easyCall == diffCall)
		{
			for (int x = 0; x < width; ++x)
			{
				for (int y = 0; y < height; ++y)
				{
					world[x][y] = new TileData(easyCall.getTile(x+1, y+1));
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
					easyTile = easyCall.getTile(x+1, y+1);
					diffTile = diffCall.getTile(x+1, y+1);
					if (easyTile == null) easyTile = new Tile();
					if (diffTile == null) diffTile = new Tile();
					world[x][y] = new TileData(Tile.composeMode(easyTile, diffTile));
					world[x][y].resolveTile(tileset, defaultTile);
				}
			}
		}
		
		// resolve cluster images
		for (int x = 0; x < width; ++x)
		{
			for (int y = 0; y < height; ++y)
			{
				resolveCluster(world[x][y].floor, world[x][y].tile.st(), x, y, tileset);
				resolveCluster(world[x][y].item,  world[x][y].tile.st(), x, y, tileset);
				resolveCluster(world[x][y].actor, world[x][y].tile.st(), x, y, tileset);
				resolveCluster(world[x][y].stone, world[x][y].tile.st(), x, y, tileset);
			}
		}
	}
}

