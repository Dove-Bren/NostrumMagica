package com.smanzana.nostrummagica.world.dungeon.room;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprintRegistry;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprintRegistry.RoomBlueprintRecord;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry.DungeonRoomRegisterEvent;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoomRef.DungeonLobbyRoomRef;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoomRef.DungeonRoomRef;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoomRef.DungeonStaircaseRoomRef;

import net.minecraft.util.ResourceLocation;
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
	
	public static final DungeonLobbyRoomRef PLANTBOSS_LOBBY = new DungeonLobbyRoomRef(PLANTBOSS_LOBBY_NAME);
	public static final DungeonRoomRef PLANTBOSS_ENTRANCE = new DungeonRoomRef(PLANTBOSS_ENTRANCE_NAME);
	public static final DungeonRoomRef PLANTBOSS_BOSSROOM = new DungeonRoomRef(PLANTBOSS_BOSSROOM_NAME);
	public static final DungeonLobbyRoomRef PORTAL_LOBBY = new DungeonLobbyRoomRef(PORTAL_LOBBY_NAME);
	public static final DungeonRoomRef PORTAL_ENTRANCE = new DungeonRoomRef(PORTAL_ENTRANCE_NAME);
	public static final DungeonRoomRef PORTAL_ENDROOM = new DungeonRoomRef(PORTAL_END_NAME);

	@SubscribeEvent
	public static final void onRoomRegistration(DungeonRoomRegisterEvent event) {
		DungeonRoomRegistry registry = event.getRegistry();
		
		// Blueprint Rooms
		for (RoomBlueprintRecord record : RoomBlueprintRegistry.instance().getAllRooms()) {
			registry.register(new BlueprintDungeonRoom(record.id));
		}
		
		// Static Rooms
		registry.register(new DragonLobby());
		registry.register(new RoomChallenge1());
		registry.register(new RoomChallenge2());
		registry.register(new RoomEnd1(false, false));
		registry.register(new RoomEnd1(false, true));
		registry.register(new RoomEnd1(true, false));
		registry.register(new RoomEnd1(true, true));
		registry.register(new RoomEntryDragon(false));
		registry.register(new RoomExtendedEntranceStaircase(false));
		registry.register(new RoomGrandStaircase());
		registry.register(new RoomJail1());
		registry.register(new RoomLectern());
		registry.register(new RoomRedDragonArena());
		registry.register(new RoomTee1());

	}
}
