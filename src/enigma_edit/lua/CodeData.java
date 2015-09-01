
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

package enigma_edit.lua;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.luaj.vm2.LuaValue;

import enigma_edit.lua.data.*;
import enigma_edit.lua.res.Autotile;
import enigma_edit.lua.res.Composer;
import enigma_edit.lua.res.Puzzle;
import enigma_edit.lua.res.Tiles;

/**
 * This class represents the state of a lua program.
 * Furthermore for each identifier there is info about the origin
 * of its current value as well as the distinction between the
 * different code paths taken for different Enigma Level difficulties.
 */
public class CodeData implements Iterable<Entry<String, Variable>>
{
	private TreeMap<String, CodeSnippet> functionMap;
	private Table                        varMap;
	private WoCallAPI20                  easyWo;
	private WoCallAPI20                  difficultWo;
	
	/**
	 * Initializes common values.
	 * @see #CodeData()
	 */
	private void initialize()
	{
		// create difficulty values
		final SimpleValue valTrue  = new SimpleValue(LuaValue.valueOf(true),  CodeSnippet.NONE);
		final SimpleValue valFalse = new SimpleValue(LuaValue.valueOf(false), CodeSnippet.NONE);
		
		varMap.assign("wo", new Table(CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		((Table)varMap.get("wo").checkValue(Mode2.EASY)).assign("IsDifficult",     valFalse, CodeSnippet.NONE, Mode.EASY);
		((Table)varMap.get("wo").checkValue(Mode2.EASY)).assign("IsDifficult",     valTrue,  CodeSnippet.NONE, Mode.DIFFICULT);
		((Table)varMap.get("wo").checkValue(Mode2.EASY)).assign("CreatingPreview", valFalse, CodeSnippet.NONE, Mode.NORMAL);
		varMap.assign("difficult", valFalse, CodeSnippet.NONE, Mode.EASY);
		varMap.assign("difficult", valTrue,  CodeSnippet.NONE, Mode.DIFFICULT);
		
		// create API constants
		Constants.initialize(this);
		
		// create tile repository
		varMap.assign("ti", new Tiles(), CodeSnippet.NONE, Mode.NORMAL);
		
		// create api dummys
		varMap.assign("no",     new Table(CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		varMap.assign("enigma", new Table(CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		
		// create resolvers
		Table res = new Table(CodeSnippet.NONE);
		res.assign("composer", new SimpleValue(LuaValue.userdataOf(Composer.constructor()), CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		res.assign("autotile", new SimpleValue(LuaValue.userdataOf(Autotile.constructor()), CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		res.assign("puzzle",   new SimpleValue(LuaValue.userdataOf(Puzzle.constructor()),   CodeSnippet.NONE), CodeSnippet.NONE, Mode.NORMAL);
		varMap.assign("res", res, CodeSnippet.NONE, Mode.NORMAL);
	}
	
	/**
	 * Constructor, that initializes this instance with common values.
	 * A table {@code wo} and the difficulty constants {@code difficult}
	 * and {@code wo["IsDifficult"]} (of type {@link SimpleValue} wrapping a lua boolean)
	 * are created. API constants are set (see {@link Constants#initialize(CodeData)}).
	 * The tile repository resolver {@code ti} is created (single instance of
	 * {@link enigma_edit.lua.res.Tiles Tiles}) and a table {@code res} is populated
	 * with the constructors for the resolvers {@link enigma_edit.lua.res.Composer Composer},
	 * {@link enigma_edit.lua.res.Autotile Autotile} and {@link enigma_edit.lua.res.Puzzle Puzzle}.
	 */
	public CodeData()
	{
		this.functionMap  = new TreeMap<String, CodeSnippet>();
		this.varMap       = new Table(CodeSnippet.NONE);
		this.easyWo       = null;
		this.difficultWo  = null;
		initialize();
	}
	
	/**
	 * Assigns a value to the given variable.
	 * 
	 * @param key     Name of the variable.
	 * @param rhs   Value to be assigned.
	 * @param assign  Assignment source code.
	 * @param mode    Mode to use the value for.
	 * @return        The updated variable instance.
	 */
	public Variable assign(String key, Source rhs, CodeSnippet assign, Mode mode)
	{
		return varMap.assign(key, rhs, assign, mode);
	}
	
	/**
	 * Get the value of a given variable completely dereferencing all
	 * references. @see NonValue#getValue(Mode)
	 * 
	 * @param key   Name of the variable.
	 * @param mode  Mode to fetch the value for.
	 * @return      The {@link Value} instance referenced by the identifier.
	 */
	public Value getValue(String key, Mode2 mode)
	{
		return varMap.getValue(key, mode);
	}
	
	/**
	 * Get the value assigned to the field of the given index.
	 * @see Dereferencable#deref(CodeData.Mode2)
	 * 
	 * @param key   Name of the variable.
	 * @param mode  Mode to fetch the value for.
	 * @return      The variable data.
	 */
	public Source deref(String key, Mode2 mode)
	{
		return varMap.deref(key, mode);
	}
	
	/**
	 * Get the value of a given variable completely dereferencing all
	 * references.
	 * 
	 * @param key   Name of the variable.
	 * @return      The {@link Value} instance referenced by the identifier.
	 */
	public MMValue getValue(String key)
	{
		return varMap.getValue(key);
	}
	
	/**
	 * Get the variable instance represented by the given identifier.
	 * 
	 * @param key   Name of the variable.
	 * @return      The corresponding {@link Variable} instance.
	 */
	public Variable get(String key)
	{
		return varMap.get(key);
	}
	
	/**
	 * Get a reference to the given variable.
	 * 
	 * @param key   Name of the variable.
	 * @param code  The referencing code part.
	 * @return      A new {@link Reference}.
	 */
	public Reference getReference(String key, CodeSnippet code)
	{
		return new Reference(varMap.get(key), code);
	}
	
	/**
	 * Check, whether a variable has been defined.
	 * 
	 * @param name  Name of the variable.
	 * @return      Existence of the given identifier. 
	 */
	public boolean exist(String name)
	{
		return varMap.exist(name);
	}
	
	/**
	 * Reset this instance to just the default data.
	 * @see CodeData#CodeData()
	 */
	public void clear()
	{
		functionMap.clear();
		varMap.clear();
		initialize();
	}
	
	/**
	 * Create an iterator for the underlying Map.
	 * @return  A new iterator instance.
	 */
	public Iterator<Entry<String, Variable>> iterator()
	{
		return varMap.iterator();
	}
	
	/**
	 * Get the code location of a function of the given name.
	 * Returns {@code null}, if the function does not exist.
	 * 
	 * @param name  Name of the function.
	 * @return      Location of the function definition.
	 */
	public CodeSnippet getFunction(String name)
	{
		return functionMap.get(name);
	}
	
	/**
	 * Add a new function definition to the data.
	 * 
	 * @param name  Name of the function.
	 * @param code  Function declaration and definition block.
	 */
	public void addFunction(String name, CodeSnippet code)
	{
		functionMap.put(name, code);
	}
	
	/**
	 * Setup the world call for the given mode.
	 * 
	 * @param world  World call for the mode.
	 * @param mode   Mode to execute the world call in.
	 */
	public void setWorld(WoCallAPI20 world, Mode mode)
	{
		switch (mode)
		{
		case EASY:      easyWo = world; break;
		case DIFFICULT: difficultWo = world; break;
		case NORMAL:    easyWo = difficultWo = world; break;
		}
	}
	
	/**
	 * Setup the world call for an empty level.
	 * Represents the call {@code wo(resolver, defaultkey, width, height)}.
	 * 
	 * @param resolver    Resolver instance (already checked by the {@link CodeAnalyser}).
	 * @param defaultkey  Default tile key (should be a string).
	 * @param width       Width of the new world (should be an integer).
	 * @param height      Height of the new world (should be an integer).
	 * @param mode        Mode that {@code wo} was called in.
	 * @param code        Code snippet containing the call.
	 */
	public void setWorld(Resolver resolver, Source defaultkey, Source width, Source height, Mode mode, CodeSnippet code)
	{
		this.setWorld(new WoCallAPI20(resolver, defaultkey, width, height, mode, code), mode);
	}
	
	/**
	 * Setup the world call for a mapped level.
	 * Represents the call {@code wo(resolver, defaultkey, map)}.
	 * 
	 * @param resolver    Resolver instance (already checked by the {@link CodeAnalyser}).
	 * @param defaultkey  Default tile key (should be a string).
	 * @param map         World map (should be a table of strings).
	 * @param mode        Mode that {@code wo} was called in.
	 * @param code        Code snippet containing the call.
	 */
	public void setWorld(Resolver resolver, Source defaultkey, Source map, Mode mode, CodeSnippet code)
	{
		this.setWorld(new WoCallAPI20(resolver, defaultkey, map, mode, code), mode);
	}
	
	/**
	 * Setup the world call for a level created by libmap.
	 * Represents the call {@code wo(resolver, libmap)}.
	 * 
	 * @param resolver  Resolver instance (already checked by the {@link CodeAnalyser}).
	 * @param libmap    World map (should be an instance of {@link LibmapMap}).
	 * @param mode      Mode that {@code wo} was called in.
	 * @param code      Code snippet containing the call.
	 */
	public void setWorld(Resolver resolver, Source libmap, Mode mode, CodeSnippet code)
	{
		this.setWorld(new WoCallAPI20(resolver, libmap, mode, code), mode);
	}
	
	/**
	 * Return the world call data.
	 * @return  The world call data as set by any of the {@code setWorld} methods.
	 */
	public WoCallAPI20 getWorldCall(Mode2 mode)
	{
		return mode == Mode2.EASY ? easyWo : difficultWo;
	}
	
	/**
	 * Check if there is any world call.
	 * @return {@code true}, if the world had been set by any of the {@code setWorld} methods.
	 */
	public boolean hasWorld(Mode mode)
	{
		switch (mode)
		{
		case DIFFICULT: return difficultWo != null;
		case EASY:      return easyWo != null;
		case NORMAL:    return difficultWo != null && easyWo != null;
		default:        return false;
		}
	}
	
	/**
	 * Check if there is any world call.
	 * @return {@code true}, if the world had been set by any of the {@code setWorld} methods.
	 */
	public boolean hasWorld()
	{
		return difficultWo != null && easyWo != null;
	}
	
	/**
	 * Dump all data to {@code System.out}.
	 */
	public void dump()
	{
		for(Map.Entry<String, Variable> entry : varMap)
		{
			if (entry.getValue() == null)
				System.out.println(entry.getKey() + " = null");
			else if (entry.getKey().equals("\"ti\"") || entry.getValue().hasAssign())
			{
				if (entry.getValue().hasNormal() && entry.getValue().easy instanceof Table)
					((Table)entry.getValue().easy).dump(entry.getKey().substring(1, entry.getKey().length()-1));
				else
					System.out.println(entry.getKey().substring(1, entry.getKey().length()-1) + " = " + entry.getValue());
			}
		}
		if (easyWo == difficultWo)
			System.out.println(easyWo);
		else
			System.out.println("if difficult then\n    " + difficultWo + "\nelse\n    " + easyWo + "\nend\n");
	}
}

