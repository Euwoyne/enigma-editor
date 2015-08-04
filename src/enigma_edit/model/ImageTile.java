
/*
  Enigma Editor
  Copyright (C) 2015 Dominik Lehmann
  
  Licensed under the EUPL, Version 1.1 or – as soon they
  will be approved by the European Commission - subsequent
  versions of the EUPL (the "Licence");
  You may not use this work except in compliance with the
  Licence.
  You may obtain a copy of the Licence at:
  
  https://joinup.ec.europa.eu/software/page/eupl
  
  Unless required by applicable law or agreed to in
  writing, software distributed under the Licence is
  distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  express or implied.
  See the Licence for the specific language governing
  permissions and limitations under the Licence.
*/

package enigma_edit.model;

import java.util.ArrayList;

import enigma_edit.lua.data.*;

public class ImageTile extends Tile
{
	private static class Images
	{
		private static void draw(RenderingAgent render, int x, int y, ArrayList<Tileset.Image> images)
		{
			for (Tileset.Image image : images)
			{
				while (image != null)
				{
					render.draw(image.sprite, x, y);
					image = image.stack;
				}
			}
		}
		
		final ArrayList<Tileset.Image> easy;
		final ArrayList<Tileset.Image> difficult;
		
		Images(ArrayList<Tileset.Image> easy, ArrayList<Tileset.Image> difficult)
		{
			this.easy = easy;
			this.difficult = difficult;
		}
		
		Images(ArrayList<Tileset.Image> normal)
		{
			this.easy = normal;
			this.difficult = normal;
		}
		
		void draw(RenderingAgent render, int x, int y, Mode mode)
		{
			switch (mode)
			{
			case EASY:      if (easy != null)                      draw(render, x, y, easy);
			case DIFFICULT: if (difficult != null)                 draw(render, x, y, difficult);
			case NORMAL:    if (easy != null && easy == difficult) draw(render, x, y, easy);
			}
		}
	}
	
	private final Images floorImg;
	private final Images itemImg;
	private final Images actorImg;
	private final Images stoneImg;
	
	private ImageTile(ImageTile easy, ImageTile difficult)
	{
		this.add(Tile.composeMode(easy, difficult), Mode.NORMAL);
		floorImg = new Images(easy.floorImg.easy, difficult.floorImg.difficult);
		itemImg  = new Images(easy.itemImg.easy,  difficult.itemImg.difficult);
		actorImg = new Images(easy.actorImg.easy, difficult.actorImg.difficult);
		stoneImg = new Images(easy.stoneImg.easy, difficult.stoneImg.difficult);
	}
	
	public ImageTile(Tile tile, Tileset tileset)
	{
		this.add(tile, Mode.NORMAL);
		
		if (floor.hasNormal())
			this.floorImg = new Images(tileset.getImages(floor.get(Mode.NORMAL), Mode.NORMAL));
		else
			this.floorImg = new Images(
					floor.hasEasy()      ? tileset.getImages(floor.get(Mode.EASY),      Mode.EASY)      : null,
					floor.hasDifficult() ? tileset.getImages(floor.get(Mode.DIFFICULT), Mode.DIFFICULT) : null);
		
		if (item.hasNormal())
			this.itemImg = new Images(tileset.getImages(item.get(Mode.NORMAL), Mode.NORMAL));
		else
			this.itemImg = new Images(
					item.hasEasy()      ? tileset.getImages(item.get(Mode.EASY),      Mode.EASY)      : null,
					item.hasDifficult() ? tileset.getImages(item.get(Mode.DIFFICULT), Mode.DIFFICULT) : null);
		
		if (actor.hasNormal())
			this.actorImg = new Images(tileset.getImages(actor.get(Mode.NORMAL), Mode.NORMAL));
		else
			this.actorImg = new Images(
					actor.hasEasy()      ? tileset.getImages(actor.get(Mode.EASY),      Mode.EASY)      : null,
					actor.hasDifficult() ? tileset.getImages(actor.get(Mode.DIFFICULT), Mode.DIFFICULT) : null);
		
		if (stone.hasNormal())
			this.stoneImg = new Images(tileset.getImages(stone.get(Mode.NORMAL), Mode.NORMAL));
		else
			this.stoneImg = new Images(
					stone.hasEasy()      ? tileset.getImages(stone.get(Mode.EASY),      Mode.EASY)      : null,
					stone.hasDifficult() ? tileset.getImages(stone.get(Mode.DIFFICULT), Mode.DIFFICULT) : null);
	}
	
	public ImageTile(Tile tile, Tileset tileset, ImageTile defaultTile)
	{
		floor.easy         = defaultTile.floor.easy;
		floor.easyKey      = defaultTile.floor.easyKey;
		floor.difficult    = defaultTile.floor.difficult;
		floor.difficultKey = defaultTile.floor.difficultKey;
		this.add(tile, Mode.NORMAL);
		
		if (floor.hasNormal())
			this.floorImg = new Images(tileset.getImages(floor.get(Mode.NORMAL), Mode.NORMAL));
		else
			this.floorImg = new Images(
					floor.hasEasy()      ? tileset.getImages(floor.get(Mode.EASY),      Mode.EASY)      : null,
					floor.hasDifficult() ? tileset.getImages(floor.get(Mode.DIFFICULT), Mode.DIFFICULT) : null);
		
		if (item.hasNormal())
			this.itemImg = new Images(tileset.getImages(item.get(Mode.NORMAL), Mode.NORMAL));
		else
			this.itemImg = new Images(
					item.hasEasy()      ? tileset.getImages(item.get(Mode.EASY),      Mode.EASY)      : null,
					item.hasDifficult() ? tileset.getImages(item.get(Mode.DIFFICULT), Mode.DIFFICULT) : null);
		
		if (actor.hasNormal())
			this.actorImg = new Images(tileset.getImages(actor.get(Mode.NORMAL), Mode.NORMAL));
		else
			this.actorImg = new Images(
					actor.hasEasy()      ? tileset.getImages(actor.get(Mode.EASY),      Mode.EASY)      : null,
					actor.hasDifficult() ? tileset.getImages(actor.get(Mode.DIFFICULT), Mode.DIFFICULT) : null);
		
		if (stone.hasNormal())
			this.stoneImg = new Images(tileset.getImages(stone.get(Mode.NORMAL), Mode.NORMAL));
		else
			this.stoneImg = new Images(
					stone.hasEasy()      ? tileset.getImages(stone.get(Mode.EASY),      Mode.EASY)      : null,
					stone.hasDifficult() ? tileset.getImages(stone.get(Mode.DIFFICULT), Mode.DIFFICULT) : null);
	}
	
	public void draw_fl(RenderingAgent render, int x, int y, Mode mode) {floorImg.draw(render, x, y, mode);}
	public void draw_it(RenderingAgent render, int x, int y, Mode mode) {itemImg.draw(render, x, y, mode);}
	public void draw_ac(RenderingAgent render, int x, int y, Mode mode) {actorImg.draw(render, x, y, mode);}
	public void draw_st(RenderingAgent render, int x, int y, Mode mode) {stoneImg.draw(render, x, y, mode);}
	
	public static ImageTile composeMode(ImageTile easy, ImageTile difficult)
	{
		return new ImageTile(easy, difficult);
	}
}

