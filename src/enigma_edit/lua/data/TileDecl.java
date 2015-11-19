
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

import enigma_edit.lua.RevId;
import enigma_edit.lua.ReverseIDProvider;

/**
 * A tile declaration.
 * This is essentially a stack of tables or references to tables.
 */
public class TileDecl extends Value implements Iterable<TileDeclPart>
{
	private LinkedList<TileDeclPart> parts;
	private int                      type;
	private RevId                    revId;
	
	/**
	 * Copy constructor.
	 * 
	 * @param tile  tile declaration to copy.
	 */
	private TileDecl(TileDecl tile)
	{
		super(tile.code);
		this.parts = new LinkedList<TileDeclPart>();
		this.type = tile.type;
		this.revId = new RevId(tile.revId);
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
		this.type = 0;
		this.revId = new RevId();
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
		this.type = part.typeMask();
		this.revId = new RevId();
	}
	
	/**
	 * Add a new part to this tile declaration.
	 * The base code snippet is extended (see {@link CodeSnippet#extend(CodeSnippet)}.
	 * 
	 * @param part  New tile part to append.
	 */
	public void add(TileDeclPart part)
	{
		this.code = this.code.extend(part.getCode());
		this.parts.add(part);
		this.type |= part.typeMask();
		this.revId.clear();
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
	public MMObjectDecl getObject(int idx)
	{
		final ObjectDecl easy = this.getObject(idx, Mode2.EASY); 
		final ObjectDecl diff = this.getObject(idx, Mode2.DIFFICULT);
		if (easy == diff) return new MMObjectDecl(easy);
		return new MMObjectDecl(easy, diff);
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
	
	/**
	 * Return the type mask.
	 * That is the types of all parts for the given mode bitwise or'd.
	 * @see TileDeclPart#objtype(Mode2)
	 * 
	 * @param mode  Mode to get the types for.
	 * @return      This object's types.
	 */
	public int objtype(Mode2 mode)
	{
		return mode == Mode2.EASY ? type & TileDeclPart.T_MASK : type >> TileDeclPart.T_SIZE;
	}
	
	/**
	 * Return the type mask.
	 * That is the types of all parts bitwise or'd.
	 * @see TileDeclPart#typeMask()
	 * 
	 * @return  This object's types.
	 */
	public int typeMask()
	{
		return type;
	}
	
	/**
	 * Return the type mask (masked for given mode).
	 * That is the types of all parts bitwise or'd.
	 * @see TileDeclPart#typeMask(Mode)
	 * 
	 * @return  This object's types.
	 */
	public int typeMask(Mode mode)
	{
		switch(mode)
		{
		case EASY:      return type & TileDeclPart.T_MASK;
		case DIFFICULT: return type & ~TileDeclPart.T_MASK;
		default:        return type;
		}
	}
	
	/**
	 * Helper function that calculates the reverse ID for a given mode.
	 * 
	 * @param prorid  Provider of {@link ObjectDecl} reverse IDs.
	 * @param mode    Mode to use for value gathering.
	 * @return        This declaration's reverse ID.
	 */
	private String calculateRevId(ReverseIDProvider prorid, Mode2 mode)
	{
		ObjectDecl fl = null, it = null, ac = null, st = null, obj;
		String kind;
		for (Iterator<ObjectDecl> objIt = constructIterator(mode); objIt.hasNext(); )
		{
			obj = objIt.next();
			kind = obj.getKind(mode);
			if (kind.startsWith("#")) kind = kind.substring(1);
			if (kind.length() < 2) continue;
			switch (kind.substring(0, 2))
			{
			case "fl": fl = obj; break;
			case "it": it = obj; break;
			case "ac": ac = obj; break;
			case "st": st = obj; break;
			}
		}
		StringBuilder out = new StringBuilder();
		out.append((fl != null) ? prorid.getReverseID(fl, mode) : "nil");
		out.append(';');
		out.append((it != null) ? prorid.getReverseID(it, mode) : "nil");
		out.append(';');
		out.append((ac != null) ? prorid.getReverseID(ac, mode) : "nil");
		out.append(';');
		out.append((st != null) ? prorid.getReverseID(st, mode) : "nil");
		return out.toString();
	}
	
	/**
	 * Return the reverse IDs.
	 * That is a string uniquely identifying the tile (being the same for every
	 * equivalent declaration; i.e. declarations yielding the same floor-item-
	 * actor-stone quadruple).
	 * 
	 * @param prorid  Provider of {@link ObjectDecl} reverse IDs.
	 * @return        This declaration's reverse ID.
	 */
	public RevId reverseID(ReverseIDProvider prorid)
	{
		if (revId.empty())
		{
			revId.easy      = calculateRevId(prorid, Mode2.EASY);
			revId.difficult = calculateRevId(prorid, Mode2.DIFFICULT);
			if (revId.easy.equals(revId.difficult)) revId.normal = revId.difficult = revId.easy;
			else revId.normal = null;
		};
		return revId;
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
	public Iterator<TileDeclPart> iterator()
	{
		return parts.iterator();
	}
	
	public Iterator<MMObjectDecl> constructIterator()
	{
		return new Iterator<MMObjectDecl>()
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
			public MMObjectDecl next()
			{
				if (index >= easyCount && index >= diffCount)
					throw new NoSuchElementException();
				return getObject(index++);
			}
		};
	}
	
	public Iterator<ObjectDecl> constructIterator(Mode2 mode)
	{
		return new Iterator<ObjectDecl>()
		{
			private final int count = objectCount(mode);
			private int       index = 0;
			
			@Override
			public boolean hasNext()
			{
				return index < count;
			}
			
			@Override
			public ObjectDecl next()
			{
				if (index >= count)
					throw new NoSuchElementException();
				return getObject(index++, mode);
			}
		};
	}
}

