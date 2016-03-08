package com.github.euwoyne.enigma_edit.error;

import com.github.euwoyne.enigma_edit.Resources;

public class GeneralError extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public GeneralError(String id)
	{
		super(Resources.errors.getString(id));
	}
	
	public GeneralError(String id, String arg1)
	{
		super(String.format(Resources.errors.getString(id), arg1));
	}
	
	public GeneralError(String id, String arg1, String arg2)
	{
		super(String.format(Resources.errors.getString(id), arg1, arg2));
	}
	
	public GeneralError(String id, String arg1, String arg2, String arg3)
	{
		super(String.format(Resources.errors.getString(id), arg1, arg2, arg3));
	}
}

