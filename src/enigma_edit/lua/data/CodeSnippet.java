
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

import org.luaj.vm2.ast.SyntaxElement;

/**
 * Lua source code location.
 * Essentially this stores position information about a part of a string.
 * For normal processing, the start- and end-character positions are stored.
 * To provide human readable information and line extraction, line- and column-
 * values are also stored for the start- and end-position.
 */
public final class CodeSnippet implements Comparable<CodeSnippet>
{
	private final int startLine;
	private final int startColumn;
	private final int startPos;
	private final int endLine;
	private final int endColumn;
	private final int endPos;
	
	/**
	 * Create {@code NONE} code snippet.
	 * This is an instance with all fields set to {@code -1} and represents unidentified
	 * code positions. This constructor is internally used to create the static member
	 * {@link #NONE}.
	 */
	private CodeSnippet()
	{
		this.startLine   = -1;
		this.startColumn = -1;
		this.startPos    = -1;
		this.endLine     = -1;
		this.endColumn   = -1;
		this.endPos      = -1;
	}
	
	/**
	 * Create code snippet with explicitly given member values.
	 * 
	 * @param startLine    Line number of the snippet's start.
	 * @param startColumn  Column number of the snippet's start.
	 * @param startPos     Character offset of the snippet's start.
	 * @param endLine      Line number of the snippet's end.
	 * @param endColumn    Column number of the snippet's end.
	 * @param endPos       Character offset of the snippet's start.
	 */
	private CodeSnippet(int startLine, int startColumn, int startPos, int endLine, int endColumn, int endPos)
	{
		this.startLine   = startLine;
		this.startColumn = startColumn;
		this.startPos    = startPos;
		this.endLine     = endLine;
		this.endColumn   = endColumn;
		this.endPos      = endPos;
	}
	
	/**
	 * Create a code snippet from start- and end-position in the code.
	 * Line- and column-numbers will be calculated by inspecting the code given.
	 * 
	 * @param code      Code this snippet is referencing.
	 * @param startPos  Start position (as character offset).
	 * @param endPos    End position (as character offset).
	 */
	public CodeSnippet(String code, int startPos, int endPos)
	{
		this.startPos = startPos;
		this.endPos   = endPos;
		
		int startLine   = 1;
		int startColumn = 1;
		int endLine     = 1;
		int endColumn   = 1;
		
		int start = 0, end = 0;
		while ((end = code.indexOf('\n', start)) >= 0)
		{
			if      (end   < startPos) ++startLine;
			else if (start < startPos) startColumn = startPos - start + 1;
			if      (end   < endPos)   ++endLine;
			else if (start < endPos)   endColumn = endPos - start;
			else break;
			start = end + 1;
		}
		
		this.startLine   = startLine;
		this.startColumn = startColumn;
		this.endLine     = endLine;
		this.endColumn   = endColumn;
	}
	
	/**
	 * Create a code snippet from line/column pairs given for start- and end-position.
	 * The real character offsets will be calculated by inspecting the given code string.
	 * 
	 * @param code         Code this snippet is referencing.
	 * @param startLine    Line number of the start position.
	 * @param startColumn  Column number of the start position.
	 * @param endLine      Line number of the end position.
	 * @param endColumn    Column number of the end position.
	 */
	public CodeSnippet(String code, int startLine, int startColumn, int endLine, int endColumn)
	{
		this.startLine   = startLine;
		this.startColumn = startColumn;
		this.endLine     = endLine;
		this.endColumn   = endColumn;

		int startPos = 0;
		int endPos   = 0;
		
		int start = 0, end = 0, line = 1;
		while ((end = code.indexOf('\n', start)) >= 0)
		{
			if (line == startLine) startPos = start + startColumn - 1;
			if (line == endLine)   endPos   = start + endColumn;
			start = end + 1;
			++line;
		}
		
		this.startPos = startPos;
		this.endPos   = endPos;
	}
	
	/**
	 * Create a code snippet from a lua syntax element.
	 * The position information will be copied from the {@code luaj} {@link SyntaxElement}
	 * object.
	 *  
	 * @param code     Referenced source code.
	 * @param element  Lua syntax element.
	 */
	public CodeSnippet(String code, SyntaxElement element)
	{
		this(code, element.beginLine, element.beginColumn, element.endLine, element.endColumn);
	}
	
	public int getBeginLine()   {return startLine;}
	public int getBeginColumn() {return startColumn;}
	public int getBeginPos()    {return startPos;}
	public int getEndLine()     {return endLine;}
	public int getEndColumn()   {return endColumn;}
	public int getEndPos()      {return endPos;}
	
