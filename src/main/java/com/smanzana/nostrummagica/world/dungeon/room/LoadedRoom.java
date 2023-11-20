package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.dungeon.LootUtil;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry.DungeonRoomRecord;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

/**
 * Room where all blocks are loaded from a file at startup.
 * @author Skyler
 *
 */
public class LoadedRoom implements IDungeonRoom {
	
	private RoomBlueprint blueprint;
	private List<DungeonExitPoint> chestsRelative;
	private final String registryID;
	
	public LoadedRoom(DungeonRoomRecord blueprintRecord) {
		this.blueprint = blueprintRecord.blueprint;
		
		if (blueprint == null) {
			throw new RuntimeException("Blueprint null when creating LoadedRoom. Wrong room name looked up, or too early?");
		}
		
		// Find and save chest locations
		chestsRelative = new ArrayList<>();
		blueprint.scanBlocks((offset, block) -> {
			BlockState state = block.getSpawnState(Direction.NORTH); 
			if (state != null && state.getBlock() == Blocks.CHEST) {
				chestsRelative.add(new DungeonExitPoint(offset, state.get(ChestBlock.FACING)));
			}
		});
		
		// Save consistent unique ID this room can be looked up later as and register as such
		this.registryID = "LoadedRoom_" + blueprintRecord.name;
		
		// Same as static room, being lazy and assuming children know what they're talking about.
		if (IDungeonRoom.GetRegisteredRoom(registryID) == null) {
			IDungeonRoom.Register(registryID, this);
		}
	}
	
	// Need to have some sort of 'exit point' placeholder block so that I can encode doorways into the blueprint
	
	@Override
	public boolean canSpawnAt(IWorld world, DungeonExitPoint start) {
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
			BlockState cur = world.getBlockState(pos);
		
			// Check if unbreakable...
			if (cur != null && cur.getBlockHardness(world, pos) == -1)
				return false;
		}
		
		return true;
	}
	
	@Override
	public void spawn(IWorld world, DungeonExitPoint start, @Nullable MutableBoundingBox bounds, UUID dungeonID) {
		// See note about dungeon vs blueprint facing in @getExits
		blueprint.spawn(world, start.getPos(), start.getFacing(), bounds, dungeonID);
		
		List<DungeonExitPoint> loots = this.getTreasureLocations(start);
		if (loots != null && !loots.isEmpty())
		for (NostrumDungeon.DungeonExitPoint lootSpot : loots) {
			if (bounds != null && !bounds.isVecInside(lootSpot.getPos())) {
				continue; // Will come back for you later <3
			}
			
			LootUtil.generateLoot(world, lootSpot.getPos(), lootSpot.getFacing());
		}
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
		// Blueprint wants your facing as you go in the door. That's there the 'opposite' comes from.
		
		// Blueprint exits are rotated to the entry entry direction (and have their own rotation too).
		final Direction modDir = RoomBlueprint.getModDir(blueprint.getEntry().getFacing(), start.getFacing());
		// Door offset and final rotation is what's in exits rotated modDir times
		
		List<DungeonExitPoint> ret;
		if (exits != null) {
			ret = new ArrayList<>(exits.size());
			for (DungeonExitPoint door : exits) {
				Direction doorDir = door.getFacing();
				int times = (modDir.getHorizontalIndex() + 2) % 4;
				while (times-- > 0) {
					doorDir = doorDir.rotateY();
				}
				final DungeonExitPoint fromEntry = new DungeonExitPoint(
						RoomBlueprint.applyRotation(door.getPos(), modDir),
						doorDir
						);
				final DungeonExitPoint relative = new DungeonExitPoint(start.getPos().add(fromEntry.getPos()), fromEntry.getFacing()); 
				ret.add(relative);
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
		List<DungeonExitPoint> ret = new ArrayList<>();
		for (DungeonExitPoint orig : chestsRelative) {
			final DungeonExitPoint relative = NostrumDungeon.asRotated(start, orig.getPos(), orig.getFacing().getOpposite()); 
			ret.add(relative);
		}
		return ret;
	}
	
	@Override
	public MutableBoundingBox getBounds(DungeonExitPoint entry) {
		BlockPos dims = blueprint.getAdjustedDimensions(entry.getFacing());
		BlockPos offset = blueprint.getAdjustedOffset(entry.getFacing());
		
		int minX = entry.getPos().getX() - offset.getX();
		int minY = entry.getPos().getY() - offset.getY();
		int minZ = entry.getPos().getZ() - offset.getZ();
		int maxX = minX + dims.getX();
		int maxY = minY + dims.getY();
		int maxZ = minZ + dims.getZ();
		
		// Have to figure out real min/max ourselves
		return new MutableBoundingBox(
				Math.min(minX, maxX),
				Math.min(minY, maxY),
				Math.min(minZ, maxZ),
				Math.max(maxX, minX),
				Math.max(maxY, minY),
				Math.max(maxZ, minZ));
	}

	@Override
	public boolean hasEnemies() {
		return true;
	}

	@Override
	public boolean hasTraps() {
		return true;
	}
	
	@Override
	public String getRoomID() {
		return this.registryID;
	}
}
