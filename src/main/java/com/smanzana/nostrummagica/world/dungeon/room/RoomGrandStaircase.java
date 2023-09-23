package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RoomGrandStaircase extends StaticRoom {
	
	public RoomGrandStaircase() {
		// end up providing the type of shrine!
		super(-2, -17, 0, 2, 3, 21,
				// Floor (-17)
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
				"XXXXX",
				// L1 (-16)
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
				"XSSSX",
				"X C X",
				"X C X",
				"XXCXX",
//				// L1 (-15)
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XXXXX",
//				"XSSSX",
//				"X   X",
//				"XU UX",
//				"XX XX",
				// L1 14
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
				"XSSSX",
				"X   X",
				"X   X",
				"X   X",
				"XXXXX",
				// L1 13
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
				"XSSSX",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"XXXXX",
				// L1 12
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
				"XSSSX",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"XXXXX",
				// L1 11
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
				"XSSSX",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"XXXXX",
				// L1 10
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
				"XSSSX",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"XXXXX",
				// L1 9
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
				"XSSSX",
				"X   X",
				"X   X",
				"X   X",
				"XE WX",
				"X   X",
				"X   X",
				"X   X",
				"X   X",
				"XXXXX",
				// L1 8
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
				"XSSSX",
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
				// L1 7
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
				"XSSSX",
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
				// L1 6
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XSSSX",
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
				// L1 5
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XSSSX",
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
				// L1 4
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XSSSX",
				"X   X",
				"X   X",
				"X   X",
				"XE WX",
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
				// L1 3
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XSSSX",
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
				// L1 2
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XSSSX",
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
				// L1 1
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XSSSX",
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
				// L1 0
				"XXXXX",
				"XXXXX",
				"XXXXX",
				"XSSSX",
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
				// Air1
				"XXCXX",
				"X C X",
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
				// Air1
				"XX XX",
				"XD DX",
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
				// Air1
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
				"X   X",
				"XXXXX",
				// Air1
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
				"XXXXX",
				'X', DungeonBlock.instance(),
				'W', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.WEST)),
				'E', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.EAST)),
				'N', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.NORTH)),
				'D', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.SOUTH)),
				'C', new BlockState(Blocks.CARPET, 14),
				'S', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(BlockStairs.FACING, Direction.NORTH).with(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).with(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)),
				' ', null);
	}

	@Override
	public int getNumExits() {
		return 1;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
		BlockPos exit = new BlockPos(0, -16, 21);
		
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
