
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

package enigma_edit.model;

public class Vector
{
	public double x;
	public double y;
	
	public Vector()                     {x = 0.0; y = 0.0;}
	public Vector(double _x, double _y) {x = _x;  y = _y;}
	
	public double abs2()          {return x * x + y * y;}
	public double abs()           {return Math.sqrt(x * x + y * y);}
	public void   norm()          {double l = abs(); x /= l; y /= l;}
	public void   norm(double l)  {norm(); x *= l; y *= l;}
	
	public Vector add(Vector v)   {x += v.x; y += v.y; return this;}
	public Vector sub(Vector v)   {x -= v.x; y -= v.y; return this;}
	public Vector mult(double d)  {x *= d;   y *= d;   return this;}
	public double mult(Vector v)  {return x * v.x + y * v.y;}
	
	public static Vector add(Vector v1, Vector v2)  {return new Vector(v1.x + v2.x, v1.y + v2.y);};
	public static Vector sub(Vector v1, Vector v2)  {return new Vector(v1.x - v2.x, v1.y - v2.y);};
	public static Vector mult(double d, Vector v)   {return new Vector(d * v.x, d * v.y);}
	public static double mult(Vector v1, Vector v2) {return v1.x * v2.x + v1.y * v2.y;}
}

