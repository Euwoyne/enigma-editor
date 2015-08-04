
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

import enigma_edit.lua.CodeAnalyser;
import org.luaj.vm2.Lua;

/**
 * An expression within the lua code, that is not interpreted by the {@link CodeAnalyser}.
 * This is any expression at all with the exceptions of equality comparisons of boolean values.
 * TODO: mention multilevel support in comment to Expression
 */
public class Expression extends SourceData
{
	final public int      op;
	final public Source[] arg;
	
	public Expression(int op, Source arg, CodeSnippet code)
	{
		super(code);
		this.op = op;
		this.arg = new Source[2];
		this.arg[0] = arg;
		this.arg[1] = null;
	}
	
	public Expression(int op, Source arg1, Source arg2, CodeSnippet code)
	{
		super(code);
		this.op = op;
		this.arg = new Source[2];
		this.arg[0] = arg1;
		this.arg[1] = arg2;
	}
	
	private String parens(Source op)
	{
		if (op instanceof Expression)
			return "(" + op + ")";
		else if (op == null)
			return "null";
		else
			return op.toString();
	}
	
	public String toString()
	{
		switch (op)
		{
		case Lua.OP_ADD:      return parens(arg[0]) + " + "   + parens(arg[1]);
		case Lua.OP_AND:      return parens(arg[0]) + " and " + parens(arg[1]);
		case Lua.OP_CONCAT:   return parens(arg[0]) + " .. "  + parens(arg[1]);
		case Lua.OP_DIV:      return parens(arg[0]) + " / "   + parens(arg[1]);
		case Lua.OP_EQ:       return parens(arg[0]) + " == "  + parens(arg[1]);
		case Lua.OP_GE:       return parens(arg[0]) + " >= "  + parens(arg[1]);
		case Lua.OP_GT:       return parens(arg[0]) + " > "   + parens(arg[1]);
		case Lua.OP_LE:       return parens(arg[0]) + " <= "  + parens(arg[1]);
		case Lua.OP_LEN:      return                   "#"    + parens(arg[0]);
		case Lua.OP_LT:       return parens(arg[0]) + " < "   + parens(arg[1]);
		case Lua.OP_MOD:      return parens(arg[0]) + " % "   + parens(arg[1]);
		case Lua.OP_MUL:      return parens(arg[0]) + " * "   + parens(arg[1]);
		case Lua.OP_NEQ:      return parens(arg[0]) + " ~= "  + parens(arg[1]);
		case Lua.OP_NOT:      return                   "!"    + parens(arg[0]);
		case Lua.OP_OR:       return parens(arg[0]) + " or "  + parens(arg[1]);
		case Lua.OP_POW:      return parens(arg[0]) + " ^ "   + parens(arg[1]);
		case Lua.OP_SUB:      return parens(arg[0]) + " - "   + parens(arg[1]);
		case Lua.OP_UNM:      return                   "-"    + parens(arg[0]);
		default:              return "OP" + Integer.toString(op) + "(" + arg[0].toString() + ", " + ((arg[1] != null) ? arg[1].toString() : "null") + ")";
		}
	}
	
	@Override public String   typename()            {return "<expression>";}
	@Override public String   typename(Mode2 mode)  {return "<expression>";}
}

