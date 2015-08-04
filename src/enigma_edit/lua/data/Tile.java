package enigma_edit.lua.data;

import enigma_edit.lua.data.TilePart.Construct;

public class Tile
{
	public interface Part
	{
		String    getKey(Mode  mode);
		String    getKey(Mode2 mode);
		Construct get(Mode  mode);
		Construct get(Mode2 mode);
		
		boolean   isNull();
		boolean   isNormal();
		boolean   hasNormal();
		boolean   hasEasy();
		boolean   hasDifficult();
		boolean   has(Mode mode);
		boolean   has(Mode2 mode);
	}
	
	protected class Part_private implements Part
	{
		public String    easyKey;
		public Construct easy;
		public String    difficultKey;
		public Construct difficult;
		
		public Part_private() {easy = null; difficult = null;}
		
		public void set(String key, Construct part, Mode mode)
		{
			if (mode != Mode.DIFFICULT) {easy      = part; if (key != null) easyKey      = key;}
			if (mode != Mode.EASY)      {difficult = part; if (key != null) difficultKey = key;}
		}
		
		public String getKey(Mode mode)
		{
			switch (mode)
			{
			case EASY:      return easyKey;
			case DIFFICULT: return difficultKey;
			case NORMAL:    if (easyKey.equals(difficultKey)) return easyKey;
			default:        return null;
			}
		}
		
		public String getKey(Mode2 mode) {return mode == Mode2.EASY ? easyKey : difficultKey;}
		
		public Construct get(Mode mode)
		{
			switch (mode)
			{
			case EASY:      return easy;
			case DIFFICULT: return difficult;
			case NORMAL:    if (easy == difficult) return easy;
			default:        return null;
			}
		}
		
		public Construct get(Mode2 mode) {return mode == Mode2.EASY ? easy : difficult;}
		
		public boolean isNull()       {return easy == null && difficult == null;}
		public boolean isNormal()     {return easy == difficult;}
		public boolean hasNormal()    {return easy == difficult && easy != null;}
		public boolean hasEasy()      {return easy != null;}
		public boolean hasDifficult() {return difficult != null;}
		
		public boolean has(Mode mode)
		{
			switch (mode)
			{
			case EASY:      return easy != null;
			case DIFFICULT: return difficult != null;
			case NORMAL:    return easy != null && difficult != null;
			default:        return false;
			}
		}
		
		public boolean has(Mode2 mode) {return mode == Mode2.EASY ? easy != null : difficult != null;}
	}
	
	protected Part_private floor;
	protected Part_private item;
	protected Part_private actor;
	protected Part_private stone;
	
	public Tile()
	{
		floor = new Part_private();
		item  = new Part_private();
		actor = new Part_private();
		stone = new Part_private();
	}
	
	public boolean isNull() {return floor.isNull() && item.isNull() && actor.isNull() && stone.isNull();}
	
	public boolean has_fl() {return !floor.isNull();}
	public boolean has_it() {return !item.isNull();}
	public boolean has_ac() {return !actor.isNull();}
	public boolean has_st() {return !stone.isNull();}
	
	public boolean has_fl(Mode mode) {return floor.has(mode);}
	public boolean has_it(Mode mode) {return item.has(mode);}
	public boolean has_ac(Mode mode) {return actor.has(mode);}
	public boolean has_st(Mode mode) {return stone.has(mode);}
	
	public Part fl() {return floor;}
	public Part it() {return item;}
	public Part ac() {return actor;}
	public Part st() {return stone;}
	
	public void set_fl(Tile tile)
	{
		this.floor.easy         = tile.floor.easy;
		this.floor.easyKey      = tile.floor.easyKey;
		this.floor.difficult    = tile.floor.difficult;
		this.floor.difficultKey = tile.floor.difficultKey;
	}
	
	public void set_it(Tile tile)
	{
		this.item.easy         = tile.item.easy;
		this.item.easyKey      = tile.item.easyKey;
		this.item.difficult    = tile.item.difficult;
		this.item.difficultKey = tile.item.difficultKey;
	}
	
	public void set_ac(Tile tile)
	{
		this.actor.easy         = tile.actor.easy;
		this.actor.easyKey      = tile.actor.easyKey;
		this.actor.difficult    = tile.actor.difficult;
		this.actor.difficultKey = tile.actor.difficultKey;
	}
	
	public void set_st(Tile tile)
	{
		this.stone.easy         = tile.stone.easy;
		this.stone.easyKey      = tile.stone.easyKey;
		this.stone.difficult    = tile.stone.difficult;
		this.stone.difficultKey = tile.stone.difficultKey;
	}
	
