
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

/**
 * Mode type.
 * The value {@code NORMAL} refers to values, unrestricted by
 * mode-dependent conditionals.
 */
public enum Mode
{
	/** easy mode. */         EASY,
	/** difficult mode. */    DIFFICULT,
	/** unrestricted mode. */ NORMAL;
	
	/**
	 * Convert to two-value mode.
	 * {@link #NORMAL} will be mapped to {@code null}.
	 * 
	 * @return  The two-value {@link Mode2} corresponding to this mode.
	 */
	public Mode2 mode2()
	{
		switch (this)
		{
		case EASY:      return Mode2.EASY;
		case DIFFICULT: return Mode2.DIFFICULT;
		default:        return null;
		}
	}
}

