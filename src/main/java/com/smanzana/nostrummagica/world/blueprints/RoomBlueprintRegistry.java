package com.smanzana.nostrummagica.world.blueprints;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.util.AutoReloadListener;
import com.smanzana.nostrummagica.util.NBTReloadListener;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint.INBTGenerator;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint.LoadContext;
import com.smanzana.nostrummagica.world.dungeon.room.BlueprintDungeonRoom;
import com.smanzana.nostrummagica.world.dungeon.room.StaticRoom;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

public class RoomBlueprintRegistry {
	
	public static final class RoomBlueprintRecord {
		public final ResourceLocation id;
		public final String name;
		public final RoomBlueprint blueprint;
		protected final int weight;
		protected final int cost;
		
		public RoomBlueprintRecord(ResourceLocation id, String name, RoomBlueprint blueprint, int weight, int cost) {
			this.blueprint = blueprint;
			this.weight = weight;
			this.id = id;
			this.cost = cost;
			this.name = name;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof RoomBlueprintRecord) {
				RoomBlueprintRecord other = (RoomBlueprintRecord) o;
				return other.id.equals(id);
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			return this.id.hashCode() * 17;
		}
	}
	
	private static final class RoomBlueprintList {
		private List<RoomBlueprintRecord> recordList;
		private int weightSum;
		
		public RoomBlueprintList() {
			recordList = new LinkedList<>();
			weightSum = 0;
		}
		
		public void add(RoomBlueprintRecord record) {
			if (recordList.contains(record)) {
				
				NostrumMagica.logger.info("Overriding RoomBlueprint registration for entry " + record.id);
				
				RoomBlueprintRecord old = recordList.get(recordList.indexOf(record));
				recordList.remove(record);
				weightSum -= old.weight;
			}
			recordList.add(record);
			weightSum += record.weight;
		}
	}
	
	private static RoomBlueprintRegistry instance = null;
	public static RoomBlueprintRegistry instance() {
		if (instance == null) {
			instance = new RoomBlueprintRegistry();
		}
		
		return instance;
	}
	
	private static final String INTERNAL_ALL_NAME = "all";
	private static final String ROOM_ROOT_ID = "root";
	private static final String ROOM_ROOT_NAME = ROOM_ROOT_ID + ".gat";
	private static final String ROOM_COMPRESSED_EXT = "gat";
	
	private Map<String, RoomBlueprintList> map;
	
	private RoomBlueprintRegistry() {
		this.map = new HashMap<>();
		
		this.roomSaveFolder = new File("./NostrumMagicaData/room_blueprint_captures/");
		this.roomLoadFolder = new File("./NostrumMagicaData/room_blueprint_captures/");
	}
	
	private void add(String tag, RoomBlueprintRecord record) {
		RoomBlueprintList list = map.get(tag);
		if (list == null) {
			list = new RoomBlueprintList();
			map.put(tag, list);
		}
		
		list.add(record);
	}
	
	public void clear() {
		map.clear();
	}
	
