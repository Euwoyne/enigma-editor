
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

package com.github.euwoyne.enigma_edit.lua;

import java.util.LinkedList;
import java.util.List;
import java.io.StringReader;

import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Block;
import org.luaj.vm2.ast.Chunk;
import org.luaj.vm2.ast.Exp;
import org.luaj.vm2.ast.FuncArgs;
import org.luaj.vm2.ast.FuncBody;
import org.luaj.vm2.ast.Name;
import org.luaj.vm2.ast.NameScope;
import org.luaj.vm2.ast.ParList;
import org.luaj.vm2.ast.Stat;
import org.luaj.vm2.ast.TableConstructor;
import org.luaj.vm2.ast.TableField;
import org.luaj.vm2.ast.Visitor;
import org.luaj.vm2.parser.LuaParser;
import org.luaj.vm2.parser.ParseException;

import com.github.euwoyne.enigma_edit.Log;
import com.github.euwoyne.enigma_edit.error.LevelLuaException;
import com.github.euwoyne.enigma_edit.lua.data.*;
import com.github.euwoyne.enigma_edit.lua.res.Tiles;

/**
 * Analyser for the {@code <luamain>} section of an Enigma level.
 */
public class CodeAnalyser
{
	/**
	 * The given lua source code will be analysed by tracking all
	 * deterministically evaluatable assignments. A reverse dependency
	 * tree is built.
	 * Note that most conditional statements will be ignored, except
	 * those regarding the level difficulty (i.e. those involving the
	 * value of {@code wo["IsDifficult"]} or {@code difficult}).
	 * 
	 * @param code                the level's lua code (i.e. the content of the {@code <luamain>} tag)
	 * @return                    the gathered data (see documentation for {@link CodeData})
	 * @throws ParseException     This indicates a lua syntax error (thrown by the parser).
	 * @throws LevelLuaException  This indicates special runtime errors or constructs, that are illegal in Enigma levels (thrown by the analyser).
	 */
	public static CodeData analyse(String code) throws ParseException, LevelLuaException
	{
		return new CodeAnalyser(code, new Log()
		{
			@Override
			public void log(Msg msg)
			{
				final java.io.PrintStream s;
				switch (msg.type)
				{
				case ERROR:   s = System.err; s.print("ERROR: ");   break;
				case WARNING: s = System.err; s.print("WARNING: "); break;
				case INFO:    s = System.out; s.print("INFO: ");    break;
				default:      s = System.out;
				}
				s.println(msg.message);
				if (!msg.location.isNone())
				{
					s.print("    ");
					s.println(msg.location.getLine(code));
					s.print("    ");
					for (int i = 0; i < msg.location.getBeginColumn(); ++i) System.err.print(' ');
					s.println('^');
				}
			}
		}).analyse();
	}
	
	/*
	 * Private data.
	 */
	private final String   code;	// lua source code
	private final Chunk    chunk;	// parse tree of the code
	private final CodeData data;	// data constructed by the analyser
	
	// Log
	private Log log;
	
	
	/**
	 * Private constructor.
	 * On construction the given {@param code} will be parsed.
	 * 
	 * @param code             the level's lua code (i.e. the content of the {@code <luamain>} tag)
	 * @throws ParseException  This indicates an lua syntax error.
	 */
	private CodeAnalyser(String code, Log log) throws ParseException
	{
		this.code  = code;
		this.chunk = new LuaParser(new StringReader(code)).Chunk();
		this.data  = new CodeData();
		this.log   = log;
	}
	
	/**
	 * Analyse the code which was given on construction.
	 * This uses the {@link AnalyticVisitor} on the chunk created on construction
	 * to generate a new {@link CodeData} instance.
	 * 
	 * @return                    the gathered data (see documentation for {@link CodeData})
	 * @throws LevelLuaException  This indicates special runtime errors or constructs, that are illegal in Enigma levels.
	 */
	private CodeData analyse() throws LevelLuaException
	{
		try
		{
			chunk.accept(new AnalyticVisitor());
			if (!data.hasWorld(Mode.EASY))
			{
				if (!data.hasWorld(Mode.DIFFICULT))
					throw new LevelLuaException.Runtime("MissingWoCall", Mode.NORMAL, CodeSnippet.NONE);
				throw new LevelLuaException.Runtime("MissingWoCall", Mode.EASY, CodeSnippet.NONE);
			}
			else if (!data.hasWorld(Mode.DIFFICULT))
				throw new LevelLuaException.Runtime("MissingWoCall", Mode.DIFFICULT, CodeSnippet.NONE);
			data.dump();
			return data;
		}
		catch (LevelLuaException.Runtime e)
		{
			throw new LevelLuaException(e);
		}
	}
	
