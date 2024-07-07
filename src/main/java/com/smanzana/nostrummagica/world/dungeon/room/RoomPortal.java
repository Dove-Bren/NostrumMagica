package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.tile.SwitchBlockTileEntity;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class RoomPortal extends StaticRoom {
	
	public RoomPortal() {
		// end up providing the type of shrine!
		super("RoomPortal", -7, -1, 0, 7, 10, 16,
				// Floor
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				// Layer 1
				"XXXXXXXCXXXXXXX",
				"X      C      X",
				"X DDD  C  DDD X",
				"X DDD  C  DDD X",
				"X DDD  C  DDD X",
				"X      C      X",
				"X      C      X",
				"X DDD  C  DDD X",
				"X DDD  C  DDD X",
				"X DDD  C  DDD X",
				"X      C      X",
				"X      C      X",
				"X DDD DDD DDD X",
				"X DDD DPD DDD X",
				"X DDD DDD DDD X",
				"X             X",
				"XXXXXXXXXXXXXXX",
				// Layer 2
				"XXXXXXX XXXXXXX",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"XXXXXXXXXXXXXXX",
				// Layer 3
				"XXXXXXXXXXXXXXX",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"XXXXXXXXXXXXXXX",
				// Layer 4
				"XXXXXXXXXXXXXXX",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"XXXXXXXXXXXXXXX",
				// Layer 5
				"XXXXXXXXXXXXXXX",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"XXXXXXXXXXXXXXX",
				// Layer 6
				"XXXXXXXXXXXXXXX",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"XXXXXXXXXXXXXXX",
				// Layer 7
				"XXXXXXXXXXXXXXX",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"XXXXXXXXXXXXXXX",
				// Layer 8
				"XXXXXXXXXXXXXXX",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"XXXXXXXXXXXXXXX",
				// Layer 9
				"XXXXXXXXXXXXXXX",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"XXXXXXXXXXXXXXX",
				// Layer 10
				"XXXXXXXXXXXXXXX",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"X             X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X DDD     DDD X",
				"X             X",
				"XXXXXXXXXXXXXXX",
				// Roof
				"XXXXXXXXXXXXXXX",
				"XXXXXXXGXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXX",
				'X', NostrumBlocks.lightDungeonBlock,
				'D', new StaticBlockState(NostrumBlocks.dungeonBlock),
				'P', NostrumBlocks.sorceryPortalSpawner,
				'C', new StaticBlockState(Blocks.RED_CARPET),
				'G', Blocks.GLOWSTONE,
				' ', null);
	}

	@Override
	public int getNumExits() {
		return 0;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
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
	public boolean supportsTreasure() {
		return false;
	}

	@Override
	public List<DungeonExitPoint> getTreasureLocations(DungeonExitPoint start) {
		return new LinkedList<>();
	}
	
	BlockPos tilePos;
	
	@Override
	protected void applyBlockOverrides(IWorld world, BlockPos worldPos, BlockPos dataPos, StaticBlockState defaultState) {
		if (dataPos.getX() == 0 && dataPos.getZ() == 13 && dataPos.getY() == 0) {
			if (tilePos != null) {
				throw new RuntimeException("Spawning multiple portal rooms at the same time!");
			}
			tilePos = worldPos;
		}
		if (dataPos.getX() == 0 && dataPos.getZ() == 1 && dataPos.getY() == 8) {
			if (tilePos == null) {
				throw new RuntimeException("Portal room spawned out of assumed order!");
			}
			world.setBlockState(worldPos, NostrumBlocks.switchBlock.getDefaultState(), 2);
			TileEntity te = world.getTileEntity(worldPos);
			if (te != null && te instanceof SwitchBlockTileEntity) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				ent.setOffset(tilePos.subtract(worldPos));
				tilePos = null;
			}
		}
	}
}
