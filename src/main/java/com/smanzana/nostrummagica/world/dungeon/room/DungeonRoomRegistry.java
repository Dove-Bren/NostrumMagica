package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class DungeonRoomRegistry {
	
	public static final class DungeonRoomRegisterEvent extends Event {
		private final DungeonRoomRegistry registry;
		
		public DungeonRoomRegisterEvent(DungeonRoomRegistry registry) {
			this.registry = registry;
		}
		
		public DungeonRoomRegistry getRegistry() {
			return this.registry;
		}
	}
	
	private static DungeonRoomRegistry Instance = new DungeonRoomRegistry();
	public static DungeonRoomRegistry GetInstance() {
		return Instance;
	}
	
	private final Map<ResourceLocation, IDungeonRoom> registry = new HashMap<>();
	public @Nullable IDungeonRoom getRegisteredRoom(ResourceLocation ID) {
		return registry.get(ID);
	}
	
	public void register(IDungeonRoom room) {
		if (registry.containsKey(room.getRoomID())) {
			throw new RuntimeException("Duplicate dungeon rooms registered");
		}
		
		registry.put(room.getRoomID(), room);
	}
	
	public final void reload() {
		this.registry.clear();
		MinecraftForge.EVENT_BUS.post(new DungeonRoomRegisterEvent(this));
	}

}