	/**
	 * A visitor that overwrites the default implementation with 
	 * empty handlers.
	 * This serves as a base class for visitors, that need to
	 * ignore every non-implemented construct.
	 * 
	 * @see AnalyticVisitor, ExpVisitor, AssignVisitor
	 */
	private static class NullVisitor extends Visitor
	{
		@Override public void visit(Chunk chunk) {}
		@Override public void visit(Block block) {}
		@Override public void visit(Stat.Assign stat) {}
		@Override public void visit(Stat.Break  stat) {}
		@Override public void visit(Stat.FuncCallStat stat) {}
		@Override public void visit(Stat.FuncDef stat) {}
		@Override public void visit(Stat.GenericFor stat) {}
		@Override public void visit(Stat.IfThenElse stat) {}
		@Override public void visit(Stat.LocalAssign stat) {}
		@Override public void visit(Stat.LocalFuncDef stat) {}
		@Override public void visit(Stat.NumericFor stat) {}
		@Override public void visit(Stat.RepeatUntil stat) {}
		@Override public void visit(Stat.Return stat) {}
		@Override public void visit(Stat.WhileDo stat) {}
		
		@Override public void visit(FuncBody body) {}
		@Override public void visit(FuncArgs args) {}
		@Override public void visit(ParList pars) {}
		
		@Override public void visit(Exp.Constant exp) {}
		
		@Override public void visit(Exp.NameExp exp) {}
		@Override public void visit(Exp.FieldExp exp) {}
		@Override public void visit(Exp.IndexExp exp) {}
		@Override public void visit(Exp.VarargsExp exp) {}
		
		@Override public void visit(Exp.ParensExp exp) {}
		@Override public void visit(Exp.UnopExp exp) {}
		@Override public void visit(Exp.BinopExp exp) {}
		
		@Override public void visit(Exp.AnonFuncDef exp) {}
		@Override public void visit(Exp.FuncCall exp) {}
		@Override public void visit(Exp.MethodCall exp) {}
		
		@Override public void visit(TableConstructor table) {}
		@Override public void visit(TableField field) {}
		
		@Override public void visit(Name name) {}
		@Override public void visit(String name) {}
		@Override public void visit(NameScope scope) {}
		@Override public void visit(Stat.Goto gotostat) {}
		@Override public void visit(Stat.Label label) {}
	}
	
	/**
	 * Statement analyser.
	 * This will gather assignments, function definitions and calls to the
	 * special functions {@code ti}, {@code cond} and {@code wo} into the
	 * outer class's {@link CodeAnalyser#code code} member.
	 */
	private class AnalyticVisitor extends NullVisitor
	{
		/** current mode state */
		private Mode mode;
		
		/**
		 * Constructor initializes {@link #mode} to {@link Mode#NORMAL}.
		 */
		public AnalyticVisitor()
		{
			mode = Mode.NORMAL;
		}
		
		@Override 
		public void visit(Chunk chunk)
		{
			chunk.block.accept(this);
		}
		
		@Override 
		public void visit(Block block)
		{
			if (block.stats != null)
				for (Object stat : block.stats)
					((Stat)stat).accept(this);
		}
		
		@Override 
		public void visit(Stat.FuncDef stat)
		{
			data.addFunction(stat.name.name.name, new CodeSnippet(code, stat));
		}
		
		@Override 
		public void visit(Stat.LocalFuncDef stat)
		{
			data.addFunction(stat.name.name, new CodeSnippet(code, stat));
		}
		
		@Override 
		public void visit(Stat.Assign stat)
		{
			final ExpVisitor visitor = new ExpVisitor();
			for (int iVar = 0, iExp = 0, nVar = stat.vars.size(), nExp = stat.exps.size(); iVar < nVar || iExp < nExp; ++iVar, ++iExp)
			{
				if (iExp < nExp)
				{
					((Exp)stat.exps.get(iExp)).accept(visitor);
					if (visitor.value instanceof WoCall)
					{
						if (iVar < nVar)
							((Exp.VarExp)stat.vars.get(iVar)).accept(new AssignVisitor(((WoCall)visitor.value).getWidth().toMultiMode(visitor.value.getCode()), new CodeSnippet(code, stat)));
						++iVar;
						if (iVar < nVar)
							((Exp.VarExp)stat.vars.get(iVar)).accept(new AssignVisitor(((WoCall)visitor.value).getHeight().toMultiMode(visitor.value.getCode()), new CodeSnippet(code, stat)));
					}
					else if (iVar < nVar)
					{
						((Exp.VarExp)stat.vars.get(iVar)).accept(new AssignVisitor(visitor.value, new CodeSnippet(code, stat)));
					}
				}
				else if (iVar < nVar)
				{
					((Exp.VarExp)stat.vars.get(iVar)).accept(new AssignVisitor(new Nil(CodeSnippet.NONE), new CodeSnippet(code, stat)));
				}
			}
		}
		
		@Override 
		public void visit(Stat.LocalAssign stat)
		{
			final ExpVisitor visitor = new ExpVisitor();
			final int nVar = stat.names  != null ? stat.names.size()  : 0;
			final int nExp = stat.values != null ? stat.values.size() : 0;
			for (int iName = 0, iExp = 0; iName < nVar || iExp < nExp; ++iName, ++iExp)
			{
				if (iExp < nExp)
				{
					((Exp)stat.values.get(iExp)).accept(visitor);
					if (visitor.value instanceof WoCall)
					{
						if (iName < nVar)
							data.assign(((Name)stat.names.get(iName)).name, ((WoCall)visitor.value).getWidth().toMultiMode(visitor.value.getCode()), new CodeSnippet(code, stat), mode);
						++iName;
						if (iName < nVar)
							data.assign(((Name)stat.names.get(iName)).name, ((WoCall)visitor.value).getHeight().toMultiMode(visitor.value.getCode()), new CodeSnippet(code, stat), mode);
					}
					else if (iName < nVar)
					{
						data.assign(((Name)stat.names.get(iName)).name, visitor.value, new CodeSnippet(code, stat), mode);
					}
				}
				else if (iName < nVar)
				{
					data.assign(((Name)stat.names.get(iName)).name, new Nil(CodeSnippet.NONE), new CodeSnippet(code, stat), mode);
				}
			}
		}
		
