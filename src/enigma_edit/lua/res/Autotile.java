
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.luaj.vm2.LuaValue;

import enigma_edit.error.LevelLuaException;
import enigma_edit.lua.data.CodeSnippet;
import enigma_edit.lua.data.MMResolver;
import enigma_edit.lua.data.MMSimpleValue;
import enigma_edit.lua.data.MMTable;
import enigma_edit.lua.data.Mode;
import enigma_edit.lua.data.Mode2;
import enigma_edit.lua.data.Nil;
import enigma_edit.lua.data.Resolver;
import enigma_edit.lua.data.SimpleValue;
import enigma_edit.lua.data.Source;
import enigma_edit.lua.data.SourceData;
import enigma_edit.lua.data.Table;
import enigma_edit.lua.data.Tile;
import enigma_edit.lua.data.Value;
import enigma_edit.lua.data.Variable;

public class Autotile extends SourceData implements Resolver
{
	private static class Rule
	{
		//Source source;
		String first;
		String last;
		String template;
		int    offset;
	}
	
	private MMResolver      subresolver;
	private ArrayList<Rule> easyRules;
	private ArrayList<Rule> difficultRules;
	private boolean         isNormal;
	
	@Override public Tiles    getTiles(Mode2 mode)      {return subresolver.deref(mode).getTiles(mode);}
	@Override public String   typename()                {return "res.autotile";}
	@Override public Resolver checkResolver(Mode2 mode) {return this;}
	
	public static Constructor constructor()
	{
		return new Constructor()
		{
			@Override public String toString() {return "res.autotile";}
			
			@Override
			public Resolver call(List<Source> args, Mode mode, CodeSnippet code) throws LevelLuaException.Runtime
			{
				// check argument count
				if (args.size() < 2)
					throw new LevelLuaException.Runtime("IllegalAutotileArgumentCount", code);
				
				// check subresolver
				final MMResolver resolver = Resolver.getSubresolver(args.get(0), mode); 
				
				// create resolver
				Autotile res = new Autotile(resolver, code);
				
				// add rules
				for (Source arg : args.subList(1, args.size()))
					res.addRule(arg, mode);
				
				// return resolver
				return res;
			}
		};
	}
	
	public Autotile(MMResolver subresolver, CodeSnippet code)
	{
		super(code);
		this.subresolver    = subresolver;
		this.easyRules      = new ArrayList<Rule>();
		this.difficultRules = new ArrayList<Rule>();
		this.isNormal       = true;
	}
	
	public static Rule getRule(Table table, Source source, Mode2 mode) throws LevelLuaException.Runtime
	{
		Rule          rule  = new Rule();
		SimpleValue[] data  = new SimpleValue[4];
		
		// check table entries
		Value temp;
		for (int i = 0; i < 4; ++i)
		{
			temp = table.getValue(i + 1, mode);
			if (temp == null || temp instanceof Nil)
			{
				if (i < 2) throw new LevelLuaException.Runtime("IllegalAutotileRuleFormat", table.getCode());
				break;
			}
			if (!(temp instanceof SimpleValue))
				throw new LevelLuaException.Runtime("IllegalAutotileRuleValue", temp.typename(), table.getCode());
			data[i] = (SimpleValue)temp;
		}
		
		// {prefix, template}
		if (data[2] == null)
		{
			if (!data[0].value.isstring())
				throw new LevelLuaException.Runtime("IllegalAutotileRulePrefix", data[0].typename(), table.getCode());
			if (!data[1].value.isstring())
				throw new LevelLuaException.Runtime("IllegalAutotileRuleTemplate", data[1].typename(), table.getCode());
			
			//rule.source   = source;
			rule.first    = data[0].value.checkjstring();
			rule.last     = rule.first;
			rule.template = data[1].value.checkjstring();
			rule.offset   = 1; 
		}
		
		// {first, last, template, [offset]}
		else 
		{
			if (!data[0].value.isstring())
				throw new LevelLuaException.Runtime("IllegalAutotileRulePrefix", data[0].typename(), table.getCode());
			if (!data[1].value.isstring())
				throw new LevelLuaException.Runtime("IllegalAutotileRulePrefix", data[1].typename(), table.getCode());
			if (!data[2].value.isstring())
				throw new LevelLuaException.Runtime("IllegalAutotileRuleTemplate", data[2].typename(), table.getCode());
			
			//rule.source   = source;
			rule.first    = data[0].value.checkjstring();
			rule.last     = data[1].value.checkjstring();
			rule.template = data[2].value.checkjstring();
			
			if (rule.first.length() != rule.last.length() || !rule.first.regionMatches(0, rule.last, 0, rule.first.length() - 1))
				throw new LevelLuaException.Runtime("IllegalAutotileRuleRange", table.getCode());
			
			if (data[3] != null)
			{
				if (!data[3].value.isinttype())
					throw new LevelLuaException.Runtime("IllegalAutotileRuleOffset", data[3].typename(), table.getCode());
				rule.offset = data[3].value.checkint();
			}
			else rule.offset = 1;
		}
		
		return rule;
	}
	
