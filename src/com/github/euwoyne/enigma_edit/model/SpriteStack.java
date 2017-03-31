package com.github.euwoyne.enigma_edit.model;

import java.util.ArrayList;

import com.github.euwoyne.enigma_edit.error.MissingImageException;

@SuppressWarnings("serial")
public class SpriteStack extends ArrayList<Sprite> implements Renderable
{
	@Override
	public void draw(RenderingAgent renderer, int x, int y, int size) throws MissingImageException
	{
		for (Sprite sprite : this)
			sprite.draw(renderer, x, y, size);
	}
}

