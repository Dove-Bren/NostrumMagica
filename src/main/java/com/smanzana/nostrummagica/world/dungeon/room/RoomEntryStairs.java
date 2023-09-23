package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.util.Direction;

// Tiny tight spiral staircase used when creating the enterance to the dungeon
public class RoomEntryStairs extends StaticRoom {
	
	public RoomEntryStairs(boolean dark) {
		super(-2, 0, -2, 2, 3, 2,
				// Floor
				"BBBBB",
				"B  BB",
				"B  UB",
				"B N B",
				"BBBBB",
				//
				"BBBBB",
				"BBL B",
				"B   B",
				"B   B",
				"BBBBB",
				//
				"BBBBB",
				"B   B",
				"BD  B",
				"BB  B",
				"BBBBB",
				//
				"BBBBB",
				"B   B",
				"B   B",
				"B RBB",
				"BBBBB",
				'B', DungeonBlock.instance(),
				'N', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.NORTH)),
				' ', null,
				'U', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(BlockStairs.FACING, Direction.NORTH).with(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).with(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)),
				'R', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(BlockStairs.FACING, Direction.EAST).with(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).with(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)),
				'D', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(BlockStairs.FACING, Direction.SOUTH).with(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).with(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)),
				'L', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(BlockStairs.FACING, Direction.WEST).with(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).with(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)));
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
