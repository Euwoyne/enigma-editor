
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

package com.github.euwoyne.enigma_edit.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;

import org.luaj.vm2.parser.ParseException;

import com.github.euwoyne.enigma_edit.error.LevelLuaException;

public class Level
{
	public static final String LUAMAIN_EMPTY = "\nti[\" \"] = {\"fl_metal\"}\n\nwo(ti, \" \", {\n\t\"                    \",\n\t\"                    \",\n\t\"                    \",\n\t\"                    \",\n\t\"                    \",\n\t\"                    \",\n\t\"                    \",\n\t\"                    \",\n\t\"                    \",\n\t\"                    \",\n\t\"                    \",\n\t\"                    \",\n\t\"                    \"})\n";
	
	public static class Upgrade
	{
		public String url;
		public int    release;
	}
	
	public Path              path;
	public LevelInfo         info;
	public Elements          elements;
	public String            luamain;
	public I18N              i18n;
	public Upgrade           upgrade;
	public LinkedList<World> worlds;
	
	public Level()
	{
		path     = null;
		info     = new LevelInfo();
		elements = new Elements();
		luamain  = "";
		i18n     = new I18N();
		upgrade  = new Upgrade();
		upgrade.url = "";
		upgrade.release = 0;
		worlds   = new LinkedList<World>(); 
	}
	
	void reset()
	{
		path = null;
		info.reset();
		i18n.clear();
		upgrade.url = "";
		upgrade.release = 0;
		worlds.clear();
	}
	
	public static Level getEmpty(String author)
	{
		Level level = new Level();
		level.luamain = LUAMAIN_EMPTY;
		level.info.identity.id = LevelInfo.getDate() + (author.length() > 3 ? author.substring(0, 3).toLowerCase() : author.toLowerCase()) + "0001";
		level.info.author.name = author;
		return level;
	}
	
	public void analyse(Tileset tileset) throws ParseException, LevelLuaException
	{
		worlds.clear();
		worlds.add(new World(luamain));
		worlds.getLast().analyse(tileset);
	}
	
	public void write(IndentWriter writer) throws IOException
	{
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
		writer.write("<el:level");
		writer.write(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		writer.write(" xsi:schemaLocation=\"http://enigma-game.org/schema/level/1 level.xsd\"");
		writer.write(" xmlns:el=\"http://enigma-game.org/schema/level/1\"");
		writer.write('\n');
		writer.indent();
		writer.write("<el:protected>\n");
		writer.indent();
		info.write(writer, worlds.size());
		elements.write(writer);
		writer.write("<el:luamain><![CDATA[\n");
		writer.write_noindent(luamain);
		writer.write("]]>\n");
		i18n.write_protected(writer);
		writer.unindent();
		writer.write("</el:protected>\n");
		if (!upgrade.url.isEmpty() || i18n.has_public())
		{
			writer.write("<el:public>\n");
			writer.indent();
			i18n.write_public(writer);
			if (!upgrade.url.isEmpty())
				writer.write(String.format("<el:upgrade el:url=\"%s\" el:release=\"%i\"/>\n", upgrade.url, upgrade.release));
			writer.unindent();
			writer.write("</el:public>\n");
		}
		writer.unindent();
		writer.write("</el:level>\n");
	}
}

