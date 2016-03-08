
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

public class LevelInfo
{
	public enum Type        {LEVEL, LIBRARY, MULTILEVEL};
	public enum Status      {RELEASED, STABLE, TEST, EXPERIMENTAL};
	public enum Engine      {DEFAULT, ENIGMA, OXYD1, PEROXYD, OXYDEXTRA, OXYDMAGNUM};
	public enum Control     {DEFAULT, FORCE, BALANCE, KEY, OTHER};
	public enum ScoreUnit   {DEFAULT, DURATION, NUMBER};
	public enum ScoreTarget {DEFAULT, TIME, PUSHES, MOVES, LUA};
	
	public static class Identity	// <el:identity/>
	{
		public String title;		//     el:title     required
		public String subtitle;		//     el:subtitle  optional
		public String id;			//     el:id        required   none of *? ()[]{}
	}								//                  convention YYYYMMDDuserNNN 
	
	public static class Version		// <el:version/>
	{
		public int    score;		//     el:score     required   1+	(levels of compatible scores)
		public int    release;		//     el:release   required   1+	(technical compatibility)
		public int    revision;		//     el:revision  required   0+
		public Status status;		//     el:status    required   "released" | "stable" | "test" | "experimental"
		
		public Version() {score = 1; release = 1; revision = 0; status = Status.EXPERIMENTAL;}
	}
	
	public static class Author		// <el:author/>
	{
		public String name;			//     el:name      optional
		public String email;		//     el:email     optional
		public String homepage;		//     el:homepage  optional
		
		public boolean isEmpty() {return name.isEmpty() && email.isEmpty() && homepage.isEmpty();}
	}
	
	public static class License		// <el:license>
	{
		public String       type;		// el:type      required
		public boolean      open;		// el:open      required   "true" | "false"
		public StringBuffer content;	// #CDATA       optional
	}								// </el:license>
	
	public static class Compatibility	// <el:compatibility>
	{
		public static class Editor		// <el:editor/>
		{
			public String name;			//     el:name      required
			public String version;		//     el:version   required
		}
		
		public static class Dependency	// <el:dependency/>
		{
			public String  path;		//     el:path      required   path relative to the 'levels' directory
			public String  id;			//     el:id        required   id as specified in the library metadata
			public int     release;		//     el:release   required   release number
			public boolean preload;		//     el:preload   required   recommended
			public String  url;			//     el:url       optional   backup address (I deem this insecure)
		}
		
		public static class External	// <el:externaldata/>
		{
			public String path;			//     el:path      required    path without ".txt" suffix
			public String url;			//     el:url       optional    download address (I deem this insecure)
		}
		
		public String enigma;						//     el:enigma   required   enigma version number
		public Engine engine;						//     el:engine   optional   "enigma" | "oxyd1" | "per.oxyd" | "oxyd.extra" | "oxyd.magnum"
		public Editor editor;						//     <el:editor> optional
		
		public HashSet<Dependency> dependencies;	//     <el:dependency/>
		public HashSet<External>   externaldata;	//     <el:externaldata/>
	}									// </el:compatibility>
	
	public static class Modes		// <el:modes/>
	{
		public boolean     easy;			// el:easy             required   if level has an easy version
		public boolean     single;			// el:single           required   has single player mode
		public boolean     network;			// el:network          required   has 2.player network mode
		public Control     control;			// el:control          optional   "force" | "balance" | "key" | "other"
		public ScoreUnit   scoreunit;		// el:scoreunit        optional   (default: "duration")
		public ScoreTarget scoretarget;		// el:scoretarget      optional   (default: "time")
		public String      scoretargetlua;	//                                value of el:scoretarget, if none of the predefined
	}
	
	public static class Comments	// <el:comments>
	{
		public static class CommentShow	// <el:credits> | <el:dedication>
		{
			public boolean showinfo;	//     el:showinfo    optional   (default: "false")
			public boolean showstart;	//     el:showstart   optional   (default: "false")
			public String  comment;		//     #CDATA         whitespace collapsed
			
			public String  toString() {return comment;}
			public boolean isEmpty()  {return comment.isEmpty();}
		}
		
		public CommentShow credits;		// <el:credits>    #CDATA </el:credits>
		public CommentShow dedication;	// <el:dedication> #CDATA </el:dedication>
		public String      code;		// <el:code>       #CDATA </el:code>
		
		public boolean isEmpty() {return credits.isEmpty() && dedication.isEmpty() && code.isEmpty();} 
	}								// </el:comments>
	
	public static class ScoreInfo	// <el:score/>
	{
		Score easy;					//     el:easy        required   "MM:SS" or "-"
		Score difficult;			//     el:difficult   required   "MM:SS" or "-"
	}
	
