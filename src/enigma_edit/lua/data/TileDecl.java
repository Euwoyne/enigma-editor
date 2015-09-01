
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * A tile declaration.
 * This is essentially a stack of tables or references to tables.
 */
public class TileDecl extends Value implements Iterable<MMTileConstruct>
{
	public LinkedList<TileDeclPart> parts;
	
	/**
	 * Copy constructor.
	 * 
	 * @param tile  tile declaration to copy.
	 */
	private TileDecl(TileDecl tile)
	{
		super(tile.code);
		this.parts = new LinkedList<TileDeclPart>();
		for (TileDeclPart part : tile.parts)
			this.parts.add(part.snapshot());
	}
	
	/**
	 * Constructor.
	 * Creates a new tile declaration.
	 */
	public TileDecl()
	{
		super(CodeSnippet.NONE);
		this.parts = new LinkedList<TileDeclPart>();
	}
	
	/**
	 * Construct with initial part.
	 * Creates a new tile declaration.
	 * 
	 * @param part  Basic code snippet.
	 */
	public TileDecl(TileDeclPart part)
	{
		super(part.getCode());
		this.parts = new LinkedList<TileDeclPart>();
		this.parts.add(part);
	}
	
	/**
	 * Add a new part to this tile declaration.
	 * The base code snippet is extended (see {@link CodeSnippet#extend(CodeSnippet)}.
	 * 
	 * @param part  New tile part to append.
	 */
	public void add(TileDeclPart part)
	{
		this.parts.add(part);
		this.code = this.code.extend(part.getCode());
	}
	
	/**
	 * Return the {@code idx}-th part of this declaration.
	 * This is, after dereferencing all tile references, the {@code idx}-th
	 * table in the concatenation expression.
	 * 
	 * @param idx   Part index.
	 * @param mode  Mode to check for.
	 * @return      Requested tile part (as it is wrapped by {@link ObjectDecl}).
	 */
	public ObjectDecl getObject(int idx, Mode2 mode)
	{
		int cnt = 0, pcnt = 0;
		for (TileDeclPart part : parts)
		{
			pcnt = part.objectCount(mode);
			if (cnt + pcnt > idx)
				return part.getObject(idx - cnt, mode);
			cnt += pcnt;
		}
		return null;
	}
	
	/**
	 * Return the {@code idx}-th part of this declaration.
	 * This is, after dereferencing all tile references, the {@code idx}-th
	 * table in the concatenation expression.
	 * 
	 * @param idx   Part index.
	 * @return      Requested tile part (in both modes).
	 */
	public MMTileConstruct getObject(int idx)
	{
		final ObjectDecl easy = this.getObject(idx, Mode2.EASY); 
		final ObjectDecl diff = this.getObject(idx, Mode2.DIFFICULT);
		if (easy == diff) return new MMTileConstruct(easy);
		return new MMTileConstruct(easy, diff);
	}
	
	/**
	 * Return the number of objects this tile consists of.
	 * 
	 * @param mode  Mode to count the parts for.
	 * @return      The number of tables this declaration consists of after dereference.
	 */
	public int objectCount(Mode2 mode)
	{
		int cnt = 0;
		for (TileDeclPart part : parts)
			cnt += part.objectCount(mode);
		return cnt;
	}
	
	@Override public String   typename()            {return "tile";}
	@Override public String   typename(Mode2 mode)  {return "tile";}
	@Override public TileDecl checkTile(Mode2 mode) {return this;}
	@Override public TileDecl snapshot()            {return new TileDecl(this);}
	
	@Override
	public String toString()
	{
		if (parts.isEmpty())   return "nil";
		if (parts.size() == 1) return parts.getFirst().toString();
		
		StringBuilder out = new StringBuilder();
		java.util.Iterator<TileDeclPart> partit = parts.iterator();
		out.append(partit.next().toString());
		while (partit.hasNext())
		{
			out.append(" .. ");
			out.append(partit.next().toString());
		}
		return out.toString();
	}
	
	@Override
	public Iterator<MMTileConstruct> iterator()
	{
		return new Iterator<MMTileConstruct>()
		{
			private final int easyCount = objectCount(Mode2.EASY);
			private final int diffCount = objectCount(Mode2.DIFFICULT);
			private int       index     = 0;
			
			@Override
			public boolean hasNext()
			{
				return index < easyCount || index < diffCount;
			}
			
			@Override
			public MMTileConstruct next()
			{
				if (index >= easyCount && index >= diffCount)
					throw new NoSuchElementException();
				return getObject(index++);
			}
		};
	}
}

