package enigma_edit.lua.api;

import java.util.TreeMap;

public class TileRepository
{
	private TreeMap<String, Tile>   tileMap;
	private TreeMap<String, String> keyMap;
	
	public void add(String key, Tile tile)
	{
		tileMap.put(key, tile);
		keyMap.put(tile.getUID(), key);
	}
	
	public String getKey(Tile tile)
	{
		return keyMap.get(tile.getUID());
	}
	
	public Tile getTile(String key)
	{
		return tileMap.get(key);
	}
	
	public void put(String key, Tile tile)
	{
		Tile old = tileMap.put(key, tile);
		keyMap.remove(old.getUID());
		keyMap.put(tile.getUID(), key);
	}
	
	public String register(Tile tile, String suggestedKeys, int keySize)
	{
		final String uid = tile.getUID();
		String key = keyMap.get(uid);
		if (key != null) return key;
		for (int i = 0; i < suggestedKeys.length(); i += keySize)
		{
			key = suggestedKeys.substring(i, i + keySize);
			if (tileMap.containsKey(key)) continue;
			tileMap.put(key, tile);
			keyMap.put(uid, key);
			return key;
		}
		return null;
	}
}

