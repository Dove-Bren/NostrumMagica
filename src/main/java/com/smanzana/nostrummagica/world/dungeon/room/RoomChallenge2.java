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
import net.minecraft.block.BlockVine;
import net.minecraft.init.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RoomChallenge2 extends StaticRoom {
	//21, ?, 21
	// 
	public RoomChallenge2() {
		super(-16, -1, 0, 4, 9, 20,
				// Floor (-1)
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 0
				"XXXXXXXXXXXXXXXXCXXXX",
				"X V   X     X   C   X",
				"X     X CCC X   C   X",
				"X     X C   X   C   X",
				"X     X C   X   C   X",
				"XXXXXXX C   X   C   X",
				"X C   X C   X   C   X",
				"X C   X C  1X   C   X",
				"X C   X C   X   C   X",
				"X C  )X C   X   C   X",
				"XXCXXXXXBXXXX   C   X",
				"X C XCC C   X   C   X",
				"X C2XCCCCCCCBCCCC   X",
				"X C XCC C   X   C   X",
				"XXCXXXXXBXXXXXXXBXXXX",
				"X C   X C   X       X",
				"X C   X C   X       X",
				"X CCCCX C  3X4   C  X",
				"X     X     X       X",
				"X     X     X       X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 1
				"XXXXXXXXXXXXXXXX XXXX",
				"X VV  X    LX  D D  X",
				"X     B     X       X",
				"X     B    LX       X",
				"X     X     X       X",
				"XXXXXXX     X       X",
				"X     X     X       X",
				"X     B     X       X",
				"X     B     X       X",
				"X    )X     X       X",
				"XX XXXXXBXXXX       X",
				"X   X  D D  XR      X",
				"X  LX       B       X",
				"X   X  U U  XR U U  X",
				"XX XXXXXBXXXXXBXBXXXX",
				"X     X     X       X",
				"X    LX     X       X",
				"X    )X     X       X",
				"X    LX     X       X",
				"X     X     X       X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 2
				"XXXXXXXXXXXXXXXXXXXXX",
				"XVVV  X     X       X",
				"X           X       X",
				"X     X     X       X",
				"X     X     X       X",
				"XXXXXXX     X       X",
				"X     X     X       X",
				"X     X     X       X",
				"X     X     X       X",
				"X    )X     X       X",
				"XXXXXXXXXXXXX       X",
				"X   X       X       X",
				"X   X       X       X",
				"X   X       X       X",
				"XXXXXXXXXXXXXXXXXXXXX",
				"X     X     X       X",
				"X     X     X       X",
				"X    )X     X       X",
				"X     X     X       X",
				"X     X     X       X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 3
				"XXXXXXXXXXXXXXXXXXXXX",
				"XVVVXXXXXXXXXXXXXXXXX",
				"XX  XXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXX)XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"X     XXXXXXX       X",
				"X     XXXXXXX       X",
				"X    )XXXXXXX       X",
				"X     XXXXXXX       X",
				"X     XXXXXXX       X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 4
				"XXXXXXXXXXXXXXXXXXXXX",
				"XVVVV     X  X      X",
				"X         B  X      X",
				"X         X  X  XX  X",
				"X         X  X  XX1 X",
				"XXXXXXXXXXX  X  XXXXX",
				"X            X  XXXXX",
				"X            X  XX5 X",
				"X  XXXXXXXX  B  XX  X",
				"X  X  XX  X  B  XX  X",
				"X  X  XX  XXXX  XX  X",
				"X  X  XXXXXXXX  XX  X",
				"X  X                X",
				"X3 X                X",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXX        X       X",
				"XXXX        X       X",
				"XXXX(               X",
				"XXXX        X       X",
				"XXXX        X       X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 5
				"XXXXXXXXXXXXXXXXXXXXX",
				"XVVVV     X  X  DD  X",
				"X         B  B      X",
				"XR        XR B  XX  X",
				"X         X  B  XXUUX",
				"XXBBBBBBBXX  B  XXXXX",
				"X            B  XXXXX",
				"X     UU     X  XXDDX",
				"X  XBBXXBBX  B  XX  X",
				"X  X  XX  X  B  XX  X",
				"X  B  XX  XXXX  XX  X",
				"XR B  XXXXXXXX  XX  X",
				"X  B   D DD D       X",
				"X  X            UU  X",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXX        X       X",
				"XXXX        X       X",
				"XXXX(       X       X",
				"XXXX        X       X",
				"XXXX        X       X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 6
				"XXXXXXXXXXXXXXXXXXXXX",
				"X VV      X  X      X",
				"X         X  X      X",
				"X         X  X  XX  X",
				"X         X  X  XX  X",
				"XXXXXXXXXXX  X  XXXXX",
				"X            X  XXXXX",
				"X            X  XX  X",
				"X  XXXXXXXX  X  XX  X",
				"X  X  XX  X  X  XX  X",
				"X  X  XX  XXXX  XX  X",
				"X  X  XXXXXXXX  XX  X",
				"X  X                X",
				"X  X                X",
				"XXXXXXXXXXXXXXXXXXXXX",
				"X           X       X",
				"XC         LX       X",
				"CCCC        X       X",
				"XC         LX       X",
				"X           X       X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 7
				"XXXXXXXXXXXXXXXXXXXXX",
				"X V       X  X      X",
				"X         X  X      X",
				"X         X  X  XX  X",
				"X         X  X  XX  X",
				"XXXXXXXXXXX  X  XXXXX",
				"X            X  XXXXX",
				"X            X  XX  X",
				"X  XXXXXXXX  X  XX  X",
				"X  X  XX  X  X  XX  X",
				"X  X  XX  XXXX  XX  X",
				"X  X  XXXXXXXX  XX  X",
				"X  X                X",
				"X  X                X",
				"XXXXXXXXXXXXXXXXXXXXX",
				"X           X       X",
				"XR          X       X",
				"            X       X",
				"XR          X       X",
				"X           X       X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 8
				"XXXXXXXXXXXXXXXXXXXXX",
				"X         X  X      X",
				"X         X  X      X",
				"X         X  X  XX  X",
				"X         X  X  XX  X",
				"XXXXXXXXXXX  X  XXXXX",
				"X            X  XXXXX",
				"X            X  XX  X",
				"X  XXXXXXXX  X  XX  X",
				"X  X  XX  X  X  XX  X",
				"X  X  XX  XXXX  XX  X",
				"X  X  XXXXXXXX  XX  X",
				"X  X                X",
				"X  X                X",
				"XXXXXXXXXXXXXXXXXXXXX",
				"X           X       X",
				"X           X       X",
				"X           X       X",
				"X           X       X",
				"X           X       X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 9
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				'X', DungeonBlock.instance(),
				' ', null,
				'(', new BlockState(Blocks.LADDER, Blocks.LADDER.getDefaultState().with(BlockLadder.FACING, Direction.EAST)), // EAST
				')', new BlockState(Blocks.LADDER, Blocks.LADDER.getDefaultState().with(BlockLadder.FACING, Direction.WEST)), // NORTH 
				'B', Blocks.IRON_BARS,
				'V', new BlockState(Blocks.VINE, Blocks.VINE.getDefaultState().with(BlockVine.NORTH, true)),
				'C', new BlockState(Blocks.CARPET, 14),
				'U', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.NORTH)),
				'R', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.EAST)),
				'D', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.SOUTH)),
				'L', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.WEST)),
				'1', new BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.GOLEM_WIND.ordinal()),
				'2', new BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.GOLEM_ENDER.ordinal()),
				'3', new BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.GOLEM_EARTH.ordinal()),
				'4', new BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.GOLEM_PHYSICAL.ordinal()),
				'5', new BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.GOLEM_FIRE.ordinal()));
	}

	@Override
	public int getNumExits() {
		return 1;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
		BlockPos exit = new BlockPos(-16, 6, 17);
		
		list.add(NostrumDungeon.asRotated(start, exit, Direction.EAST));
		
		return list;
	}

	@Override
	public int getDifficulty() {
		return 8;
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
		return Lists.newArrayList(
				NostrumDungeon.asRotated(start,
						new BlockPos(-5, 0, 2),
						Direction.WEST),
				NostrumDungeon.asRotated(start,
						new BlockPos(-6, 0, 15),
						Direction.SOUTH));
	}
	
}
