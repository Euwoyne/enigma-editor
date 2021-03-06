
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

package com.github.euwoyne.enigma_edit.lua.data;

import java.util.List;

import com.github.euwoyne.enigma_edit.error.LevelLuaException;
import com.github.euwoyne.enigma_edit.lua.ReverseInfo;
import com.github.euwoyne.enigma_edit.lua.res.Tiles;

/**
 * Interface of a resolver.
 * A resolver is used to compute a {@link TileDecl tile declaration} from a
 * string in the world map. To enable editing, we also require the reverse
 * process of determining the key string from a given tile declaration.
 */
public interface Resolver extends Source
{
	/**
	 * Resolve the given {@code key} to a tile declaration.
	 * 
	 * @param key   Key to resolve.
	 * @param mode  Difficulty mode to use.
	 * @return      The requested tile.
	 */
	public Tile resolve(String key, Mode mode);
	
	/**
	 * Set the appropriate key in the given {@link ReverseInfo} instance.
	 * If the indicated tile can be constructed by this resolver, the return
	 * value is equal to the requested types as given by {@link ReverseInfo#typeMask()}.
	 * Otherwise, a partial resolving is attempted. The key will only be set for
	 * some of the parts of the requested tile. The return value indicates,
	 * which parts have been resolved. 
	 * 
	 * @param info  Reversal information structure.
	 * @return      The type-mask of the resolved parts.
	 */
	public int reverse(ReverseInfo info);
	
	/**
	 * Get the tile repository used by this resolver.
	 * This method might return {@code null}, if the final resolver is a custom
	 * resolver function.
	 * 
	 * @param mode  Difficulty mode to use.
	 * @return      ImageTile repository used by this resolver.
	 */
	public Tiles getTiles(Mode2 mode);
	
	/**
	 * Get the next resolver in the chain.
	 * This method might return {@code null}, if this is the final resolver.
	 * Especially the {@link Tiles} resolver returns {@code null}.
	 * 
	 * @param mode  Difficulty mode to use.
	 * @return      This resolver's subresolver.
	 */
	public Resolver getSubresolver(Mode2 mode);
	
	/**
	 * Resolver constructor interface.
	 * This interface re-declares the {@link ApiFunction#call(List, Mode, CodeSnippet) call}
	 * method to return a resolver. Every class implementing the Resolver interface
	 * should have a static method {@code Constructor constructor()}, that returns an
	 * implementation of this interface.
	 */
	public static interface Constructor extends ApiFunction
	{
		/**
		 * Creates a resolver.
		 * 
		 * @param args  Arguments to the resolver constructor.
		 * @param mode  Mode that the constructor is called in.
		 * @param code  Location of the constructor call.
		 * @return      A newly constructed resolver instance.
		 * 
		 * @throws LevelLuaException.Runtime  Typically thrown, if the given arguments ({@code args}) are invalid.
		 */
		@Override
		public Resolver call(List<Source> args, Mode mode, CodeSnippet code) throws LevelLuaException.Runtime;
	}
	
	/**
	 * Checks, if the given source is a resolver (helper function).
	 * If it is not, a {@link LevelLuaException.Runtime} with id {@code "IllegalSubresolver"} is thrown.
	 * 
	 * @param source  Sub-resolver source.
	 * @param mode    Current mode.
	 * @return        The result of {@link Source#checkResolver(Mode)}.
	 */
	public static MMResolver getSubresolver(Source source, Mode mode) throws LevelLuaException.Runtime
	{
		final MMResolver resolver = source.checkResolver(mode); 
		if (resolver.isNull(mode))
		{
			if (mode != Mode.NORMAL)
				throw new LevelLuaException.Runtime("IllegalSubresolver", mode, source.typename(mode.mode2()), source.getCode());
			else if (resolver.isNull())
				throw new LevelLuaException.Runtime("IllegalSubresolver", source.typename(Mode2.DIFFICULT), source.getCode());
			else if (resolver.hasDifficult())
				throw new LevelLuaException.Runtime("IllegalSubresolver", Mode.EASY, source.typename(Mode2.EASY), source.getCode());
			else
				throw new LevelLuaException.Runtime("IllegalSubresolver", Mode.DIFFICULT, source.typename(Mode2.DIFFICULT), source.getCode());
		}
		return resolver;
	}
}