	private void add(String key, Construct part, String kind, Mode mode)
	{
		if (kind.startsWith("#")) kind = kind.substring(1);
		switch (kind.substring(0, 2))
		{
		case "fl": floor.set(key, part, mode); break;
		case "it": item.set( key, part, mode); break;
		case "ac": actor.set(key, part, mode); break;
		case "st": stone.set(key, part, mode); break;
		}
	}
	
	private void add(String key, Construct part, Table table, Mode mode)
	{
		if (!table.exist(1)) return;
		final MMSimpleValue kind = table.get(1).checkSimple(mode);
		if (kind != null)
		{
			if (kind.hasNormal() || mode != Mode.NORMAL)
				add(key, part, kind.get(mode).toString_noquote(), mode);
			else
			{
				if (kind.hasEasy())
					add(key, part, kind.easy.toString_noquote(), Mode.EASY);
				if (kind.hasDifficult())
					add(key, part, kind.difficult.toString_noquote(), Mode.DIFFICULT);
			}
		}
	}
	
	public void add(String key, TileDecl decl, Mode mode)
	{
		MMTable     table;
		for (MMTileConstruct part : decl)
		{
			table = part.checkTable(mode);
			if (table.isNull(mode)) continue;
			if (table.hasNormal() && table.easy.isNormal())
			{
				this.add(key, part.easy, table.easy, mode);
			}
			else
			{
				if (mode != Mode.DIFFICULT && table.hasEasy())
					this.add(key, part.easy, table.easy, Mode.EASY);
				
				if (mode != Mode.EASY && table.hasDifficult())
					this.add(key, part.difficult, table.difficult, Mode.DIFFICULT);
			}
		}
	}
	
	public void add(String key, MMTileDecl decl, Mode mode)
	{
		if (decl.hasNormal())
		{
			this.add(key, decl.easy, mode);
		}
		else
		{
			if (mode != Mode.DIFFICULT && decl.hasEasy())
				this.add(key, decl.easy, Mode.EASY);
			
			if (mode != Mode.EASY && decl.hasDifficult())
				this.add(key, decl.difficult, Mode.DIFFICULT);
		}
	}
	
	private static void add(Part_private target, Part_private source, Mode mode)
	{
		if (mode != Mode.DIFFICULT && source.hasEasy())
		{
			target.easy    = source.easy;
			target.easyKey = source.easyKey;
		}
		if (mode != Mode.EASY && source.hasDifficult())
		{
			target.difficult    = source.difficult;
			target.difficultKey = source.difficultKey;
		}
	}
	
	public void add(Tile tile, Mode mode)
	{
		if (tile.has_fl(mode)) add(this.floor, tile.floor, mode);
		if (tile.has_it(mode)) add(this.item,  tile.item,  mode);
		if (tile.has_ac(mode)) add(this.actor, tile.actor, mode);
		if (tile.has_st(mode)) add(this.stone, tile.stone, mode);
	}
	
	public void substitute(Table table, Mode mode)
	{
		this.add(null, new Construct(table), table, mode);
	}
	
	public void substitute(MMTable table, Mode mode)
	{
		if (table.hasNormal())
		{
			this.substitute(table.easy, mode);
		}
		else
		{
			if (mode != Mode.DIFFICULT && table.hasEasy())
				this.substitute(table.easy, Mode.EASY);
			if (mode != Mode.EASY && table.hasDifficult())
				this.substitute(table.difficult, Mode.DIFFICULT);
		}
	}
	
	public static Tile composeMode(Tile easy, Tile difficult)
	{
		Tile tile = new Tile();
		tile.floor.easy         = easy.floor.easy;
		tile.floor.easyKey      = easy.floor.easyKey;
		tile.item.easy          = easy.item.easy;
		tile.item.easyKey       = easy.item.easyKey;
		tile.actor.easy         = easy.actor.easy;
		tile.actor.easyKey      = easy.actor.easyKey;
		tile.stone.easy         = easy.stone.easy;
		tile.stone.easyKey      = easy.stone.easyKey;
		tile.floor.difficult    = difficult.floor.difficult;
		tile.floor.difficultKey = difficult.floor.difficultKey;
		tile.item.difficult     = difficult.item.difficult;
		tile.item.difficultKey  = difficult.item.difficultKey;
		tile.actor.difficult    = difficult.actor.difficult;
		tile.actor.difficultKey = difficult.actor.difficultKey;
		tile.stone.difficult    = difficult.stone.difficult;
		tile.stone.difficultKey = difficult.stone.difficultKey;
		return tile;
	}
}

