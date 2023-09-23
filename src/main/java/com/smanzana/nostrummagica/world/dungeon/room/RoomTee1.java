package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RoomTee1 extends StaticRoom {

	public RoomTee1() {
		super(-4, -1, 0, 4, 7, 19,
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
				"X       X",
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
				'X', DungeonBlock.instance(),
				' ', null,
				'L', new BlockState(Blocks.LADDER, Blocks.LADDER.getDefaultState().with(BlockLadder.FACING, Direction.NORTH)),
				'B', Blocks.IRON_BARS,
				'W', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.WEST)),
				'E', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.EAST)),
				'S', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.SOUTH)),
				'C', new BlockState(Blocks.CARPET, 14),
				'G', new BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.GOLEM_LIGHTNING.ordinal()));
	}

	@Override
	public int getNumExits() {
		return 2;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
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
		return true;
	}

	@Override
	public DungeonExitPoint getKeyLocation(DungeonExitPoint start) {
		return NostrumDungeon.asRotated(start,
				new BlockPos(-3, 4, 18),
				Direction.EAST);
	}

	@Override
	public List<DungeonExitPoint> getTreasureLocations(DungeonExitPoint start) {
		return Lists.newArrayList(
				NostrumDungeon.asRotated(start,
						new BlockPos(-3, 4, 16),
						Direction.EAST));
	}
	
}
