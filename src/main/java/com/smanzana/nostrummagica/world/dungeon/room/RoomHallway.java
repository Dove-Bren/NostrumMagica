package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RoomHallway extends StaticRoom {
	
	public RoomHallway() {
		// end up providing the type of shrine!
		super("RoomHallway", -2, -1, 0, 2, 3, 10,
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
				"XXCXX",
				// Layer 2
				"XX XX",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"XE WX",
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
				'X', NostrumBlocks.lightDungeonBlock,
				'W', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.WEST)),
				'E', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.EAST)),
				'C', new StaticBlockState(net.minecraft.block.Blocks.RED_CARPET),
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
	public boolean supportsTreasure() {
		return false;
	}

	@Override
	public List<DungeonExitPoint> getTreasureLocations(DungeonExitPoint start) {
		return new LinkedList<>();
	}
}
