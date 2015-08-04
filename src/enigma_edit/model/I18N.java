
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

package enigma_edit.model;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.NoSuchElementException;

public class I18N
{
	public static class KeyString
	{
		private static class Translation
		{
			public String  string;
			public boolean protect;
			
			public Translation(String s, boolean p) {string = s; protect = p;}
		}
		
		public Boolean                 translate;
		public String                  english;
		public String                  comment;
		
		private TreeMap<String, Translation> translations;
		
		public KeyString()                                                     {this(true, "", "");}
		public KeyString(String _english)                                      {this(true, _english, "");}
		public KeyString(String _english, String _comment)                     {this(true, _english, _comment);}
		public KeyString(boolean _translate)                                   {this(_translate, "", "");}
		public KeyString(boolean _translate, String _english)                  {this(_translate, _english, "");}
		public KeyString(boolean _translate, String _english, String _comment)
		{
			translate = _translate;
			english   = _english;
			comment   = _comment;
			translations = new TreeMap<String, Translation>();
		}
		
		public String get(String lang)
		{
			Translation out = translations.get(lang);
			if (out == null) return english;
			return out.string;
		}
		
		public String get_throw(String lang) throws NoSuchElementException
		{
			if (lang.equals("en")) return english;
			Translation out = translations.get(lang);
			if (out == null) throw new NoSuchElementException("No translation for <" + lang + ">  within the i18n table.");
			return out.string;
		}
		
		public boolean is_protected(String lang)
		{
			if (lang.equals("en")) return true;
			Translation out = translations.get(lang);
			if (out == null) throw new NoSuchElementException("No translation for <" + lang + ">  within the i18n table.");
			return out.protect;
		}
		
		public void put(String lang, String str)                  {translations.put(lang, new Translation(str, false));}
		public void put(String lang, String str, boolean protect) {translations.put(lang, new Translation(str, protect));}
	}
	
	private TreeMap<String, KeyString> map;
	
	public I18N()
	{
		map = new TreeMap<String, KeyString>();
	}
	
	public boolean exists(String key)              {return map.containsKey(key);}
	public boolean exists(String key, String lang) {return map.containsKey(key) && map.get(key).translations.containsKey(lang);}
	
	public KeyString get(String key) throws NoSuchElementException
	{
		KeyString str = map.get(key);
		if (str == null) throw new NoSuchElementException("No key \"" + key + "\" within the i18n table.");
		return str;
	}
	
	public KeyString get_nothrow(String key)
	{
		return map.get(key);
	}
	
	public Boolean translate(String id) throws NoSuchElementException {return get(id).translate;}
	public String  english(String id)   throws NoSuchElementException {return get(id).english;}
	public String  comment(String id)   throws NoSuchElementException {return get(id).comment;}
	
	private KeyString.Translation _get(String key, String lang) throws NoSuchElementException
	{
		KeyString str = map.get(key);
		if (str == null) throw new NoSuchElementException("No key \"" + key + "\" within the i18n table.");
		KeyString.Translation out = str.translations.get(lang);
		if (out == null) throw new NoSuchElementException("No translation for <" + lang + "> of key \"" + key + "\" within the i18n table.");
		return out;
	}
	
	private KeyString.Translation _get(String key, String lang, boolean nothrow)
	{
		try	{return _get(key, lang);}
		catch (Exception e)
		{
			if (nothrow) return null;
			throw e;
		}
	}
	
	public String  get(         String key, String lang) throws NoSuchElementException {return _get(key, lang).string;}
	public boolean is_protected(String key, String lang) throws NoSuchElementException {return _get(key, lang).protect;}
	
	public String get(String key, String lang, boolean nothrow) throws NoSuchElementException
	{
		return _get(key, lang, nothrow).string;
	}
	
	public boolean is_protected(String key, String lang, boolean nothrow) throws NoSuchElementException
	{
		return _get(key, lang, nothrow).protect;
	}
	
	public String get_nothrow(String key, String lang)
	{
		return _get(key, lang, true).string;
	}
	
	public boolean is_protected_nothrow(String key, String lang) throws NoSuchElementException
	{
		return _get(key, lang, true).protect;
	}
	
