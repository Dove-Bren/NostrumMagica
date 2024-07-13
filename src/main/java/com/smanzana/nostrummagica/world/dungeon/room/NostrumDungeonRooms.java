package com.smanzana.nostrummagica.world.dungeon.room;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprintRegistry;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprintRegistry.RoomBlueprintRecord;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry.DungeonRoomRegisterEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumDungeonRooms {

	@SubscribeEvent
	public static final void onRoomRegistration(DungeonRoomRegisterEvent event) {
		DungeonRoomRegistry registry = event.getRegistry();
		
		// Blueprint Rooms
		for (RoomBlueprintRecord record : RoomBlueprintRegistry.instance().getAllRooms()) {
			registry.register(new BlueprintDungeonRoom(record.id));
		}
		
		// Static Rooms
		registry.register(new DragonStartRoom());
		registry.register(new RoomChallenge1());
		registry.register(new RoomChallenge2());
		registry.register(new RoomEnd1(false, false));
		registry.register(new RoomEnd1(false, true));
		registry.register(new RoomEnd1(true, false));
		registry.register(new RoomEnd1(true, true));
		registry.register(new RoomEntryDragon(false));
		registry.register(new RoomEntryStairs(false));
		registry.register(new RoomExtendedDragonStaircase(false));
		registry.register(new RoomExtendedEntranceStaircase(false));
		registry.register(new RoomGrandStaircase());
		registry.register(new RoomJail1());
		registry.register(new RoomLectern());
		registry.register(new RoomRedDragonArena());
		registry.register(new RoomTee1());

	}
}
