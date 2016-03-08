package com.github.euwoyne.enigma_edit.lua;

import com.github.euwoyne.enigma_edit.lua.data.Mode;
import com.github.euwoyne.enigma_edit.lua.data.TileDecl;
import com.github.euwoyne.enigma_edit.lua.res.Tiles;

public class CodeEditor
{
	//private CodeData data;
	private Tiles    tiles;
	
	public CodeEditor(CodeData data)
	{
		//this.data  = data;
		this.tiles = data.getTiles();
	}
	
	public void prepareTile(String key, TileDecl decl, Mode mode)
	{
		// TODO: tile code insertion
		if (tiles.exist(key))
			System.out.println("Update tile: ti[\"" + key + "\"] = " + decl.toString());
		else
			System.out.println("Create tile: ti[\"" + key + "\"] = " + decl.toString());
	}
	
	public StringBuilder apply(StringBuilder code)
	{
		// TODO: code update
		System.out.println("Update code: NOT IMPLEMENTED!");
		return code;
	}
}
