
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

import java.util.Random;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class EnigmaAPI extends TwoArgFunction
{
	private World world;
	
	public void setTarget(World world)
	{
		this.world = world;
	}
	
	@Override
	public LuaValue call(LuaValue modname, LuaValue env)
	{
		new enigma().call(modname, env);
		new en().call(modname, env);
		new po().call(modname, env);
		new wo().call(modname, env);
		new ti().call(modname, env);
		return LuaValue.userdataOf(this);
	}
	
	public class en extends TwoArgFunction
	{
		@Override
		public LuaValue call(LuaValue modname, LuaValue env)
		{
			LuaValue library = tableOf();
			library.set("random", new random());
			env.set("en", library);
			return library;
		}
		
		class random extends VarArgFunction
		{
			Random rand;
			random() {rand = new Random(); this.name = "en.random";}
			
			@Override
			public Varargs invoke(Varargs args)
			{
				switch (args.narg())
				{
				case 0:
					return LuaValue.valueOf(rand.nextDouble());
				case 1:
					return LuaValue.valueOf(rand.nextInt(args.toint(0)) + 1);
				default: 
					final int m = args.toint(0);
					final int n = args.toint(1);
					return LuaValue.valueOf(rand.nextInt(n-m+1)+m);
				}
			}
		}
	}

	public class enigma extends TwoArgFunction
	{
		@Override
		public LuaValue call(LuaValue modname, LuaValue env)
		{
			LuaValue library = tableOf();
			library.set("MakeObject", new unimplemented("enigma.MakeObject"));
			library.set("GetKind", new unimplemented("enigma.GetKind"));
			library.set("SetAttrib", new unimplemented("enigma.SetAttrib"));
			library.set("GetAttrib", new unimplemented("enigma.GetAttrib"));
			library.set("GetObjectTemplate", new unimplemented("enigma.GetObjectTemplate"));
			library.set("SetScrambleIntensity", new unimplemented("enigma.SetScrambleIntensity"));
			env.set("enigma", library);
			return library;
		}
		
		class unimplemented extends VarArgFunction
		{
			unimplemented(String name) {this.name = name;}
			
			@Override
			public Varargs invoke(Varargs args)
			{
				System.err.println("Call to unimplemented API function '" + this.name + "'");
				return NIL;
			}
		}
	}

	public class po extends TwoArgFunction
	{
		private LuaTable mt;
		
		public po()
		{
			mt = tableOf();
			mt.set("__add",    new Add());
			mt.set("__sub",    new Sub());
			mt.set("__concat", new Concat());
		}
		
		@Override
		public LuaValue call(LuaValue modname, LuaValue env)
		{
			LuaTable pomt = tableOf();
			pomt.set("__call",     new Constructor());
			pomt.set("__index",    new Index());
			pomt.set("__newindex", new NewIndex());
			LuaValue library = tableOf();
			library.setmetatable(pomt);
			env.set("po", library);
			return library;
		}
		
		class Constructor extends VarArgFunction
		{
			private Position create(int i, LuaValue arg)
			{
				switch (arg.type())
				{
				case LuaValue.TUSERDATA:
					if (arg.isuserdata(enigma_edit.lua.Object.class))
						return new Position(((Object)arg.checkuserdata()).pos);
					return new Position((Position)arg.checkuserdata(Position.class));
					
				case LuaValue.TTABLE:
					LuaTable table = arg.checktable();
					this.argcheck(table.length() == 2, i, "table of length '2' expected, got length '" + table.length() + "'");
					return new Position(
							table.checknumber(1).todouble(),
							table.checknumber(2).todouble());
				
				default:
					LuaValue.argerror(i, "Position expected, got '" + LuaValue.TYPE_NAMES[arg.type()] + "'");
					return new Position();
				}
			}
			
			@Override
			public Varargs invoke(Varargs args)
			{
				switch (args.narg())
				{
				case 1:
					return LuaValue.userdataOf(new Position(), mt);
							
				case 2:
					if (!args.istable(2))
						return LuaValue.userdataOf(create(1, args.checkvalue(2)), mt);
					
					LuaTable table = args.checktable(2);
					if (table.get(1).isnumber())
						return LuaValue.userdataOf(create(1, table), mt);
					
					PositionList list = new PositionList();
					
					LuaValue key = NIL;
					Varargs  it  = table.inext(key);
					while (!it.arg(0).isnil())
					{
						it = table.inext(it.arg(0));
						list.push(create(it.arg(0).checkint(), it.arg(1)));
					}
					
					return LuaValue.userdataOf(list, mt);
					
				default:
					return LuaValue.userdataOf
					(
						new Position(
							args.checknumber(2).todouble(),
							args.checknumber(3).todouble()),
						mt
					);
				}
			}
		}
		
		class Index extends TwoArgFunction
		{
			@Override
			public LuaValue call(LuaValue table, LuaValue key)
			{
				System.err.println("Call to unimplemented API function 'po.__index'");
				return NIL;
			}
		}
		
		class NewIndex extends ThreeArgFunction
		{
			@Override
			public LuaValue call(LuaValue table, LuaValue key, LuaValue value)
			{
				System.err.println("Call to unimplemented API function 'po.__newindex'");
				return NIL;
			}
		}
		
		class Add extends TwoArgFunction
		{
			@Override
			public LuaValue call(LuaValue op1, LuaValue op2)
			{
				if (op1.isuserdata(PositionList.class))
				{
					PositionList p1 = (PositionList)op1.checkuserdata(PositionList.class);
					Position     p2 = (Position)    op2.checkuserdata(Position.class);
					return LuaValue.userdataOf(PositionList.add(p1, p2), mt);
				}
				Position p1 = (Position)op1.checkuserdata(Position.class);
				if (op2.isuserdata(PositionList.class))
				{
					PositionList p2 = (PositionList)op2.checkuserdata(PositionList.class);
					return LuaValue.userdataOf(PositionList.add(p1, p2), mt);
				}
				Position p2 = (Position)op2.checkuserdata(Position.class);
				return LuaValue.userdataOf(Position.add(p1, p2), mt);
			}
		}
		
		class Sub extends TwoArgFunction
		{
			@Override
			public LuaValue call(LuaValue op1, LuaValue op2)
			{
				if (op1.isuserdata(PositionList.class))
				{
					PositionList p1 = (PositionList)op1.checkuserdata(PositionList.class);
					Position     p2 = (Position)    op2.checkuserdata(Position.class);
					return LuaValue.userdataOf(PositionList.sub(p1, p2), mt);
				}
				Position p1 = (Position)op1.checkuserdata(Position.class);
				if (op2.isuserdata(PositionList.class))
				{
					PositionList p2 = (PositionList)op2.checkuserdata(PositionList.class);
					return LuaValue.userdataOf(PositionList.sub(p1, p2), mt);
				}
				Position p2 = (Position)op2.checkuserdata(Position.class);
				return LuaValue.userdataOf(Position.sub(p1, p2), mt);
			}
		}
		
		class Concat extends TwoArgFunction
		{
			@Override
			public LuaValue call(LuaValue op1, LuaValue op2)
			{
				if (op1.isuserdata(PositionList.class))
				{
					PositionList p1 = (PositionList)op1.checkuserdata(PositionList.class);
					if (op2.isuserdata(PositionList.class))
					{
						PositionList p2 = (PositionList)op2.checkuserdata(PositionList.class);
						return LuaValue.userdataOf(PositionList.concat(p1, p2), mt);
					}
					Position p2 = (Position) op2.checkuserdata(Position.class);
					return LuaValue.userdataOf(PositionList.concat(p1, p2), mt);
				}
				Position p1 = (Position)op1.checkuserdata(Position.class);
				if (op2.isuserdata(PositionList.class))
				{
					PositionList p2 = (PositionList)op2.checkuserdata(PositionList.class);
					return LuaValue.userdataOf(PositionList.concat(p1, p2), mt);
				}
				Position p2 = (Position)op2.checkuserdata(Position.class);
				return LuaValue.userdataOf(PositionList.concat(p1, p2), mt);
			}
		}
	}

	public class wo extends TwoArgFunction
	{
		@Override
		public LuaValue call(LuaValue modname, LuaValue env)
		{
			LuaTable womt = tableOf();
			womt.set("__call",    new Call());
			LuaValue library = tableOf();
			library.setmetatable(womt);
			library.set("_register", new Register());
			env.set("wo", library);
			return library;
		}
		
		class Call extends VarArgFunction
		{
			@Override
			public Varargs invoke(Varargs args)
			{
				this.argcheck(args.narg() > 0, 1, "at least two arguments expected, got none");
				this.argcheck(args.narg() > 1, 2, "at least two arguments expected, got only one");
				
				int width = 0, height = 0;
				switch (args.narg())
				{
				case 2:
				case 3:
					LuaTable map = args.checktable(3);
					LuaValue key = LuaValue.NIL;
					while (true)
					{
						Varargs n = map.inext(key);
						if ((key = n.arg1()).isnil())
							break;
						++height;
						LuaString v = n.arg(2).checkstring();
						if (v.length() > width)
							width = v.length();
					}
					world.create(width,  height);
						
				default:
					width = args.toint(3);
					height = args.toint(4);
					world.create(width,  height);
					
				}
				return LuaValue.varargsOf(LuaValue.valueOf(width), LuaValue.valueOf(height));
			}
		}
		
		class Register extends ThreeArgFunction
		{
			@Override
			public LuaValue call(LuaValue world, LuaValue name, LuaValue func)
			{
				LuaTable table = world.checktable();
				table.set(name, func);
				return null;
			}
		}
	}
	
	public class ti extends TwoArgFunction
	{
		private Object createObject(LuaTable table)
		{
			Object object = new Object();
			object.kind = table.get(1).checkjstring();
			return object;
		}
		
		private Tile createTile(LuaTable table)
		{
			Tile tile = new Tile();
			tile.push(createObject(table));
			return tile;
		}
		
		private LuaTable mt;
		
		public ti()
		{
			mt = tableOf();
			mt.set("__concat", new Concat());
		}
		
		@Override
		public LuaValue call(LuaValue modname, LuaValue env)
		{
			LuaTable pomt = tableOf();
			pomt.set("__call",     new Constructor());
			pomt.set("__index",    new Index());
			pomt.set("__newindex", new NewIndex());
			LuaValue library = tableOf();
			library.setmetatable(pomt);
			env.set("ti", library);
			return library;
		}
		
		class Constructor extends TwoArgFunction
		{
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1)
			{
				return LuaValue.userdataOf(createTile(arg1.checktable()), mt);
			}
		}
		
		class Concat extends TwoArgFunction
		{
			@Override
			public LuaValue call(LuaValue tile1, LuaValue tile2)
			{
				if (tile1.isuserdata(Tile.class))
				{
					Tile t1 = (Tile)tile1.checkuserdata(Tile.class);
					if (tile2.isuserdata(Tile.class))
					{
						Tile t2 = (Tile)tile2.checkuserdata(Tile.class);
						return LuaValue.userdataOf(Tile.concat(t1, t2), mt);
					}
					Object t2 = createObject(tile2.checktable());
					return LuaValue.userdataOf(Tile.concat(t1, t2), mt);
				}
				Object t1 = createObject(tile1.checktable());
				if (tile2.isuserdata(Tile.class))
				{
					Tile t2 = (Tile)tile2.checkuserdata(Tile.class);
					return LuaValue.userdataOf(Tile.concat(t1, t2), mt);
				}
				Object t2 = createObject(tile2.checktable());
				return LuaValue.userdataOf(Tile.concat(t1, t2), mt);
			}
		}
		
		class Index extends TwoArgFunction
		{
			@Override
			public LuaValue call(LuaValue table, LuaValue key)
			{
				return table.rawget(key);
			}
		}
		
		class NewIndex extends ThreeArgFunction
		{
			@Override
			public LuaValue call(LuaValue table, LuaValue key, LuaValue value)
			{
				Tile tile;
				if (value.istable())
					tile = createTile(value.checktable());
				else
					tile = (Tile)value.checkuserdata(Tile.class);
				tile.setKey(key.checkjstring());
				
				world.addTile(tile);
				table.rawset(key, LuaValue.userdataOf(tile, mt));
				return NIL;
			}
		}
	}
}

