
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

package enigma_edit.lua.data;

/**
 * Multi-mode simple value.
 * @see SimpleValue
 */
public class MMSimpleValue extends MM<SimpleValue>
{
	public MMSimpleValue(SimpleValue normal)                          {super(normal);}
	public MMSimpleValue(SimpleValue easy, SimpleValue difficult)     {super(easy, difficult);}
	public MMSimpleValue(MMSimpleValue easy, MMSimpleValue difficult) {super(easy.easy, difficult.difficult);}
	
	@Override public String typename() {return "<mixed simple>";}
}