		@Override 
		public void visit(Stat.IfThenElse stat)
		{
			final ExpVisitor visitor = new ExpVisitor();
			stat.ifexp.accept(visitor);
			final MMSimpleValue val = visitor.value.checkSimple(mode);
			if (!val.isNull(mode))
			{
				Boolean easyb = null;
				Boolean diffb = null;
				if (mode != Mode.DIFFICULT && val.hasEasy() && val.easy.value.isboolean())
					easyb = new Boolean(val.easy.value.checkboolean());
				if (mode != Mode.EASY && val.hasDifficult() && val.difficult.value.isboolean())
					diffb = new Boolean(val.difficult.value.checkboolean());
				switch (mode)
				{
				case EASY:
					if (easyb != null)
					{
						if (easyb.booleanValue())
							stat.ifblock.accept(this);
						else if (stat.elseifblocks == null && stat.elseblock != null)
							stat.elseblock.accept(this);
					}
					break;
					
				case DIFFICULT:
					if (diffb != null)
					{
						if (diffb.booleanValue())
							stat.ifblock.accept(this);
						else if (stat.elseifblocks == null && stat.elseblock != null)
							stat.elseblock.accept(this);
					}
					break;
					
				case NORMAL:
				default:
					if (easyb != null && easyb.equals(diffb))
					{
						if (easyb.booleanValue())
							stat.ifblock.accept(this);
						else if (stat.elseifblocks == null && stat.elseblock != null)
							stat.elseblock.accept(this);
					}
					else if (easyb != null && diffb != null)
					{
						mode = Mode.EASY;
						if (easyb.booleanValue())
							stat.ifblock.accept(this);
						else if (stat.elseifblocks == null && stat.elseblock != null)
							stat.elseblock.accept(this);
						mode = Mode.DIFFICULT;
						if (diffb.booleanValue())
							stat.ifblock.accept(this);
						else if (stat.elseifblocks == null && stat.elseblock != null)
							stat.elseblock.accept(this);
						mode = Mode.NORMAL;
					}
					break;
				}
			}
		}
		
		/**
		 * Function calls are just passed on to an {@link ExpVisitor#visit(org.luaj.vm2.ast.Exp.FuncCall) ExpVisitor}.
		 */
		public void visit(Stat.FuncCallStat stat)
		{
			stat.funccall.accept(new ExpVisitor());
		}
		
		/**
		 * Expression analyser.
		 * This visitor tries to evaluate a given expression in a deterministic manner.
		 * If the analysis succeeds, the result is stored in the {@link #value} member.
		 */
		private class ExpVisitor extends NullVisitor
		{
			/** Value representing the analysed expression. */
			private Source value;
			
			/**
			 * Handle constants.
			 * A {@link SimpleValue} will be created.
			 * 
			 * @param exp  literal constant.
			 */
			public void visit(Exp.Constant exp)
			{
				value = new SimpleValue(exp.value, new CodeSnippet(code, exp));
			}
			
			/**
			 * Handle identifiers.
			 * This just returns a reference by calling {@link CodeData#getReference}.
			 * 
			 * @param exp  identifier expression.
			 */
			public void visit(Exp.NameExp exp)
			{
				value = data.getReference(exp.name.name, new CodeSnippet(code, exp));
			}
			
			/**
			 * Handle table fields.
			 * If the indexed value is not a table, no error will be thrown.
			 * In this case, there will just be a warning sent to the log and
			 * a {@link Nil} value will be returned.
			 * 
			 * @param exp  table field expression ({@code table.field}).
			 */
			public void visit(Exp.FieldExp exp)
			{
				ExpVisitor visitor = new ExpVisitor();
				exp.lhs.accept(visitor);
				final MMTable table = visitor.value.checkTable(mode);
				if (table.isNull(mode))
				{
					log.log(Log.MsgType.WARNING,
							new LevelLuaException.Runtime("IndexOfNonTable", mode, new CodeSnippet(code, exp.lhs).get(code), visitor.value.typename(mode), CodeSnippet.NONE).getMessage(),
							new CodeSnippet(code, exp));
					value = new Nil(new CodeSnippet(code, exp));
					return;
				}
				if (table.hasNormal() && table.easy instanceof Tiles)
					value = ((Tiles)table.easy).getReference(exp.name.name, new CodeSnippet(code, exp));
				else
					value = table.getReference(exp.name.name, new CodeSnippet(code, exp));
			}
			
