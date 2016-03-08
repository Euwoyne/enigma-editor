
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

import java.util.HashSet;
import java.util.List;

import org.luaj.vm2.LuaValue;

import com.github.euwoyne.enigma_edit.error.LevelLuaException;
import com.github.euwoyne.enigma_edit.lua.ReverseInfo;
import com.github.euwoyne.enigma_edit.lua.data.*;

public class Puzzle extends SourceData implements Resolver
{
	private final MMResolver      subresolver;
	private final HashSet<String> easyKeys;
	private final HashSet<String> difficultKeys;
	
	@Override public Tiles    getTiles(Mode2 mode)      {return subresolver.deref(mode).getTiles(mode);}
	@Override public String   typename()                {return "res.puzzle";}
	@Override public Resolver checkResolver(Mode2 mode) {return this;}
	
	public static Constructor constructor()
	{
		return new Constructor()
		{
			@Override public String toString() {return "res.puzzle";}
			
			@Override
			public Resolver call(List<Source> args, Mode mode, CodeSnippet code) throws LevelLuaException.Runtime
			{
				// check argument count
				if (args.isEmpty())
					throw new LevelLuaException.Runtime("IllegalPuzzleArgumentCount", code);
				
				// check subresolver
				final MMResolver resolver = Resolver.getSubresolver(args.get(0), mode); 
				
				// create resolver
				Puzzle res = new Puzzle(resolver, code);
				
				// add tiles
				for (Source arg : args.subList(1, args.size()))
					res.addRule(arg, mode);
					
				// create resolver
				return res;
			}
		};
	}
	
	public Puzzle(MMResolver subresolver, CodeSnippet code)
	{
		super(code);
		this.subresolver   = subresolver;
		this.easyKeys      = new HashSet<String>();
		this.difficultKeys = new HashSet<String>();
	}
	
	public void addRule(Source source, Mode mode)
	{
		// check source
		final MMSimpleValue key = source.checkSimple(mode);
		if (key.isNull(mode))
			throw new LevelLuaException.Runtime("IllegalPuzzleRule", mode, source.typename(mode), source.getCode());
		
		// add key
		if (mode != Mode.DIFFICULT && key.hasEasy())
				easyKeys.add(key.easy.value.checkjstring());
		
		if (mode != Mode.EASY && key.hasDifficult())
				difficultKeys.add(key.difficult.value.checkjstring());
	}
	
