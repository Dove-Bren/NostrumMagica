package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry.DungeonRoomRecord;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

/**
 * Reference to a dungeon room. Used instead of passing instances themselves
 * because instances change out when data is reloaded.
 * Make sure to check if it's valid before using!
 * @author Skyler
 *
 */
public interface IDungeonRoomRef<T extends IDungeonRoom> extends IDungeonRoom {

	////////////////////////////////////////////////////
	/////////////// Reference methods //////////////////
	////////////////////////////////////////////////////
	
	/**
	 * Returns whether this reference is valid and points to an existing room.
	 * If true, get() should be safe to call. If false, calling get() will result in an exception.
	 * @return
	 */
	public boolean isValid();
	
	/**
	 * Attempt to dereference this reference and get the referred-to room.
	 * Throws an exception if the room does not exist.
	 * @return
	 */
	public T get() throws IllegalStateException;
	public default T getRoom() {
		return get();
	}
	
	/**
	 * Attempts to dereference this reference, but returns null on failure.
	 * @return
	 */
	public @Nullable T getUnchecked();
	
	
	////////////////////////////////////////////////////
	//////// Convenience use-through methods   /////////
	////////////////////////////////////////////////////
	
	@Override
	public default boolean canSpawnAt(IWorld world, BlueprintLocation start) {
		return get().canSpawnAt(world, start);
	}
	
	@Override
	public default int getNumExits() {
		return get().getNumExits();
	}
	
	@Override
	public default List<BlueprintLocation> getExits(BlueprintLocation start) {
		return get().getExits(start);
	}
	
	@Override
	public default MutableBoundingBox getBounds(BlueprintLocation entry) {
		return get().getBounds(entry);
	}
	
	@Override
	public default int getDifficulty() {
		return get().getDifficulty();
	}
	
	@Override
	public default int getRoomCost() { 
		return get().getRoomCost();
	}
	
	@Override
	public default boolean supportsDoor() {
		return get().supportsDoor();
	}

	@Override
	public default BlueprintLocation getDoorLocation(BlueprintLocation start) {
		return get().getDoorLocation(start);
	}

	@Override
	public default boolean supportsKey() {
		return get().supportsKey();
	}
	
	@Override
	public default BlueprintLocation getKeyLocation(BlueprintLocation start) {
		return get().getKeyLocation(start);
	}

	@Override
	public default boolean supportsTreasure() {
		return get().supportsTreasure();
	}

	@Override
	public default List<BlueprintLocation> getTreasureLocations(BlueprintLocation start) {
		return get().getTreasureLocations(start);
	}

	@Override
	public default boolean hasEnemies() {
		return get().hasEnemies();
	}

	@Override
	public default boolean hasTraps() {
		return get().hasTraps();
	}

	@Override
	public default void spawn(IWorld world, BlueprintLocation start, @Nullable MutableBoundingBox bounds, UUID dungeonID) {
		get().spawn(world, start, bounds, dungeonID);
	}
	
	@Override
	public default void spawn(IWorld world, BlueprintLocation start) {
		get().spawn(world, start);
	}

	@Override	
	public default ResourceLocation getRoomID() {
		return get().getRoomID();
	}
	
	
	////////////////////////////////////////////////////
	///////////// Typed wrappers  //////////////////////
	////////////////////////////////////////////////////
	
	public static abstract class BaseDungeonRoomRef<T extends IDungeonRoom> implements IDungeonRoomRef<T> {
		private final ResourceLocation roomID;
		
		public BaseDungeonRoomRef(ResourceLocation roomID) {
			this.roomID = roomID;
		}

		@Override
		public boolean isValid() {
			return get() != null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public @Nullable T getUnchecked() {
			@Nullable DungeonRoomRecord raw = DungeonRoomRegistry.GetInstance().getRegisteredRoom(this.roomID);
			if (raw != null) {
				try {
					return (T) raw.room;
				} catch (ClassCastException e) {
					;
				}
			}
			return null;
		}
		
		@Override
		public T get() throws IllegalStateException {
			@Nullable T val = getUnchecked();
			if (val == null) {
				throw new IllegalStateException("Failed to look up room: " + this.roomID);
			}
			return val;
		}
	}
	
	public static class DungeonRoomRef extends BaseDungeonRoomRef<IDungeonRoom> {
		public DungeonRoomRef(ResourceLocation roomID) {
			super(roomID);
		}
	}
	
	public static class DungeonStaircaseRoomRef extends BaseDungeonRoomRef<IStaircaseRoom> {
		public DungeonStaircaseRoomRef(ResourceLocation roomID) {
			super(roomID);
		}
	}
	
	public static class DungeonLobbyRoomRef extends BaseDungeonRoomRef<IDungeonLobbyRoom> {
		public DungeonLobbyRoomRef(ResourceLocation roomID) {
			super(roomID);
		}
	}
}
