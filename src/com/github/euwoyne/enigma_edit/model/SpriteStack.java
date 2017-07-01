package com.github.euwoyne.enigma_edit.model;

import java.util.ArrayList;

import com.github.euwoyne.enigma_edit.error.MissingImageException;

public class SpriteStack extends ArrayList<Sprite> implements Renderable
{
	private static final long serialVersionUID = 1L;
	
	@Override
	public void draw(RenderingAgent renderer, int x, int y, int size) throws MissingImageException
	{
		for (Sprite sprite : this)
			sprite.draw(renderer, x, y, size);
	}
}

