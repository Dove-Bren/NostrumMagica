package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RoomEnd1 extends StaticRoom {
	
	private boolean withChest;
	private boolean withEnemy;
	
	public RoomEnd1(boolean withChest, boolean withEnemy) {
		super(-4, -1, 0, 4, 3, 7,
				// Floor
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
				"XMX C   X",
				"XBX CCC X",
				"XBXXXXCXX",
				"X     C X",
				"X CCCCC X",
				"X       X",
				"XXXXXXXXX",
				// Layer 2
				"XXXX XXXX",
				"X X     X",
				"XBX N   X",
				"XBXXXX XX",
				"XE      X",
				"X       X",
				"XE      X",
				"XXXXXXXXX",
				// Layer 3
				"XXXXXXXXX",
				"XXX     X",
				"XXX     X",
				"XXXXXXXXX",
				"X       X",
				"X       X",
				"X       X",
				"XXXXXXXXX",
				// Layer 4
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				"XXXXXXXXX",
				'X', DungeonBlock.instance(),
				'C', new BlockState(Blocks.CARPET, 14),
				'N', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, Direction.NORTH)),
				'E', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, Direction.EAST)),
				'B', Blocks.BOOKSHELF,
				' ', null,
				'M', (withChest ? null : (withEnemy ? new BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.GOLEM_PHYSICAL.ordinal()) : null)));
		
		this.withChest = withChest;
		this.withEnemy = withEnemy;
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
		return withEnemy && !withChest;
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
		return !withEnemy && withChest;
	}

	@Override
	public DungeonExitPoint getKeyLocation(DungeonExitPoint start) {
		return NostrumDungeon.asRotated(start,
				new BlockPos(-3, 0, 1), Direction.SOUTH);
	}

	@Override
	public List<DungeonExitPoint> getTreasureLocations(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
		list.add(NostrumDungeon.asRotated(start,
				new BlockPos(-3, 0, 5), Direction.EAST));
		
		if (withChest)
			list.add(NostrumDungeon.asRotated(start,
					new BlockPos(-3, 0, 1), Direction.SOUTH)
				);
		
		return list;
	}
}