	public void addRule(Source source, Mode mode) throws LevelLuaException.Runtime
	{
		// check source
		MMTable table = source.checkTable(mode); 
		if (table.isNull(mode))
			throw new LevelLuaException.Runtime("IllegalAutotileRule", mode, source.typename(mode), source.getCode());
		
		// add rule
		switch (mode)
		{
		case EASY:
			if (table.hasEasy())
				easyRules.add(getRule(table.easy, source, Mode2.EASY));
			isNormal = false;
			break;
		
		case DIFFICULT:
			if (table.hasDifficult())
				difficultRules.add(getRule(table.difficult, source, Mode2.DIFFICULT));
			isNormal = false;
			break;
		
		case NORMAL:
			final Rule easy = table.hasEasy()      ? getRule(table.easy,      source, Mode2.EASY) : null;
			final Rule diff = table.hasDifficult() ? getRule(table.difficult, source, Mode2.EASY) : null;
			easyRules.add(easy);
			if (table.isNormal())
				difficultRules.add(easy);
			else
				difficultRules.add(diff);
			break;
		}
	}
	
	private static boolean substitute(SimpleValue value, Variable var, String sub, Mode mode)
	{
		final String s = value.value.checkjstring();
		final String r = s.replaceAll("%%", "%" + sub);
		if (s.equals(r)) return false;
		var.assign(new SimpleValue(LuaValue.valueOf(r), value.getCode()), var.getAssign(mode), mode);
		return true;
	}
	
	private static boolean substitute(Table table, String sub)
	{
		class SubLoop implements Consumer<Entry<String, Variable>>
		{
			public boolean ret = false;
			
			@Override public void accept(Entry<String, Variable> entry)
			{
				final Variable      var   = entry.getValue();
				final MMSimpleValue value = var.checkSimple();
				if (value.hasNormal() && value.easy.value.isstring())
				{
					ret = ret | substitute(value.easy, var, sub, Mode.NORMAL);
				}
				else
				{
					if (value.hasEasy() && value.easy.value.isstring())
						ret = ret | substitute(value.easy, var, sub, Mode.EASY);
					if (value.hasDifficult() && value.difficult.value.isstring())
						ret = ret | substitute(value.difficult, var, sub, Mode.DIFFICULT);
				}
			}
		};
		
		SubLoop loop = new SubLoop();
		table.forEach(loop);
		return loop.ret;
	}
	
	private static void substitute(MMTable table, String sub, Tile target, Mode mode)
	{
		switch (mode)
		{
		case EASY:
			if (table.hasEasy() && substitute(table.easy, sub))
				target.substitute(table, mode);
			break;
			
		case DIFFICULT:
			if (table.hasDifficult() && substitute(table.difficult, sub))
				target.substitute(table, mode);
			break;
			
		case NORMAL:
			if (table.hasNormal())
			{
				if (substitute(table.easy, sub))
					target.substitute(table, mode);
			}
			else
			{
				if (   ((table.hasEasy())      ? substitute(table.easy, sub)      : false)
				    || ((table.hasDifficult()) ? substitute(table.difficult, sub) : false))
						target.substitute(table, mode);
			}
			break;
		}
	}
	