			/**
			 * Handle table indexing.
			 * If the indexed value is not a table, no error will be thrown.
			 * In this case, there will just be a warning sent to the log and
			 * a {@link Nil} value will be returned.
			 * 
			 * @param exp  table index expression ({@code table[index]}).
			 */
			public void visit(Exp.IndexExp exp)
			{
				final ExpVisitor visitor = new ExpVisitor();
				exp.lhs.accept(visitor);
				final MMTable table = visitor.value.checkTable(mode);
				if (table.isNull(mode))
				{
					log.log(Log.MsgType.WARNING,
							new LevelLuaException.Runtime("IndexOfNonTable", mode, new CodeSnippet(code, exp.lhs).get(code), visitor.value.typename(mode), CodeSnippet.NONE).getMessage(),
							new CodeSnippet(code, exp));
					value = new Nil(new CodeSnippet(code, exp));
					return;
				}
				exp.exp.accept(visitor);
				final SimpleValue idx = visitor.value.checkSimple(mode).get(mode);
				if (idx == null)
				{
					value = new Nil(new CodeSnippet(code, exp));
				}
				else
				{	
					if (idx.value.isstring())
					{
						if (table.get(mode) instanceof Tiles)
							value = ((Tiles)table.get(mode)).getReference(idx.value.checkjstring(), new CodeSnippet(code, exp));
						else
							value = table.getReference(idx.value.checkjstring(), new CodeSnippet(code, exp));
					}
					else if (idx.value.isinttype())
					{
						if (table.get(mode) instanceof Tiles)
							value = ((Tiles)table.get(mode)).getReference(idx.value.checkint(), new CodeSnippet(code, exp));
						else
							value = table.getReference(idx.value.checkint(), new CodeSnippet(code, exp));
					}
					else if (idx.value.isnumber())
					{
						if (table.get(mode) instanceof Tiles)
							value = ((Tiles)table.get(mode)).getReferenceI(idx.value.checknumber().checkjstring(), new CodeSnippet(code, exp));
						else
							value = table.getReferenceI(idx.value.checknumber().checkjstring(), new CodeSnippet(code, exp));
					}
					else
					{
						value = new Nil(new CodeSnippet(code, exp));
					}
				}
			}
			
			/**
			 * Handle table literals.
			 * Each field will be analysed and assigned to a new {@link Table} instance,
			 * that in turn is placed as the result value.
			 * 
			 * @param exp  table constructor.
			 */
			public void visit(TableConstructor table)
			{
				final Table      val = new Table(new CodeSnippet(code, table));
				final ExpVisitor exp = new ExpVisitor();
				
				TableField field;
				int        index = 0;
				
				if (table.fields != null)
				{
					for (Object obj : table.fields)
					{
						field = (TableField)obj;
						if (field.name != null)
						{
							field.rhs.accept(exp);
							val.assign(field.name, exp.value, val.getCode(), Mode.NORMAL);
						}
						else if (field.index != null)
						{
							field.rhs.accept(exp);
							ExpVisitor visitor = new ExpVisitor();
							field.index.accept(visitor);
							final MMSimpleValue idx = visitor.value.checkSimple(mode);
							if (mode == Mode.NORMAL && !idx.hasNormal())
							{
								if (idx.hasEasy())
								{
									if (idx.easy.value.isstring())
										val.assign(idx.easy.value.checkjstring(), exp.value, val.getCode(), Mode.EASY);
									else if (idx.easy.value.isinttype())
										val.assign(idx.easy.value.checkint(), exp.value, val.getCode(), Mode.EASY);
									else if (idx.easy.value.isnumber())
										val.assignI(idx.easy.value.checknumber().checkjstring(), exp.value, val.getCode(), Mode.EASY);
								}
								if (idx.hasDifficult())
								{
									if (idx.difficult.value.isstring())
										val.assign(idx.difficult.value.checkjstring(), exp.value, val.getCode(), Mode.DIFFICULT);
									else if (idx.difficult.value.isinttype())
										val.assign(idx.difficult.value.checkint(), exp.value, val.getCode(), Mode.DIFFICULT);
									else if (idx.difficult.value.isnumber())
										val.assignI(idx.difficult.value.checknumber().checkjstring(), exp.value, val.getCode(), Mode.DIFFICULT);
								}
							}
							else
							{
								final SimpleValue idxval = idx.get(mode);
								if (idxval.value.isstring())
									val.assign(idxval.value.checkjstring(), exp.value, val.getCode(), Mode.NORMAL);
								else if (idxval.value.isinttype())
									val.assign(idxval.value.checkint(), exp.value, val.getCode(), Mode.NORMAL);
								else if (idxval.value.isnumber())
									val.assignI(idxval.value.checknumber().checkjstring(), exp.value, val.getCode(), Mode.NORMAL);
							}
						}
						else
						{
							field.rhs.accept(exp);
							val.assign(++index, exp.value, val.getCode(), Mode.NORMAL);
						}
					}
				}
				value = val;
			}
			
