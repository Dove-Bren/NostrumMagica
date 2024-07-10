package com.smanzana.nostrummagica.world.dungeon;

import com.smanzana.nostrummagica.NostrumMagica;
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

import net.minecraft.util.ResourceLocation;

public class NostrumDungeons {
	
	public static final ResourceLocation PLANTBOSS_LOBBY_NAME = NostrumMagica.Loc("plantboss_lobby");
	public static final ResourceLocation PLANTBOSS_ENTRANCE_NAME = NostrumMagica.Loc("plantboss_dungeon_entrance");
	public static final ResourceLocation PLANTBOSS_BOSSROOM_NAME = NostrumMagica.Loc("plant_boss_room");

	public static NostrumDungeon PLANTBOSS_DUNGEON = new NostrumLoadedDungeon(
			"plant_boss",
			new LoadedStartRoom(DungeonRoomRegistry.instance().getRoomRecord(PLANTBOSS_LOBBY_NAME),
					DungeonRoomRegistry.instance().getRoomRecord(PLANTBOSS_ENTRANCE_NAME)),
				new LoadedRoom(DungeonRoomRegistry.instance().getRoomRecord(PLANTBOSS_BOSSROOM_NAME))
			).setColor(0x80106020)
			.add(new RoomGrandStaircase())
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
			).setColor(0x80601005)
				.add(new RoomGrandStaircase())
				.add(new RoomEnd1(false, true))
				.add(new RoomJail1())
				.add(new RoomJail1())
				.add(new RoomChallenge2())
				.add(new RoomChallenge2())
				.add(new RoomLectern())
				.add(new RoomEnd1(true, false))
				.add(new RoomEnd1(false, false));
	
	public static final ResourceLocation PORTAL_LOBBY_NAME = NostrumMagica.Loc("portal_lobby");
	public static final ResourceLocation PORTAL_ENTRANCE_NAME = NostrumMagica.Loc("portal_entrance");
	public static final ResourceLocation PORTAL_END_NAME = NostrumMagica.Loc("portal_room");
	
	public static NostrumDungeon PORTAL_DUNGEON = new NostrumLoadedDungeon(
			"portal",
			new LoadedStartRoom(DungeonRoomRegistry.instance().getRoomRecord(PORTAL_LOBBY_NAME),
					DungeonRoomRegistry.instance().getRoomRecord(PORTAL_ENTRANCE_NAME)),
			new LoadedRoom(DungeonRoomRegistry.instance().getRoomRecord(PORTAL_END_NAME))
			).setColor(0x80402080)
				.add(new RoomGrandStaircase())
				.add(new RoomEnd1(false, true))
				.add(new RoomJail1())
				.add(new RoomChallenge2())
				.add(new RoomLectern())
				.add(new RoomEnd1(true, false))
				.add(new RoomEnd1(false, false));

}
