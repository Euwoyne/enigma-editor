
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

package com.github.euwoyne.enigma_edit;

import java.nio.file.Paths;
import com.github.euwoyne.enigma_edit.control.Controller;

class EnigmaEditor
{
	public static void main(String[] argv) 
	{
		// setup options
		System.out.print("Setup options...");
		Options  options   = new Options();
		options.binaryPath = Paths.get("/usr/local/bin/enigma");
		options.enigmaPath = Paths.get("/usr/local/share/enigma");
		options.userPath   = Paths.get(System.getProperty("user.home")).resolve(".enigma");
		System.out.println("DONE");
		
		// start controller
		new Controller(options);
    }
}