			/**
			 * Handle unary operators.
			 * In most cases this will result in an {@link Expression} object being returned.
			 * Just logical negation (operator {@code not}) of difficulty values (i.e. a value of
			 * type {@link Difficulty}) will return a negated difficulty value instead.
			 * 
			 * @param exp  unary operation expression.
			 */
			public void visit(Exp.UnopExp exp)
			{
				exp.rhs.accept(this);
				final CodeSnippet codesnippet = new CodeSnippet(code, exp);
				if (exp.op == Lua.OP_NOT)
				{
					final MMSimpleValue val = value.checkSimple(mode);
					if (val.hasNormal() && val.easy.value.isboolean())
						value = new SimpleValue(LuaValue.valueOf(!val.easy.value.toboolean()), codesnippet);
					else if (val.isNull(mode))
						value = new Expression(exp.op, value, new CodeSnippet(code, exp));
					else
						value = new MultiMode(new SimpleValue(val.hasEasy()      ? LuaValue.valueOf(!val.easy.value.toboolean())      : null, codesnippet),
						                      new SimpleValue(val.hasDifficult() ? LuaValue.valueOf(!val.difficult.value.toboolean()) : null, codesnippet),
						                      codesnippet);
				}
				else
					value = new Expression(exp.op, value, new CodeSnippet(code, exp));
			}
			
			/**
			 * Handle binary operators.
			 * In most cases this will result in an {@link Expression} object being returned.
			 * Just concatenation (operator {@code ..}) of tile declarations and checking difficulty
			 * (operators {@code ==} and {@code ~=}) will result in special values being returned (i.e. a
			 * {@link TileDecl} or a {@link Difficulty} respectively).
			 * 
			 * @param exp  binary operation expression.
			 */
			public void visit(Exp.BinopExp exp)
			{
				// get arguments
				final ExpVisitor lhs = new ExpVisitor();
				final ExpVisitor rhs = new ExpVisitor();
				exp.lhs.accept(lhs);
				exp.rhs.accept(rhs);
				
				// check operator
				switch (exp.op)
				{
				
				// concatenation operator  '..'
				// (special handling for concatenating tile declarations)
				case Lua.OP_CONCAT:
					
					// concatenation of tile values (return tile)
					try
					{
						//*
						MMTilePart part1 = lhs.value.checkTilePart(mode);
						MMTilePart part2 = rhs.value.checkTilePart(mode);
						if (!part1.isNull(mode))
						{
 							if (!part2.isNull())
 							{
 								MMTileDecl tile;
 								if (part1.hasNormal())
									tile = new MMTileDecl(new TileDecl(part1.easy));
								else
									tile = new MMTileDecl(part1.hasEasy() ? new TileDecl(part1.easy) : null, part1.hasDifficult() ? new TileDecl(part1.difficult) : null);
								tile.add(part2, mode);
								value = new MultiMode(tile, new CodeSnippet(code, exp));
 							}
 							else if (rhs.value instanceof MultiMode)
 							{
 								MMTileDecl tile2 = rhs.value.checkTile(mode);
 								if (!tile2.isNull())
 								{
 									tile2.add(part1, mode);
 								}
 								else
 								{
 									MMTileDecl tile;
 									if (part1.hasNormal())
 										tile = new MMTileDecl(new TileDecl(part1.easy));
 									else
 										tile = new MMTileDecl(part1.hasEasy() ? new TileDecl(part1.easy) : null, part1.hasDifficult() ? new TileDecl(part1.difficult) : null);
 									tile.add(new ObjectDecl(rhs.value), mode);
 									value = new MultiMode(tile, new CodeSnippet(code, exp));
 								}
								value = rhs.value;
 							}
 							else
 							{
 								MMTileDecl tile;
 								if (part1.hasNormal())
									tile = new MMTileDecl(new TileDecl(part1.easy));
								else
									tile = new MMTileDecl(part1.hasEasy() ? new TileDecl(part1.easy) : null, part1.hasDifficult() ? new TileDecl(part1.difficult) : null);
								tile.add(new ObjectDecl(rhs.value), mode);
								value = new MultiMode(tile, new CodeSnippet(code, exp));
 							}
						}
						else if (lhs.value instanceof MultiMode)
 						{
 							MMTileDecl tile1 = ((MultiMode)lhs.value).checkTile(mode);
 							if (!part2.isNull())
 							{
 								tile1.add(part2, mode);
 							}
 							else if (rhs.value instanceof MultiMode)
 							{
 								MMTileDecl tile2 = rhs.value.checkTile(mode);
 								if (!tile1.isNull())
 									tile1.add(tile2, mode);
 								else
 									tile1.add(new ObjectDecl(rhs.value), mode);
 							}
 							else
 							{
								tile1.add(new ObjectDecl(rhs.value), mode);
 							}
							value = lhs.value;
 						}
						else
						{
							MMTileDecl tile;
							tile = new MMTileDecl(new TileDecl(new ObjectDecl(lhs.value)));
 							if (!part2.isNull())
 							{
 								tile.add(part2, mode);
 							}
 							else if (rhs.value instanceof MultiMode)
 							{
 								MMTileDecl tile2 = rhs.value.checkTile(mode);
 								if (!tile.isNull())
 									tile.add(tile2, mode);
 								else
 									tile.add(new ObjectDecl(rhs.value), mode);
 							}
 							else
 							{
								tile.add(new ObjectDecl(rhs.value), mode);
 							}
							value = new MultiMode(tile, new CodeSnippet(code, exp));
						}
					}
					catch (LevelLuaException.Runtime err)
					{
						System.err.println(err.toString() + " [" + lhs.value.typename() + "]");
						System.err.println(err.code.get(code));
						err.printStackTrace();
						value = new Expression(Lua.OP_CONCAT, lhs.value, rhs.value, new CodeSnippet(code, exp));
					}
					break;
				
				// equality operators  '==' and '~='
				// (special handling for difficulty value)
				case Lua.OP_EQ:
				case Lua.OP_NEQ:
					
					final CodeSnippet codesnippet = new CodeSnippet(code, exp);
					
					final MMSimpleValue val1  = lhs.value.checkSimple(mode);
					final MMSimpleValue val2  = rhs.value.checkSimple(mode);
					
					final Boolean easyleft  = (val1.hasEasy()      && val1.easy.value.isboolean())      ? val1.easy.value.toboolean()      : null;
					final Boolean easyright = (val2.hasEasy()      && val2.easy.value.isboolean())      ? val2.easy.value.toboolean()      : null;
					final Boolean diffleft  = (val1.hasDifficult() && val1.difficult.value.isboolean()) ? val1.difficult.value.toboolean() : null;
					final Boolean diffright = (val2.hasDifficult() && val2.difficult.value.isboolean()) ? val2.difficult.value.toboolean() : null;
					
					// generic equality test (return expression object)
					if ((easyleft == null || easyright == null) && (diffleft == null || diffright == null))
						value = new Expression(Lua.OP_CONCAT, lhs.value, rhs.value, new CodeSnippet(code, exp));
					
					// mode independent equality test
					else if ((easyleft == easyright) == (diffleft == diffright))
						value = new SimpleValue(LuaValue.valueOf(exp.op == Lua.OP_EQ ? easyleft == easyright : easyleft != easyright), codesnippet);
					
					// mode test
					else
						value = new MultiMode(
								(easyleft != null && easyright != null) ? new SimpleValue(LuaValue.valueOf(exp.op == Lua.OP_EQ ? easyleft == easyright : easyleft != easyright), codesnippet) : null,
								(diffleft != null && diffright != null) ? new SimpleValue(LuaValue.valueOf(exp.op == Lua.OP_EQ ? diffleft == diffright : diffleft != diffright), codesnippet) : null
							, codesnippet);
					break;
				
				// all other operators
				// (just return an expression object)
				default:
					value = new Expression(exp.op, lhs.value, rhs.value, new CodeSnippet(code, exp));;
				}
			}
			
