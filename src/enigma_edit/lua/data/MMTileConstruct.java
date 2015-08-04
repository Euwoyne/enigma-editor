
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

package enigma_edit.lua.data;

import enigma_edit.lua.data.TilePart.Construct;

/**
 * Multi-mode tile constructor.
 */
public class MMTileConstruct extends MM<Construct>
{
	public MMTileConstruct(Construct normal)                                {super(normal);}
	public MMTileConstruct(Construct easy, Construct difficult)             {super(easy, difficult);}
	public MMTileConstruct(MMTileConstruct easy, MMTileConstruct difficult) {super(easy.easy, difficult.difficult);}
	
	@Override public String typename() {return "<mixed tile-constructor>";}
}

