
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

package com.github.euwoyne.enigma_edit.view;

import java.awt.Font;

import javax.swing.JEditorPane;

import jsyntaxpane.syntaxkits.EnigmaSyntaxKit;

public class CodeEditor extends JEditorPane
{
	private static final long serialVersionUID = 1L;
	
	public void setup()
	{
		this.setEditorKit(new EnigmaSyntaxKit());
		this.setFont(new Font("Ubuntu Mono", 0, 16));
		//((EnigmaSyntaxKit)this.getEditorKit()).getConfig().put("Style.KEYWORD", "0x000000, 3");
	}
}

