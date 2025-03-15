package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.SingleSpawnerBlock;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeons;

import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class RoomTee1 extends StaticRoom {
	
	public static final ResourceLocation ID = NostrumMagica.Loc("room_tee1");

	public RoomTee1() {
		super(ID, -4, -1, 0, 4, 7, 19,
				// Floor
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				// Layer 1
				"XXXXCXXXX",
				"X   C   X",
				"X   C   X",
				"X   C   X",
				"X   C  FX",
				"X   C   X",
				"X   C   X",
				"X   C   X",
				"X   C   X",
				"X   C   X",
				"X   C   X",
				"X   C   X",
				"X   C   X",
				"X   C   X",
				"X   C   X",
				"X   C   X",
				"CCCCCCCCC",
				"X       X",
				"X      LX",
				"XXXXXXXXX",
				// Layer 2
				"XXXX XXXX",
				"X  S S  X",
				"X       X",
				"XE     WX",
				"X       X",
				"X       X",
				"X       X",
				"XE     WX",
				"X       X",
				"X       X",
				"X       X",
				"XE     WX",
				"X       X",
				"X       X",
				"X       X",
				"XE     WX",
				"         ",
				"XE     WX",
				"X      LX",
				"XXXXXXXXX",
				// Layer 3
				"XXXXXXXXX",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X      LX",
				"XXXXXXXXX",
				// Layer
				"XXXXXXXXX",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXLX",
				"XXXXXXXXX",
				// Layer 4
				"XXXXXXXXX",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"XBBBBBBBX",
				"XT      X",
				"X   G   X",
				"X      LX",
				"XXXXXXXXX",
				// Layer 5
				"XXXXXXXXX",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"XXXXXXXXX",
				// Layer 6
				"XXXXXXXXX",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"X       X",
				"XXXXXXXXX",
				// Layer 7
				// Ceil
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				'X', NostrumBlocks.lightDungeonBlock,
				' ', null,
				'L', new StaticBlockState(Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH)),
				'B', Blocks.IRON_BARS,
				'W', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.WEST)),
				'E', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.EAST)),
				'S', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.SOUTH)),
				'C', new StaticBlockState(Blocks.RED_CARPET),
				'T', new StaticBlockState(Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST)),
				'G', new StaticBlockState(NostrumBlocks.singleSpawner.getState(SingleSpawnerBlock.Type.GOLEM_LIGHTNING)));
	}

	@Override
	public int getNumExits() {
		return 2;
	}

	@Override
	public List<BlueprintLocation> getExits(BlueprintLocation start) {
		List<BlueprintLocation> list = new LinkedList<>();
		
		list.add(NostrumDungeon.asRotated(start,
						new BlockPos(-4, 0, 16),
						Direction.EAST));
		
		list.add(NostrumDungeon.asRotated(start,
				new BlockPos(4, 0, 16),
				Direction.WEST));
		
		return list;
	}

	@Override
	public int getDifficulty() {
		return 3;
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
		return Lists.newArrayList(
				NostrumDungeon.asRotated(start,
						new BlockPos(-3, 4, 16),
						Direction.EAST));
	}

	@Override
	public List<String> getRoomTags() {
		return Lists.newArrayList(NostrumDungeons.TAG_DRAGON);
	}

	@Override
	public String getRoomName() {
		return "A Path Split";
	}
	
}