			/**
			 * Handle parentheses.
			 * No special treatment necessary. The contained expression is just passed on.
			 * 
			 * @param exp  bracketed expression.
			 */
			public void visit(Exp.ParensExp exp)
			{
				exp.exp.accept(this);
			}
			
			/**
			 * Handle function calls.
			 * Function calls will generally be ignored completely. Only calls to
			 * the special functions 'ti'   (creation of a tile),
			 *                       'wo'   (creation of the world) and
			 *                       'cond' (conditional in case of argument being a difficulty)
			 * and the construction of resolvers will be handled.<br/>
			 * The function 'ti' will return a {@link TileDecl}.<br/>
			 * The function 'wo' will return a {@link WoCall}.<br/>
			 * The function 'cond' will return a {@link Variable}
			 *     with values set to the arguments of both respective difficulties.
			 *     If the condition is no difficulty, then {@link Nil} is returned.
			 * 
			 * @param exp  function call to be analysed.
			 */
			public void visit(Exp.FuncCall exp)
			{
				// API 2.0 functions
				if (exp.lhs instanceof Exp.NameExp)
				{
					// ti()
					//   converts tables to tile declarations
					if (((Exp.NameExp)exp.lhs).name.name.equals("ti"))
					{
						if (exp.args.exps == null || exp.args.exps.isEmpty())
							return;
						((Exp)exp.args.exps.get(0)).accept(this);
						value = new ObjectDecl(value);
					}
					
					// cond()
					//   creates a multi-mode variable, if the condition is a difficulty
					//   otherwise 'nil' will be returned
					else if (((Exp.NameExp)exp.lhs).name.name.equals("cond"))
					{
						if (exp.args.exps != null && exp.args.exps.size() == 3)
						{
							final ExpVisitor visitor = new ExpVisitor();
							((Exp)exp.args.exps.get(0)).accept(visitor);
							final MMSimpleValue val = visitor.value.checkSimple(mode);
							if (!val.isNull(mode))
							{
								Boolean easyb = null;
								Boolean diffb = null;
								if (mode != Mode.DIFFICULT && val.hasEasy() && val.easy.value.isboolean())
									easyb = new Boolean(val.easy.value.checkboolean());
								if (mode != Mode.EASY && val.hasDifficult() && val.difficult.value.isboolean())
									diffb = new Boolean(val.difficult.value.checkboolean());
								switch (mode)
								{
								case EASY:
									if (easyb != null)
									{
										if (easyb.booleanValue())
											((Exp)exp.args.exps.get(1)).accept(this);
										else
											((Exp)exp.args.exps.get(2)).accept(this);
									}
									break;
									
								case DIFFICULT:
									if (diffb != null)
									{
										if (diffb.booleanValue())
											((Exp)exp.args.exps.get(1)).accept(this);
										else
											((Exp)exp.args.exps.get(2)).accept(this);
									}
									break;
									
								case NORMAL:
								default:
									if (easyb != null && easyb.equals(diffb))
									{
										if (easyb.booleanValue())
											((Exp)exp.args.exps.get(1)).accept(this);
										else
											((Exp)exp.args.exps.get(2)).accept(this);
									}
									else if (easyb != null || diffb != null)
									{
										Source easyval = null;
										if (easyb != null)
										{
											if (easyb.booleanValue())
												((Exp)exp.args.exps.get(1)).accept(this);
											else
												((Exp)exp.args.exps.get(2)).accept(this);
											easyval = value;
										}
										
										Source diffval = null;
										if (diffb != null)
										{
											if (diffb.booleanValue())
												((Exp)exp.args.exps.get(1)).accept(this);
											else
												((Exp)exp.args.exps.get(2)).accept(this);
											diffval = value;
										}
										
										value = new MultiMode(easyval, diffval, new CodeSnippet(code, exp));
									}
									else value = new Nil(new CodeSnippet(code, exp));
									break;
								}
							}
							else
							{
								System.err.println("WARNING: expected imple value as first argument to 'cond()', got " + visitor.value.typename(mode));
								value = new Nil(new CodeSnippet(code, exp));
							}
						}
						else
						{
							System.err.println("WARNING: expected 3 arguments to 'cond()', got " + (exp.args.exps == null ? "null" : exp.args.exps.size()));
							value = new Nil(new CodeSnippet(code, exp));
						}
					}
					
					// wo()
					//   constructs the world-data and checks the given resolver
					else if (((Exp.NameExp)exp.lhs).name.name.equals("wo"))
					{
						// check argument count
						if (exp.args.exps.size() < 2)
							throw new LevelLuaException.Runtime("IllegalWoCallArgumentCount", Integer.toString(exp.args.exps.size()), new CodeSnippet(code, exp));
							
						// check resolver
						((Exp)exp.args.exps.get(0)).accept(this);
						MMResolver res = value.checkResolver(mode);
						if (res.isNull(mode))
							throw new LevelLuaException.Runtime("IllegalSubresolver", mode, value.typename(mode), new CodeSnippet(code, (Exp)exp.args.exps.get(0)));
						
						// check other arguments
						switch (exp.args.exps.size())
						{
						case 2: {
							// get libmap
							((Exp)exp.args.exps.get(1)).accept(this);
							
							// create world data
							data.setWorld(res, this.value, mode, new CodeSnippet(code, exp));}
							break;
							
						case 3: {
							// get default key
							((Exp)exp.args.exps.get(1)).accept(this);
							final Source defaultKey = this.value; 
						
							// get map
							((Exp)exp.args.exps.get(2)).accept(this);
							final Source map = this.value;
							
							// create world data
							data.setWorld(res, defaultKey, map, mode, new CodeSnippet(code, exp));}
							break;
							
						case 4: {
							// get default key
							((Exp)exp.args.exps.get(1)).accept(this);
							final Source defaultKey = this.value; 
						
							// get width and height
							((Exp)exp.args.exps.get(2)).accept(this);
							final Source width = this.value;
							((Exp)exp.args.exps.get(3)).accept(this);
							final Source height = this.value;
							
							// create world data
							data.setWorld(res, defaultKey, width, height, mode, new CodeSnippet(code, exp));}
							break;
						}
						value = data.getWorldCall(mode == Mode.NORMAL ? Mode2.EASY : mode.mode2());
					}
				}
				else
				{
					// call to resolver constructors
					exp.lhs.accept(this);
					final MMSimpleValue func = value.checkSimple(mode);
					if (!func.isNull(mode))
					{
						List<Source> args = new LinkedList<Source>();
						for (Object arg : exp.args.exps)
						{
							((Exp)arg).accept(this);
							args.add(this.value);
						}
						
						value = null;
						if (func.isNormal())
						{
							if (func.easy.value.isuserdata(ApiFunction.class))
							{
								ApiFunction apifunc = (ApiFunction)func.easy.value.checkuserdata(ApiFunction.class);
								value = apifunc.call(args, mode, new CodeSnippet(code, exp));
							}
							else
							{
								value = new Nil(new CodeSnippet(code, exp));
							}
						}
						else
						{
							Resolver.Constructor easyRes = null;
							Resolver.Constructor diffRes = null;
							if (func.hasEasy() && func.easy.value.isuserdata(Resolver.Constructor.class))
								easyRes = (Resolver.Constructor)func.easy.value.checkuserdata(Resolver.Constructor.class);
							if (func.hasDifficult() && func.difficult.value.isuserdata(Resolver.Constructor.class))
								diffRes = (Resolver.Constructor)func.difficult.value.checkuserdata(Resolver.Constructor.class);
							if (easyRes != null || diffRes != null)
							{
								value = new MMResolver(easyRes != null ? easyRes.call(args, mode, new CodeSnippet(code, exp)) : null,
								                       diffRes != null ? diffRes.call(args, mode, new CodeSnippet(code, exp)) : null);
							}
							else
							{
								ApiFunction easy = null;
								ApiFunction diff = null;
								if (func.hasEasy() && func.easy.value.isuserdata(ApiFunction.class))
									easy = (ApiFunction)func.easy.value.checkuserdata(ApiFunction.class);
								if (!func.isNormal() && func.hasDifficult() && func.difficult.value.isuserdata(ApiFunction.class))
									diff = (ApiFunction)func.difficult.value.checkuserdata(ApiFunction.class);
								if (easy != null || diff != null)
								{
									final CodeSnippet codesnippet = new CodeSnippet(code, exp);
									value = new MultiMode(easy != null ? easy.call(args, mode, codesnippet) : null,
									                      diff != null ? diff.call(args, mode, codesnippet) : null,
									                      codesnippet);
								}
							}
						}
						
						if (value == null)
							value = new Nil(new CodeSnippet(code, exp));
					}
					else value = new Nil(new CodeSnippet(code, exp));
				}
			}
		}
		
