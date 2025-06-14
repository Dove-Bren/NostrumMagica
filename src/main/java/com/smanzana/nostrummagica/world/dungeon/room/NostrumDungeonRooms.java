package com.smanzana.nostrummagica.world.dungeon.room;

import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomRegistry;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomRegistry.DungeonRoomRegisterEvent;
import com.smanzana.autodungeons.world.dungeon.room.IDungeonRoomRef.DungeonLobbyRoomRef;
import com.smanzana.autodungeons.world.dungeon.room.IDungeonRoomRef.DungeonRoomRef;
import com.smanzana.autodungeons.world.dungeon.room.IDungeonRoomRef.DungeonStaircaseRoomRef;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NostrumDungeonRooms {
	
	//////////////////////////////////////////
	////       Static Room References     ////
	//////////////////////////////////////////
	public static final DungeonLobbyRoomRef RefDragonLobby = new DungeonLobbyRoomRef(DragonLobby.ID);
	public static final DungeonRoomRef RefChallenge1 = new DungeonRoomRef(RoomChallenge1.ID);
	public static final DungeonRoomRef RefChallenge2 = new DungeonRoomRef(RoomChallenge2.ID);
	public static final DungeonRoomRef RefEnd1_Empty = new DungeonRoomRef(RoomEnd1.ID(false, false));
	public static final DungeonRoomRef RefEnd1_Enemy = new DungeonRoomRef(RoomEnd1.ID(false, true));
	public static final DungeonRoomRef RefEnd1_Chest = new DungeonRoomRef(RoomEnd1.ID(true, false));
	public static final DungeonRoomRef RefEnd1_Full = new DungeonRoomRef(RoomEnd1.ID(true, true));
	public static final DungeonRoomRef RefEntryDragon = new DungeonRoomRef(RoomEntryDragon.ID);
	public static final DungeonStaircaseRoomRef RefExtendedEntranceStaircaseLight = new DungeonStaircaseRoomRef(RoomExtendedEntranceStaircase.ID_LIGHT);
	public static final DungeonStaircaseRoomRef RefExtendedEntranceStaircaseDark = new DungeonStaircaseRoomRef(RoomExtendedEntranceStaircase.ID_DARK);
	public static final DungeonRoomRef RefGrandStaircase = new DungeonRoomRef(RoomGrandStaircase.ID);
	public static final DungeonRoomRef RefJail1 = new DungeonRoomRef(RoomJail1.ID);
	public static final DungeonRoomRef RefLectern = new DungeonRoomRef(RoomLectern.ID);
	public static final DungeonRoomRef RefRedDragonArena = new DungeonRoomRef(RoomRedDragonArena.ID);
	public static final DungeonRoomRef RefTee1 = new DungeonRoomRef(RoomTee1.ID);
	
	//////////////////////////////////////////
	// REQUIRED hardcoded blueprint References ////
	//////////////////////////////////////////
	private static final ResourceLocation PLANTBOSS_LOBBY_NAME = NostrumMagica.Loc("plantboss_lobby");
	private static final ResourceLocation PLANTBOSS_ENTRANCE_NAME = NostrumMagica.Loc("plantboss_dungeon_entrance");
	private static final ResourceLocation PLANTBOSS_BOSSROOM_NAME = NostrumMagica.Loc("plant_boss_room");
	private static final ResourceLocation PORTAL_LOBBY_NAME = NostrumMagica.Loc("portal_lobby");
	private static final ResourceLocation PORTAL_ENTRANCE_NAME = NostrumMagica.Loc("portal_entrance");
	private static final ResourceLocation PORTAL_END_NAME = NostrumMagica.Loc("portal_room");
	private static final ResourceLocation MANI_CASTLE_START_NAME = NostrumMagica.Loc("mani_castle/mani_castle_start");
	
	public static final DungeonLobbyRoomRef PLANTBOSS_LOBBY = new DungeonLobbyRoomRef(PLANTBOSS_LOBBY_NAME);
	public static final DungeonRoomRef PLANTBOSS_ENTRANCE = new DungeonRoomRef(PLANTBOSS_ENTRANCE_NAME);
	public static final DungeonRoomRef PLANTBOSS_BOSSROOM = new DungeonRoomRef(PLANTBOSS_BOSSROOM_NAME);
	public static final DungeonLobbyRoomRef PORTAL_LOBBY = new DungeonLobbyRoomRef(PORTAL_LOBBY_NAME);
	public static final DungeonRoomRef PORTAL_ENTRANCE = new DungeonRoomRef(PORTAL_ENTRANCE_NAME);
	public static final DungeonRoomRef PORTAL_ENDROOM = new DungeonRoomRef(PORTAL_END_NAME);
	public static final DungeonRoomRef RefManiCastleStart = new DungeonRoomRef(MANI_CASTLE_START_NAME);

	@SubscribeEvent
	public static final void onRoomRegistration(DungeonRoomRegisterEvent event) {
		DungeonRoomRegistry registry = event.getRegistry();
		
		// Static Rooms
		registerStaticRoom(registry, new DragonLobby());
		registerStaticRoom(registry, new RoomChallenge1());
		registerStaticRoom(registry, new RoomChallenge2());
		registerStaticRoom(registry, new RoomEnd1(false, false));
		registerStaticRoom(registry, new RoomEnd1(false, true));
		registerStaticRoom(registry, new RoomEnd1(true, false));
		registerStaticRoom(registry, new RoomEnd1(true, true));
		registerStaticRoom(registry, new RoomEntryDragon(false));
		registerStaticRoom(registry, new RoomExtendedEntranceStaircase(false));
		registerStaticRoom(registry, new RoomGrandStaircase());
		registerStaticRoom(registry, new RoomJail1());
		registerStaticRoom(registry, new RoomLectern());
		registerStaticRoom(registry, new RoomRedDragonArena());
		registerStaticRoom(registry, new RoomTee1());
	}
	
	protected static final void registerStaticRoom(DungeonRoomRegistry registry, StaticRoom room) {
		registry.register(room.getRoomName(), room, room.getRoomWeight(), room.getRoomCost(), room.getRoomTags());
	}
}
