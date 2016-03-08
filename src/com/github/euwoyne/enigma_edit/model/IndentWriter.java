
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

import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class IndentWriter extends OutputStream
{
	private OutputStream os;
	private String       charset;
	private byte[]       space;
	private int          indent;
	private boolean      newline;
	
	private void write_indent() throws IOException
	{
		for (int i = 0; i < indent; ++i)
			os.write(space);
	}
	
	public IndentWriter(OutputStream _os, String _charset) throws UnsupportedEncodingException
	{
		os = _os;
		charset = _charset;
		space = "    ".getBytes(charset);
		indent = 0;
		newline = true;
	}
	
	public IndentWriter(OutputStream _os) throws UnsupportedEncodingException
	{
		this(_os, "UTF-8");
	}
	
	public String get_charset() {return charset;}
	public byte[] get_space()   {return space;}
	public int    get_indent()  {return indent;}
	
	public void set_charset(String _charset) throws UnsupportedEncodingException
	{
		space = new String(space, charset).getBytes(_charset);
		charset = _charset;
	}
	
	public void set_space(String s) throws UnsupportedEncodingException
	{
		space = s.getBytes(charset);
	}
	
	public void set_indent(int ind)
	{
		indent = ind;
	}
	
	public void indent()   {++indent;}
	public void unindent() {if (indent > 0) --indent;}
	
	@Override
	public void write(int arg0) throws IOException
	{
		if (newline)
			write_indent();
		os.write(arg0);
		newline = (arg0 == '\n');
	}
	
	public void write(String arg0)                throws IOException {this.write(arg0.getBytes(charset));}
	public void write(String arg0, boolean force) throws IOException {newline |= force; this.write(arg0.getBytes(charset));}
	
	public void write(StringBuffer arg0)                throws IOException {this.write(arg0.toString().getBytes(charset));}
	public void write(StringBuffer arg0, boolean force) throws IOException {newline |= force; this.write(arg0.toString().getBytes(charset));}
	
	public void write_noindent(String arg0) throws IOException
	{
		os.write(arg0.getBytes(charset));
	}
	
	public void write_noindent(StringBuffer arg0) throws IOException
	{
		os.write(arg0.toString().getBytes(charset));
	}		
}

