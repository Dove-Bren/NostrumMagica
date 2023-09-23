package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RoomVHallway extends StaticRoom {
	
	public RoomVHallway() {
		super(-4, -11, 0, 1, 3, 5,
				// Floor
				"XXXXXX",
				"XXXXXX",
				"XXXXXX",
				"XXXXXX",
				"XXXXXX",
				"XXXXXX",
				// Layer 1
				"XXXXXX",
				"X M  X",
				"X    X",
				"XU   X",
				"XCCCCX",
				"XXXXCX",
				// Layer 2
				"XXXXXX",
				"XX   X",
				"XU   X",
				"X    X",
				"X  N X",
				"XXXX X",
				// Layer
				"XXXXXX",
				"X R  X",
				"X    X",
				"X    X",
				"X    X",
				"XXXXXX",
				// Layer
				"XXXXXX",
				"X  RXX",
				"X    X",
				"X    X",
				"X    X",
				"XXXXXX",
				// Layer
				"XXXXXX",
				"X S  X",
				"X   DX",
				"X    X",
				"X    X",
				"XXXXXX",
				// Layer
				"XXXXXX",
				"X    X",
				"X    X",
				"X   DX",
				"X   XX",
				"XXXXXX",
				// Layer
				"XXXXXX",
				"X    X",
				"X    X",
				"X    X",
				"X  L X",
				"XXXXXX",
				// Layer
				"XXXXXX",
				"X    X",
				"X    X",
				"X    X",
				"XXL  X",
				"XXXXXX",
				// Layer
				"XXXXXX",
				"X    X",
				"X   WX",
				"XU  WX",
				"X    X",
				"XXXXXX",
				// Layer
				"XXXXXX",
				"XXXXXX",
				"XUXXXX",
				"X XXXX",
				"X   XX",
				"XXXXXX",
				// Layer
				"XXXXCX",
				"XCCCCX",
				"X    X",
				"X    X",
				"X    X",
				"XXXXXX",
				// Layer
				"XXXX X",
				"X  S X",
				"X    X",
				"X    X",
				"X    X",
				"XXXXXX",
				// Layer
				"XXXXXX",
				"X    X",
				"X    X",
				"X    X",
				"X    X",
				"XXXXXX",
				// Layer
				"XXXXXX",
				"XXXXXX",
				"XXXXXX",
				"XXXXXX",
				"XXXXXX",
				"XXXXXX",
				'X', DungeonBlock.instance(),
				'W', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.WEST)),
				'N', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.NORTH)),
				'S', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.SOUTH)),
				'C', new BlockState(Blocks.CARPET, 14),
				'U', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(BlockStairs.FACING, Direction.NORTH).with(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).with(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)),
				'R', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(BlockStairs.FACING, Direction.EAST).with(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).with(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)),
				'D', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(BlockStairs.FACING, Direction.SOUTH).with(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).with(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)),
				'L', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(BlockStairs.FACING, Direction.WEST).with(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).with(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)),
				' ', null,
				'M', new BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.GOLEM_WIND.ordinal()));
	}

	@Override
	public int getNumExits() {
		return 1;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
		BlockPos exit = new BlockPos(0, -10, 5);
		
		list.add(NostrumDungeon.asRotated(start, exit, Direction.NORTH));
		
		return list;
	}

	@Override
	public int getDifficulty() {
		return 0;
	}

	@Override
	public boolean hasEnemies() {
		return true;
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
