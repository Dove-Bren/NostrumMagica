package com.smanzana.nostrummagica.world.dungeon;

import com.smanzana.nostrummagica.world.dungeon.room.DragonStartRoom;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;
import com.smanzana.nostrummagica.world.dungeon.room.LoadedRoom;
import com.smanzana.nostrummagica.world.dungeon.room.LoadedStartRoom;
import com.smanzana.nostrummagica.world.dungeon.room.RoomArena;
import com.smanzana.nostrummagica.world.dungeon.room.RoomChallenge2;
import com.smanzana.nostrummagica.world.dungeon.room.RoomEnd1;
import com.smanzana.nostrummagica.world.dungeon.room.RoomGrandStaircase;
import com.smanzana.nostrummagica.world.dungeon.room.RoomJail1;
import com.smanzana.nostrummagica.world.dungeon.room.RoomLectern;

public class NostrumDungeons {
	
	public static final String PLANTBOSS_ROOM_NAME = "plant_boss_room";

	public static NostrumDungeon PLANTBOSS_DUNGEON = new NostrumLoadedDungeon(
			"plant_boss",
			new LoadedStartRoom(DungeonRoomRegistry.instance().getRoomRecord("plantboss_lobby"),
					DungeonRoomRegistry.instance().getRoomRecord("plantboss_dungeon_entrance")),
				new LoadedRoom(DungeonRoomRegistry.instance().getRoomRecord(PLANTBOSS_ROOM_NAME))
			).add(new RoomGrandStaircase())
		 .add(new RoomEnd1(false, true))
		 .add(new RoomJail1())
		 .add(new RoomJail1())
		 .add(new RoomChallenge2())
		 .add(new RoomChallenge2())
		 .add(new RoomLectern())
		 .add(new RoomEnd1(true, false))
		 .add(new RoomEnd1(false, false));

	public static NostrumDungeon DRAGON_DUNGEON = new NostrumLoadedDungeon(
			"dragon",
			new DragonStartRoom(),
			new RoomArena(),
			4, 1
			).add(new RoomGrandStaircase())
			 .add(new RoomEnd1(false, true))
			 .add(new RoomJail1())
			 .add(new RoomJail1())
			 .add(new RoomChallenge2())
			 .add(new RoomChallenge2())
			 .add(new RoomLectern())
			 .add(new RoomEnd1(true, false))
			 .add(new RoomEnd1(false, false));
	
	public static final String PORTAL_ROOM_NAME = "portal_room";
	
	public static NostrumDungeon PORTAL_DUNGEON = new NostrumLoadedDungeon(
			"portal",
			new LoadedStartRoom(DungeonRoomRegistry.instance().getRoomRecord("portal_lobby"),
					DungeonRoomRegistry.instance().getRoomRecord("portal_entrance")),
			new LoadedRoom(DungeonRoomRegistry.instance().getRoomRecord(PORTAL_ROOM_NAME))
			).add(new RoomGrandStaircase())
			 .add(new RoomEnd1(false, true))
			 .add(new RoomJail1())
			 .add(new RoomChallenge2())
			 .add(new RoomLectern())
			 .add(new RoomEnd1(true, false))
			 .add(new RoomEnd1(false, false));

}
