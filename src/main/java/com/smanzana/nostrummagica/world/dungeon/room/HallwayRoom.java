package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class HallwayRoom extends StaticRoom {
	
	public HallwayRoom() {
		// end up providing the type of shrine!
		super(-2, -1, 0, 2, 3, 10,
				// Floor
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				// Layer 1
				"XX XX",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"XX XX",
				// Layer 2
				"XX XX",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"XX XX",
				// Layer 3
				"XXXXX",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"XXXXX",
				// Layer 4
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				'X', DungeonBlock.instance(),
				' ', null);
	}

	@Override
	public int getNumExits() {
		return 1;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
		BlockPos exit = new BlockPos(0, 0, 10);
		
		list.add(NostrumDungeon.asRotated(start, exit, EnumFacing.NORTH));
		
		return list;
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
