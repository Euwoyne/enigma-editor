
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

package enigma_edit;

import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import jsyntaxpane.syntaxkits.EnigmaSyntaxKit;
import enigma_edit.model.Tileset;
import enigma_edit.model.TilesetReader;
import enigma_edit.view.swing.MainWnd;
import enigma_edit.view.swing.SpriteSet;

class EnigmaEditor
{
	public static void main(String[] argv) throws FileNotFoundException, IOException 
    {
		try
		{
			// setup options
			Options  options   = new Options();
			options.binaryPath = Paths.get("/usr/local/bin/enigma");
			options.enigmaPath = Paths.get("/usr/local/share/enigma");
			options.userPath   = Paths.get(System.getProperty("user.home")).resolve(".enigma");
			
			// setup sprites
			SpriteSet spriteset = new SpriteSet(options.enigmaPath, new Font("normal", Font.PLAIN, 10));
			
			// setup tileset
			Tileset       tileset = new Tileset();
			TilesetReader reader  = new TilesetReader();
			reader.setTarget(tileset);
			reader.parse("data/tileset.xml");
			tileset.loadImages(spriteset);
			
			// setup user interface
			EnigmaSyntaxKit.initKit();
			new MainWnd(tileset, options);
		}
		catch (IOException | ParserConfigurationException | SAXException e)
		{
			e.printStackTrace();
		}
    }
}

