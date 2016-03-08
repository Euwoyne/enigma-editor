
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

package com.github.euwoyne.enigma_edit.lua.data;

/**
 * Multi-mode tile constructor.
 */
public class MMObjectDecl extends MM<ObjectDecl>
{
	public MMObjectDecl(ObjectDecl normal)                         {super(normal);}
	public MMObjectDecl(ObjectDecl easy, ObjectDecl difficult)     {super(easy, difficult);}
	public MMObjectDecl(MMObjectDecl easy, MMObjectDecl difficult) {super(easy.easy, difficult.difficult);}
	
	@Override public String typename() {return "<mixed object declaration>";}
}