	private static void substitute(Tile template, String sub, Tile target, Mode mode)
	{
		target.add(template, mode);
		if (template.has_fl(mode)) substitute(template.fl().get(mode).checkTable(mode).snapshot(), sub, target, mode);
		if (template.has_it(mode)) substitute(template.it().get(mode).checkTable(mode).snapshot(), sub, target, mode);
		if (template.has_ac(mode)) substitute(template.ac().get(mode).checkTable(mode).snapshot(), sub, target, mode);
		if (template.has_st(mode)) substitute(template.st().get(mode).checkTable(mode).snapshot(), sub, target, mode);
	}
	
	private Tile applyRules(ArrayList<Rule> rules, String key, Mode mode)
	{
		for (Rule rule : rules)
		{
			if (   key.substring(0, rule.first.length()).compareTo(rule.first) >= 0
				&& key.substring(0, rule.last.length() ).compareTo(rule.last ) <= 0)
			{
				String sub;
				if (rule.first == rule.last)	// {prefix, template}
					sub = key.substring(rule.first.length());
				else							// {first, last, template, [offset]}
					sub = Integer.toString(rule.offset + key.charAt(rule.first.length() - 1) - rule.first.charAt(rule.first.length() - 1));
				
				Tile tile = new Tile();
				substitute(subresolver.resolve(rule.template, mode), sub, tile, mode);
				return tile;
			}
		}
		return null;
	}
	
	@Override
	public Tile resolve(String key, Mode mode)
	{
		Tile tile = null;
		if (isNormal)
		{
			tile = applyRules(easyRules, key, mode);
		}
		else
		{
			switch (mode)
			{
			case EASY:
				tile = applyRules(easyRules, key, mode);
				break;
			
			case DIFFICULT:
				tile = applyRules(difficultRules, key, mode);
				break;
			
			case NORMAL:
				final Tile easy = applyRules(easyRules,      key, Mode.EASY);
				final Tile diff = applyRules(difficultRules, key, Mode.DIFFICULT);
				
				if      (easy == null) tile = diff;
				else if (diff == null) tile = easy;
				else                   tile = Tile.composeMode(easy, diff);
				
				break;
			}
		}
		
		return (tile != null) ? tile : subresolver.resolve(key, mode);
	}
	
	@Override
	public String reverse(Tile tile, Mode2 mode)
	{
		// TODO: implement reverse tile lookup (for 'res.autotile')
		return subresolver.deref(mode).reverse(tile,  mode);
	}
	
	@Override public String toString()
	{
		StringBuilder str = new StringBuilder();
		if (!isNormal)
			str.append("cond(wo[\"IsDifficult\"], ");
		
		str.append("res.autotile(" + subresolver);
		for (Rule rule : easyRules)
		{
			str.append(", {\"" + rule.first + "\"");
			if (!rule.first.equals(rule.last))
				str.append(", \"" + rule.last + "\"");
			str.append(", \"" + rule.template + "\"");
			if (rule.offset != 1)
				str.append(", " + rule.offset + "");
			str.append("}");
		}
		str.append(")");
		
		if (!isNormal)
		{
			str.append(", res.autotile(" + subresolver);
			for (Rule rule : difficultRules)
			{
				str.append(", {\"" + rule.first + "\"");
				if (!rule.first.equals(rule.last))
					str.append(", \"" + rule.last + "\"");
				str.append(", \"" + rule.template + "\"");
				if (rule.offset != 1)
					str.append(", " + rule.offset + "");
				str.append("}");
			}
			str.append("))");
		}
		
		return str.toString();
	}
}

