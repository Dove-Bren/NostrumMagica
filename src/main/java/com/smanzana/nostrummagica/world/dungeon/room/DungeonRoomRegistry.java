package com.smanzana.nostrummagica.world.dungeon.room;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

public class DungeonRoomRegistry {
	
	private static final class DungeonRoomRecord {
		public RoomBlueprint blueprint;
		public int weight;
		public String name;
		
		public DungeonRoomRecord(String name, RoomBlueprint blueprint, int weight) {
			this.blueprint = blueprint;
			this.weight = weight;
			this.name = name;
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
			recordList.add(record);
			weightSum += record.weight;
		}
	}
	
	private static DungeonRoomRegistry instance = null;
	public static DungeonRoomRegistry instance() {
		if (instance == null) {
			instance = new DungeonRoomRegistry();
		}
		
		return instance;
	}
	
	private static final String INTERNAL_ALL_NAME = "all";
	
	private Map<String, DungeonRoomList> map;
	
	private DungeonRoomRegistry() {
		this.map = new HashMap<>();
		
		this.roomSaveFolder = new File(ModConfig.config.base.getConfigFile().getParentFile(), "NostrumMagica/dungeon_room_captures/");
		this.roomLoadFolder = new File(ModConfig.config.base.getConfigFile().getParentFile(), "NostrumMagica/dungeon_rooms/");
	}
	
	private void add(String tag, DungeonRoomRecord record) {
		DungeonRoomList list = map.get(tag);
		if (list == null) {
			list = new DungeonRoomList();
			map.put(tag, list);
		}
		
		list.add(record);
	}
	
	public void register(String name, RoomBlueprint blueprint, int weight, List<String> tags) {
		DungeonRoomRecord record = new DungeonRoomRecord(name, blueprint, weight);
		add(INTERNAL_ALL_NAME, record);
		for (String tag : tags) {
			add(tag, record);
		}
	}
	
	@Nullable
	public RoomBlueprint getRandomRoom() {
		return getRandomRoom(INTERNAL_ALL_NAME);
	}
	
	@Nullable
	public RoomBlueprint getRandomRoom(String tag) {
		RoomBlueprint ret = null;
		DungeonRoomList list = map.get(tag);
		if (list != null) {
			int idx = NostrumMagica.rand.nextInt(list.weightSum);
			for (DungeonRoomRecord record : list.recordList) {
				idx -= record.weight;
				if (idx < 0) {
					ret = record.blueprint;
					break;
				}
			}
		}
		
		return ret;
	}
	
	@Nullable
	public RoomBlueprint getRoom(String roomName) {
		RoomBlueprint ret = null;
		DungeonRoomList list = map.get(INTERNAL_ALL_NAME);
		if (list != null) {
			for (DungeonRoomRecord record : list.recordList) {
				if (record.name.equalsIgnoreCase(roomName)) {
					ret = record.blueprint;
					break;
				}
			}
		}
		
		return ret;
	}
	
	public List<RoomBlueprint> getAllRooms() {
		return this.getAllRooms(INTERNAL_ALL_NAME);
	}
	
	public List<RoomBlueprint> getAllRooms(String tag) {
		DungeonRoomList list = map.get(tag);
		List<RoomBlueprint> ret;
		
		if (list != null) {
			ret = new ArrayList<>(list.recordList.size());
			for (DungeonRoomRecord record : list.recordList) {
				ret.add(record.blueprint);
				// TODO use weight...
			}
		} else {
			ret = new LinkedList<>();
		}
		
		return ret;
	}
	
	private static final String NBT_BLUEPRINT = "blueprint";
	private static final String NBT_TAGS = "tags";
	private static final String NBT_WEIGHT = "weight";
	private static final String NBT_NAME = "name";
	
	protected static final NBTTagCompound toNBT(RoomBlueprint blueprint, String name, int weight, List<String> tags) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString(NBT_NAME, name);
		nbt.setInteger(NBT_WEIGHT, weight);
		nbt.setTag(NBT_BLUEPRINT, blueprint.toNBT());
		
