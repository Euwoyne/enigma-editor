
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

/**
 * This is a representation of the value of an identifier.
 * This class contains the locations of the last assignment to a
 * variable and its resulting content each for both easy- and
 * difficult-mode.
 */
public class Variable extends MM<Source> implements Assignable
{
	private CodeSnippet easyAssign;
	private CodeSnippet difficultAssign;
	
	/**
	 * Private constructor (given each single member).
	 * 
	 * @param easy             Easy-mode value. 
	 * @param easyAssign       Reference to the assignment of the easy-mode value.
	 * @param difficult        Difficult-mode value.
	 * @param difficultAssign  Reference to the assignment of the difficult-mode value.
	 */
	private Variable(Source easy, CodeSnippet easyAssign, Source difficult, CodeSnippet difficultAssign)
	{
		super(easy, difficult);
		this.easyAssign      = easyAssign;
		this.difficultAssign = difficultAssign;
	}
	
	/**
	 * Variable creation (with easy- and difficult-mode in one assignment).
	 * This occurs by using a call to the lua API function {@code cond}. 
	 * 
	 * @param easy       Easy-mode value. 
	 * @param difficult  Difficult-mode value.
	 * @param assign     Reference to the assignment code.
	 */
	public Variable(Source easy, Source difficult, CodeSnippet assign)
	{
		super(easy, difficult);
		this.easyAssign      = assign;
		this.difficultAssign = assign;
	}
	
	/**
	 * Normal variable creation.
	 * 
	 * @param value   Initial value.
	 * @param assign  Reference to the assignment code.
	 * @param mode    Mode, the variable is created in.
	 */
	public Variable(Source value, CodeSnippet assign, Mode mode)
	{
		super(
			(mode != Mode.DIFFICULT) ? value : null,
			(mode != Mode.EASY)      ? value : null
		);
		switch (mode)
		{
		case EASY:
			this.easyAssign      = assign;
			this.difficultAssign = null;
		    break;
		case DIFFICULT:
			this.easyAssign      = null;
			this.difficultAssign = assign;
			break;
		case NORMAL:
			this.easyAssign      = assign;
			this.difficultAssign = assign;
			break;
		}
	}
	
	@Override
	public void assign(Source value, CodeSnippet assign, Mode mode)
	{
		switch (mode)
		{
		case EASY:      this.easy            = value;
		                this.easyAssign      = assign; break;
		
		case DIFFICULT: this.difficult       = value;
		                this.difficultAssign = assign; break;
		
		case NORMAL:    this.easy            = value;
                        this.easyAssign      = assign;
		                this.difficult       = value;
                        this.difficultAssign = assign; break;
		}
	}
	
	@Override
	public Source deref(Mode2 mode)
	{
		switch (mode)
		{
		case EASY:      return easy;
		case DIFFICULT: return difficult;
		default:        return hasNormal() ? easy : null;
		}
	}
	
	@Override
	public String typename(Mode2 mode)
	{
		switch (mode)
		{
		case EASY:      return hasEasy()      ? easy.typename()      : "null";
		case DIFFICULT: return hasDifficult() ? difficult.typename() : "null";
		default:        return hasNormal()    ? easy.typename()      : "null";
		}
	}
	
	/**
	 * Fetch the assignment code snippet for the given mode.
	 * 
	 * @param mode  Mode to fetch the assignment location for.
	 * @return      Code reference to the last assignment.
	 */
	public CodeSnippet getAssign(Mode mode)
	{
		switch (mode)
		{
		case EASY:      return easyAssign;
		case DIFFICULT: return difficultAssign;
		default:        return (difficultAssign == easyAssign) ? easyAssign : null;
		}
	}
	
	/**
	 * Check, if there is an assignment associated with the current value.
	 */
	public boolean hasAssign()
	{
		return (!hasEasy() || !easyAssign.isNone()) && (!hasDifficult() && !difficultAssign.isNone());
	}
	
	/**
	 * Check, if this variable was assigned in one single statement.
	 * @return {@code true}, if the assign code locations are identical.
	 */
	public boolean isSingleAssigned() {return difficultAssign == easyAssign;}
	
	@Override
	public String typename()
	{
		return "<variable>";
	}
	
	@Override
	public Variable snapshot()
	{
		return new Variable(easy, easyAssign, difficult, difficultAssign);
	}
	
	@Override
	public String toString()
	{
		if (isNull())    return "nil";
		if (hasNormal()) return easy.toString();
		return "cond(wo[\"IsDifficult\"], "
			+ (hasDifficult() ? difficult.toString() : "nil") + ", "
			+ (hasEasy()      ? easy.toString()      : "nil") + ")";
	}
	
	public String toString(Mode mode)
	{
		switch (mode)
		{
		case EASY:      return hasEasy()      ? easy.toString()      : "nil";
		case DIFFICULT: return hasDifficult() ? difficult.toString() : "nil";
		default:        return toString();
		}
	}
}

