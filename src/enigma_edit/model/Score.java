
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

public class Score
{
	private int time;
	
	public Score()                 {time = 0;}
	public Score(int min, int sec) {time = 60 * min + sec;}
	
	public int     get_seconds() {return time % 60;}
	public int     get_minutes() {return time / 60;}
	public boolean is_valid()    {return time != 0;}
	
	public void    reset()               {time = 0;}
	public void    set(int min, int sec) {time = 60 * min + sec;}
	
	public String toString()
	{
		if (time > 0)
			return String.format("%i:%i", time / 60, time % 60);
		else
			return "-";
	}
	
	public static Score parseScore(String s)
	{
		s = s.trim();
		final String[] mmss = s.split(":", 2);
		if (mmss.length != 2)
			return new Score();
		return new Score(Integer.parseUnsignedInt(mmss[0]), Integer.parseUnsignedInt(mmss[1]));
	}
}

