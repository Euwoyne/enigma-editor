package com.github.euwoyne.enigma_edit.error;

import com.github.euwoyne.enigma_edit.Resources;

public class IllegalKeyLength extends GeneralError
{
	private static final long serialVersionUID = 1L;
	
	public IllegalKeyLength(int gotLength, int expectedLength)
	{
		super(String.format(Resources.errors.getString("IllegalKeyLength"), Integer.toString(gotLength), Integer.toString(expectedLength)));
	}

}

