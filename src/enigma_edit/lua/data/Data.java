
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
 * Base class for any data.
 */
public abstract class Data implements Typed
{
	@Override public Data snapshot() {return this;};
	
	/*
	 *  TYPE ACCESS
	 * =============
	 */
	@Override public String typename(Mode  mode) {return mode == Mode.NORMAL ? this.typename() : this.typename(mode.mode2());}
	
	
	/*
	 *  TYPE CHECKING
	 * ===============
	 */
	@Override public MMNil         checkNil()                 {return new MMNil        (checkNil(      Mode2.EASY), checkNil(      Mode2.DIFFICULT));}
	@Override public MMSimpleValue checkSimple()              {return new MMSimpleValue(checkSimple(   Mode2.EASY), checkSimple(   Mode2.DIFFICULT));}
	@Override public MMTable       checkTable()               {return new MMTable      (checkTable(    Mode2.EASY), checkTable(    Mode2.DIFFICULT));}
	@Override public MMTilePart    checkTilePart()            {return new MMTilePart   (checkTilePart( Mode2.EASY), checkTilePart( Mode2.DIFFICULT));}
	@Override public MMTileDecl    checkTile()                {return new MMTileDecl   (checkTile(     Mode2.EASY), checkTile(     Mode2.DIFFICULT));}
	@Override public MMResolver    checkResolver()            {return new MMResolver   (checkResolver( Mode2.EASY), checkResolver( Mode2.DIFFICULT));}
	
	@Override public MMNil         checkNil(Mode mode)        {return new MMNil        (mode != Mode.DIFFICULT ? checkNil(      Mode2.EASY)      : null,
	                                                                                    mode != Mode.EASY      ? checkNil(      Mode2.DIFFICULT) : null);}
	@Override public MMSimpleValue checkSimple(Mode mode)     {return new MMSimpleValue(mode != Mode.DIFFICULT ? checkSimple(   Mode2.EASY)      : null,
                                                                                        mode != Mode.EASY      ? checkSimple(   Mode2.DIFFICULT) : null);}
	@Override public MMTable       checkTable(Mode mode)      {return new MMTable(      mode != Mode.DIFFICULT ? checkTable(    Mode2.EASY)      : null,
                                                                                        mode != Mode.EASY      ? checkTable(    Mode2.DIFFICULT) : null);}
	@Override public MMTilePart    checkTilePart(Mode mode)   {return new MMTilePart(   mode != Mode.DIFFICULT ? checkTilePart( Mode2.EASY)      : null,
	                                                                                    mode != Mode.EASY      ? checkTilePart( Mode2.DIFFICULT) : null);}
	@Override public MMTileDecl    checkTile(Mode mode)       {return new MMTileDecl(   mode != Mode.DIFFICULT ? checkTile(     Mode2.EASY)      : null,
	                                                                                    mode != Mode.EASY      ? checkTile(     Mode2.DIFFICULT) : null);}
	@Override public MMResolver    checkResolver(Mode mode)   {return new MMResolver(   mode != Mode.DIFFICULT ? checkResolver( Mode2.EASY)      : null,
	                                                                                    mode != Mode.EASY      ? checkResolver( Mode2.DIFFICULT) : null);}
	
	/*
	 *  MODE CHECKING
	 * ===============
	 */
	@Override public boolean isNormal()      {return true;}
	@Override public boolean isMixed()       {return false;}
	@Override public boolean isComplete()    {return true;}
	@Override public boolean isNull()        {return false;}
	@Override public boolean onlyEasy()      {return false;}
	@Override public boolean onlyDifficult() {return false;}
	@Override public boolean hasEasy()       {return true;}
	@Override public boolean hasDifficult()  {return true;}
	@Override public boolean hasNormal()     {return true;}
	
	@Override
	public boolean isDefined(Mode mode)
	{
		switch (mode)
		{
		case EASY:      return hasEasy();
		case DIFFICULT: return hasDifficult();
		case NORMAL:    return hasNormal();
		default:        return false;
		}
	}
}

