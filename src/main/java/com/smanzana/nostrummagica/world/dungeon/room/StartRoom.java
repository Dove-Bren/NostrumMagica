package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class StartRoom extends StaticRoom {
	
	public StartRoom() {
		// End up passing in height to surface?
		super(-5, -1, -5, 5, 5, 5,
				// Floor
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				// Layer 1
				"XXXXXCXXXXX",
				"XBB  C  BBX",
				"XB   C   BX",
				"X  CCCCC  X",
				"X  CCCCC  X",
				"CCCCDXXCCCC",
				"X  CXXXC  X",
				"X  CCCCC  X",
				"XB   C   BX",
				"XBB  C  BBX",
				"XXXXXCXXXXX",
				// Layer 2
				"XXXXX XXXXX",
				"XBB      BX",
				"X         X",
				"X         X",
				"X         X",
				"     XX    ",
				"X    RX   X",
				"X         X",
				"XB       BX",
				"XB      BBX",
				"XXXXX XXXXX",
				// Layer 3
				"XXXXXXXXXXX",
				"XB	       X",
				"X         X",
				"X         X",
				"X     X   X",
				"X    XU   X",
				"X         X",
				"X         X",
				"XB        X",
				"X       BBX",
				"XXXXXXXXXXX",
				// Layer 4
				"XXXXXXXXXXX",
				"X 	       X",
				"X         X",
				"X         X",
				"X   XL    X",
				"X    X    X",
				"X         X",
				"X         X",
				"X         X",
				"X        BX",
				"XXXXXXXXXXX",
				// Layer 5
				"XXXXXXXXXXX",
				"X 	       X",
				"X         X",
				"X  G   G  X",
				"X         X",
				"X   DX    X",
				"X   X     X",
				"X  G   G  X",
				"X         X",
				"X         X",
				"XXXXXXXXXXX",
				// Ceil
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXX   XXXX",
				"XXXX X XXXX",
				"XXXX RXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				'X', DungeonBlock.instance(),
				' ', null,
				'G', Blocks.GLOWSTONE,
				'B', Blocks.BOOKSHELF,
				'C', new BlockState(Blocks.CARPET, 14),
				'U', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)),
				'R', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)),
				'D', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)),
				'L', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)));
	}

	@Override
	public int getNumExits() {
		return 4;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
		BlockPos pos;
		
		pos = new BlockPos(0, 0, -5);
		list.add(NostrumDungeon.asRotated(start, pos, EnumFacing.SOUTH));
		
		pos = new BlockPos(0, 0, 5);
		list.add(NostrumDungeon.asRotated(start, pos, EnumFacing.NORTH));
		
		pos = new BlockPos(-5, 0, 0);
		list.add(NostrumDungeon.asRotated(start, pos, EnumFacing.EAST));
		
		pos = new BlockPos(5, 0, 0);
		list.add(NostrumDungeon.asRotated(start, pos, EnumFacing.WEST));
		
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
		List<DungeonExitPoint> list = new LinkedList<>();
		
		BlockPos pos = new BlockPos(-4, 0, -4);
		list.add(NostrumDungeon.asRotated(start, pos, EnumFacing.SOUTH));
		
		return list;
	}
}
