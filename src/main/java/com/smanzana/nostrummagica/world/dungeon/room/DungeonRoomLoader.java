package com.smanzana.nostrummagica.world.dungeon.room;

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

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.util.AutoReloadListener;
import com.smanzana.nostrummagica.util.NBTReloadListener;
import com.smanzana.nostrummagica.world.blueprints.Blueprint;
import com.smanzana.nostrummagica.world.blueprints.Blueprint.INBTGenerator;
import com.smanzana.nostrummagica.world.blueprints.Blueprint.LoadContext;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry.DungeonRoomRegisterEvent;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DungeonRoomLoader {
	
	private static DungeonRoomLoader instance = null;
	public static DungeonRoomLoader instance() {
		if (instance == null) {
			instance = new DungeonRoomLoader();
		}
		
		return instance;
	}
	
	private static final String ROOM_ROOT_ID = "root";
	private static final String ROOM_ROOT_NAME = ROOM_ROOT_ID + ".gat";
	private static final String ROOM_COMPRESSED_EXT = "gat";
	
	private DungeonRoomLoader() {
		
		this.roomSaveFolder = new File("./NostrumMagicaData/room_blueprint_captures/");
		this.roomLoadFolder = new File("./NostrumMagicaData/room_blueprint_captures/");
	}
	
	private static final class DungeonRoomEntry {
		public Blueprint blueprint;
		public final ResourceLocation id;
		public final String name;
		public final List<String> tags;
		public final int weight;
		public final int cost;
		
		public DungeonRoomEntry(Blueprint blueprint, ResourceLocation id, String name, List<String> tags, int weight, int cost) {
			this.blueprint = blueprint;
			this.id = id;
			this.name = name;
			this.tags = tags;
			this.weight = weight;
			this.cost = cost;
		}
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
	
	private final Blueprint loadBlueprintFromNBT(LoadContext context, CompoundNBT nbt) {
		// Get and stash name for loading debug
		String name = nbt.getString(NBT_NAME);
		context.name = name;
		
		Blueprint blueprint = Blueprint.FromNBT(context, nbt.getCompound(NBT_BLUEPRINT));
		return blueprint;
	}
	
	public final DungeonRoomEntry loadEntryFromNBT(LoadContext context, ResourceLocation id, CompoundNBT nbt) {
		Blueprint blueprint = loadBlueprintFromNBT(context, nbt);
		
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
		
		// For version bumping
		//int unusedWarning;
		//writeRoomAsFile(blueprint, name, weight, cost, tags);
		
		return new DungeonRoomEntry(blueprint, id, name, tags, weight, cost);
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
	
	public final boolean writeRoomAsFile(Blueprint blueprint, String name, int weight, int cost, List<String> tags) {
		return writeRoomAsFile(new DungeonRoomEntry(blueprint, null, name, tags, weight, cost));
	}
	
	protected final boolean writeRoomAsFile(DungeonRoomEntry entry) {
		boolean success = true;
		String path = null;
		
		if (entry.blueprint.shouldSplit()) {
			File baseDir = new File(this.roomSaveFolder, entry.name);
			if (!baseDir.mkdirs()) {
				throw new RuntimeException("Failed to create directories for complex room: " + baseDir.getPath());
			}
			
			INBTGenerator gen = entry.blueprint.toNBTWithBreakdown();
			NostrumMagica.logger.info("Writing complex room " + entry.name + " as " + gen.getTotal() + " pieces");
			
			for (int i = 0; gen.hasNext(); i++) {
				String fileName;
				CompoundNBT nbt = gen.next();
				if (i == 0) {
					// Root room has extra info and needs to be identified
					fileName = ROOM_ROOT_NAME;
				} else {
					fileName = entry.name + "_" + i + "." + ROOM_COMPRESSED_EXT;
				}
				
				File outFile = new File(baseDir, fileName);
				success = writeRoomAsFileInternal(outFile, nbt, entry.name, entry.weight, entry.cost, entry.tags);
				path = outFile.getPath();
			}
		} else {
			File outFile = new File(this.roomSaveFolder, entry.name + "." + ROOM_COMPRESSED_EXT);
			success = writeRoomAsFileInternal(outFile,
					entry.blueprint.toNBT(),
					entry.name, entry.weight, entry.cost, entry.tags);
			path = outFile.getPath();
		}
		
		if (success) {
			NostrumMagica.logger.info("Room written to " + path);
		}
		
		return success;
	}
	
	private static class RoomReloadListener extends NBTReloadListener {
		
		protected static RoomReloadListener lastInstance = null;
		
		protected final List<DungeonRoomEntry> loadedRooms;
		
		public RoomReloadListener(String folder) {
			super(folder, "gat", true);
			loadedRooms = new ArrayList<>();
			MinecraftForge.EVENT_BUS.register(this);
			
			if (lastInstance != null) {
				MinecraftForge.EVENT_BUS.unregister(lastInstance);
			}
			lastInstance = this;
		}
		
		@Override
		public void apply(Map<ResourceLocation, CompoundNBT> data, IResourceManager resourceManagerIn, IProfiler profilerIn) {
			loadedRooms.clear();
			NostrumMagica.logger.info("Loading room blueprints from {} resources", data.size());
			long start;
			long now;
			final DungeonRoomLoader loader = DungeonRoomLoader.instance();
			
			for (Entry<ResourceLocation, CompoundNBT> entry : data.entrySet()) {
				final LoadContext context = new LoadContext(entry.getKey().toString());
				
				start = System.currentTimeMillis();
				loadedRooms.add(loader.loadEntryFromNBT(context, entry.getKey(), entry.getValue()));
				now = System.currentTimeMillis();
				
				if (now - start > 100) {
					NostrumMagica.logger.warn("Took " + (now-start) + "ms to read " + entry.getKey());
				}
			}
		}
		
		@SubscribeEvent
		public final void onRoomRegistration(DungeonRoomRegisterEvent event) {
			DungeonRoomRegistry registry = event.getRegistry();
			
			// Blueprint Rooms
			for (DungeonRoomEntry entry : loadedRooms) {
				registry.register(entry.name, new BlueprintDungeonRoom(entry.id, entry.blueprint), entry.weight, entry.cost, entry.tags);;
			}
		}
	}
	
	private static class RoomCompReloadListener extends AutoReloadListener<Map<ResourceLocation, Map<String, CompoundNBT>>> {
		
		private static RoomCompReloadListener lastInstance = null;
		
		protected final List<DungeonRoomEntry> loadedRooms;
		
		public RoomCompReloadListener(String folder) {
			super(folder, "cmp");
			loadedRooms = new ArrayList<>();
			MinecraftForge.EVENT_BUS.register(this);
			
			if (lastInstance != null) {
				MinecraftForge.EVENT_BUS.unregister(lastInstance);
			}
			lastInstance = this;
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
			final DungeonRoomLoader loader = DungeonRoomLoader.instance();
			
			// Verification above means we should always have a root. Load that directly as first room
			LoadContext context = new LoadContext(comp.toString(), ROOM_ROOT_ID);
			start = System.currentTimeMillis();
			DungeonRoomEntry root = loader.loadEntryFromNBT(context, comp, data.get(ROOM_ROOT_ID));
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
					Blueprint piece = loader.loadBlueprintFromNBT(context, compRow.getValue());
					now = System.currentTimeMillis();
					
					if (now - start > 100) {
						NostrumMagica.logger.warn("Took " + (now-start) + "ms to read " + compRow.getKey());
					}
					
					start = System.currentTimeMillis();
					root.blueprint = root.blueprint.join(piece);
					now = System.currentTimeMillis();
					if ((now-start) > 100) {
						NostrumMagica.logger.warn("Took " + (now-start) + "ms to merge in " + comp + "/" + compRow.getKey());
					}
				}
				this.loadedRooms.add(root);
			}
		}

		@Override
		public void apply(Map<ResourceLocation, Map<String, CompoundNBT>> data, IResourceManager resourceManagerIn, IProfiler profilerIn) {
			// For each comp grouping...
			loadedRooms.clear();
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
		
		@SubscribeEvent
		public final void onRoomRegistration(DungeonRoomRegisterEvent event) {
			DungeonRoomRegistry registry = event.getRegistry();
			
			// Blueprint Rooms
			for (DungeonRoomEntry entry : loadedRooms) {
				registry.register(entry.name, new BlueprintDungeonRoom(entry.id, entry.blueprint), entry.weight, entry.cost, entry.tags);;
			}
		}
	}
	
	private static final class ReloadListenerData {
		public Map<ResourceLocation, CompoundNBT> roomData;
		public Map<ResourceLocation, Map<String, CompoundNBT>> compData;
	}
	
	public static class BlueprintReloadListener extends ReloadListener<ReloadListenerData> {
		
		private final RoomReloadListener roomListener;
		private final RoomCompReloadListener compListener;
		
		public BlueprintReloadListener(String folder) {
			roomListener = new RoomReloadListener(folder);
			compListener = new RoomCompReloadListener(folder);
		}

		@Override
		protected ReloadListenerData prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
			final ReloadListenerData data = new ReloadListenerData();
			
			// Note: this whole class is here so that after applying, we can trigger a dungeon room reload.
			
			// This serializes these two operations instead of them happening in parallel :(
			data.roomData = this.roomListener.prepare(resourceManagerIn, profilerIn);
			data.compData = this.compListener.prepare(resourceManagerIn, profilerIn);
			
			return data;
		}

		@Override
		protected void apply(ReloadListenerData data, IResourceManager resourceManagerIn, IProfiler profilerIn) {
			this.roomListener.apply(data.roomData, resourceManagerIn, profilerIn);
			this.compListener.apply(data.compData, resourceManagerIn, profilerIn);
			
			// This should not be here yet, but I can't find a good 'after data loading' event
			DungeonRoomRegistry.GetInstance().reload();
		}
	}
}
