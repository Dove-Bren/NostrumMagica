package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Entrance staircase. Extends up to nearly surface level and then spawns a shrine.
 * @author Skyler
 *
 */
public class RoomExtendedStaircase implements IDungeonRoom {

	private RoomEntryStairs stairs;
	private RoomEntryShrine shrine;
	
	public RoomExtendedStaircase(SpellComponentWrapper component, boolean dark) {
		stairs = new RoomEntryStairs(dark);
		shrine = new RoomEntryShrine(component, dark);
	}
	
	@Override
	public boolean canSpawnAt(World world, DungeonExitPoint start) {
		int minX = start.getPos().getX() - 5;
		int minY = start.getPos().getY();
		int minZ = start.getPos().getZ() - 5;
		int maxX = start.getPos().getX() + 5;
		int maxY = start.getPos().getY();
		int maxZ = start.getPos().getZ() + 5;
		for (int i = minX; i <= maxX; i++)
		for (int j = minY; j <= maxY; j++)
		for (int k = minZ; k <= maxZ; k++) {
			BlockPos pos = new BlockPos(i, j, k);
			IBlockState cur = world.getBlockState(pos);
		
			// Check if unbreakable...
			if (cur != null && cur.getBlockHardness(world, pos) == -1)
				return false;
		}
		
		return true;
	}
	
	@Override
	public void spawn(NostrumDungeon dungeon, World world, DungeonExitPoint start) {
		
		int stairHeight = 4;
		BlockPos pos = start.getPos();
		
		Chunk chunk = world.getChunkFromBlockCoords(pos);
        BlockPos blockpos;
        BlockPos blockpos1;

        for (blockpos = new BlockPos(pos.getX(), chunk.getTopFilledSegment() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
        	blockpos1 = blockpos.down();
        	IBlockState state = chunk.getBlockState(blockpos1);
            
            if (state.getMaterial().isLiquid() || (state.getMaterial().blocksMovement() && !state.getBlock().isLeaves(state, world, blockpos1) && !state.getBlock().isFoliage(world, blockpos1))) {
            	break;
            }
        }

		int maxY = blockpos.getY();
		BlockPos cur = start.getPos();
		while (cur.getY() + stairHeight < maxY) {
			stairs.spawn(dungeon, world, new DungeonExitPoint(cur, start.getFacing()));
			cur = cur.add(0, stairHeight, 0);
		}
		
		shrine.spawn(dungeon, world, new DungeonExitPoint(cur, start.getFacing()));
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
		return null;
	}

	@Override
	public boolean hasEnemies() {
		return false;
	}

	@Override
	public boolean hasTraps() {
		return false;
	}
}