	/**
	 * Get the actual text of this code snippet.
	 * 
	 * @param code  The source code this snippet was created for.
	 * @return      The sub-string of the given code as defined by this instance.
	 */
	public String get(String code)
	{
		return isNone() ? "" : code.substring(startPos, endPos);
	}
	
	/**
	 * Get the complete line of code this code snippet starts in.
	 * 
	 * @param code  The source code this snippet was created for.
	 * @return      The line of code, that contains this snippet's start position. 
	 */
	public String getLine(String code)
	{
		return isNone() ? "" : code.substring(code.lastIndexOf('\n', startPos) + 1, code.indexOf('\n', startPos));
	}
	
	/**
	 * Check, if the given snippet is completely part of this one.
	 * 
	 * @param snippet  Snippet to check for being part of this.
	 * @return         {@code true}, if the given snippet lies completely within this one.
	 */
	public boolean contains(CodeSnippet snippet)
	{
		return snippet.startPos >= this.startPos && snippet.endPos <= this.endPos; 
	}
	
	/**
	 * Replace the part of {@code code} defined by this snippet with a given string.
	 * Note, that this operation invalidates every code snippet, that ends after the
	 * start position of this snippet. You must take care to update those, if you want
	 * to keep using them with the updated code string.
	 * 
	 * @param code         Source code this snippet is part of.
	 * @param replacement  String to replace the part of {@code code} defined by this snippet.
	 * @return             Updated code string.
	 */
	public String change(String code, String replacement)
	{
		return isNone() ? code : code.substring(0, startPos).concat(replacement).concat(code.substring(endPos));
	}
	
	/**
	 * Extends this code snippet to contain the given one.
	 * 
	 * @param code  Code snippet to be included in this one.
	 * @return      Extended code snippet.
	 */
	public CodeSnippet extend(CodeSnippet code)
	{
		if (startPos < 0) return code;
		if (code.startPos < 0) return this;
		if (code.startPos >= startPos && code.endPos <= endPos)
			return this;
		if (code.startPos <= startPos && code.endPos >= endPos)
			return code;
		if (code.startPos >= startPos && code.endPos >= endPos)
			return new CodeSnippet(startLine, startColumn, startPos, code.endLine, code.endColumn, code.endPos);
		if (code.startPos <= startPos && code.endPos <= endPos)
			return new CodeSnippet(code.startLine, code.startColumn, code.startPos, endLine, endColumn, endPos);
		return null;
	}
	
	/**
	 * Equality operator.
	 */
	@Override
	public boolean equals(Object o)
	{
		return (o instanceof CodeSnippet &&
			((CodeSnippet)o).startPos    == startPos    &&
			((CodeSnippet)o).endPos      == endPos      &&
			((CodeSnippet)o).startLine   == startLine   &&
			((CodeSnippet)o).endLine     == endLine     &&
			((CodeSnippet)o).startColumn == startColumn &&
			((CodeSnippet)o).endColumn   == endColumn);
	}
	
	/**
	 * Check, if this code snippet is entirely behind another one. 
	 */
	public boolean isBehind(CodeSnippet code)
	{
		return startPos >= code.endPos;
	}
	
	/**
	 * Very rough ordering on the code snippets.
	 */
	@Override
	public int compareTo(CodeSnippet o)
	{
		if (this.startPos == o.startPos && this.endPos == o.endPos) return 0;
		
		if (this.endPos   < o.startPos) return -3;
		if (this.startPos < o.startPos) return -2;
		if (this.startPos > o.endPos)   return  3;
		if (this.endPos   < o.endPos)   return  2;
		
		return 1;
	}
	
	/**
	 * String representation of the start position.
	 * @return  A string of the form {@code line:column}.
	 */
	public String startString()
	{
		if (startLine < 0 || startColumn < 0) return "?:?";
		return Integer.toString(startLine) + ":" + Integer.toString(startColumn);
	}
	
	/**
	 * String representation of the end position.
	 * @return  A string of the form {@code line:column}.
	 */
	public String endString()
	{
		if (endLine < 0 || endColumn < 0) return "?:?";
		return Integer.toString(endLine) + ":" + Integer.toString(endColumn);
	}
	
	/**
	 * An undefined code snippet (used as a kind of {@code null} value).
	 * @see #CodeSnippet()
	 */
	public static final CodeSnippet NONE = new CodeSnippet();
	
	/**
	 * Check, if this code snippet is undefined.
	 * @return  {@code true}, if this has negative start- or end-position.
	 * @see #NONE
	 */
	public boolean isNone()
	{
		return startPos < 0 || endPos < 0;
	}
}

