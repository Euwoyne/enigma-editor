
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

package enigma_edit.lua.data;

public interface Typed
{
	/**
	 * If the state of this object is not immutable, this method will
	 * create and return copy of this object.
	 * @return  Either {@code this}, if this object is immutable or otherwise a deep copy of {@code this}.
	 */
	abstract Typed snapshot();
	
	/*
	 *  TYPE ACCESS
	 * =============
	 */
	/**
	 * Returns the type of this object in human readable form.
	 * This is primarily used in error messages.
	 */
	abstract String typename();
	
	/**
	 * Returns the type of the value represented by this object in human readable form.
	 * If this instance is a value already, the result is the same as for {@link #typename()}.
	 * Otherwise this is either a multi-mode value or a reference, in which case the type
	 * of the underlying instance will be returned.
	 */
	abstract String typename(Mode2 mode);
	
	/**
	 * Returns the type of the value represented by this object in human readable form.
	 * If this instance is a value already, the result is the same as for {@link #typename()}.
	 * Otherwise this is either a multi-mode value or a reference, in which case the type
	 * of the underlying instance will be returned.
	 */
	abstract String typename(Mode  mode);
	
	/*
	 *  TYPE CHECKING
	 * ===============
	 */
	/** Check if this represents a {@link Nil} value in any mode. If it does, it is returned; otherwise {@code null} is. */
	abstract MMNil checkNil();
	
	/** Check if this represents a {@link SimpleValue} in any mode. If it does, it is returned; otherwise {@code null} is. */
	abstract MMSimpleValue checkSimple();
	
	/** Check if this represents a {@link Table} in any mode. If it does, it is returned; otherwise {@code null} is. */
	abstract MMTable checkTable();
	
	/** Check if this represents a {@link TilePart} in any mode. If it does, it is returned; otherwise {@code null} is. */
	abstract MMTilePart checkTilePart();
	
	/** Check if this represents a {@link TileDecl} in any mode. If it does, it is returned; otherwise {@code null} is. */
	abstract MMTileDecl checkTile();
	
	/** Check if this represents a {@link Resolver} in any mode. If it does, it is returned; otherwise {@code null} is. */
	abstract MMResolver checkResolver();
	
	/** Check if this represents a {@link Nil} value in the given mode. If it does, it is returned; otherwise {@code null} is. */
	abstract MMNil checkNil(Mode mode);
	
	/** Check if this represents a {@link SimpleValue} in the given mode. If it does, it is returned; otherwise {@code null} is. */
	abstract MMSimpleValue checkSimple(Mode mode);
	
	/** Check if this represents a {@link Table} in the given mode. If it does, it is returned; otherwise {@code null} is. */
	abstract MMTable checkTable(Mode mode);
	
	/** Check if this represents a {@link TilePart} in the given mode. If it does, it is returned; otherwise {@code null} is. */
	abstract MMTilePart checkTilePart(Mode mode);
	
	/** Check if this represents a {@link TileDecl} in the given mode. If it does, it is returned; otherwise {@code null} is. */
	abstract MMTileDecl checkTile(Mode mode);
	
	/** Check if this represents a {@link Resolver} in the given mode. If it does, it is returned; otherwise {@code null} is. */
	abstract MMResolver checkResolver(Mode mode);
	
	/** Get the underlying {@link Value} instance. */
	abstract Value checkValue(Mode2 mode);
	
	/** Check if this represents a {@link Nil} value in the given mode. If it does, it is returned; otherwise {@code null} is. */
	abstract Nil checkNil(Mode2 mode);
	
	/** Check if this represents a {@link SimpleValue} in the given mode. If it does, it is returned; otherwise {@code null} is. */
	abstract SimpleValue checkSimple(Mode2 mode);
	
	/** Check if this represents a {@link Table} in the given mode. If it does, it is returned; otherwise {@code null} is. */
	abstract Table checkTable(Mode2 mode);
	
	/** Check if this represents a {@link TilePart} in the given mode. If it does, it is returned; otherwise {@code null} is. */
	abstract TilePart checkTilePart(Mode2 mode);
	
	/** Check if this represents a {@link TileDecl} in the given mode. If it does, it is returned; otherwise {@code null} is. */
	abstract TileDecl checkTile(Mode2 mode);
	
	/** Check if this represents a {@link Resolver} in the given mode. If it does, it is returned; otherwise {@code null} is. */
	abstract Resolver checkResolver(Mode2 mode);
	
	/*
	 *  MODE CHECKING
	 * ===============
	 */
	/** Check if this has the same value in both modes (might be {@code nil}). */
	abstract boolean isNormal();
	
	/** Check if this is defined in both difficult and easy mode but with differing values. */
	abstract boolean isMixed();
	
	/** Check if this is defined in both difficult and easy mode. */
	abstract boolean isComplete();
	
	/** Check if this is undefined in any circumstance. */
	abstract boolean isNull();
	
	/** Check if this is only defined in easy mode. */
	abstract boolean onlyEasy();
	
	/** Check if this is only defined in difficult mode. */
	abstract boolean onlyDifficult();
	
	/** Check if this is defined in easy mode. */
	abstract boolean hasEasy();
	
	/** Check if this is defined in difficult mode. */
	abstract boolean hasDifficult();
	
	/** Check if this is defined in normal mode. */
	abstract boolean hasNormal();
	
	/** Check if this is defined in the given mode. */
	abstract boolean isDefined(Mode mode);
}

