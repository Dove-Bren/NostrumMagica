package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class StartRoom extends StaticRoom {
	
	public StartRoom() {
		// End up passing in height to surface?
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
				"XXXXX XXXXX",
				"XBB     BBX",
				"XB       BX",
				"X         X",
				"X         X",
				"           ",
				"X         X",
				"X         X",
				"XB       BX",
				"XBB     BBX",
				"XXXXX XXXXX",
				// Layer 2
				"XXXXX XXXXX",
				"XBB      BX",
				"X         X",
				"X         X",
				"X         X",
				"           ",
				"X         X",
				"X         X",
				"XB       BX",
				"XB      BBX",
				"XXXXX XXXXX",
				// Layer 3
				"XXXXXXXXXXX",
				"XB	       X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"XB        X",
				"X       BBX",
				"XXXXXXXXXXX",
				// Layer 4
				"XXXXXXXXXXX",
				"X 	       X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X        BX",
				"XXXXXXXXXXX",
				// Layer 5
				"XXXXXXXXXXX",
				"X 	       X",
				"X         X",
				"X  G   G  X",
				"X         X",
				"X         X",
				"X         X",
				"X  G   G  X",
				"X         X",
				"X         X",
				"XXXXXXXXXXX",
				// Ceil
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXX   XXXX",
				"XXXX   XXXX",
				"XXXX   XXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				'X', DungeonBlock.instance(),
				' ', null,
				'G', Blocks.GLOWSTONE,
				'B', Blocks.BOOKSHELF);
	}

	@Override
	public int getNumExits() {
		return 4;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
		BlockPos pos;
		
		pos = new BlockPos(0, 0, -5);
		list.add(NostrumDungeon.asRotated(start, pos, EnumFacing.SOUTH));
		
		pos = new BlockPos(0, 0, 5);
		list.add(NostrumDungeon.asRotated(start, pos, EnumFacing.NORTH));
		
		pos = new BlockPos(-5, 0, 0);
		list.add(NostrumDungeon.asRotated(start, pos, EnumFacing.EAST));
		
		pos = new BlockPos(5, 0, 0);
		list.add(NostrumDungeon.asRotated(start, pos, EnumFacing.WEST));
		
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
