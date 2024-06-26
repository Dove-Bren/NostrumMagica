package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.NostrumSingleSpawner;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RoomChallenge2 extends StaticRoom {
	//21, ?, 21
	// 
	public RoomChallenge2() {
		super("RoomChallenge2", -16, -1, 0, 4, 9, 20,
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
				'X', NostrumBlocks.lightDungeonBlock,
				' ', null,
				'(', new StaticBlockState(Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.EAST)), // EAST
				')', new StaticBlockState(Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.WEST)), // NORTH 
				'B', Blocks.IRON_BARS,
				'V', new StaticBlockState(Blocks.VINE.getDefaultState().with(VineBlock.NORTH, true)),
				'C', new StaticBlockState(Blocks.RED_CARPET),
				'U', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				'R', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.EAST)),
				'D', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.SOUTH)),
				'L', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.WEST)),
				'1', new StaticBlockState(NostrumBlocks.singleSpawner.getState(NostrumSingleSpawner.Type.GOLEM_WIND)),
				'2', new StaticBlockState(NostrumBlocks.singleSpawner.getState(NostrumSingleSpawner.Type.GOLEM_ENDER)),
				'3', new StaticBlockState(NostrumBlocks.singleSpawner.getState(NostrumSingleSpawner.Type.GOLEM_EARTH)),
				'4', new StaticBlockState(NostrumBlocks.singleSpawner.getState(NostrumSingleSpawner.Type.GOLEM_PHYSICAL)),
				'5', new StaticBlockState(NostrumBlocks.singleSpawner.getState(NostrumSingleSpawner.Type.GOLEM_FIRE)));
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
