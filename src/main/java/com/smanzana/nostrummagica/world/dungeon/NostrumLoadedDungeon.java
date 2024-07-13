package com.smanzana.nostrummagica.world.dungeon;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.world.blueprints.RoomBlueprintRegistry;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprintRegistry.RoomBlueprintRecord;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonStartRoom;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoomRef;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoomRef.DungeonRoomRef;

/**
 * Dungeon with rooms picked randomly from loaded dungeon registry
 * @author Skyler
 *
 */
public class NostrumLoadedDungeon extends NostrumDungeon {
	
	public final String tag;
	private final List<IDungeonRoomRef<?>> staticRooms;
	
	public NostrumLoadedDungeon(String tag, DungeonStartRoom starting, IDungeonRoomRef<?> ending) {
		this(tag, starting, ending, 2, 3);
	}
	
	public NostrumLoadedDungeon(String tag, DungeonStartRoom starting, IDungeonRoomRef<?> ending, int minPath, int randPath) {
		super(starting, ending, minPath, randPath);
		this.tag = tag;
		this.staticRooms = new ArrayList<>();
	}
	
	@Override
	public NostrumDungeon add(IDungeonRoomRef<?> room) {
		this.staticRooms.add(room);
		return super.add(room);
	}
	
	@Override
	public void clearRooms() {
		this.staticRooms.clear();
		super.clearRooms();
	}
	
	@Override
	protected List<IDungeonRoomRef<?>> getRooms() {
		List<IDungeonRoomRef<?>> ret = new ArrayList<>(this.staticRooms.size() + 32);
		ret.addAll(this.staticRooms);
		
		int unused; // If tags were in the dungeon registry instead of the blueprint one, this wouldn't have to be a special thing
					// and could use one NostrumDungeon class.
					// Including could add tags to static rooms and get rid of the #add method at all!
		for (RoomBlueprintRecord blueprint : RoomBlueprintRegistry.instance().getAllRooms(tag)) {
			ret.add(new DungeonRoomRef(blueprint.id)); // Hardcodes assumption that dungeonroom id is same as blueprint id
		}
		
		return ret;
	}
}
