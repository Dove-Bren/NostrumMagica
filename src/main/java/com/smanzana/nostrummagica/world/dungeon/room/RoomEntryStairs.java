package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;

// Tiny tight spiral staircase used when creating the enterance to the dungeon
public class RoomEntryStairs extends StaticRoom {
	
	public RoomEntryStairs(boolean dark) {
		super("RoomEntryStairs", -2, 0, -2, 2, 3, 2,
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
				'B', NostrumBlocks.lightDungeonBlock,
				'N', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				' ', null,
				'U', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'R', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'D', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'L', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)));
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
