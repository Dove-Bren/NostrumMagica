package com.smanzana.nostrummagica.world.dungeon;

import com.smanzana.nostrummagica.world.dungeon.room.DungeonStartRoom;
import com.smanzana.nostrummagica.world.dungeon.room.NostrumDungeonRooms;

public class NostrumDungeons {
	
	public static NostrumDungeon PLANTBOSS_DUNGEON = new NostrumLoadedDungeon(
			"plant_boss",
			new DungeonStartRoom(NostrumDungeonRooms.PLANTBOSS_LOBBY,
					NostrumDungeonRooms.PLANTBOSS_ENTRANCE, NostrumDungeonRooms.RefExtendedEntranceStaircaseLight),
				NostrumDungeonRooms.PLANTBOSS_BOSSROOM
			).setColor(0x80106020)
			.add(NostrumDungeonRooms.RefGrandStaircase)
			.add(NostrumDungeonRooms.RefEnd1_Enemy)
			.add(NostrumDungeonRooms.RefJail1)
			.add(NostrumDungeonRooms.RefChallenge2)
			.add(NostrumDungeonRooms.RefLectern)
			.add(NostrumDungeonRooms.RefEnd1_Chest)
			.add(NostrumDungeonRooms.RefEnd1_Empty);

	public static NostrumDungeon DRAGON_DUNGEON = new NostrumLoadedDungeon(
			"dragon",
			new DungeonStartRoom(NostrumDungeonRooms.RefDragonLobby, NostrumDungeonRooms.RefEntryDragon, NostrumDungeonRooms.RefExtendedEntranceStaircaseLight),
			NostrumDungeonRooms.RefRedDragonArena,
			4, 1
			).setColor(0x80601005)
				.add(NostrumDungeonRooms.RefGrandStaircase)
				.add(NostrumDungeonRooms.RefEnd1_Enemy)
				.add(NostrumDungeonRooms.RefJail1)
				.add(NostrumDungeonRooms.RefChallenge1)
				.add(NostrumDungeonRooms.RefChallenge2)
				.add(NostrumDungeonRooms.RefLectern)
				.add(NostrumDungeonRooms.RefTee1)
				.add(NostrumDungeonRooms.RefEnd1_Chest)
				.add(NostrumDungeonRooms.RefEnd1_Enemy);
	
	public static NostrumDungeon PORTAL_DUNGEON = new NostrumLoadedDungeon(
			"portal",
			new DungeonStartRoom(NostrumDungeonRooms.PORTAL_LOBBY,
					NostrumDungeonRooms.PORTAL_ENTRANCE,
					NostrumDungeonRooms.RefExtendedEntranceStaircaseLight),
			NostrumDungeonRooms.PORTAL_ENDROOM
			).setColor(0x80402080)
				.add(NostrumDungeonRooms.RefGrandStaircase)
				.add(NostrumDungeonRooms.RefEnd1_Enemy)
				.add(NostrumDungeonRooms.RefJail1)
				.add(NostrumDungeonRooms.RefChallenge2)
				.add(NostrumDungeonRooms.RefLectern)
				.add(NostrumDungeonRooms.RefEnd1_Enemy)
				.add(NostrumDungeonRooms.RefEnd1_Empty);

}
