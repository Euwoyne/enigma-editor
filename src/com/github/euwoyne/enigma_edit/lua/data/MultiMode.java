
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

package com.github.euwoyne.enigma_edit.lua.data;

/**
 * A multi-mode value, that has its own source.
 */
public class MultiMode extends SourceData implements Dereferencable
{
	/** Easy-mode value */
	private Source easy;
	
	/** Difficult-mode value */
	private Source difficult;
	
	/**
	 * Create from an already multi-mode valued dereferencable.
	 * This is used to wrap an instance of {@link MM} into a
	 * {@link Source}.
	 * 
	 * @param value  Value to initializes this object with.
	 * @param code   Code, that created the dereferencable.
	 */
	public MultiMode(Dereferencable value, CodeSnippet code)
	{
		super(code);
		this.easy = value.deref(Mode2.EASY);
		this.difficult = value.deref(Mode2.DIFFICULT);
	}
	
	/**
	 * Create from separate values for each mode.
	 * 
	 * @param easy       Value in easy mode.
	 * @param difficult  Value in difficult mode.
	 * @param code       Code that joined the two values into one.
	 */
	
	public MultiMode(Source easy, Source difficult, CodeSnippet code)
	{
		super(code);
		this.easy = easy;
		this.difficult = difficult;
	}
	
	@Override public boolean isNormal()      {return easy == difficult;}
	@Override public boolean isMixed()       {return easy != null && difficult != null && easy != difficult;}
	@Override public boolean isComplete()    {return easy != null && difficult != null;}
	@Override public boolean isNull()        {return easy == null && difficult == null;}
	@Override public boolean onlyEasy()      {return easy != null && difficult == null;}
	@Override public boolean onlyDifficult() {return easy == null && difficult != null;}
	@Override public boolean hasEasy()       {return easy != null;}
	@Override public boolean hasDifficult()  {return difficult != null;}
	@Override public boolean hasNormal()     {return easy == difficult && easy != null;}
	
	@Override public Source        deref(Mode2 mode)          {return mode == Mode2.EASY ? easy : difficult;}
	
	@Override public String        typename()                 {return "<multi-mode source>";}
	@Override public String        typename(Mode2 mode)       {return deref(mode).typename(mode);}
	@Override public SourceData    snapshot()                 {return this;}
	@Override public String        toString()
	{
		if (easy == difficult) return (easy != null) ? easy.toString() : "null";
		if (easy == null)      return difficult.toString();
		if (difficult == null) return easy.toString();
		return "cond(difficult, " + difficult.toString() + ", " + easy.toString() + ")";
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
	
	@Override public Value         checkValue(Mode2 mode)     {final Source val = deref(mode); return (val != null) ? val.checkValue(mode)     : null;}
	@Override public Nil           checkNil(Mode2 mode)       {final Source val = deref(mode); return (val != null) ? val.checkNil(mode)       : null;}
	@Override public SimpleValue   checkSimple(Mode2 mode)    {final Source val = deref(mode); return (val != null) ? val.checkSimple(mode)    : null;}
	@Override public Table         checkTable(Mode2 mode)     {final Source val = deref(mode); return (val != null) ? val.checkTable(mode)     : null;}
	@Override public TileDeclPart  checkTilePart(Mode2 mode)  {final Source val = deref(mode); return (val != null) ? val.checkTilePart(mode)  : null;}
	@Override public TileDecl      checkTile(Mode2 mode)      {final Source val = deref(mode); return (val != null) ? val.checkTile(mode)      : null;}
	@Override public Resolver      checkResolver(Mode2 mode)  {final Source val = deref(mode); return (val != null) ? val.checkResolver(mode)  : null;}
}