		/**
		 * Analyses the left-hand side of an assign-statement and performs the assignment.
		 * This visitor should be accepted by the left-hand {@code VarExp} instance of
		 * the assign-statement. It will interpret the expression and (if possible)
		 * assign the value given to the constructor to the determined identifier/field.
		 */
		private class AssignVisitor extends NullVisitor
		{
			private final Source      rhs;
			private final CodeSnippet assign;
			
			/**
			 * Construct a visitor to assign a given value to the analysed expression.
			 * 
			 * @param rhs     the value to be assigned to the accepting expression.
			 * @param assign  the complete assignment code snippet (will be stored with the value).
			 */
			public AssignVisitor(Source rhs, CodeSnippet assign)
			{
				this.rhs = rhs;
				this.assign = assign;
			}
			
			/**
			 * Normal variable assignment.
			 * This makes use of {@link CodeData#assign(String, SourceData, CodeSnippet, Mode)}.
			 * 
			 * @param exp  identifier to assign to.
			 */
			public void visit(Exp.NameExp exp)
			{
				data.assign(exp.name.name, rhs, assign, mode);
			}
			
			/**
			 * Table field assignment.
			 * Indicates a statement in the form of {@code table.field = expression}.
			 * This makes use of {@link Table#assign(String, SourceData, CodeSnippet, Mode)}.
			 * 
			 * @param exp  field expression; providing the table instance and the field-name.
			 */
			public void visit(Exp.FieldExp exp)
			{
				ExpVisitor visitor = new ExpVisitor();
				exp.lhs.accept(visitor);
				final MMTable table = visitor.value.checkTable(mode);
				if (!table.isNull(mode))
					table.assign(exp.name.name, rhs, assign, mode);
				else
				{
					log.log(Log.MsgType.WARNING,
							new LevelLuaException.Runtime("IndexOfNonTable", mode, new CodeSnippet(code, exp.lhs).get(code), (visitor.value == null) ? "null" : visitor.value.typename(mode), CodeSnippet.NONE).getMessage(),
							new CodeSnippet(code, exp));
				}
			}
			
