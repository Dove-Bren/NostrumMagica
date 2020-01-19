package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Room where all blocks are loaded from a file at startup.
 * @author Skyler
 *
 */
public class LoadedRoom implements IDungeonRoom {
	
	private RoomBlueprint blueprint;
	
	public LoadedRoom(RoomBlueprint blueprint) {
		this.blueprint = blueprint;
	}
	
	// Need to have some sort of 'exit point' placeholder block so that I can encode doorways into the blueprint
	
	@Override
	public boolean canSpawnAt(World world, DungeonExitPoint start) {
		BlockPos dims = blueprint.getAdjustedDimensions(start.getFacing());
		BlockPos offset = blueprint.getAdjustedOffset(start.getFacing());
		
		int minX = start.getPos().getX() - offset.getX();
		int minY = start.getPos().getY() - offset.getY();
		int minZ = start.getPos().getZ() - offset.getZ();
		int maxX = minX + dims.getX();
		int maxY = minY + dims.getY();
		int maxZ = minZ + dims.getZ();
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
		// See note about dungeon vs blueprint facing in @getExits
		blueprint.spawn(world, start.getPos(), start.getFacing().getOpposite());
	}

	@Override
	public int getNumExits() {
		Collection<DungeonExitPoint> exits = blueprint.getExits();
		return exits == null ? 0 : exits.size();
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		Collection<DungeonExitPoint> exits = blueprint.getExits();
		
		// Dungeon notion of direction is backwards to blueprints:
		// Dungeon wants facing to be you looking back through the door
		// Blueprint wants your facing as you go in the door
		List<DungeonExitPoint> ret;
		if (exits != null) {
			ret = new ArrayList<>(exits.size());
			for (DungeonExitPoint door : exits) {
				ret.add(new DungeonExitPoint(door.getPos(), door.getFacing().getOpposite()));
			}
		} else {
			ret = new LinkedList<>();
		}
		return ret;
	}

	@Override
	public int getDifficulty() {
		return 1;
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
		return new LinkedList<>();
	}

	@Override
	public boolean hasEnemies() {
		return true;
	}

	@Override
	public boolean hasTraps() {
		return true;
	}
}