		NBTTagList list = new NBTTagList();
		for (String tag : tags) {
			list.appendTag(new NBTTagString(tag));
		}
		nbt.setTag(NBT_TAGS, list);
		
		return nbt;
	}
	
	private final RoomBlueprint loadFromNBT(NBTTagCompound nbt, boolean doRegister) {
		String name = nbt.getString(NBT_NAME);
		int weight = nbt.getInteger(NBT_WEIGHT);
		
		if (name.isEmpty() || weight <= 0) {
			return null;
		}
		
		RoomBlueprint blueprint = RoomBlueprint.fromNBT(nbt.getCompoundTag(NBT_BLUEPRINT));
		if (blueprint == null) {
			return null;
		}
		
		List<String> tags = new LinkedList<>();
		NBTTagList list = nbt.getTagList(NBT_TAGS, NBT.TAG_STRING);
		while (!list.hasNoTags()) {
			tags.add(((NBTTagString) list.removeTag(0)).getString());
		}
		
		if (doRegister) {
			this.register(name, blueprint, weight, tags);
		}
		
		return blueprint;
	}
	
	public final RoomBlueprint loadFromNBT(NBTTagCompound nbt) {
		return this.loadFromNBT(nbt, false);
	}
	
	public final File roomLoadFolder;
	public final File roomSaveFolder;
	
	private void findFiles(File base, List<File> files) {
		if (base.isDirectory()) {
			for (File subfile : base.listFiles()) {
				findFiles(subfile, files);
			}
		} else {
			files.add(base);
		}
	}
	
	private void loadFromFile(File file) {
		try {
			long startTime = System.currentTimeMillis();
			long time;
			
			ProgressBar bar = ProgressManager.push("Reading Room", 1);
			bar.step(file.getName());
			NBTTagCompound nbt = CompressedStreamTools.read(file);
			ProgressManager.pop(bar);
			
			time = System.currentTimeMillis() - startTime;
			//if (time > 100) {
				NostrumMagica.logger.warn("Took " + time + "ms to read " + file.getName());
			//}
			
			startTime = System.currentTimeMillis();
			if (nbt != null) {
				loadFromNBT(nbt, true);
			}
			
			time = System.currentTimeMillis() - startTime;
			//if (time > 100) {
				NostrumMagica.logger.warn("Took " + time + "ms to load " + file.getName());
			//}
		} catch (IOException e) {
			e.printStackTrace();
			NostrumMagica.logger.error("Failed to load room from " + file.toString());
		}
	}
	
	/**
	 * Attempts to load all room files from the base room directory.
	 * This recursively scans for rooms.
	 * Rooms should be written using the {@link #writeRoomAsFile(RoomBlueprint, String, int, List)} method;
	 * @param baseDir
	 */
	public void loadRegistryFromDisk() {
		this.map.clear();
		List<File> files = new LinkedList<>();
		findFiles(this.roomLoadFolder, files);
		
		NostrumMagica.logger.info("Loading room cache (" + files.size() + " rooms)...");
		final long startTime = System.currentTimeMillis();
		for (File file : files) {
			loadFromFile(file);
		}
		
		int count = 0;
		DungeonRoomList list = map.get(INTERNAL_ALL_NAME);
		if (list != null)
			count = list.recordList.size();
		
		NostrumMagica.logger.info("Loaded " + count + " rooms (" + (((double)(System.currentTimeMillis() - startTime) / 1000D)) + " seconds)");
	}
	
	public final boolean writeRoomAsFile(RoomBlueprint blueprint, String name, int weight, List<String> tags) {
		boolean success = true;
		File saveFile = new File(this.roomSaveFolder, name + ".dat");
		NBTTagCompound nbt = toNBT(blueprint, name, weight, tags);
		
		try {
			saveFile.mkdirs();
			CompressedStreamTools.safeWrite(nbt, saveFile);
			NostrumMagica.logger.info("Room written to " + saveFile.getPath());
		} catch (IOException e) {
			e.printStackTrace();
			
			System.out.println("Failed to write out serialized file " + saveFile.toString());
			NostrumMagica.logger.error("Failed to write room to " + saveFile.toString());
			success = false;
		}
		
		return success;
	}
}
