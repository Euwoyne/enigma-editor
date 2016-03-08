
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

package com.github.euwoyne.enigma_edit.lua.res;

import com.github.euwoyne.enigma_edit.lua.ReverseInfo;
import com.github.euwoyne.enigma_edit.lua.data.CodeSnippet;
import com.github.euwoyne.enigma_edit.lua.data.Mode;
import com.github.euwoyne.enigma_edit.lua.data.Mode2;
import com.github.euwoyne.enigma_edit.lua.data.Resolver;
import com.github.euwoyne.enigma_edit.lua.data.SourceData;
import com.github.euwoyne.enigma_edit.lua.data.Tile;

public class Custom extends SourceData implements Resolver
{
	public Custom(CodeSnippet code)                            {super(code);}
	@Override public Tile     resolve(String key,  Mode mode)  {return null;}
	@Override public int      reverse(ReverseInfo info)        {return 0;}
	@Override public Resolver getSubresolver(Mode2 mode)       {return null;}
	@Override public Tiles    getTiles(Mode2 mode)             {return null;}
	@Override public String   typename()                       {return "res.custom";}
	@Override public Resolver checkResolver(Mode2 mode)        {return this;}
}

