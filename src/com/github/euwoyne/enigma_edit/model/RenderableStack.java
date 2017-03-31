package com.github.euwoyne.enigma_edit.model;

import java.util.ArrayList;

import com.github.euwoyne.enigma_edit.error.MissingImageException;

@SuppressWarnings("serial")
public class RenderableStack extends ArrayList<Renderable> implements Renderable
{
	@Override
	public void draw(RenderingAgent renderer, int x, int y, int size) throws MissingImageException
	{
		for (Renderable r : this)
			r.draw(renderer, x, y, size);
	}
}

