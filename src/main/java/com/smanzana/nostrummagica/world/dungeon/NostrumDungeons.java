package com.smanzana.nostrummagica.world.dungeon;

import com.smanzana.autodungeons.world.dungeon.Dungeon;
import com.smanzana.autodungeons.world.dungeon.room.DungeonStartRoom;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.dungeon.room.NostrumDungeonRooms;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NostrumDungeons {
	
	public static final String TAG_PLANTBOSS = "plant_boss";
	protected static final ResourceLocation ID_PLANTBOSS_DUNGEON = NostrumMagica.Loc("plantboss");
	
	public static Dungeon PLANTBOSS_DUNGEON = new NostrumDungeon(
			TAG_PLANTBOSS,
			new DungeonStartRoom(NostrumDungeonRooms.PLANTBOSS_LOBBY,
					NostrumDungeonRooms.PLANTBOSS_ENTRANCE, NostrumDungeonRooms.RefExtendedEntranceStaircaseLight),
				NostrumDungeonRooms.PLANTBOSS_BOSSROOM
			).setColor(0x80106020)
			.setLootTable(NostrumMagica.Loc("nostrum_shrine_room"))
			;
	
	public static final String TAG_DRAGON = "dragon";
	protected static final ResourceLocation ID_DRAGON_DUNGEON = NostrumMagica.Loc("dragon");

	public static Dungeon DRAGON_DUNGEON = new NostrumDungeon(
			TAG_DRAGON,
			new DungeonStartRoom(NostrumDungeonRooms.RefDragonLobby, NostrumDungeonRooms.RefEntryDragon, NostrumDungeonRooms.RefExtendedEntranceStaircaseLight),
			NostrumDungeonRooms.RefRedDragonArena,
			4, 1
			).setColor(0x80601005)
			.setLootTable(NostrumMagica.Loc("nostrum_shrine_room"))
			;
	
	public static final String TAG_PORTAL = "portal";
	protected static final ResourceLocation ID_PORTAL_DUNGEON = NostrumMagica.Loc("portal");
	
	public static Dungeon PORTAL_DUNGEON = new NostrumDungeon(
			TAG_PORTAL,
			new DungeonStartRoom(NostrumDungeonRooms.PORTAL_LOBBY,
					NostrumDungeonRooms.PORTAL_ENTRANCE,
					NostrumDungeonRooms.RefExtendedEntranceStaircaseLight),
			NostrumDungeonRooms.PORTAL_ENDROOM
			).setColor(0x80402080)
			.setLootTable(NostrumMagica.Loc("nostrum_shrine_room"))
			;

	@SubscribeEvent
	public static final void onRoomRegistration(RegistryEvent.Register<Dungeon> event) {
		final IForgeRegistry<Dungeon> registry = event.getRegistry();
		
		registry.register(DRAGON_DUNGEON.setRegistryName(ID_DRAGON_DUNGEON));
		registry.register(PORTAL_DUNGEON.setRegistryName(ID_PORTAL_DUNGEON));
		registry.register(PLANTBOSS_DUNGEON.setRegistryName(ID_PLANTBOSS_DUNGEON));
	}
}
