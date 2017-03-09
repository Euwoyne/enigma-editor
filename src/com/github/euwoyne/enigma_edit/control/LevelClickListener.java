package com.github.euwoyne.enigma_edit.control;

import com.github.euwoyne.enigma_edit.lua.data.Tile;

public interface LevelClickListener
{
	void levelClicked(int x, int y, Tile tile);
}
