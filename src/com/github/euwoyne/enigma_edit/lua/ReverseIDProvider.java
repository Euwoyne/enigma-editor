package com.github.euwoyne.enigma_edit.lua;

import com.github.euwoyne.enigma_edit.lua.data.Mode2;
import com.github.euwoyne.enigma_edit.lua.data.ObjectDecl;

public interface ReverseIDProvider
{
	String getReverseID(ObjectDecl decl, Mode2 mode);
}

