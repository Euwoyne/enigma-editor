
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

import enigma_edit.lua.res.Tiles;

/**
 * This is a special {@link FieldReference}, that stores a reference to
 * a member of the {@link Tiles Tiles}-Resolver.
 */
public class TileReference extends FieldReference implements TileDeclPart
{
	/**
	 * Constructor.
	 * 
	 * @param tiles  The tiles resolver (hosting the referenced tile).
	 * @param key    Key corresponding to the referenced tile.
	 * @param code   Code location of this reference.
	 */
	public TileReference(Tiles tiles, String key, CodeSnippet code)
	{
		super(tiles, key, code);
	}
	
	@Override public ObjectDecl getObject(int idx, Mode2 mode)
	{
		final TileDecl tile = this.checkTile(mode);
		return tile != null ? tile.getObject(idx, mode) : null;
	}
	
	@Override public MMTileConstruct getObject(int idx)
	{
		final MMTileDecl tile = this.checkTile();
		return tile != null ? tile.getObject(idx) : null;
	}
	
	@Override public int           objectCount(Mode2 mode)   {final TileDecl tile = this.checkTile(mode); return tile != null ? tile.objectCount(mode) : 0;}
	@Override public CodeSnippet   getCode()                 {return code;}
	@Override public TileDeclPart  checkTilePart(Mode2 mode) {return this;}
	@Override public MMTilePart    checkTilePart()           {return new MMTilePart(this);}
	@Override public String        typename()                {return "<tile-reference>";}
	@Override public String        toString()                {return "ti[" + this.key + "]";}
	@Override public TileReference snapshot()                {return this;}
}

