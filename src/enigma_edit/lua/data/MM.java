
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
 * A value with different sources per mode.
 */
abstract class MM<T extends Source> extends Data implements Dereferencable
{
	/** Easy mode value. */
	public T easy;
	
	/** Difficult mode value. */
	public T difficult;
	
	/**
	 * Normal mode constructor.
	 * Sets {@link this#easy} adn {@link this#difficult} both to the given value.
	 * 
	 * @param normal  Normal mode value.
	 */
	MM(T normal)
	{
		this.easy = normal;
		this.difficult = normal;
	}
	
	/**
	 * Multi-mode constructor.
	 * 
	 * @param easy       Value for easy mode.
	 * @param difficult  Value for difficult mode.
	 */
	MM(T easy, T difficult)
	{
		this.easy = easy;
		this.difficult = difficult;
	}
	
	/** Check if this is undefined in the given mode. */
	public boolean isNull(Mode mode)
	{
		switch (mode)
		{
		case EASY:      return easy == null;
		case DIFFICULT: return difficult == null;
		default:        return easy == null || difficult == null; 
		}
	}
	
	@Override public boolean isNormal()      {return hasNormal() && easy.isNormal();}
	@Override public boolean isMixed()       {return easy != null && difficult != null && easy != difficult;}
	@Override public boolean isComplete()    {return easy != null && difficult != null;}
	@Override public boolean isNull()        {return easy == null && difficult == null;}
	@Override public boolean onlyEasy()      {return easy != null && difficult == null;}
	@Override public boolean onlyDifficult() {return easy == null && difficult != null;}
	@Override public boolean hasEasy()       {return easy != null;}
	@Override public boolean hasDifficult()  {return difficult != null;}
	@Override public boolean hasNormal()     {return easy == difficult && easy != null;}
	
	/** Get the value corresponding to the given mode */
	public T get(Mode mode)
	{
		switch (mode)
		{
		case EASY:      return easy;
		case DIFFICULT: return difficult;
		case NORMAL:
		default:        return (easy == difficult) ? easy : null;
		}
	}
	
	/** Get the value corresponding to the given mode */
	public T get(Mode2 mode)
	{
		return mode == Mode2.EASY ? easy : difficult;
	}
	
	/**
	 * Create a multi-mode instance.
	 * @param code  Code snippet this multi-mode value is uniquely constructed by.
	 */
	public MultiMode toMultiMode(CodeSnippet code)
	{
		return new MultiMode(easy, difficult, code);
	}
	
	@Override public T      deref(Mode2 mode)    {return (mode == Mode2.EASY) ? easy : difficult;}
	@Override public String typename()           {return "<mixed>";}
	@Override public String typename(Mode2 mode)
	{
		switch (mode)
		{
		case EASY:      return (easy == null)      ? "nil" : easy.typename(mode);
		case DIFFICULT: return (difficult == null) ? "nil" : difficult.typename(mode);
		default:        return "nil";
		}
	}
	
	@Override
	public String toString()
	{
		if (easy == difficult) return (easy != null) ? easy.toString() : "null";
		if (easy == null)      return difficult.toString();
		if (difficult == null) return easy.toString();
		return "cond(wo[\"isDifficult\"], " + difficult.toString() + ", " + easy.toString() + ")";
	}
	
	@Override public MMNil         checkNil()                 {return new MMNil(        checkNil(      Mode2.EASY), checkNil(      Mode2.DIFFICULT));}
	@Override public MMSimpleValue checkSimple()              {return new MMSimpleValue(checkSimple(   Mode2.EASY), checkSimple(   Mode2.DIFFICULT));}
	@Override public MMTable       checkTable()               {return new MMTable(      checkTable(    Mode2.EASY), checkTable(    Mode2.DIFFICULT));}
	@Override public MMTilePart    checkTilePart()            {return new MMTilePart(   checkTilePart( Mode2.EASY), checkTilePart( Mode2.DIFFICULT));}
	@Override public MMTileDecl    checkTile()                {return new MMTileDecl(   checkTile(     Mode2.EASY), checkTile(     Mode2.DIFFICULT));}
	@Override public MMResolver    checkResolver()            {return new MMResolver(   checkResolver( Mode2.EASY), checkResolver( Mode2.DIFFICULT));}
	
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
	
	@Override public Value         checkValue(Mode2 mode)     {final T val = deref(mode); return (val != null) ? val.checkValue(mode)     : null;}
	@Override public Nil           checkNil(Mode2 mode)       {final T val = deref(mode); return (val != null) ? val.checkNil(mode)       : null;}
	@Override public SimpleValue   checkSimple(Mode2 mode)    {final T val = deref(mode); return (val != null) ? val.checkSimple(mode)    : null;}
	@Override public Table         checkTable(Mode2 mode)     {final T val = deref(mode); return (val != null) ? val.checkTable(mode)     : null;}
	@Override public TileDeclPart      checkTilePart(Mode2 mode)  {final T val = deref(mode); return (val != null) ? val.checkTilePart(mode)  : null;}
	@Override public TileDecl      checkTile(Mode2 mode)      {final T val = deref(mode); return (val != null) ? val.checkTile(mode)      : null;}
	@Override public Resolver      checkResolver(Mode2 mode)  {final T val = deref(mode); return (val != null) ? val.checkResolver(mode)  : null;}
}

