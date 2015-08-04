package enigma_edit.lua.data;

/**
 * Base class for any call, that is used to create the world.
 * this includes the API 2.0 function {@code wo} as well as the
 * API 1.0 function {@code CreateWorld} and the old {@code create_world}.
 */
public interface WoCall
{
	/**
	 * Resolve the tile for the given position.
	 * 
	 * @param x     X coordinate of the field ({@code 1 <= x <= width})
	 * @param y     Y coordinate of the field ({@code 1 <= y <= height})
	 * @return      ImageTile at the specified position {@code (x,y)} on the map;
	 *              or {@code null}, if the position is outside of the map or if the resolver fails.
	 */
	Tile getTile(int x, int y);
	
	/**
	 * Returns the tile declaration used, when no object was defined.
	 * 
	 * @param mode  Mode to resolve the tile for.
	 * @return      Default tile for the given mode.
	 */
	public Tile getDefaultTile(Mode2 mode);
	
	/**
	 * Get the worlds width (in tiles).
	 * 
	 * @param mode  Mode to check the world for.
	 * @return      The width of the requested world (in tiles). Or {@code -1}, in case of non-unique result.
	 */
	int getWidth(Mode mode);

	/**
	 * Get the worlds width (in tiles).
	 * @return The width of the requested world (in tiles).
	 */
	MMSimpleValue getWidth();

	/**
	 * Get the worlds height (in tiles).
	 * 
	 * @param mode  Mode to check the world for.
	 * @return      The height of the requested world (in tiles).
	 */
	int getHeight(Mode mode);

	/**
	 * Get the worlds height (in tiles).
	 * @return The width of the requested world (in tiles).
	 */
	MMSimpleValue getHeight();
}

