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

public class RoomJail1 extends StaticRoom {
	//19, 10, 25
	// 
	public RoomJail1() {
		super(-4, -1, 0, 14, 9, 24,
				// Floor
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				// Layer 1
				"XXXXCXXXXXXXXXXXXXX",
				"X   C   XBB   X L X",
				"X   C   I     X   X",
				"X   C   I         X",
				"X   C   X     X   X",
				"X   C   I     X   X",
				"X   C   I     X   X",
				"X   C   X     X   X",
				"X   C   XXXXXXX   X",
				"X   C   I     X   X",
				"X   C   I         X",
				"X   C   X     X   X",
				"X   C   X     X   X",
				"X   C   XXXXXXX   X",
				"X   C   X         X",
				"X   C   I  G  X   X",
				"X   C   I     X   X",
				"X   C   XXXXXXXX XX",
				"X   C             X",
				"X   C     H       X",
				"X   C             X",
				"X   CCCCCCCCCCCCCCC",
				"X   C             X",
				"X   C             X",
				"XXXXCXXXXXXXXXXXXXX",
				// Layer 2
				"XXXX XXXXXXXXXXXXXX",
				"X  S S  XB    X L X",
				"X       I     X   X",
				"X       I         X",
				"X      WX     XE  X",
				"X       I     X   X",
				"X       I     X   X",
				"X       X     X   X",
				"X       XXXXXXX   X",
				"X       I     XE  X",
				"X       I         X",
				"X       X     X   X",
				"X      WX     X   X",
				"X      WXXXXXXXE  X",
				"X       X         X",
				"X       I     X   X",
				"X       I     X   X",
				"X       XXXXXXXX XX",
				"X                 X",
				"X                 X",
				"X                WX",
				"X                  ",
				"X                WX",
				"X  N N            X",
				"XXXX XXXXXXXXXXXXXX",
				// Layer 3
				"XXXXXXXXXXXXXXXXXXX",
				"X       X     X L X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       XXXXXXX   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       XXXXXXX   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       XXXXXXXXXXX",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"XXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXX",
				"X       XXXXXXXXLXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"XXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXX",
				"X       I     X L X",
				"X       IH    X   X",
				"X       I  X  X   X",
				"X       I  X  X   X",
				"X       XXXX  I   X",
				"X       I  X  I   X",
				"X       I  XXXX   X",
				"X       I     X   X",
				"X       I   G X   X",
				"X       XXXX  X   X",
				"X       I  X  I   X",
				"X       I  X      X",
				"X       I  X  I   X",
				"X       I  XXXXI IX",
				"X       I         X",
				"X       IH     G  X",
				"X       IIIIIIIIIIX",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"XXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXX",
				"X       I     XS SX",
				"X             X   X",
				"X          X  X   X",
				"X       IN X  X   X",
				"X       XXXX  I   X",
				"X       IS X  I   X",
				"X          XXXX   X",
				"X             XE  X",
				"X       I     XE  X",
				"X       XXXX  X   X",
				"X          X  I   X",
				"X          X      X",
				"X          X  I   X",
				"X          XXXXI IX",
				"X           SS    X",
				"X       I         X",
				"X       II        X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"XXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXX",
				"X       I     X   X",
				"X             X   X",
				"X          X  X   X",
				"X       I  X  X   X",
				"X       XXXX  I   X",
				"X       I  X  I   X",
				"X          XXXX   X",
				"X             X   X",
				"X       I     X   X",
				"X       XXXX  X   X",
				"X          X  I   X",
				"X          X      X",
				"X          X  I   X",
				"X          XXXXI IX",
				"X                 X",
				"X       I         X",
				"X       II        X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"XXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXX",
				"X       I     X   X",
				"X       I     X   X",
				"X       I  X  X   X",
				"X       I  X  X   X",
				"X       XXXX  I   X",
				"X       I  X  I   X",
				"X       I  XXXX   X",
				"X       I     X   X",
				"X       I     X   X",
				"X       XXXX  X   X",
				"X          X  I   X",
				"X          X  I   X",
				"X          X  I   X",
				"X          XXXXIIIX",
				"X                 X",
				"X       I         X",
				"X       II        X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"XXXXXXXXXXXXXXXXXXX",
				// 
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				// Ceil
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				'X', DungeonBlock.instance(),
				' ', null,
				'L', new BlockState(Blocks.LADDER, Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, Direction.SOUTH)),
				'B', Blocks.BOOKSHELF,
				'I', Blocks.IRON_BARS,
				'E', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, Direction.EAST)),
				'S', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, Direction.SOUTH)),
				'W', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, Direction.WEST)),
				'N', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, Direction.NORTH)),
				'C', new BlockState(Blocks.CARPET, 14),
				'G', new BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.GOLEM_EARTH.ordinal()),
				'H', new BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.GOLEM_ICE.ordinal()));
	}

	@Override
	public int getNumExits() {
		return 2;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
		list.add(NostrumDungeon.asRotated(start,
						new BlockPos(0, 0, 24),
						Direction.NORTH));
		
		list.add(NostrumDungeon.asRotated(start,
				new BlockPos(14, 0, 21),
				Direction.WEST));
		
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
						new BlockPos(5, 0, 12),
						Direction.EAST),
				NostrumDungeon.asRotated(start,
						new BlockPos(6, 4, 11),
						Direction.SOUTH));
	}
	
}
