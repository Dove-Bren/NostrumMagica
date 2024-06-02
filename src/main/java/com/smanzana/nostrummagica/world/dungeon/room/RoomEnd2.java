package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.NostrumSingleSpawner;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RoomEnd2 extends StaticRoom {
	
	private boolean withChest;
	
	public RoomEnd2(boolean withChest) {
		super("RoomEnd2", -2, -1, 0, 7, 3, 7,
				// Floor
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				// Layer 1
				"XXCXXXXXXX",
				"X C      X",
				"X CCCCCC X",
				"XXXXXXXCXX",
				"X    X C X",
				"X XXXX C X",
				"X    O   X",
				"XXXXXXXXXX",
				// Layer 2
				"XX XXXXXXX",
				"XS       X",
				"X   N    X",
				"XXXXXXX XX",
				"X    X M X",
				"X XXXX   X",
				"X    ON NX",
				"XXXXXXXXXX",
				// Layer 3
				"XXXXXXXXXX",
				"X        X",
				"X        X",
				"XXXXXXXXXX",
				"X    X   X",
				"X XXXX   X",
				"X        X",
				"XXXXXXXXXX",
				// Layer 4
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				"XXXXXXXXXX",
				'X', NostrumBlocks.lightDungeonBlock,
				'C', new StaticBlockState(Blocks.RED_CARPET),
				'N', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				'S', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.SOUTH)),
				' ', null,
				'M', new StaticBlockState(NostrumBlocks.singleSpawner.getState(NostrumSingleSpawner.Type.GOLEM_PHYSICAL)),
				'O', withChest ? null : NostrumBlocks.lightDungeonBlock);
		
		this.withChest = withChest;
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
		List<DungeonExitPoint> list = new LinkedList<>();
		
		list.add(NostrumDungeon.asRotated(start,
				new BlockPos(5, 0, 6), Direction.NORTH));
		
		if (withChest)
			list.add(NostrumDungeon.asRotated(start,
					new BlockPos(2, 0, 4), Direction.WEST)
				);
		
		return list;
	}
}
