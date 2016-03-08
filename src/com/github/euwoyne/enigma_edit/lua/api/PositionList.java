
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

package com.github.euwoyne.enigma_edit.lua.api;

import java.util.ArrayList;

public class PositionList
{
	private ArrayList<Position> data;
	
	public PositionList()
	{
		data = new ArrayList<Position>();
	}
	
	public void push(Position pos)
	{
		data.add(pos);
	}
	
	public static PositionList add(PositionList list, Position pos)
	{
		PositionList out = new PositionList();
		for (Position p : list.data)
			out.data.add(Position.add(p, pos));
		return out;
	}
	
	public static PositionList add(Position pos, PositionList list)
	{
		PositionList out = new PositionList();
		for (Position p : list.data)
			out.data.add(Position.add(pos, p));
		return out;
	}
	
	public static PositionList sub(PositionList list, Position pos)
	{
		PositionList out = new PositionList();
		for (Position p : list.data)
			out.data.add(Position.sub(p, pos));
		return out;
	}
	
	public static PositionList sub(Position pos, PositionList list)
	{
		PositionList out = new PositionList();
		for (Position p : list.data)
			out.data.add(Position.sub(pos, p));
		return out;
	}
	
	public static PositionList concat(Position pos1, Position pos2)
	{
		PositionList out = new PositionList();
		out.data.add(pos1);
		out.data.add(pos2);
		return out;
	}
	
	public static PositionList concat(PositionList list, Position pos)
	{
		PositionList out = new PositionList();
		out.data.addAll(list.data);
		out.data.add(pos);
		return out;
	}
	
	public static PositionList concat(Position pos, PositionList list)
	{
		PositionList out = new PositionList();
		out.data.add(pos);
		out.data.addAll(list.data);
		return out;
	}
	
	public static PositionList concat(PositionList list1, PositionList list2)
	{
		PositionList out = new PositionList();
		out.data.addAll(list1.data);
		out.data.addAll(list2.data);
		return out;
	}
}

