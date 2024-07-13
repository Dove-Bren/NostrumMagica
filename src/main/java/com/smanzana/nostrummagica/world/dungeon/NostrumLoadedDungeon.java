package com.smanzana.nostrummagica.world.dungeon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smanzana.nostrummagica.world.blueprints.RoomBlueprintRegistry;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprintRegistry.RoomBlueprintRecord;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoom;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonStartRoom;
import com.smanzana.nostrummagica.world.dungeon.room.BlueprintDungeonRoom;

import net.minecraft.util.ResourceLocation;

/**
 * Dungeon with rooms picked randomly from loaded dungeon registry
 * @author Skyler
 *
 */
public class NostrumLoadedDungeon extends NostrumDungeon {
	
	public final String tag;
	private final List<IDungeonRoom> staticRooms;
	
	public NostrumLoadedDungeon(String tag, IDungeonStartRoom starting, IDungeonRoom ending) {
		this(tag, starting, ending, 2, 3);
	}
	
	public NostrumLoadedDungeon(String tag, IDungeonStartRoom starting, IDungeonRoom ending, int minPath, int randPath) {
		super(starting, ending, minPath, randPath);
		this.tag = tag;
		this.staticRooms = new ArrayList<>();
	}
	
	@Override
	public NostrumDungeon add(IDungeonRoom room) {
		this.staticRooms.add(room);
		return super.add(room);
	}
	
	@Override
	public void clearRooms() {
		this.staticRooms.clear();
		super.clearRooms();
	}
	
	@Override
	protected List<IDungeonRoom> getRooms() {
		List<IDungeonRoom> ret = new ArrayList<>(this.staticRooms.size() + 32);
		ret.addAll(this.staticRooms);
		
		for (RoomBlueprintRecord blueprint : RoomBlueprintRegistry.instance().getAllRooms(tag)) {
			ret.add(GetLoadedRoom(blueprint.id));
		}
		
		return ret;
	}
	
	// Cache the loaded rooms so that they can do their own caching.
	// LoadedRooms automatically look at the registry and refresh themselves if the room
	// record changes. We don't need to do that since they handle it.
	private static final Map<ResourceLocation, BlueprintDungeonRoom> LOADED_ROOM_CACHE = new HashMap<>();
	private static final BlueprintDungeonRoom GetLoadedRoom(ResourceLocation location) {
		return LOADED_ROOM_CACHE.computeIfAbsent(location, l -> new BlueprintDungeonRoom(location));
	}
}
