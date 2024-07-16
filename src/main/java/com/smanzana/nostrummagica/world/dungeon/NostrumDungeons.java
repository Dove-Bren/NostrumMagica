package com.smanzana.nostrummagica.world.dungeon;

import com.smanzana.nostrummagica.world.dungeon.room.DungeonStartRoom;
import com.smanzana.nostrummagica.world.dungeon.room.NostrumDungeonRooms;

public class NostrumDungeons {
	
	public static final String TAG_PLANTBOSS = "plant_boss";
	
	public static NostrumDungeon PLANTBOSS_DUNGEON = new NostrumDungeon(
			TAG_PLANTBOSS,
			new DungeonStartRoom(NostrumDungeonRooms.PLANTBOSS_LOBBY,
					NostrumDungeonRooms.PLANTBOSS_ENTRANCE, NostrumDungeonRooms.RefExtendedEntranceStaircaseLight),
				NostrumDungeonRooms.PLANTBOSS_BOSSROOM
			).setColor(0x80106020)
			;
	
	public static final String TAG_DRAGON = "dragon";

	public static NostrumDungeon DRAGON_DUNGEON = new NostrumDungeon(
			TAG_DRAGON,
			new DungeonStartRoom(NostrumDungeonRooms.RefDragonLobby, NostrumDungeonRooms.RefEntryDragon, NostrumDungeonRooms.RefExtendedEntranceStaircaseLight),
			NostrumDungeonRooms.RefRedDragonArena,
			4, 1
			).setColor(0x80601005)
			;
	
	public static final String TAG_PORTAL = "portal";
	
	public static NostrumDungeon PORTAL_DUNGEON = new NostrumDungeon(
			TAG_PORTAL,
			new DungeonStartRoom(NostrumDungeonRooms.PORTAL_LOBBY,
					NostrumDungeonRooms.PORTAL_ENTRANCE,
					NostrumDungeonRooms.RefExtendedEntranceStaircaseLight),
			NostrumDungeonRooms.PORTAL_ENDROOM
			).setColor(0x80402080)
			;

}
