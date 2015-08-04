
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

package enigma_edit.lua;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.LuaC;

public class APILoader
{
	private Prototype  startup; 
	private Prototype  api1init;
	private Prototype  api2init;
	
	public APILoader(Path enigmaPath) throws IOException
	{
		startup  = LuaC.instance.compile(new FileInputStream(enigmaPath + "/startup.lua"),  "startup.lua");
		api1init = LuaC.instance.compile(new FileInputStream(enigmaPath + "/api1init.lua"), "api1init.lua");
		api2init = LuaC.instance.compile(new FileInputStream(enigmaPath + "/api2init.lua"), "api2init.lua");
	}
	
	public EnigmaAPI prepareAPI(Globals globals)
	{
		globals.load("enigmaAPI = require('enigma_edit.lua.EnigmaAPI')").invoke();
		EnigmaAPI api = (EnigmaAPI)globals.get("enigmaAPI").touserdata(EnigmaAPI.class);
		globals.set("enigmaAPI", LuaValue.NIL);
		
		new LuaClosure(startup,  globals).call();
		new LuaClosure(api1init, globals).call();
		new LuaClosure(api2init, globals).call();
		
		return api;
	}
}