	public Type          type;			// el:type  required  "level" | "library" | "multilevel"
	public Identity      identity;		// <el:identity/>
	public Version       version;		// <el:version/>
	public Author        author;		// <el:author/>
	public String        copyright;		// <el:copyright> #CDATA  </el:copyright>
	public License       license;		// <el:license/>
	public Compatibility compat;		// <el:compatibility> ... </el:compatibility>
	public Modes         modes;			// <el:modes/>
	public Comments      comments;		// <el:comments> ... </el:comments>
	public ScoreInfo     score;			// <el:score/>
	public String        updateUrl;		// <el:update/>
	
	public static String getDate()
	{
		return new SimpleDateFormat("yyyyMMdd").format(new Date());
	}
	
	public void setDateId()
	{
		identity.id = getDate();
	}
	
	public void reset()
	{
		type = Type.LEVEL;
		identity.title = "";
		identity.subtitle = "";
		identity.id = "";
		version.score = 1;
		version.release = 1;
		version.revision = 0;
		version.status = Status.EXPERIMENTAL;
		author.name = "";
		author.email = "";
		author.homepage = "";
		copyright = "";
		license.type = "";
		license.open = true;
		license.content = new StringBuffer();
		compat.enigma = "1.20";
		compat.engine = Engine.ENIGMA;
		compat.editor.name = "";
		compat.editor.version = "";
		compat.externaldata.clear();
		compat.dependencies.clear();
		modes.single = true;
		modes.easy = false;
		modes.network = false;
		modes.control = Control.DEFAULT;
		modes.scoreunit = ScoreUnit.DEFAULT;
		modes.scoretarget = ScoreTarget.DEFAULT;
		modes.scoretargetlua = "";
		comments.credits.showinfo = false;
		comments.credits.showstart = false;
		comments.credits.comment = "";
		comments.dedication.showinfo = false;
		comments.dedication.showstart = false;
		comments.dedication.comment = "";
		comments.code = "";
		score.easy.reset();
		score.difficult.reset();
		updateUrl = "";
	}
	
	public LevelInfo()
	{
		identity = new Identity();
		version = new Version();
		author = new Author();
		license = new License();
		license.content = new StringBuffer();
		compat = new Compatibility();
		compat.editor = new Compatibility.Editor();
		compat.externaldata = new HashSet<Compatibility.External>();
		compat.dependencies = new HashSet<Compatibility.Dependency>();
		modes = new Modes();
		comments = new Comments();
		comments.credits = new Comments.CommentShow();
		comments.dedication = new Comments.CommentShow();
		score = new ScoreInfo();
		score.easy = new Score();
		score.difficult = new Score();
		this.reset();
	}
	
