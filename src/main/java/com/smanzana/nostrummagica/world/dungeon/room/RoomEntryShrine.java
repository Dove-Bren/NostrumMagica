package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

// Tiny tight spiral staircase used when creating the enterance to the dungeon
public class RoomEntryShrine extends StaticRoom {
	
	public RoomEntryShrine(boolean dark) {
		
		super(-5, 0, -5, 5, 21, 5,
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
				"B         B",
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
				"BB  BBB  BB",
				"B   l l   B",
				"           ",
				"           ",
				"B         B",
				"B         B",
				"B         B",
				"           ",
				"           ",
				"B         B",
				"BB  BBB  BB",
				//
				"BB  BBB  BB",
				"B         B",
				"           ",
				"           ",
				"B         B",
				"B         B",
				"B         B",
				"           ",
				"           ",
				"B         B",
				"BB  BBB  BB",
				//
				"BB  BBB  BB",
				"B         B",
				"           ",
				"           ",
				"B         B",
				"B         B",
				"          B",
				"           ",
				"           ",
				"B         B",
				"BB  BBB  BB",
				//
				"BB  BBB  BB",
				"B         B",
				"           ",
				"           ",
				"B         B",
				"B         B",
				"          B",
				"           ",
				"           ",
				"B         B",
				"BB  BBB  BB",
				//
				"BB  BB   BB",
				"B          ",
				"           ",
				"           ",
				"          B",
				"B         B",
				"           ",
				"           ",
				"           ",
				"B         B",
				"BB  BBB  B ",
				//
				"BB  BB   B ",
				"B          ",
				"           ",
				"           ",
				"           ",
				"B         B",
				"          B",
				"           ",
				"           ",
				"B         B",
				"BB  BB   B ",
				//
				"BB  BB     ",
				"B          ",
				"           ",
				"           ",
				"           ",
				"          B",
				"           ",
				"           ",
				"           ",
				"          B",
				"BB  BB     ",
				//
				"BB  BB     ",
				"B          ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"          B",
				"BB   B     ",
				//
				"BB   B     ",
				"B          ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"BB         ",
				//
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				//
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				//
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				//
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				//
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				//
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				"           ",
				'B', DungeonBlock.instance(),
				'N', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH)),
				'S', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH)),
				'l', new BlockState(Blocks.LADDER, Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH)),
				' ', null,
				'U', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)));
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