	public void register(ResourceLocation id, String name, RoomBlueprint blueprint, int weight, int cost, List<String> tags) {
		RoomBlueprintRecord record = new RoomBlueprintRecord(id, name, blueprint, weight, cost);
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
		RoomBlueprintList list = map.get(tag);
		if (list != null) {
			int idx = NostrumMagica.rand.nextInt(list.weightSum);
			for (RoomBlueprintRecord record : list.recordList) {
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
	public RoomBlueprint getRoom(ResourceLocation roomID) {
		RoomBlueprintRecord record = getRoomRecord(roomID);
		if (record != null) {
			return record.blueprint;
		} else {
			return null;
		}
	}
	
	@Nullable
	public RoomBlueprintRecord getRoomRecord(ResourceLocation roomID) {
		RoomBlueprintRecord ret = null;
		RoomBlueprintList list = map.get(INTERNAL_ALL_NAME);
		if (list != null) {
			for (RoomBlueprintRecord record : list.recordList) {
				if (record.id.equals(roomID)) {
					ret = record;
					break;
				}
			}
		}
		
		return ret;
	}
	
	public List<RoomBlueprintRecord> getAllRooms() {
		return this.getAllRooms(INTERNAL_ALL_NAME);
	}
	
	public List<RoomBlueprintRecord> getAllRooms(String tag) {
		RoomBlueprintList list = map.get(tag);
		List<RoomBlueprintRecord> ret;
		
		if (list != null) {
			ret = new ArrayList<>(list.recordList.size());
			for (RoomBlueprintRecord record : list.recordList) {
				ret.add(record);
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
	private static final String NBT_COST = "cost";
	
	protected static final CompoundNBT toNBT(CompoundNBT blueprintTag, String name, int weight, int cost, List<String> tags) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString(NBT_NAME, name);
		nbt.putInt(NBT_WEIGHT, weight);
		nbt.putInt(NBT_COST, cost);
		nbt.put(NBT_BLUEPRINT, blueprintTag);
		
		ListNBT list = new ListNBT();
		for (String tag : tags) {
			list.add(StringNBT.valueOf(tag));
		}
		nbt.put(NBT_TAGS, list);
		
		return nbt;
	}
	
	private final RoomBlueprint loadRoomFromNBT(LoadContext context, CompoundNBT nbt) {
		String name = nbt.getString(NBT_NAME);
		int weight = nbt.getInt(NBT_WEIGHT);
		
		if (name.isEmpty() || weight <= 0) {
			return null;
		}
		
		context.name = name;
		
		
		RoomBlueprint blueprint = RoomBlueprint.fromNBT(context, nbt.getCompound(NBT_BLUEPRINT));
		return blueprint;
	}
	
	public final RoomBlueprint loadAndRegisterFromNBT(LoadContext context, ResourceLocation id, CompoundNBT nbt) {
		RoomBlueprint blueprint = loadRoomFromNBT(context, nbt);
		
		if (blueprint == null) {
			return null;
		}
		
		String name = nbt.getString(NBT_NAME);
		int weight = nbt.getInt(NBT_WEIGHT);
		int cost = nbt.contains(NBT_COST) ? nbt.getInt(NBT_COST) : 1;
		
		// TODO join to master if this is a piece?
		
		List<String> tags = new LinkedList<>();
		ListNBT list = nbt.getList(NBT_TAGS, NBT.TAG_STRING);
		
		int tagCount = list.size();
		for (int i = 0; i < tagCount; i++) {
			tags.add(list.getString(i));
		}
		
		this.register(id, name, blueprint, weight, cost, tags);
		
		// For version bumping
//		int unusedWarning;
//		writeRoomAsFile(blueprint, id, weight, tags);
		
		return blueprint;
	}
	
	public final File roomLoadFolder;
	public final File roomSaveFolder;
	
	private final boolean writeRoomAsFileInternal(File saveFile, CompoundNBT blueprintTag, String name, int weight, int cost, List<String> tags) {
		boolean success = true;
		
		try {
			CompressedStreamTools.writeCompressed(toNBT(blueprintTag, name, weight, cost, tags), new FileOutputStream(saveFile));
			//CompressedStreamTools.safeWrite(toNBT(blueprintTag, name, weight, tags), saveFile);
		} catch (IOException e) {
			e.printStackTrace();
			
			System.out.println("Failed to write out serialized file " + saveFile.getAbsolutePath());
			NostrumMagica.logger.error("Failed to write room to " + saveFile.getAbsolutePath());
			success = false;
		}
		
		return success;
	}
	
	public final boolean writeRoomAsFile(RoomBlueprint blueprint, String name, int weight, int cost, List<String> tags) {
		boolean success = true;
		String path = null;
		
		if (blueprint.shouldSplit()) {
			File baseDir = new File(this.roomSaveFolder, name);
			if (!baseDir.mkdirs()) {
				throw new RuntimeException("Failed to create directories for complex room: " + baseDir.getPath());
			}
			
			INBTGenerator gen = blueprint.toNBTWithBreakdown();
			NostrumMagica.logger.info("Writing complex room " + name + " as " + gen.getTotal() + " pieces");
			
			for (int i = 0; gen.hasNext(); i++) {
				String fileName;
				CompoundNBT nbt = gen.next();
				if (i == 0) {
					// Root room has extra info and needs to be identified
					fileName = ROOM_ROOT_NAME;
				} else {
					fileName = name + "_" + i + "." + ROOM_COMPRESSED_EXT;
				}
				
				File outFile = new File(baseDir, fileName);
				success = writeRoomAsFileInternal(outFile, nbt, name, weight, cost, tags);
				path = outFile.getPath();
			}
		} else {
			File outFile = new File(this.roomSaveFolder, name + "." + ROOM_COMPRESSED_EXT);
			success = writeRoomAsFileInternal(outFile,
					blueprint.toNBT(),
					name, weight, cost, tags);
			path = outFile.getPath();
		}
		
		if (success) {
			NostrumMagica.logger.info("Room written to " + path);
		}
		
		return success;
	}
	
	private static class RoomReloadListener extends NBTReloadListener {
		
		public RoomReloadListener(String folder) {
			super(folder, "gat", true);
		}
		
		@Override
		public void apply(Map<ResourceLocation, CompoundNBT> data, IResourceManager resourceManagerIn, IProfiler profilerIn) {
			NostrumMagica.logger.info("Loading room blueprints from {} resources", data.size());
			long start;
			long now;
			final RoomBlueprintRegistry loader = RoomBlueprintRegistry.instance();
			
			for (Entry<ResourceLocation, CompoundNBT> entry : data.entrySet()) {
				final LoadContext context = new LoadContext(entry.getKey().toString());
				
				start = System.currentTimeMillis();
				loader.loadAndRegisterFromNBT(context, entry.getKey(), entry.getValue());
				now = System.currentTimeMillis();
				
				if (now - start > 100) {
					NostrumMagica.logger.warn("Took " + (now-start) + "ms to read " + entry.getKey());
				}
			}
			
		}
	}
	
	private static class RoomCompReloadListener extends AutoReloadListener<Map<ResourceLocation, Map<String, CompoundNBT>>> {
		
		public RoomCompReloadListener(String folder) {
			super(folder, "cmp");
		}
		
		@Override
		protected Map<ResourceLocation, Map<String, CompoundNBT>> prepareResource(Map<ResourceLocation, Map<String, CompoundNBT>> builder, ResourceLocation location, InputStream input) throws IOException, IllegalStateException {
			if (builder == null) {
				builder = new HashMap<>();
			}
			
			// Figure out the folder path
			String compPath = getCompPath(location.getPath());
			String subpath;
			if (!compPath.isEmpty()) {
				// Trim location down to comp path, and keep track of subpath
				subpath = location.getPath().substring(compPath.length() + 1);
				location = new ResourceLocation(location.getNamespace(), compPath);
			} else {
				subpath = location.getPath();
				location = new ResourceLocation(location.getNamespace(), "");
			}
			
			// Read NBT
			CompoundNBT tag = CompressedStreamTools.readCompressed(input);
			
			// Add to map
			CompoundNBT existing = builder.computeIfAbsent(location, p -> new HashMap<>())
				.put(subpath, tag);
			
			if (existing != null) {
				throw new IllegalStateException("Duplicate data file ignored with ID " + location + "/" + subpath);
			}
			
			return builder;
		}
		
		@Override
		protected Map<ResourceLocation, Map<String, CompoundNBT>> checkPreparedData(Map<ResourceLocation, Map<String, CompoundNBT>> data, IResourceManager resourceManagerIn, IProfiler profilerIn) {
			// Verify each path has a root
			Iterator<Entry<ResourceLocation, Map<String, CompoundNBT>>> it = data.entrySet().iterator();
			while (it.hasNext()) {
				Entry<ResourceLocation, Map<String, CompoundNBT>> entry = it.next();
				final ResourceLocation compName = entry.getKey();
				final Map<String, CompoundNBT> compData = entry.getValue();
				if (!compData.containsKey(ROOM_ROOT_ID)) {
					NostrumMagica.logger.error("Failed to find root for room composition: " + compName);
					it.remove();
				}
			}
			
			return data;
		}
		
		protected void loadComp(ResourceLocation comp, Map<String, CompoundNBT> data, IResourceManager resourceManagerIn, IProfiler profilerIn) {
			long start;
			long now;
			final RoomBlueprintRegistry loader = RoomBlueprintRegistry.instance();
			
			// Verification above means we should always have a root. Load that directly as first room
			LoadContext context = new LoadContext(comp.toString(), ROOM_ROOT_ID);
			start = System.currentTimeMillis();
			RoomBlueprint root = loader.loadAndRegisterFromNBT(context, comp, data.get(ROOM_ROOT_ID));
			now = System.currentTimeMillis();
			if ((now-start) > 100) {
				NostrumMagica.logger.warn("Took " + (now-start) + "ms to load root for " + comp);
			}
			
			if (root == null) {
				NostrumMagica.logger.error("Failed to load root for composite room " + comp);
			} else {
				for (Entry<String, CompoundNBT> compRow : data.entrySet()) {
					if (compRow.getKey().equalsIgnoreCase(ROOM_ROOT_ID)) {
						continue; // handled outside of loop
					}
					
					context = new LoadContext(comp.toString(), compRow.getKey());
					
					start = System.currentTimeMillis();
					RoomBlueprint piece = loader.loadRoomFromNBT(context, compRow.getValue());
					now = System.currentTimeMillis();
					
					if (now - start > 100) {
						NostrumMagica.logger.warn("Took " + (now-start) + "ms to read " + compRow.getKey());
					}
					
					start = System.currentTimeMillis();
					root = root.join(piece);
					now = System.currentTimeMillis();
					if ((now-start) > 100) {
						NostrumMagica.logger.warn("Took " + (now-start) + "ms to merge in " + comp + "/" + compRow.getKey());
					}
				}
			}
		}

		@Override
		public void apply(Map<ResourceLocation, Map<String, CompoundNBT>> data, IResourceManager resourceManagerIn, IProfiler profilerIn) {
			// For each comp grouping...
			NostrumMagica.logger.info("Loading {} room blueprint compositions", data.size());
			int pieceCount = 0;
			
			for (Entry<ResourceLocation, Map<String, CompoundNBT>> entry : data.entrySet()) {
				final ResourceLocation comp = entry.getKey();
				final Map<String, CompoundNBT> compMap = entry.getValue();
				pieceCount += compMap.size();
				loadComp(comp, compMap, resourceManagerIn, profilerIn);
			}
			
			NostrumMagica.logger.info("Loaded {} room blueprint compositions from {} pieces", data.size(), pieceCount);
		}
		
		/**
		 * Return the "composition" path. This is the whole path up to the actual filename.
		 * For example, "testcomp/comp1.cat" would return "testcomp".
		 * "mycomps/testcomp/comp1.cat" would return "mycomps/testcomp".
		 * This lets compositions still be organized by folder.
		 * @param path
		 * @return
		 */
		protected String getCompPath(String path) {
			path = path.replace('\\', '/');
			int idx = path.lastIndexOf('/');
			if (idx == -1) {
				return "";
			} else {
				return path.substring(0, idx);
			}
		}
	}
	
	private static final class ReloadListenerData {
		public Map<ResourceLocation, CompoundNBT> roomData;
		public Map<ResourceLocation, Map<String, CompoundNBT>> compData;
	}
	
	public static class RoomBlueprintReloadListener extends ReloadListener<ReloadListenerData> {
		
		private final RoomReloadListener roomListener;
		private final RoomCompReloadListener compListener;
		
		public RoomBlueprintReloadListener(String folder) {
			roomListener = new RoomReloadListener(folder);
			compListener = new RoomCompReloadListener(folder);
		}

		@Override
		protected ReloadListenerData prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
			final ReloadListenerData data = new ReloadListenerData();
			final RoomBlueprintRegistry loader = RoomBlueprintRegistry.instance();
			
			// The reason I am writing this class: clear out the loader once before any type of room loading
			loader.clear();
			
			// This serializes these two operations instead of them happening in parallel :(
			data.roomData = this.roomListener.prepare(resourceManagerIn, profilerIn);
			data.compData = this.compListener.prepare(resourceManagerIn, profilerIn);
			
			return data;
		}

		@Override
		protected void apply(ReloadListenerData data, IResourceManager resourceManagerIn, IProfiler profilerIn) {
			this.roomListener.apply(data.roomData, resourceManagerIn, profilerIn);
			this.compListener.apply(data.compData, resourceManagerIn, profilerIn);
			
			final RoomBlueprintRegistry loader = RoomBlueprintRegistry.instance();
			{
				// After loading/registering the blueprints, register dungeon rooms for each
				// TODO remove this and move somewhere else and make this be the BlueprintLoader!!!!
				for (RoomBlueprintRecord blueprintRecord : loader.getAllRooms()) {
					new BlueprintDungeonRoom(blueprintRecord.id);
				}
				
				// Hack in support for registering statics here too
				StaticRoom.RegisterStaticRooms();
			}
		}
	}
}