	public static void createPuzzle(String key, Tile tile, Table decl, Mode mode)
	{
		// check declaration 
		if (!decl.exist(1)) return;
		final MMSimpleValue kind = decl.get(1).checkSimple(mode);
		if (kind.isNull(mode)) return;
		
		// split call, if there are different kind on different modes
		if (mode == Mode.NORMAL && !kind.isNormal())
		{
			if (kind.hasEasy())      createPuzzle(key, tile, decl, Mode.EASY);
			if (kind.hasDifficult()) createPuzzle(key, tile, decl, Mode.DIFFICULT);
			return;
		}
		
		// check, if the given stone is a puzzle stone
		if (!kind.get(mode).value.checkjstring().startsWith("st_puzzle"))
			return;
		
		// generate connections
		final Table table = decl.snapshot();
		final char c = key.charAt(key.length()-1);
		if (c >= '0' && c <= '9')
		{
			table.assign("cluster", new SimpleValue(LuaValue.valueOf((int)(c - '0')), CodeSnippet.NONE), CodeSnippet.NONE, mode);
			table.assign("connections", new Nil(CodeSnippet.NONE), CodeSnippet.NONE, mode);
		    table.assign("hollow",  new SimpleValue(LuaValue.valueOf(c > '4'), CodeSnippet.NONE), CodeSnippet.NONE, mode);
		}
		else if (c >= 'a' && c <= 'o' || c >= 'A' && c <= 'O')
		{
			table.assign("cluster", new Nil(CodeSnippet.NONE), CodeSnippet.NONE, mode);
			switch (c)
			{
			case 'a': case 'A': table.assign("connections", new SimpleValue(LuaValue.valueOf("w"),    CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'b': case 'B': table.assign("connections", new SimpleValue(LuaValue.valueOf("s"),    CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'c': case 'C': table.assign("connections", new SimpleValue(LuaValue.valueOf("sw"),   CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'd': case 'D': table.assign("connections", new SimpleValue(LuaValue.valueOf("e"),    CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'e': case 'E': table.assign("connections", new SimpleValue(LuaValue.valueOf("ew"),   CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'f': case 'F': table.assign("connections", new SimpleValue(LuaValue.valueOf("es"),   CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'g': case 'G': table.assign("connections", new SimpleValue(LuaValue.valueOf("esw"),  CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'h': case 'H': table.assign("connections", new SimpleValue(LuaValue.valueOf("n"),    CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'i': case 'I': table.assign("connections", new SimpleValue(LuaValue.valueOf("nw"),   CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'j': case 'J': table.assign("connections", new SimpleValue(LuaValue.valueOf("ns"),   CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'k': case 'K': table.assign("connections", new SimpleValue(LuaValue.valueOf("nsw"),  CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'l': case 'L': table.assign("connections", new SimpleValue(LuaValue.valueOf("ne"),   CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'm': case 'M': table.assign("connections", new SimpleValue(LuaValue.valueOf("new"),  CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'n': case 'N': table.assign("connections", new SimpleValue(LuaValue.valueOf("nes"),  CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			case 'o': case 'O': table.assign("connections", new SimpleValue(LuaValue.valueOf("nesw"), CodeSnippet.NONE), CodeSnippet.NONE, mode); break;
			}
		    table.assign("hollow",  new SimpleValue(LuaValue.valueOf(c < 'a'), CodeSnippet.NONE), CodeSnippet.NONE, mode);
		}
		else if (c >= 'p' && c <= 'z')
		{
			table.assign("cluster", new SimpleValue(LuaValue.valueOf((int)(c - 'p') + 10), CodeSnippet.NONE), CodeSnippet.NONE, mode);
			table.assign("connections", new Nil(CodeSnippet.NONE), CodeSnippet.NONE, mode);
		    table.assign("hollow",  new SimpleValue(LuaValue.valueOf(false), CodeSnippet.NONE), CodeSnippet.NONE, mode);
		}
		else if (c >= 'P' && c <= 'Z')
		{
			table.assign("cluster", new SimpleValue(LuaValue.valueOf((int)(c - 'P') + 10), CodeSnippet.NONE), CodeSnippet.NONE, mode);
			table.assign("connections", new Nil(CodeSnippet.NONE), CodeSnippet.NONE, mode);
		    table.assign("hollow",  new SimpleValue(LuaValue.valueOf(true), CodeSnippet.NONE), CodeSnippet.NONE, mode);
		}
		
		// set new stone
		tile.substitute(table, mode);
	}
	
	@Override
	public Tile resolve(String key, Mode mode)
	{
		final String subkey = key.substring(0, key.length()-1);
		if (!easyKeys.contains(subkey) && !difficultKeys.contains(subkey))
			return subresolver.resolve(key, mode);
		final Tile tile = subresolver.resolve(subkey, mode);
		final Tile.Part stone = tile.st();
		if (stone.isNull())
			return tile;
		
		if (mode != Mode.NORMAL)
		{
			createPuzzle(key, tile, stone.get(mode).checkTable(mode.mode2()), mode);
		}
		else
		{
			if (stone.hasNormal())
				createPuzzle(key, tile, stone.get(mode).checkTable(mode).easy, mode);
			else
			{
				if (stone.hasEasy())
					createPuzzle(key, tile, stone.get(Mode.EASY).checkTable(Mode2.EASY), Mode.EASY);
				if (stone.hasDifficult())
					createPuzzle(key, tile, stone.get(Mode.DIFFICULT).checkTable(Mode2.DIFFICULT), Mode.DIFFICULT);
			}
		}
		return tile;
	}

	@Override
	public int reverse(ReverseInfo info)
	{
		// TODO: implement reverse tile lookup (for 'res.puzzle')
		return subresolver.reverse(info);
	}
	
	@Override
	public Resolver getSubresolver(Mode2 mode)
	{
		return subresolver.get(mode);
	}
	
	@Override public String toString()
	{
		StringBuilder str = new StringBuilder();
		final boolean normal = easyKeys.equals(difficultKeys);
		if (!normal)
			str.append("cond(wo[\"IsDifficult\"], ");
		
		str.append("res.puzzle(" + subresolver);
		for (String key : easyKeys)
			str.append(", \"" + key + "\"");
		str.append(")");
		
		if (!normal)
		{
			str.append(", res.puzzle(" + subresolver);
			for (String key : difficultKeys)
				str.append(", \"" + key + "\"");
			str.append("))");
		}
		
		return str.toString();
	}
}

