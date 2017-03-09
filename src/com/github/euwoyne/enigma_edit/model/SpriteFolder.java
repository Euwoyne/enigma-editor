package com.github.euwoyne.enigma_edit.model;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeMap;

import com.github.euwoyne.enigma_edit.error.WrongSpriteDirException;

public class SpriteFolder
{
	private TreeMap<Integer, Path> gfxPaths;
	
	private Path _getPath(String file, int size)
	{
		final Path gfxDir = gfxPaths.get(size);
		if (gfxDir == null) return null;
		return gfxDir.resolve(file);
	}
	
	public SpriteFolder(Path gfxPath) throws WrongSpriteDirException
	{
		this(gfxPath.toFile());
	}
	
	public SpriteFolder(File gfxPath) throws WrongSpriteDirException
	{
		this.gfxPaths = new TreeMap<Integer, Path>();
		
		if (!gfxPath.isDirectory())
			throw new WrongSpriteDirException(gfxPath.getAbsolutePath());
		
		for (File subdir : gfxPath.listFiles(new FileFilter() {
			@Override public boolean accept(File pathname) {
				return pathname.isDirectory() && pathname.getName().startsWith("gfx");
			}}))
		{
			try
			{
				gfxPaths.put(Integer.parseInt(subdir.getName().substring(3)), subdir.toPath());
			}
			catch (NumberFormatException e) {}
		}
	}
	
	public Set<Integer> getSizes()
	{
		return gfxPaths.keySet();
	}
	
	public boolean exist(String file, int size)
	{
		return _getPath(file, size).toFile().isFile();
	}
	
	public Integer getBestSize(String file, int size)
	{
		Integer nsize = gfxPaths.ceilingKey(size);
		while (nsize != null && !exist(file, nsize))
		{
			nsize = gfxPaths.higherKey(nsize);
		}
		if (nsize == null)
		{
			nsize = gfxPaths.floorKey(size);
			while (nsize != null && !exist(file, nsize))
			{
				nsize = gfxPaths.lowerKey(nsize);
			}
		}
		return nsize;
	}
	
	public Path getPath(String file, int size)
	{
		final Path path = _getPath(file, size);
		if (path == null || !path.toFile().isFile())
			return null;
		return path;
	}
	
	public Path getBestPath(String file, int size)
	{
		Integer nsize = gfxPaths.ceilingKey(size);
		Path    path  = null;
		while (nsize != null)
		{
			path = _getPath(file, nsize);
			if (path.toFile().isFile()) break;
			nsize = gfxPaths.higherKey(nsize);
		}
		if (nsize == null)
		{
			nsize = gfxPaths.floorKey(size);
			while (nsize != null)
			{
				path = _getPath(file, nsize);
				if (path.toFile().isFile()) break;
				nsize = gfxPaths.lowerKey(nsize);
			}
		}
		return nsize != null ? path : null;
	}
}

