package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

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
	
	public static final class DungeonRoomRecord {
		public final String name;
		public final IDungeonRoom room;
		protected final int weight;
		protected final int cost;
		
		public DungeonRoomRecord(String name, IDungeonRoom room, int weight, int cost) {
			this.room = room;
			this.weight = weight;
			this.cost = cost;
			this.name = name;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof DungeonRoomRecord) {
				DungeonRoomRecord other = (DungeonRoomRecord) o;
				return other.room.getRoomID().equals(room.getRoomID());
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			return this.room.getRoomID().hashCode() * 17;
		}
	}
	
	private static final class DungeonRoomList {
		private List<DungeonRoomRecord> recordList;
		private int weightSum;
		
		public DungeonRoomList() {
			recordList = new LinkedList<>();
			weightSum = 0;
		}
		
		public void add(DungeonRoomRecord record) {
			if (recordList.contains(record)) {
				
				NostrumMagica.logger.info("Overriding DungeonRoomList registration for entry " + record.room.getRoomID());
				
				DungeonRoomRecord old = recordList.get(recordList.indexOf(record));
				recordList.remove(record);
				weightSum -= old.weight;
			}
			recordList.add(record);
			weightSum += record.weight;
		}
	}
	
	private static final String INTERNAL_ALL_NAME = "all";
	
	private static DungeonRoomRegistry Instance = new DungeonRoomRegistry();
	public static DungeonRoomRegistry GetInstance() {
		return Instance;
	}

	private final Map<String, DungeonRoomList> tagMap;
	private final Map<ResourceLocation, DungeonRoomRecord> registry;
	
	private DungeonRoomRegistry() {
		this.tagMap = new HashMap<>();
		registry = new HashMap<>();
	}
	
	private void addToTagMap(String tag, DungeonRoomRecord record) {
		tagMap.computeIfAbsent(tag, t -> new DungeonRoomList()).add(record);
	}
	
	public void register(String name, IDungeonRoom room, int weight, int cost, List<String> tags) {
		if (registry.containsKey(room.getRoomID())) {
			throw new RuntimeException("Duplicate dungeon rooms registered: " + room.getRoomID());
		}
		
		final DungeonRoomRecord record = new DungeonRoomRecord(name, room, weight, cost);
		registry.put(room.getRoomID(), record);
		addToTagMap(INTERNAL_ALL_NAME, record);
		for (String tag : tags) {
			addToTagMap(tag, record);
		}
	}
	
	protected void clear() {
		this.registry.clear();
		this.tagMap.clear();
	}
	
	public final void reload() {
		clear();
		MinecraftForge.EVENT_BUS.post(new DungeonRoomRegisterEvent(this));
	}
	
	public @Nullable DungeonRoomRecord getRegisteredRoom(ResourceLocation ID) {
		return registry.get(ID);
	}
	
	public List<DungeonRoomRecord> getAllRooms() {
		return this.getAllRooms(INTERNAL_ALL_NAME);
	}
	
	public List<DungeonRoomRecord> getAllRooms(String tag) {
		DungeonRoomList list = tagMap.get(tag);
		List<DungeonRoomRecord> ret;
		
		if (list != null) {
			ret = new ArrayList<>(list.recordList.size());
			for (DungeonRoomRecord record : list.recordList) {
				ret.add(record);
				// TODO use weight...
			}
		} else {
			ret = new LinkedList<>();
		}
		
		return ret;
	}
	
	@Nullable
	public DungeonRoomRecord getRandomRoom() {
		return getRandomRoom(INTERNAL_ALL_NAME);
	}
	
	@Nullable
	public DungeonRoomRecord getRandomRoom(String tag) {
		DungeonRoomRecord ret = null;
		DungeonRoomList list = tagMap.get(tag);
		if (list != null) {
			int idx = NostrumMagica.rand.nextInt(list.weightSum);
			for (DungeonRoomRecord record : list.recordList) {
				idx -= record.weight;
				if (idx < 0) {
					ret = record;
					break;
				}
			}
		}
		
		return ret;
	}

}