	void write(IndentWriter writer, int quantity) throws IOException
	{
		// write info element
		writer.write("<el:info el:type=\"");
		switch (type) {
		case LEVEL:      writer.write("\"level\"");      break;
		case LIBRARY:    writer.write("\"library\"");    break;
		case MULTILEVEL: writer.write("\"multilevel\"");
						 writer.write(" el:quantity=\"" + quantity + "\"");
		                 break;
		}
		writer.write(">\n");
		writer.indent();
		
		// identity element
		writer.write(String.format("<el:identity el:title=\"%s\"", identity.title));
		if (!identity.subtitle.isEmpty())
			writer.write(String.format(" el:subtitle=\"%s\"", identity.subtitle));
		writer.write(String.format(" el:id=\"%s\"", identity.id));
		writer.write("/>\n");
		
		// version element
		writer.write(String.format("<el:version el:score=\"%i\" el:release\"%i\" el:revision=\"%i\"",
		                                   version.score,  version.release, version.revision));
		writer.write(" el:status=");
		switch (version.status)
		{
		case RELEASED:     writer.write("\"released\""); break;
		case STABLE:       writer.write("\"stable\""); break;
		case TEST:         writer.write("\"test\""); break;
		case EXPERIMENTAL: writer.write("\"experimental\""); break;
		}
		writer.write("/>\n");
		
		// author element
		if (!author.isEmpty())
		{
			writer.write("<el:author ");
			if (!author.name.isEmpty())
				writer.write(String.format(" el:name=\"%s\"", author.name));
			if (!author.email.isEmpty())
				writer.write(String.format(" el:email=\"%s\"", author.email));
			if (!author.homepage.isEmpty())
				writer.write(String.format(" el:homepage=\"%s\"", author.homepage));
		}
		
		// copyright element
		writer.write("<el:copyright>");
		writer.write(copyright);
		writer.write("</el:copyright>\n");
		
		// license element
		writer.write(String.format("<el:license el:type=\"%s\" el:open=\"%s\"",
				                   license.type, license.open ? "true" : "false")); 
		
		if (license.type == "special")
		{
			writer.write(">\n");
			writer.write(license.content);
			writer.write('\n');
			writer.write("</el:license>\n");
		}
		else
			writer.write("/>\n");
		
		// compatibility element
		writer.write(String.format("<el:compatibility el:enigma=\"%s\"", compat.enigma));
		if (compat.engine != Engine.DEFAULT)
		{
			writer.write(" el:engine=");
			switch (compat.engine)
			{
			case DEFAULT:
			case ENIGMA:     writer.write("\"enigma\"");      break;
			case OXYD1:      writer.write("\"oxyd1\"");       break;
			case PEROXYD:    writer.write("\"per.oxyd\"");    break;
			case OXYDEXTRA:  writer.write("\"oxyd.extra\"");  break;
			case OXYDMAGNUM: writer.write("\"oxyd.magnum\""); break;
			}
		}
		writer.write(">\n");
		
		writer.indent();
		for (Compatibility.Dependency dep : compat.dependencies)
		{
			if (dep.url.isEmpty())
				writer.write(String.format("<el:dependency el:path=\"%s\" el:id=\"%s\" el:release=\"%i\" el:preload=\"%s\"/>\n",
						                   dep.path, dep.id, dep.release, dep.preload ? "true" : "false"));
			else
				writer.write(String.format("<el:dependency el:path=\"%s\" el:id=\"%s\" el:release=\"%i\" el:preload=\"%s\" el:url=\"%s\"/>\n",
	                                       dep.path, dep.id, dep.release, dep.preload ? "true" : "false", dep.url));
		}
		
		for (Compatibility.External ext : compat.externaldata)
		{
			if (ext.url.isEmpty())
				writer.write(String.format("<el:externaldata el:path=\"%s\"/>\n", ext.path));
			else
				writer.write(String.format("<el:dependency el:path=\"%s\" el:url=\"%s\"/>\n", ext.path, ext.url));
		}
		
		if (!compat.editor.name.isEmpty())
		{
			writer.write(String.format("<el:editor el:name=\"%s\" el:version=\"%s\"/>\n", compat.editor.name, compat.editor.version));
		}
		writer.unindent();
		writer.write("</el:compatibility>\n");
		
		// modes element
		writer.write(String.format("<el:modes el:easy=\"%s\" el:single=\"%s\" el:network=\"%s\"",
				                   modes.easy ? "true" : "false", modes.single ? "true" : "false", modes.network ? "true" : "false"));
		
		if (modes.control != Control.DEFAULT)
		{
			writer.write(" el:control=");
			switch (modes.control)
			{
			case DEFAULT:
			case FORCE:   writer.write("\"force\"");   break;
			case BALANCE: writer.write("\"balance\""); break;
			case KEY:     writer.write("\"key\"");     break;
			case OTHER:   writer.write("\"other\"");   break;
			}
		}
		
		if (modes.scoreunit != ScoreUnit.DEFAULT)
		{
			writer.write(" el:scoreunit=");
			switch (modes.scoreunit)
			{
			case DEFAULT:
			case DURATION: writer.write("\"duration\""); break;
			case NUMBER:   writer.write("\"number\"");   break;
			}
		}
		
		if (modes.scoretarget != ScoreTarget.DEFAULT)
		{
			writer.write(" el:scoretarget=");
			switch (modes.scoretarget)
			{
			case DEFAULT:
			case TIME:   writer.write("\"time\"");   break;
			case PUSHES: writer.write("\"pushes\""); break;
			case MOVES:  writer.write("\"moves\"");  break;
			case LUA:    writer.write("\"" + modes.scoretargetlua + "\""); break;
			}
		}
		
		writer.write("/>\n");
		
		// comments element
		if (!comments.isEmpty())
		{
			writer.write("<el:comments>\n");
			writer.indent();
			if (!comments.credits.isEmpty())
			{
				writer.write("<el:credits");
				if (comments.credits.showinfo)	writer.write(" el:showinfo=\"true\"");
				if (comments.credits.showstart)	writer.write(" el:showstart=\"true\"");
				writer.write(">");
				writer.write(comments.credits.comment);
				writer.write("</el:credits>\n");
			}
			if (!comments.dedication.isEmpty())
			{
				writer.write("<el:dedication");
				if (comments.dedication.showinfo)	writer.write(" el:showinfo=\"true\"");
				if (comments.dedication.showstart)	writer.write(" el:showstart=\"true\"");
				writer.write(">");
				writer.write(comments.dedication.comment);
				writer.write("</el:dedication>\n");
			}
			if (!comments.code.isEmpty())
			{
				writer.write("<el:code>");
				writer.write(comments.code);
				writer.write("</el:code>\n");
			}
			writer.unindent();
			writer.write("</el:comments>\n");
		}
		
		// update
		if (!updateUrl.isEmpty())
			writer.write(String.format("<el:update el:url=\"%s\"/>\n", updateUrl));
		
		// score
		writer.write(String.format("<el:score el:easy=\"%s\" el:difficult=\"%s\"/>\n", score.easy, score.difficult));
		
		// close info element
		writer.unindent();
		writer.write("</el:info>\n");
	}
}

