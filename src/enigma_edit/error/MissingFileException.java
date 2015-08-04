
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

package enigma_edit.error;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JOptionPane;

import enigma_edit.Resources;

public class MissingFileException extends IOException
{
	private static final long serialVersionUID = 1L;
	
	String filename;
	
	MissingFileException(String msg, String filename)
	{
		super(msg);
		this.filename = filename;
	}
	
	public MissingFileException(String filename)
	{
		super(String.format(Resources.errors.getString("MissingFile"), filename));
		this.filename = filename;
	}
	
	public String getFilename() {return filename;}
	
	public void showDialog(Component parent, boolean exit)
	{
		JOptionPane.showMessageDialog(parent, this.getMessage(), Resources.errors.getString("MissingFileTitle"), JOptionPane.ERROR_MESSAGE);
		if (exit) System.exit(-1);
	}
}

