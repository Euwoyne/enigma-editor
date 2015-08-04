
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

import enigma_edit.lua.data.Mode;
import enigma_edit.lua.data.Mode2;
import enigma_edit.lua.data.Tile;
import enigma_edit.model.Tileset.NamedImage;

public interface ImageTile
{
	public static interface Part extends Iterable<NamedImage>, Renderable
	{
		boolean isEmpty();
		boolean isCluster();
	}
	
	public static interface MMPart
	{
		Part get(Mode  mode);
		Part get(Mode2 mode);
	}
	
	Tile   tile();
	
	MMPart fl();
	MMPart it();
	MMPart ac();
	MMPart st();
	
	void draw_fl(RenderingAgent renderer, int x, int y, Mode mode);
	void draw_it(RenderingAgent renderer, int x, int y, Mode mode);
	void draw_ac(RenderingAgent renderer, int x, int y, Mode mode);
	void draw_st(RenderingAgent renderer, int x, int y, Mode mode);
}

