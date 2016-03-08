
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

import com.github.euwoyne.enigma_edit.Resources;

public class MissingImageException extends MissingFileException
{
	private static final long serialVersionUID = 1L;
	
	MissingImageException(String msg, String filename, Object _ignored_)
	{
		super(msg, filename);
	}
	
	public MissingImageException(String filename)
	{
		super(String.format(Resources.errors.getString("MissingImage"), filename), filename);
	}
	
	public MissingImageException(String filename, String kind)
	{
		super(String.format(Resources.errors.getString("MissingImage_kind"), filename, kind), filename);
	}
}

