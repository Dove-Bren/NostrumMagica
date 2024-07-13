package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

public interface IDungeonRoom {
	
	/**
	 * Check whether the room can be spawned at the indicated position with the
	 * given direction. This should check all affected blocks and make sure that
	 * no unbreakable blocks, etc are overlapped
	 * @param world
	 * @return
	 */
	public boolean canSpawnAt(IWorld world, BlueprintLocation start); // TODO ! Some of these checks might spill into other chunks!
	
	/**
	 * Return the number of exits this room has
	 * @return
	 */
	public int getNumExits();
	
	/**
	 * Return a list of exists that would be generated if the room was
	 * placed at the given position and facing
	 * @return
	 */
	public List<BlueprintLocation> getExits(BlueprintLocation start);
	
	/**
	 * Return the bounds of this dungeon room if it were spawned at the
	 * given position and facing.
	 * @param entry
	 * @return
	 */
	public MutableBoundingBox getBounds(BlueprintLocation entry);
	
	/**
	 * Returns the difficulty of the given room, which is used when figuring outa
	 * which room to spawn
	 * @return difficulty on scale from 1 to 10
	 */
	public int getDifficulty();
	
	/**
	 * Return the relative cost for the room. 1 is the default.
	 * Rooms with more content should cost more to make the paths be the same relative content depth.
	 * @return
	 */
	public default int getRoomCost() { return 1; }
	
	//public boolean hasPuzzle();
	
	public boolean supportsDoor();
	
	public default BlueprintLocation getDoorLocation(BlueprintLocation start) {return null;}
	
	public boolean supportsKey();
	
	// If supportsKey returns false, expected to return null
	public BlueprintLocation getKeyLocation(BlueprintLocation start);
	
	public boolean supportsTreasure();
	
	public List<BlueprintLocation> getTreasureLocations(BlueprintLocation start);
	
	public boolean hasEnemies();
	
	public boolean hasTraps();
	
	public void spawn(IWorld world, BlueprintLocation start, @Nullable MutableBoundingBox bounds, UUID dungeonID);
	
	default public void spawn(IWorld world, BlueprintLocation start) {
		spawn(world, start, (MutableBoundingBox) null, UUID.randomUUID());
	}
	
	public ResourceLocation getRoomID();
	
	static final Map<ResourceLocation, IDungeonRoom> Registry = new HashMap<>();
	public static @Nullable IDungeonRoom GetRegisteredRoom(ResourceLocation ID) {
		return Registry.get(ID);
	}
	
	public static void Register(ResourceLocation ID, IDungeonRoom room) {
		if (Registry.containsKey(ID)) {
			throw new RuntimeException("Duplicate dungeon rooms registered");
		}
		
		Registry.put(ID, room);
	}

}
