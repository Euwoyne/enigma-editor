package enigma_edit.error;

import enigma_edit.Resources;
import enigma_edit.lua.data.CodeSnippet;
import enigma_edit.lua.data.Mode;

public class LevelLuaException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public static class Runtime extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public CodeSnippet code;
		
		public Runtime(String id, CodeSnippet code)
		{
			super(Resources.luaLevelErrors.getString(id));
			this.code = code;
		}
		
		public Runtime(String id, String arg1, CodeSnippet code)
		{
			super(String.format(Resources.luaLevelErrors.getString(id), arg1));
			this.code = code;
		}
		
		public Runtime(String id, String arg1, String arg2, CodeSnippet code)
		{
			super(String.format(Resources.luaLevelErrors.getString(id), arg1, arg2));
			this.code = code;
		}
		
		public Runtime(String id, String arg1, String arg2, String arg3, CodeSnippet code)
		{
			super(String.format(Resources.luaLevelErrors.getString(id), arg1, arg2, arg3));
			this.code = code;
		}
		
		private static String modeId(String id, Mode mode)
		{
			switch (mode)
			{
			case DIFFICULT: return id + ".DIFFICULT";
			case EASY:      return id + ".EASY";
			case NORMAL:
			default:        return id;
			}
		}
		
		public Runtime(String id, Mode mode, CodeSnippet code)
		{
			super(Resources.luaLevelErrors.getString(modeId(id, mode)));
			this.code = code;
		}
		
		public Runtime(String id, Mode mode, String arg1, CodeSnippet code)
		{
			super(String.format(Resources.luaLevelErrors.getString(modeId(id, mode)), arg1));
			this.code = code;
		}
		
		public Runtime(String id, Mode mode, String arg1, String arg2, CodeSnippet code)
		{
			super(String.format(Resources.luaLevelErrors.getString(modeId(id, mode)), arg1, arg2));
			this.code = code;
		}
		
		public Runtime(String id, Mode mode, String arg1, String arg2, String arg3, CodeSnippet code)
		{
			super(String.format(Resources.luaLevelErrors.getString(modeId(id, mode)), arg1, arg2, arg3));
			this.code = code;
		}
	}
	
	public CodeSnippet code;
	
	public LevelLuaException(Runtime runtime)
	{
		super(runtime.getMessage(), runtime);
		code = runtime.code;
	}
}

