
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

import java.util.List;

import enigma_edit.error.LevelLuaException;
import enigma_edit.lua.data.CodeSnippet;
import enigma_edit.lua.data.MMResolver;
import enigma_edit.lua.data.MMSimpleValue;
import enigma_edit.lua.data.Mode;
import enigma_edit.lua.data.Mode2;
import enigma_edit.lua.data.Resolver;
import enigma_edit.lua.data.Source;
import enigma_edit.lua.data.SourceData;
import enigma_edit.lua.data.Tile;

public class Composer extends SourceData implements Resolver
{
	private final MMResolver subresolver;
	//private final Source     sequence;
	private final String     easySequence;
	private final String     difficultSequence;
	
	@Override public Tiles    getTiles(Mode2 mode)      {return subresolver.deref(mode).getTiles(mode);}
	@Override public String   typename()                {return "res.composer";}
	@Override public Resolver checkResolver(Mode2 mode) {return this;}
	
	public static Constructor constructor()
	{
		return new Constructor()
		{
			@Override public String toString() {return "res.composer";}
			
			@Override
			public Resolver call(List<Source> args, Mode mode, CodeSnippet code) throws LevelLuaException.Runtime
			{
				// check argument count
				if (args.isEmpty())
					throw new LevelLuaException.Runtime("IllegalComposerArgumentCount", code);
				
				// check subresolver
				final MMResolver resolver = Resolver.getSubresolver(args.get(0), mode); 
				
				// check sequence
				if (args.size() < 2)
					return new Composer(resolver, code);
				
				final MMSimpleValue sequence = args.get(1).checkSimple(mode);
				if (sequence.isNull(mode))
				{
					if (mode != Mode.NORMAL)
						throw new LevelLuaException.Runtime("IllegalComposerSequence", mode, args.get(1).typename(mode.mode2()), args.get(0).getCode());
					else if (resolver.isNull())
						throw new LevelLuaException.Runtime("IllegalComposerSequence", args.get(1).typename(Mode2.DIFFICULT), args.get(0).getCode());
					else if (resolver.hasDifficult())
						throw new LevelLuaException.Runtime("IllegalComposerSequence", Mode.EASY, args.get(1).typename(Mode2.EASY), args.get(0).getCode());
					else
						throw new LevelLuaException.Runtime("IllegalComposerSequence", Mode.DIFFICULT, args.get(1).typename(Mode2.DIFFICULT), args.get(0).getCode());
				}
				
				// create resolver
				return new Composer(resolver, args.get(1), code, mode);
			}
		};
	}
	
	public Composer(MMResolver subresolver, CodeSnippet code)
	{
		super(code);
		this.subresolver       = subresolver;
		//this.sequence          = new SimpleValue(LuaString.valueOf("123456789"), CodeSnippet.NONE);
		this.easySequence      = "123456789";
		this.difficultSequence = "123456789";
	}
	
	public Composer(MMResolver subresolver, Source sequence, CodeSnippet code, Mode mode)
	{
		super(code);
		this.subresolver       = subresolver;
		//this.sequence          = sequence.snapshot();
		this.easySequence      = mode == Mode.DIFFICULT ? sequence.checkSimple(Mode.DIFFICULT).toString() : sequence.checkSimple(Mode.EASY).toString();
		this.difficultSequence = mode == Mode.EASY      ? sequence.checkSimple(Mode.EASY).toString()      : sequence.checkSimple(Mode.DIFFICULT).toString();
	}
	
	public void resolve(String key, Tile target, String seq, Mode mode)
	{
		Tile temp;
		StringBuilder str = new StringBuilder();;
		for (char c = '1'; c <= '0' + key.length(); ++c)
		{
			for (int i = 0; i < seq.length() && i < key.length(); ++i)
			{
				if (seq.charAt(i) == c)
					str.append(key.charAt(i));
				else
					str.append(' ');
			}
			while (str.length() < key.length())
				str.append(' ');
			temp = subresolver.resolve(str.toString(), mode);
			if (temp != null)
				target.add(temp, mode);
			str.delete(0, str.length());
		}
	}
	
	@Override
	public Tile resolve(String key, Mode mode)
	{
		Tile tile = subresolver.resolve(key, mode);
		if (!tile.isNull())
			return tile;
		else
			tile = new Tile();
		
		if (easySequence.equals(difficultSequence))
		{
			resolve(key, tile, easySequence, mode);
		}
		else
		{
			if (mode != Mode.DIFFICULT) resolve(key, tile, easySequence,      Mode.EASY);
			if (mode != Mode.EASY)      resolve(key, tile, difficultSequence, Mode.DIFFICULT);
		}
		
		return tile;
	}
	
	@Override
	public String reverse(Tile tile, Mode2 mode)
	{
		// TODO: implement reverse tile lookup (for 'res.composer')
		return subresolver.deref(mode).reverse(tile, mode);
	}
	
	@Override
	public String toString()
	{
		if (easySequence == difficultSequence)
			return "res.composer(" + subresolver + (easySequence.equals("123456789") ? ")" : ", \"" + easySequence + "\")");
		return "res.composer(" + subresolver + ", cond(wo[\"IsDifficult\"], " + difficultSequence + ", " + easySequence + "))";
	}
}

