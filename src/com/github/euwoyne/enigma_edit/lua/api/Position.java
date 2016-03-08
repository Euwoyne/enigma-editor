
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

public class Position
{
	public double x;
	public double y;
	
	public Position()                     {x = 0.0; y = 0.0;}
	public Position(Position p)           {x = p.x; y = p.y;}
	public Position(double _x, double _y) {x = _x;  y = _y;}
	
	public double abs2()                  {return x * x + y * y;}
	public double abs()                   {return Math.sqrt(x * x + y * y);}
	public void   norm()                  {double l = abs(); x /= l; y /= l;}
	public void   norm(double l)          {norm(); x *= l; y *= l;}
	
	public Position add(Position p)       {x += p.x; y += p.y; return this;}
	public Position sub(Position p)       {x -= p.x; y -= p.y; return this;}
	public Position mult(double d)        {x *= d;   y *= d;   return this;}
	public double   mult(Position p)      {return x * p.x + y * p.y;}
	
	public static Position add (Position p1, Position p2) {return new Position(p1.x + p2.x, p1.y + p2.y);};
	public static Position sub (Position p1, Position p2) {return new Position(p1.x - p2.x, p1.y - p2.y);};
	public static Position mult(double   d,  Position v)  {return new Position(d * v.x, d * v.y);}
	public static double   mult(Position p1, Position p2) {return p1.x * p2.x + p1.y * p2.y;}
}

