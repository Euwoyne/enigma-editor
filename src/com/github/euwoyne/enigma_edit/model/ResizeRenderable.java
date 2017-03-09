package com.github.euwoyne.enigma_edit.model;

import com.github.euwoyne.enigma_edit.error.MissingImageException;

public interface ResizeRenderable
{
	void draw(RenderingAgent renderer, int x, int y, int size) throws MissingImageException;
}

