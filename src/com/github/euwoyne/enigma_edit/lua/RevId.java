package com.github.euwoyne.enigma_edit.lua;

import com.github.euwoyne.enigma_edit.lua.data.Mode;

public class RevId
{
	public String easy, difficult, normal;
	
	public RevId()             {easy = difficult = normal = null;}
	public RevId(RevId revId)  {easy = revId.easy; difficult = revId.difficult; normal = revId.normal;}
	public void    clear()     {easy = difficult = normal = null;}
	public boolean empty()     {return easy == null && difficult == null && normal == null;}
	
	public String get(Mode mode)
	{
		switch (mode)
		{
		case EASY:      return easy;
		case DIFFICULT: return difficult;
		default:        return normal;
		}
	}
	
	public static RevId composeMode(RevId easy, RevId difficult)
	{
		RevId out = new RevId();
		out.easy = easy.easy;
		out.difficult = difficult.difficult;
		if (out.easy.equals(out.difficult))
			out.normal = out.difficult = out.easy;
		return out;
	}
}
