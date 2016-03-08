
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
 * Part of a tile declaration.
 * There are two types of objects that can make up a tile.
 * <ol><li>A simple tile declaration by a defining lua table (see {@link ObjectDecl}).</li>
 * <li>A reference to an existing tile (see {@link TileReference}).</li></ol>
 */
public interface TileDeclPart extends Source 
{
	@Override public TileDeclPart snapshot();
	
	/** &quot;Floor&quot; type bit (used in {@link objtype}). */
	public static final int FL     = 0x01;
	
	/** &quot;Item&quot; type bit (used in {@link objtype}). */
	public static final int IT     = 0x02;
	
	/** &quot;Actorr&quot; type bit (used in {@link objtype}). */
	public static final int AC     = 0x04;
	
	/** &quot;Stone&quot; type bit (used in {@link objtype}). */
	public static final int ST     = 0x08;
	
	/** &quot;Other&quot; type bit (used in {@link objtype}). */
	public static final int OT     = 0x10;
	
	/** Object type bitmask length (in bits). */
	public static final int T_SIZE = 5;
	
	/** Object type mask (first {@link T_SIZE} bits set). */
	public static final int T_MASK = 0x1f;
	
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
	public MMObjectDecl getObject(int idx);
	
	/**
	 * Return the number of tables this part consists of.
	 * 
	 * @param mode  Mode to check for.
	 * @return      Number of tables in this part.
	 */
	public int objectCount(Mode2 mode);
	
	/**
	 * Return the type of the tile part for the given mode.
	 * 
	 * @return  Bitmask of the type bit constants {@link FL}, {@link IT},
	 *          {@link AC}, {@link ST} and {@link OT}. Or {@code 0}, if the
	 *          type is not recognized. 
	 */
	public int objtype(Mode2 mode);
	
	/**
	 * Return the type mask of the tile part.
	 * 
	 * @return  Type bitmask for the tile. The least significant {@link T_SIZE}
	 *          bits correspond to the easy mode type. The bits for difficult
	 *          mode follow immediately after.
	 */
	public int typeMask();
	
	/**
	 * Return the type mask of the tile part.
	 * This masks out bits not used in the given mode.
	 * 
	 * @return  Type bitmask for the tile. The least significant {@link T_SIZE}
	 *          bits correspond to the easy mode type. The bits for difficult
	 *          mode follow immediately after. The bits not relevant for the
	 *          given mode will be set to zero. 
	 */
	public int typeMask(Mode mode);
}