	public void put(String key, KeyString string)                                  {map.put(key, string);}
	public void put(String key, String english)                                    {map.put(key, new KeyString(english));}
	public void put(String key, String english, String comment)                    {map.put(key, new KeyString(english, comment));}
	public void put(String key, boolean translate)                                 {map.put(key, new KeyString(translate));}
	public void put(String key, boolean translate, String english)                 {map.put(key, new KeyString(translate, english));}
	public void put(String key, boolean translate, String english, String comment) {map.put(key, new KeyString(translate, english, comment));}
	
	public KeyString create(String key)
	{
		KeyString string = new KeyString();
		map.put(key, string);
		return string;
	}
	
	public void clear()
	{
		map.clear();
	}
	
	public boolean has_public()
	{
		for (Entry<String, KeyString> entry : map.entrySet())
			for (Entry<String, KeyString.Translation> trans : entry.getValue().translations.entrySet())
				if (!trans.getValue().protect)
					return true;
		return false;
	}
	
	public void write_public(IndentWriter writer) throws IOException
	{
		// local existence markers
		boolean has_public;		// only write <el:i18n>, if there are any public translations
		boolean key_public;		// only add <el:string>, if there are any public translations
		
		has_public = false;		// initialize public marker (for level)
		
		// iterate translatable strings
		for (Entry<String, KeyString> entry : map.entrySet())
		{
			key_public = false;	// initialize public marker (for key)
			
			// iterate translations
			for (Entry<String, KeyString.Translation> trans : entry.getValue().translations.entrySet())
			{
				if (trans.getValue().protect) continue;	// ignore protected
				
				// on the first public translation at all
				if (!has_public)
				{
					// create <el:i18n> tag
					writer.write("<el:i18n>\n");
					writer.indent();
					has_public = true;
				}
				
				// on the first public translation of this key
				if (!key_public)
				{
					// create <el:string> entry
					writer.write(String.format("<el:string el:key=\"%s\">\n", entry.getKey()));
					writer.indent();
					key_public = true;
				}
				
				// write translation
				writer.write(String.format("<el:translation el:lang=\"%s\">", trans.getKey()));
				writer.write(trans.getValue().string);
				writer.write("</el:translation>\n");
			}
			
			// end <el:string> entry (if it was created at all)
			if (key_public)
			{
				writer.unindent();
				writer.write("</el:string>\n");
			}
		}
		
		// end <el:i18n> entry (if there is any)
		if (has_public)
		{
			writer.unindent();
			writer.write("</el:i18n>\n");
		}
	}
	
	public void write_protected(IndentWriter writer) throws IOException
	{
		// write <el:i18n> tag
		if (map.isEmpty())
		{
			writer.write("<el:i18n/>\n");
			return;
		};
		writer.write("<el:i18n>\n");
		writer.indent();
		
		// iterate translatable strings
		for (Entry<String, KeyString> entry : map.entrySet())
		{
			// enter each string into the <el:protected> section
			writer.write(String.format("<el:string el:key=\"%s\">\n", entry.getKey()));
			writer.indent();
			
			// English translation string
			writer.write("<el:english");
			
			if (!entry.getValue().translate)
				writer.write(" el:translate=\"false\"");
			
			if (!entry.getValue().comment.isEmpty())
				writer.write(String.format(" el:comment=\"%s\"", entry.getValue().comment));
			
			if (entry.getValue().english.isEmpty())
				writer.write("/>\n");
			else
				writer.write(String.format(">%s</el:english>\n", entry.getValue().english));
			
			// iterate translations
			for (Entry<String, KeyString.Translation> trans : entry.getValue().translations.entrySet())
			{
				if (trans.getValue().protect)
				{
					writer.write(String.format("<el:translation el:lang=\"%s\">", trans.getKey()));
					writer.write(trans.getValue().string);
					writer.write("</el:translation>\n");
				}
			}
			
			// close string
			writer.unindent();
			writer.write("</el:string>\n");
		}
		
		// close <el:i18n> tag
		writer.unindent();
		writer.write("</el:i18n>\n");
	}
}

