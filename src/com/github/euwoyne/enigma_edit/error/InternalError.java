package com.github.euwoyne.enigma_edit.error;

import com.github.euwoyne.enigma_edit.Resources;

public class InternalError extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	
	public InternalError(String id)
	{
		super(Resources.errors.getString(id));
	}
	
	public InternalError(String id, String arg1)
	{
		super(String.format(Resources.errors.getString(id), arg1));
	}
	
	public InternalError(String id, String arg1, String arg2)
	{
		super(String.format(Resources.errors.getString(id), arg1, arg2));
	}
	
	public InternalError(String id, String arg1, String arg2, String arg3)
	{
		super(String.format(Resources.errors.getString(id), arg1, arg2, arg3));
	}
}

