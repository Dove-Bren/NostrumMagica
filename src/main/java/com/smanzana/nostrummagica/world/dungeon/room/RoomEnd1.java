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

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

public class RoomEnd1 extends StaticRoom {
	
	public static final ResourceLocation ID(boolean withChest, boolean withEnemy) { return NostrumMagica.Loc("room_end1" + (withChest ? "_chest" : "") + (withEnemy ? "_enemy" : "")); }
	
	private boolean withChest;
	private boolean withEnemy;
	
	public RoomEnd1(boolean withChest, boolean withEnemy) {
		super(ID(withChest, withEnemy), -4, -1, 0, 4, 3, 7,
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
				'X', NostrumBlocks.lightDungeonBlock,
				'C', new StaticBlockState(Blocks.RED_CARPET),
				'N', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				'E', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.EAST)),
				'B', Blocks.BOOKSHELF,
				' ', null,
				'M', (withChest ? null : (withEnemy ? new StaticBlockState(NostrumBlocks.singleSpawner.getState(SingleSpawnerBlock.Type.GOLEM_PHYSICAL)) : null)));
		
		this.withChest = withChest;
		this.withEnemy = withEnemy;
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
		return false;
	}

	@Override
	public BlueprintLocation getKeyLocation(BlueprintLocation start) {
		return NostrumDungeon.asRotated(start,
				new BlockPos(-3, 0, 1), Direction.SOUTH);
	}
	
	@Override
	public boolean supportsTreasure() {
		return true;
	}

	@Override
	public List<BlueprintLocation> getTreasureLocations(BlueprintLocation start) {
		List<BlueprintLocation> list = new LinkedList<>();
		
		list.add(NostrumDungeon.asRotated(start,
				new BlockPos(-3, 0, 5), Direction.EAST));
		
		if (withChest)
			list.add(NostrumDungeon.asRotated(start,
					new BlockPos(-3, 0, 1), Direction.SOUTH)
				);
		
		return list;
	}

	@Override
	public List<String> getRoomTags() {
		return Lists.newArrayList(NostrumDungeons.TAG_DRAGON, NostrumDungeons.TAG_PLANTBOSS, NostrumDungeons.TAG_PORTAL);
	}

	@Override
	public String getRoomName() {
		return "A Small Treasure";
	}
}
