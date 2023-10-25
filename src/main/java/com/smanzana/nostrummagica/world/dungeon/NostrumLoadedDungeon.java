package com.smanzana.nostrummagica.world.dungeon;

import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry.DungeonRoomRecord;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoom;
import com.smanzana.nostrummagica.world.dungeon.room.LoadedRoom;

/**
 * Dungeon with rooms picked randomly from loaded dungeon registry
 * @author Skyler
 *
 */
public class NostrumLoadedDungeon extends NostrumDungeon {
	
	public String tag;
	
	public NostrumLoadedDungeon(String tag, IDungeonRoom starting, IDungeonRoom ending) {
		this(tag, starting, ending, 2, 3);
	}
	
	public NostrumLoadedDungeon(String tag, IDungeonRoom starting, IDungeonRoom ending, int minPath, int randPath) {
		super(starting, ending, minPath, randPath);
		this.tag = tag;
		
		for (DungeonRoomRecord blueprint : DungeonRoomRegistry.instance().getAllRooms(tag)) {
			this.add(new LoadedRoom(blueprint));
		}
	}
}
