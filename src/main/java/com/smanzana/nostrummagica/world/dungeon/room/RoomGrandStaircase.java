package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;

import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RoomGrandStaircase extends StaticRoom {
	
	public RoomGrandStaircase() {
		// end up providing the type of shrine!
		super(NostrumMagica.Loc("room_grand_staircase"), -2, -17, 0, 2, 3, 21,
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
				"XX XX",
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
				'X', NostrumBlocks.lightDungeonBlock,
				'W', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.WEST)),
				'E', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.EAST)),
				'N', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				'D', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.SOUTH)),
				'C', new StaticBlockState(Blocks.RED_CARPET),
				'S', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				' ', null);
	}	

	@Override
	public int getNumExits() {
		return 1;
	}

	@Override
	public List<BlueprintLocation> getExits(BlueprintLocation start) {
		List<BlueprintLocation> list = new LinkedList<>();
		
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
	public BlueprintLocation getKeyLocation(BlueprintLocation start) {
		return null;
	}
	
	@Override
	public boolean supportsTreasure() {
		return false;
	}

	@Override
	public List<BlueprintLocation> getTreasureLocations(BlueprintLocation start) {
		return new LinkedList<>();
	}
}