			/**
			 * Indexed table field assignment.
			 * Indicates a statement in the form of {@code table[index] = expression}.
			 * The field expression is checked for type, to invoke the correct assign-
			 * method of {@link Table}.
			 * 
			 * @param exp  index expression; providing the table instance and the index.
			 */
			public void visit(Exp.IndexExp exp)
			{
				final ExpVisitor visitor = new ExpVisitor();
				exp.lhs.accept(visitor);
				final MMTable table = visitor.value.checkTable(mode);
				if (table.isNull(mode))
				{
					log.log(Log.MsgType.WARNING,
							new LevelLuaException.Runtime("IndexOfNonTable", mode, new CodeSnippet(code, exp.lhs).get(code), (visitor.value == null) ? "null" : visitor.value.typename(mode), CodeSnippet.NONE).getMessage(),
							new CodeSnippet(code, exp));
					return;
				}
				exp.exp.accept(visitor);
				final MMSimpleValue index = visitor.value.checkSimple(mode);
				if (mode != Mode.DIFFICULT && index.hasEasy())
				{
					if (table.get(mode) instanceof Tiles)
						table.assign(index.easy.value.checkjstring(), rhs, assign, mode);
					else if (index.easy.value.isinttype())
						table.assign(index.easy.value.checkint(), rhs, assign, mode);
					else if (index.easy.value.isnumber())
						table.assignI(index.easy.value.checknumber().tojstring(), rhs, assign, mode);
					else if (index.easy.value.isstring())
						table.assign(index.easy.value.checkjstring(), rhs, assign, mode);
				}
				if (!index.isNormal() && mode != Mode.EASY && index.hasDifficult())
				{
					if (table.get(mode) instanceof Tiles)
						table.assign(index.difficult.value.checkjstring(), rhs, assign, mode);
					else if (index.difficult.value.isinttype())
						table.assign(index.difficult.value.checkint(), rhs, assign, mode);
					else if (index.difficult.value.isnumber())
						table.assignI(index.difficult.value.checknumber().tojstring(), rhs, assign, mode);
					else if (index.difficult.value.isstring())
						table.assign(index.difficult.value.checkjstring(), rhs, assign, mode);
				}
			}
		}
	}
}

