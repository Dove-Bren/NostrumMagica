package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.IWorldHeightReader;

import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.Heightmap;

/**
 * Entrance staircase. Extends up to nearly surface level and then spawns a shrine.
 * @author Skyler
 *
 */
public class RoomExtendedDragonStaircase implements IStaircaseRoom {

	private RoomEntryStairs stairs;
	
	public RoomExtendedDragonStaircase(boolean dark) {
		stairs = new RoomEntryStairs(dark);
		
		if (IDungeonRoom.GetRegisteredRoom(getRoomID()) == null) { 
			IDungeonRoom.Register(getRoomID(), this);
		}
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
	public void spawn(IWorld world, DungeonExitPoint start, MutableBoundingBox bounds, UUID dungeonID) {
		getEntryStart((type, x, z) -> world.getHeight(type, x, z), start, true, world, bounds, dungeonID);
	}
	
	@Override
	public DungeonExitPoint getEntryStart(IWorldHeightReader world, DungeonExitPoint start) {
		return getEntryStart(world, start, false, null, null, null);
	}
	
	private DungeonExitPoint getEntryStart(IWorldHeightReader heightReader, DungeonExitPoint start, boolean spawn, IWorld world, MutableBoundingBox bounds, UUID dungeonID) {
		int stairHeight = 4;
		BlockPos pos = start.getPos();
		
		BlockPos blockpos = new BlockPos(pos.getX(), heightReader.getHeight(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ()), pos.getZ());
		
		int maxY = blockpos.getY();
		BlockPos cur = start.getPos();
		while (cur.getY() < maxY - 17) {
			if (spawn) {
				stairs.spawn(world, new DungeonExitPoint(cur, start.getFacing()), bounds, dungeonID);
			}
			cur = cur.add(0, stairHeight, 0);
		}
		
		return new DungeonExitPoint(cur, start.getFacing());
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
	public boolean supportsTreasure() {
		return false;
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
	public ResourceLocation getRoomID() {
		return NostrumMagica.Loc("room_extended_eragon_staircase"); // Would want to incorporate dark or not?
	}
	
	@Override
	public MutableBoundingBox getBounds(DungeonExitPoint start) {
		// This should repeat what spawn does and find the actual bounds, but that requires querying the world which
		// this method would like to not do.
		// So instead, guess based on start to an approximate height of 128.
		
		final BlockPos topPos = new BlockPos(start.getPos().getX(), 128, start.getPos().getZ());
		MutableBoundingBox bounds = null;
		
		// Add staircase down to actual start
		final int stairHeight = 4;
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		cursor.setPos(start.getPos());
		for (int i = start.getPos().getY(); i < topPos.getY(); i+= stairHeight) {
			cursor.setY(i);
			if (bounds == null) {
				bounds = stairs.getBounds(new DungeonExitPoint(cursor, start.getFacing()));
			} else {
				bounds.expandTo(stairs.getBounds(new DungeonExitPoint(cursor, start.getFacing())));
			}
		}
		
		return bounds;
	}
}
