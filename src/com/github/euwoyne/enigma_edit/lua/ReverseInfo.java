package com.github.euwoyne.enigma_edit.lua;

import com.github.euwoyne.enigma_edit.lua.data.Mode;

public interface ReverseInfo extends ReverseIDProvider
{
	/*
	 *  Request Parameters
	 * ====================
	 */
	
	/**
	 * Reverse resolver status.
	 * Indicates success of the reverse resolve algorithm.
	 */
	public static enum Status
	{
		/** The requested tile is still unresolved. */
		UNRESOLVED,
		
		/** Some parts have been resolved. */
		PARTIAL,
		
		/** All parts are resolved. The given key represents the requested tile. */
		COMPLETE
	};
	
	/**
	 * Get resolve status.
	 * @see #Status
	 * 
	 * @return  Current status.
	 */
	public Status getStatus();
	
	/**
	 * Get requested mode.
	 * @return  The mode, that the tile was requested for.
	 */
	public Mode  getMode();
	
	
	/*
	 *  Resolve Interface
	 * ===================
	 * (to be used by the resolver) 
	 */
	
	public void   setKey(String key);
	public void   setKey(String key, int typeMask);
	
	public int    typeMask();
	public RevId  reverseId();
	public RevId  reverseId(int typeMask);
}

