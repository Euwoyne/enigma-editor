
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

import java.util.ResourceBundle;

public class Resources
{
	public static ResourceBundle xmlTilesetErrors = ResourceBundle.getBundle("enigma_edit.error.TilesetErrorsBundle");
	public static ResourceBundle xmlLevelErrors   = ResourceBundle.getBundle("enigma_edit.error.LevelXMLErrorsBundle");
	public static ResourceBundle luaLevelErrors   = ResourceBundle.getBundle("enigma_edit.error.LevelLuaErrorsBundle");
	public static ResourceBundle errors           = ResourceBundle.getBundle("enigma_edit.error.ErrorsBundle");
	public static ResourceBundle uiText           = ResourceBundle.getBundle("enigma_edit.view.UITextBundle");
	
	public static void reload()
	{
		xmlTilesetErrors = ResourceBundle.getBundle("enigma_edit.error.TilesetErrorsBundle");
		xmlLevelErrors   = ResourceBundle.getBundle("enigma_edit.error.LevelXMLErrorsBundle");
		luaLevelErrors   = ResourceBundle.getBundle("enigma_edit.error.LevelLuaErrorsBundle");
		errors           = ResourceBundle.getBundle("enigma_edit.error.ErrorsBundle");
		uiText           = ResourceBundle.getBundle("enigma_edit.view.UITextBundle");
	}
}

