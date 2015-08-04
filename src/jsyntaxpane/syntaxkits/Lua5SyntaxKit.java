
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

package jsyntaxpane.syntaxkits;

import jsyntaxpane.Lexer;
import jsyntaxpane.lexers.Lua5Lexer;

public class Lua5SyntaxKit extends jsyntaxpane.DefaultSyntaxKit
{
	private static final long serialVersionUID = 7581557344657108088L;

	public Lua5SyntaxKit()            {super(new Lua5Lexer());}
	public Lua5SyntaxKit(Lexer lexer) {super(lexer);}
	
	@Override
	public String getContentType() {return "text/x-lua";}
}

