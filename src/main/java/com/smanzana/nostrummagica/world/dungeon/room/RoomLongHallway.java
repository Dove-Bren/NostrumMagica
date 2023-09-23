package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RoomLongHallway extends StaticRoom {
	
	public RoomLongHallway() {
		// end up providing the type of shrine!
		super(-2, -1, 0, 2, 3, 20,
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
				"XXCXX",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"X C X",
				"XXCXX",
				// Layer 2
				"XX XX",
				"X   X",
				"X   X",
				"X   X",
				"XE WX",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"XE WX",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"XE WX",
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
				"X   X",
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
				'W', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.WEST)),
				'E', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.EAST)),
				'C', new BlockState(Blocks.CARPET, 14),
				' ', null);
	}

	@Override
	public int getNumExits() {
		return 1;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
		BlockPos exit = new BlockPos(0, 0, 20);
		
		list.add(NostrumDungeon.asRotated(start, exit, Direction.NORTH));
		
		return list;
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
		return new LinkedList<>();
	}
}
