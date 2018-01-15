package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

public class ShrineRoom extends StaticRoom {
	
	public ShrineRoom() {
		// end up providing the type of shrine!
		super(-5, -1, -5, 5, 5, 5,
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
				// Layer 2
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
				// Ceil
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
				'X', DungeonBlock.instance(),
				' ', null);
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
	public boolean hasPuzzle() {
		return false;
	}

	@Override
	public boolean hasEnemies() {
		return false;
	}

	@Override
	public boolean hasTraps() {
		return false;
	}
	
}
