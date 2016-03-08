package com.github.euwoyne.enigma_edit.model;

import com.github.euwoyne.enigma_edit.lua.RevId;
import com.github.euwoyne.enigma_edit.lua.ReverseInfo;
import com.github.euwoyne.enigma_edit.lua.data.Mode;
import com.github.euwoyne.enigma_edit.lua.data.Mode2;
import com.github.euwoyne.enigma_edit.lua.data.ObjectDecl;

public class ReverseResolver implements ReverseInfo
{
	final Tileset tileset;
	
	ReverseResolver(Tileset tileset)
	{
		this.tileset = tileset;
	}
	
	@Override
	public String getReverseID(ObjectDecl decl, Mode2 mode)
	{
		return tileset.getReverseID(decl, mode);
	}

	@Override
	public Status getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mode getMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setKey(String key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setKey(String key, int typeMask) {
		// TODO Auto-generated method stub

	}

	@Override
	public int typeMask() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RevId reverseId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RevId reverseId(int typeMask) {
		// TODO Auto-generated method stub
		return null;
	}

}

