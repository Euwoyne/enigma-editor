
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

import enigma_edit.lua.res.Tiles;

/**
 * Multi-mode resolver instances.
 * @see Resolver
 */
public class MMResolver extends MM<Resolver> implements Resolver
{
	public MMResolver(Resolver normal)                       {super(normal);}
	public MMResolver(Resolver easy, Resolver difficult)     {super(easy, difficult);}
	public MMResolver(MMResolver easy, MMResolver difficult) {super(easy.easy, difficult.difficult);}
	
	@Override public String      typename() {return "resolver";}
	@Override public MMResolver  snapshot() {return this;};
	@Override public CodeSnippet getCode()  {throw new UnsupportedOperationException("The class 'MMResolver' has no associated code.");};
	
	@Override
	public Tile resolve(String key, Mode mode)
	{
		switch (mode)
		{
		case EASY:      return (easy != null)      ? easy.resolve(key, mode)      : null;
		case DIFFICULT: return (difficult != null) ? difficult.resolve(key, mode) : null;
		case NORMAL:
			if (easy == difficult)
				return easy.resolve(key, mode);
			Tile tile = new Tile();
			if (easy != null)      tile.add(easy.resolve(key, Mode.EASY), Mode.EASY);
			if (difficult != null) tile.add(difficult.resolve(key, Mode.EASY), Mode.DIFFICULT);
			return tile;
		}
		return null;
	}
	
	@Override
	public String reverse(Tile tile, Mode2 mode)
	{
		if (mode == Mode2.EASY) return (easy != null) ? easy.reverse(tile, mode) : null;
		return (difficult != null) ? difficult.reverse(tile, mode) : null;
	}
	
	@Override
	public Tiles getTiles(Mode2 mode)
	{
		if (mode == Mode2.EASY) return (easy != null) ? easy.getTiles(mode) : null;
		return (difficult != null) ? difficult.getTiles(mode) : null;
	}
}

