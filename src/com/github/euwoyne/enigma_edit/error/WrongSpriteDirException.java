package com.github.euwoyne.enigma_edit.error;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JOptionPane;

import com.github.euwoyne.enigma_edit.Resources;

public class WrongSpriteDirException extends IOException
{
	private static final long serialVersionUID = 1L;
	
	String filename;
	
	WrongSpriteDirException(String msg, String filename)
	{
		super(msg);
		this.filename = filename;
	}
	
	public WrongSpriteDirException(String filename)
	{
		super(String.format(Resources.errors.getString("WrongSpriteDir"), filename));
		this.filename = filename;
	}
	
	public String getFilename() {return filename;}
	
	public void showDialog(Component parent, boolean exit)
	{
		JOptionPane.showMessageDialog(parent, this.getMessage(), Resources.errors.getString("WrongSpriteDirTitle"), JOptionPane.ERROR_MESSAGE);
		if (exit) System.exit(-1);
	}
}

