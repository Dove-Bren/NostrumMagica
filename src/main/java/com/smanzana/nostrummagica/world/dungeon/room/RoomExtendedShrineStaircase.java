package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;

/**
 * Entrance staircase. Extends up to nearly surface level and then spawns a shrine.
 * @author Skyler
 *
 */
public class RoomExtendedShrineStaircase implements IDungeonRoom {

	private RoomEntryStairs stairs;
	private RoomEntryShrine shrine;
	
	public RoomExtendedShrineStaircase(SpellComponentWrapper component, boolean dark) {
		stairs = new RoomEntryStairs(dark);
		shrine = new RoomEntryShrine(component, dark);
		
		IDungeonRoom.Register(this.getRoomID(), this);
	}
	
	@Override
	public boolean canSpawnAt(IWorld world, DungeonExitPoint start) {
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
			BlockState cur = world.getBlockState(pos);
		
			// Check if unbreakable...
			if (cur != null && cur.getBlockHardness(world, pos) == -1)
				return false;
		}
		
		return true;
	}
	
	@Override
	public void spawn(IWorld world, DungeonExitPoint start, @Nullable MutableBoundingBox bounds, UUID dungeonID) {
		
		int stairHeight = 4;
		BlockPos pos = start.getPos();
		
		IChunk chunk = world.getChunk(pos);
        BlockPos blockpos;
        BlockPos blockpos1;

        for (blockpos = new BlockPos(pos.getX(), chunk.getTopFilledSegment() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
        	blockpos1 = blockpos.down();
        	BlockState state = chunk.getBlockState(blockpos1);
            
            if (state.getMaterial().isLiquid() || (state.getMaterial().blocksMovement() && state.getMaterial() != Material.LEAVES && state.getBlockHardness(world, blockpos1) != 0)) {
            	break;
            }
        }

		int maxY = blockpos.getY();
		BlockPos cur = start.getPos();
		while (cur.getY() + stairHeight < maxY) {
			stairs.spawn(world, new DungeonExitPoint(cur, start.getFacing()), bounds, dungeonID);
			cur = cur.add(0, stairHeight, 0);
		}
		
		shrine.spawn(world, new DungeonExitPoint(cur, start.getFacing()), bounds, dungeonID);
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

	@Override
	public String getRoomID() {
		return "RoomExtendedShrineStaircase"; // Would need to incorporate component type and uniqify itself?
	}
	
	@Override
	public MutableBoundingBox getBounds(DungeonExitPoint start) {
		// This should repeat what spawn does and find the actual bounds, but that requires querying the world which
		// this method would like to not do.
		// So instead, guess based on start to an approximate height of 128.
		
		final BlockPos topPos = new BlockPos(start.getPos().getX(), 128, start.getPos().getZ());
		MutableBoundingBox bounds = shrine.getBounds(new DungeonExitPoint(topPos, start.getFacing()));
		
		// Add staircase down to actual start
		final int stairHeight = 4;
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		cursor.setPos(start.getPos());
		for (int i = start.getPos().getY(); i < topPos.getY(); i+= stairHeight) {
			cursor.setY(i);
			bounds.expandTo(stairs.getBounds(new DungeonExitPoint(cursor, start.getFacing())));
		}
		
		return bounds;
	}
}
