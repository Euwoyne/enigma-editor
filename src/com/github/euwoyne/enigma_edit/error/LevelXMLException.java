
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

package com.github.euwoyne.enigma_edit.error;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

import com.github.euwoyne.enigma_edit.Resources;

public class LevelXMLException extends SAXParseException
{
	private static final long serialVersionUID = 1L;
	
	public LevelXMLException(String id, Locator locator)
	{
		super(Resources.xmlLevelErrors.getString(id), locator);
	}
	
	public LevelXMLException(String id, String arg1, Locator locator)
	{
		super(String.format(Resources.xmlLevelErrors.getString(id), arg1), locator);
	}
	
	public LevelXMLException(String id, String arg1, String arg2, Locator locator)
	{
		super(String.format(Resources.xmlLevelErrors.getString(id), arg1, arg2), locator);
	}
	
	public LevelXMLException(String id, String arg1, String arg2, String arg3, Locator locator)
	{
		super(String.format(Resources.xmlLevelErrors.getString(id), arg1, arg2, arg3), locator);
	}
}

