
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
 * Multi.mode tile declaration.
 * @see TileDecl
 */
public class MMTileDecl extends MM<TileDecl>
{
	public MMTileDecl(TileDecl normal)                       {super(normal);}
	public MMTileDecl(TileDecl easy, TileDecl difficult)     {super(easy, difficult);}
	public MMTileDecl(MMTileDecl easy, MMTileDecl difficult) {super(easy.easy, difficult.difficult);}
	
	public void add(TileDeclPart part, Mode mode)
	{
		switch (mode)
		{
		case EASY:
			if (easy == null)
				easy = new TileDecl(part);
			else
			{
				if (easy == difficult)
					easy = easy.snapshot();
				easy.add(part);
			}
			break;
		
		case DIFFICULT:
			if (difficult == null)
				difficult = new TileDecl(part);
			else
			{
				if (easy == difficult)
					difficult = difficult.snapshot();
				difficult.add(part);
			}
			break;
		
		case NORMAL:
		default:
			if (easy == difficult)
			{
				if (easy == null)
					easy = difficult = new TileDecl(part);
				else
					easy.add(part);
			}
			else
			{
				if (easy == null)
					easy = new TileDecl(part);
				else
					easy.add(part);
				
				if (difficult == null)
					difficult = new TileDecl(part);
				else
					difficult.add(part);
			}
			break;
		}
	}
	
	public void add(MMTilePart part, Mode mode)
	{
		if (part.hasNormal())
			this.add(part.easy, mode);
		else
		{
			if (mode != Mode.DIFFICULT && part.hasEasy())
				this.add(part.easy, Mode.EASY);
			if (mode != Mode.EASY && part.hasDifficult())
				this.add(part.difficult, Mode.DIFFICULT);
		}
	}
	
	public void add(MMTileDecl tile, Mode mode)
	{
		if (tile.hasNormal())
		{
			for (TileDeclPart part : tile.easy.parts)
				this.add(part, mode);
		}
		else
		{
			if (mode != Mode.DIFFICULT && tile.hasEasy())
				for (TileDeclPart part : tile.easy.parts)
					this.add(part, Mode.EASY);
			
			if (mode != Mode.EASY && tile.hasDifficult())
				for (TileDeclPart part : tile.difficult.parts)
					this.add(part, Mode.DIFFICULT);
		}
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
		final ObjectDecl easy = this.easy.getObject(idx, Mode2.EASY); 
		final ObjectDecl diff = this.difficult.getObject(idx, Mode2.DIFFICULT);
		if (easy == diff) return new MMTileConstruct(easy);
		return new MMTileConstruct(easy, diff);
	}
	
	@Override public String typename() {return "tile";}
}

