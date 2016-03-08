
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

package com.github.euwoyne.enigma_edit;

import com.github.euwoyne.enigma_edit.lua.data.CodeSnippet;

/**
 * This is the abstract base class for a message log.
 */
public abstract class Log
{
	/**
	 * Log message type.
	 */
	public static enum MsgType
	{
		/** general information */ INFO,
		/** warning message     */ WARNING,
		/** error message       */ ERROR
	};
	
	/**
	 * Message structure.
	 */
	public static class Msg
	{
		/** message type */
		public final MsgType     type;
		
		/** message content */
		public final String      message;
		
		/** source location (might be {@link CodeSnippet#NONE NONE}) */
		public final CodeSnippet location;
		
		/** Constructor (initializes the members with the given values; set {@code file} to {@code ""}) */
		public Msg(MsgType type, String msg, CodeSnippet loc) {this.type = type; this.message = msg; this.location = loc;}
		
		/** Constructor (with {@code location} set to {@link CodeSnippet#NONE NONE}) */
		public Msg(MsgType type, String msg) {this.type = type; this.message = msg; this.location = CodeSnippet.NONE;}
	}
	
	/**
	 * Logging procedure (to be implemented by child class).
	 * @param msg  The message to be logged.
	 */
	public abstract void log(Msg msg);
	
	/**
	 * Convenience method, creating a new {@link Msg} and calling {@link #log(Msg)}.
	 * 
	 * @param type  Message type.
	 * @param text  Message content.
	 */
	public void log(MsgType type, String text) {this.log(new Msg(type, text));}
	
	/**
	 * Convenience method, creating a new {@link Msg} and calling {@link #log(Msg)}.
	 * 
	 * @param type      Message type.
	 * @param text      Message content.
	 * @param location  Source location.
	 */
	public void log(MsgType type, String text, CodeSnippet location) {this.log(new Msg(type, text, location));}
	
	/**
	 * The {@code null} log (does nothing).
	 */
	private static class Null extends Log
	{
		@Override public void log(Msg msg) {}
		@Override public void log(MsgType type, String text) {}
		@Override public void log(MsgType type, String text, CodeSnippet location) {}
	}
	
	/**
	 * Static instance of the {@link Null} log (does nothing).
	 */
	public static Null nullLog = new Null();
}

