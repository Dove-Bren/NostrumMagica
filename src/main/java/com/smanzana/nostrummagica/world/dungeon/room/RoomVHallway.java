package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.SingleSpawnerBlock;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RoomVHallway extends StaticRoom {
	
	public RoomVHallway() {
		super("RoomVHallway", -4, -11, 0, 1, 3, 5,
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
				'X', NostrumBlocks.lightDungeonBlock,
				'W', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.WEST)),
				'N', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				'S', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.SOUTH)),
				'C', new StaticBlockState(Blocks.RED_CARPET),
				'U', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'R', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'D', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'L', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				' ', null,
				'M', new StaticBlockState(NostrumBlocks.singleSpawner.getState(SingleSpawnerBlock.Type.GOLEM_WIND)));
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
