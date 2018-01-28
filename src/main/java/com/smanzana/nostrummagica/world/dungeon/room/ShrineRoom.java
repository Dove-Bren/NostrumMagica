package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.init.Blocks;

public class ShrineRoom extends StaticRoom implements ISpellComponentRoom {
	
	private SpellComponentWrapper component;
	
	public ShrineRoom() {
		// end up providing the type of shrine!
		super(-5, -1, 0, 5, 5, 10,
				// Floor
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				// Layer 1
				"XXXXX XXXXX",
				"X 	X      X",
				"X  XXXX   X",
				"X         X",
				"X         X",
				"X         X",
				"X     XXXXX",
				"X         X",
				"X         X",
				"X         X",
				"XXXXXXXXXXX",
				// Layer 2
				"XXXXX XXXXX",
				"X 	X      X",
				"X  XXXX   X",
				"X         X",
				"X         X",
				"X         X",
				"X     XXXXX",
				"X         X",
				"X         X",
				"X         X",
				"XXXXXXXXXXX",
				// Layer 3
				"XXXXXXXXXXX",
				"X 	X      X",
				"X  XXXX   X",
				"X         X",
				"X         X",
				"X         X",
				"X     XXXXX",
				"X         X",
				"X         X",
				"X         X",
				"XXXXXXXXXXX",
				// Layer 4
				"XXXXXXXXXXX",
				"X 	X      X",
				"X  XXXX   X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"XXXXXXXXXXX",
				// Layer 5
				"XXXXXXXXXXX",
				"X 	X      X",
				"X  XXXX   X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"XXXXXXXXXXX",
				// Ceil
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXDXXXXX",
				"XXXXXTXXXXX",
				"XXXXXTXXXXX",
				"XXXXXTXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				'X', Blocks.GOLD_BLOCK,//DungeonBlock.instance(),
				' ', null,
				'D', Blocks.REDSTONE_BLOCK,
				'T', Blocks.COAL_BLOCK);
	}

	@Override
	public int getNumExits() {
		return 0;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		return new LinkedList<>();
	}

	@Override
	public int getDifficulty() {
		return 0;
	}

	@Override
	public boolean hasEnemies() {
		return false;
	}

	@Override
	public boolean hasTraps() {
		return false;
	}

	@Override
	public boolean supportsDoor() {
		return false;
	}

	@Override
	public boolean supportsKey() {
		return false;
	}

	@Override
	public DungeonExitPoint getKeyLocation(DungeonExitPoint start) {
		return null;
	}

	@Override
	public List<DungeonExitPoint> getTreasureLocations(DungeonExitPoint start) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setComponent(SpellComponentWrapper component) {
		this.component = component;
	}
}
