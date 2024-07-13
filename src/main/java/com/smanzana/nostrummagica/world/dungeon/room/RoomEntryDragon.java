package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.SingleSpawnerBlock;
import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;

import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

// Tiny tight spiral staircase used when creating the entrance to the dungeon
public class RoomEntryDragon extends StaticRoom {
	
	// Number of y levels below the ground we'd like to be
	public static final int LevelsBelow = 7;
	
	public RoomEntryDragon(boolean dark) {
		
		super(NostrumMagica.Loc("room_entry_dragon"), -5, 0, -5, 5, 14, 5,
				// Floor
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBB  BBBBB",
				"BBBB  UBBBB",
				"BBBB   BBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B   l l   B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B    F    B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B   lSl   B",
				"B         B",
				"B         B",
				"B         B",
				"BE       WB",
				"B         B",
				"B         B",
				"B         B",
				"B    N    B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B   l l   B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B   l l   B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B   l l   B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"B         B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"BBBBlBlBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B   l l   B",
				"B         B",
				"B  BBBBBBBB",
				"B         B",
				"B     BB  B",
				"B  M  BB  B",
				"BBBBBBBB  B",
				"B         B",
				"B         B",
				"BB#######BB",
				// 
				"BBBBBBBBBBB",
				"B   lSl   B",
				"B         B",
				"B  BBBBBBBB",
				"B         B",
				"B     BB  B",
				"B  N  BB  B",
				"BBBBBBBB  B",
				"B   S     B",
				"B         B",
				"BBB#####BBB",
				//
				"BBBBBBBBBBB",
				"B         B",
				"B         B",
				"B  BBBBBBBB",
				"B         B",
				"B     BB  B",
				"B     BB  B",
				"BBBBBBBB  B",
				"B         B",
				"B         B",
				"BBBBB#BBBBB",
				//
				"BBBBBBBBBBB",
				"BBBBBBB   B",
				"BBBBBBBB  B",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				// y11
				"BBBBBBBBBBB",
				"B         B",
				"B         B",
				"B  BBBBBBBB",
				"B         B",
				"B  BBBBB  B",
				"B  B F B  B",
				"B  B   B  B",
				"B         B",
				"B         B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B        WB",
				"B        WB",
				"B  BBBBBBBB",
				"B        GB",
				"B  BBBBB  B",
				"B WB S BE B",
				"B  B   B  B",
				"B         B",
				"B   N N   B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"B         B",
				"B         B",
				"B  BBBBBBBB",
				"B         B",
				"B  BBBBB  B",
				"B  B   B  B",
				"B  B   B  B",
				"B         B",
				"B         B",
				"BBBBBBBBBBB",
				//
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				"BBBBBBBBBBB",
				'B', NostrumBlocks.lightDungeonBlock,
				'N', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				'S', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.SOUTH)),
				'W', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.WEST)),
				'E', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.EAST)),
				'l', new StaticBlockState(Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH)),
				' ', null,
				'U', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'F', new StaticBlockState(NostrumBlocks.singleSpawner.getState(SingleSpawnerBlock.Type.GOLEM_FIRE)),
				'M', new StaticBlockState(NostrumBlocks.singleSpawner.getState(SingleSpawnerBlock.Type.GOLEM_PHYSICAL)),
				'G', new StaticBlockState(NostrumBlocks.singleSpawner.getState(SingleSpawnerBlock.Type.GOLEM_WIND)),
				'#', Blocks.IRON_BARS);
	}

	@Override
	public int getNumExits() {
		return 0;
	}

	@Override
	public List<BlueprintLocation> getExits(BlueprintLocation start) {
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
	public BlueprintLocation getKeyLocation(BlueprintLocation start) {
		return null;
	}
	
	@Override
	public boolean supportsTreasure() {
		return true;
	}

	@Override
	public List<BlueprintLocation> getTreasureLocations(BlueprintLocation start) {
		List<BlueprintLocation> list = new LinkedList<>();
		
		BlockPos pos = new BlockPos(-1, 11, 1);
		list.add(NostrumDungeon.asRotated(start, pos, Direction.SOUTH));
		
		pos = new BlockPos(1, 11, 1);
		list.add(NostrumDungeon.asRotated(start, pos, Direction.SOUTH));
		
		return list;
	}
	
	@Override
	public void spawn(IWorld world, BlueprintLocation start, @Nullable MutableBoundingBox bounds, UUID dungeonID) {
		super.spawn(world, start, bounds, dungeonID);
	}
}
