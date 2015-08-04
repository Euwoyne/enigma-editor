package enigma_edit.model;

import javax.imageio.IIOException;

import enigma_edit.error.MissingImageException;

public interface Renderable
{
	void draw(RenderingAgent renderer, int x, int y) throws MissingImageException, IIOException;
}

