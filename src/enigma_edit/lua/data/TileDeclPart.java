
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
 * Part of a tile declaration.
 * There are two types of objects that can make up a tile.
 * <ol><li>A simple tile declaration by a defining lua table (see {@link ObjectDecl}).</li>
 * <li>A reference to an existing tile (see {@link TileReference}).</li></ol>
 */
public interface TileDeclPart extends Source 
{
	@Override public TileDeclPart snapshot();
	
	/**
	 * Return the {@code idx}-th part of this declaration.
	 * This is, after dereferencing all tile references, the {@code idx}-th
	 * table in the concatenation expression.
	 * 
	 * @param idx   Part index.
	 * @param mode  Mode to check for.
	 * @return      Requested tile part (as it is wrapped by {@link ObjectDecl}).
	 */
	public ObjectDecl getObject(int idx, Mode2 mode);
	
	/**
	 * Return the {@code idx}-th part of this declaration.
	 * This is, after dereferencing all tile references, the {@code idx}-th
	 * table in the concatenation expression.
	 * 
	 * @param idx   Part index.
	 * @return      Requested tile part (in both modes).
	 */
	public MMTileConstruct getObject(int idx);
	
	/**
	 * Return the number of tables this part consists of.
	 * 
	 * @param mode  Mode to check for.
	 * @return      Number of tables in this part.
	 */
	public int objectCount(Mode2 mode);
}

