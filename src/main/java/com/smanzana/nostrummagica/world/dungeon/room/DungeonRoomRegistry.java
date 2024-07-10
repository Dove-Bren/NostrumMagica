package com.smanzana.nostrummagica.world.dungeon.room;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint.INBTGenerator;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint.LoadContext;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

public class DungeonRoomRegistry {
	
	public static final class DungeonRoomRecord {
		public final ResourceLocation id;
		public final String name;
		public final RoomBlueprint blueprint;
		protected final int weight;
		protected final int cost;
		
		public DungeonRoomRecord(ResourceLocation id, String name, RoomBlueprint blueprint, int weight, int cost) {
			this.blueprint = blueprint;
			this.weight = weight;
			this.id = id;
			this.cost = cost;
			this.name = name;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof DungeonRoomRecord) {
				DungeonRoomRecord other = (DungeonRoomRecord) o;
				return other.id.equals(id);
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			return this.id.hashCode() * 17;
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
				
				NostrumMagica.logger.info("Overriding DungeonRoom registration for entry " + record.id);
				
				DungeonRoomRecord old = recordList.get(recordList.indexOf(record));
				recordList.remove(record);
				weightSum -= old.weight;
			}
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
	private static final String ROOM_ROOT_ID = "root";
	private static final String ROOM_ROOT_NAME = ROOM_ROOT_ID + ".gat";
	private static final String ROOM_COMPRESSED_EXT = "gat";
	private static final String ROOM_ROOT_NAME_COMP = ROOM_ROOT_ID + "." + ROOM_COMPRESSED_EXT;
	
	private Map<String, DungeonRoomList> map;
	
	private DungeonRoomRegistry() {
		this.map = new HashMap<>();
		
		this.roomSaveFolder = new File("./NostrumMagicaData/dungeon_room_captures/");
		this.roomLoadFolder = new File("./NostrumMagicaData/dungeon_room_captures/");
	}
	
	private void add(String tag, DungeonRoomRecord record) {
		DungeonRoomList list = map.get(tag);
		if (list == null) {
			list = new DungeonRoomList();
			map.put(tag, list);
		}
		
		list.add(record);
	}
	
	public void clear() {
		map.clear();
	}
	
	public void register(ResourceLocation id, String name, RoomBlueprint blueprint, int weight, int cost, List<String> tags) {
		System.out.println("Registered " + id); int unused;
		DungeonRoomRecord record = new DungeonRoomRecord(id, name, blueprint, weight, cost);
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
	public RoomBlueprint getRoom(ResourceLocation roomID) {
		DungeonRoomRecord record = getRoomRecord(roomID);
		if (record != null) {
			return record.blueprint;
		} else {
			return null;
		}
	}
	
	@Nullable
	public DungeonRoomRecord getRoomRecord(ResourceLocation roomID) {
		DungeonRoomRecord ret = null;
		DungeonRoomList list = map.get(INTERNAL_ALL_NAME);
		if (list != null) {
			for (DungeonRoomRecord record : list.recordList) {
				if (record.id.equals(roomID)) {
					ret = record;
					break;
				}
			}
		}
		
		return ret;
	}
	
	public List<DungeonRoomRecord> getAllRooms() {
		return this.getAllRooms(INTERNAL_ALL_NAME);
	}
	
	public List<DungeonRoomRecord> getAllRooms(String tag) {
		DungeonRoomList list = map.get(tag);
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
	
	private void findFiles(File base, List<File> files, List<File> dirs) {
		if (base.exists()) {
			if (base.isDirectory()) {
				File[] subfiles = base.listFiles();
				
				// See if this is a composition
				for (File subfile : subfiles) {
					if (subfile.getName().equalsIgnoreCase(ROOM_ROOT_NAME)
							|| subfile.getName().equalsIgnoreCase(ROOM_ROOT_NAME_COMP)) {
						dirs.add(base);
						return;
					}
				}
				
				for (File subfile : subfiles) {
					findFiles(subfile, files, dirs);
				}
			} else {
				files.add(base);
			}
		}
	}
	
	@Deprecated
	private ResourceLocation makeIDFor(String name) {
		int unused; // REMOVE
		if (name.endsWith(".gat")) {
			name = name.substring(0, name.length() - 4);
		}
		return NostrumMagica.Loc(name);
	}
	
	@Deprecated
	private ResourceLocation makeIDFor(File file) {
		return makeIDFor(file.getName());
	}
	
	private void loadFromFile(File file) {
		try {
			long startTime = System.currentTimeMillis();
			long time;
			
			//ProgressBar bar = ProgressManager.push("Reading Room", 1);
			//bar.step(file.getName());
			CompoundNBT nbt;
			if (file.getName().endsWith(ROOM_COMPRESSED_EXT)) {
				nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));
			} else {
				nbt = CompressedStreamTools.read(file);
			}
			
			//ProgressManager.pop(bar);
			
			time = System.currentTimeMillis() - startTime;
			if (time > 100) {
				NostrumMagica.logger.warn("Took " + time + "ms to read " + file.getName());
			}
			
			startTime = System.currentTimeMillis();
			if (nbt != null) {
				loadAndRegisterFromNBT(new LoadContext(file.getAbsolutePath()), makeIDFor(file), nbt);
			}
			
			time = System.currentTimeMillis() - startTime;
			if (time > 100) {
				NostrumMagica.logger.warn("Took " + time + "ms to load " + file.getName());
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
			NostrumMagica.logger.error("Failed to load room from " + file.toString());
		}
	}
	
	private void loadFromFileStream(String name, InputStream stream) {
		try {
			long startTime = System.currentTimeMillis();
			long time;
			
			//ProgressBar bar = ProgressManager.push("Reading Room", 1);
			//bar.step(name);
			CompoundNBT nbt;
			if (name.endsWith(ROOM_COMPRESSED_EXT)) {
				nbt = CompressedStreamTools.readCompressed(stream);
			} else {
				nbt = CompressedStreamTools.read(new DataInputStream(stream));
			}
			
			//ProgressManager.pop(bar);
			
			time = System.currentTimeMillis() - startTime;
			if (time > 100) {
				NostrumMagica.logger.warn("Took " + time + "ms to read " + name);
			}
			
			startTime = System.currentTimeMillis();
			if (nbt != null) {
				loadAndRegisterFromNBT(new LoadContext(name), makeIDFor(name), nbt);
			}
			
			time = System.currentTimeMillis() - startTime;
			if (time > 100) {
				NostrumMagica.logger.warn("Took " + time + "ms to load " + name);
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
			NostrumMagica.logger.error("Failed to load room from " + name);
		}
	}
	
	private void loadFromCompDir(File dir) {
		// Look for root, load it, and then load everything else in and join them
		try {
			long startTime = System.currentTimeMillis();
			long time;
			RoomBlueprint root = null;
			File rootFile = null;
			
			File[] subfiles = dir.listFiles();
			
			//ProgressBar bar = ProgressManager.push("Reading Room", subfiles.length);
			
			for (File subfile : subfiles) {
				if (subfile.getName().equalsIgnoreCase(ROOM_ROOT_NAME) || subfile.getName().equalsIgnoreCase(ROOM_ROOT_NAME_COMP)) {
					rootFile = subfile;
					CompoundNBT nbt;
					if (subfile.getName().endsWith(ROOM_COMPRESSED_EXT)) {
						nbt = CompressedStreamTools.readCompressed(new FileInputStream(subfile));
					} else {
						nbt = CompressedStreamTools.read(subfile);
					}
					
					time = System.currentTimeMillis() - startTime;
					if (time > 100) {
						NostrumMagica.logger.warn("Took " + time + "ms to read " + dir.getName() + "/" + subfile.getName());
					}
					startTime = System.currentTimeMillis();
					
					root = loadAndRegisterFromNBT(new LoadContext(subfile.getAbsolutePath()), makeIDFor(subfile), nbt);
					
					time = System.currentTimeMillis() - startTime;
					if (time > 100) {
						NostrumMagica.logger.warn("Took " + time + "ms to load " + dir.getName() + "/" + subfile.getName());
					}
					startTime = System.currentTimeMillis();
					
					//bar.step(subfile.getName());
				}
			}
			
			if (root == null) {
				NostrumMagica.logger.fatal("Failed to load root for " + dir.getName());
				return;
			}
			
			// Load up all pieces and join
			for (File subfile : subfiles) {
				if (subfile == rootFile) {
					continue;
				}
				
				CompoundNBT nbt;
				if (subfile.getName().endsWith(ROOM_COMPRESSED_EXT)) {
					nbt = CompressedStreamTools.readCompressed(new FileInputStream(subfile));
				} else {
					nbt = CompressedStreamTools.read(subfile);
				}
				
				time = System.currentTimeMillis() - startTime;
				if (time > 100) {
					NostrumMagica.logger.warn("Took " + time + "ms to read " + dir.getName() + "/" + subfile.getName());
				}
				startTime = System.currentTimeMillis();
				
				RoomBlueprint blueprint = loadRoomFromNBT(new LoadContext(subfile.getAbsolutePath()), nbt);
				
				time = System.currentTimeMillis() - startTime;
				if (time > 100) {
					NostrumMagica.logger.warn("Took " + time + "ms to load " + dir.getName() + "/" + subfile.getName());
				}
				startTime = System.currentTimeMillis();
				
				root.join(blueprint);
				
				time = System.currentTimeMillis() - startTime;
				if (time > 100) {
					NostrumMagica.logger.warn("Took " + time + "ms to merge in " + dir.getName() + "/" + subfile.getName());
				}
				startTime = System.currentTimeMillis();
				
				//bar.step(subfile.getName());
			}
			
			//ProgressManager.pop(bar);
		} catch (IOException e) {
			e.printStackTrace();
			NostrumMagica.logger.error("Failed to load complex room from " + dir.toString());
		}
	}
	
	private void loadFromCompDirStreams(String compName, String[] fileNames, InputStream[] streams) {
		// Root is first stream
		try {
			long startTime = System.currentTimeMillis();
			long time;
			RoomBlueprint root = null;
			//ProgressBar bar = ProgressManager.push("Reading Room", streams.length);
			
			CompoundNBT nbt;
			
			if (fileNames.length != streams.length) {
				throw new RuntimeException("Number of streams doesn't match number of files");
			}
			
			for (int i = 0; i < fileNames.length; i++) {
				if (fileNames[i].endsWith(ROOM_COMPRESSED_EXT)) {
					nbt = CompressedStreamTools.readCompressed(streams[i]);
				} else {
					nbt = CompressedStreamTools.read(new DataInputStream(streams[i]));
				}
				
				time = System.currentTimeMillis() - startTime;
				if (time > 100) {
					NostrumMagica.logger.warn("Took " + time + "ms to read " + compName + "/" + fileNames[i]);
				}
				startTime = System.currentTimeMillis();
				
				RoomBlueprint blueprint;
				if (root == null) {
					blueprint = loadAndRegisterFromNBT(new LoadContext(compName + "/" + fileNames[i]), makeIDFor(compName), nbt);
				} else {
					blueprint = loadRoomFromNBT(new LoadContext(compName + "/" + fileNames[i]), nbt);
				}
				
				time = System.currentTimeMillis() - startTime;
				if (time > 100) {
					NostrumMagica.logger.warn("Took " + time + "ms to load " + compName + "/" + fileNames[i]);
				}
				startTime = System.currentTimeMillis();
				
				if (root == null) {
					root = blueprint;
				} else {
					root.join(blueprint);
				}
				
				//bar.step(fileNames[i]);
			}
			
			if (root == null) {
				NostrumMagica.logger.fatal("Failed to load root for " + compName);
				return;
			}
			
			//ProgressManager.pop(bar);
		} catch (IOException e) {
			e.printStackTrace();
			NostrumMagica.logger.error("Failed to load complex room from " + compName);
		}
	}
	
	public void loadRegistryFromBuiltin() {
		// TODO make this be dynamic. Eitehr add manifest at build time to jar, or do the gross iteration of the jar stuff.
		
		String[] fileNames = {
				"portal_room.gat",
				"plantboss_dungeon_entrance.gat",
				"plantboss_lobby.gat",
				"portal_entrance.gat",
				"portal_lobby.gat",
				"plant_boss_room.gat",
//				"sorcery_lobby.gat",
//				"dungeon_challenge_1.gat",
//				"dungeon_challenge_2.gat",
//				"dungeon_challenge_3.gat",
//				"dungeon_challenge_4.gat",
//				"dungeon_challenge_5.gat",
//				"dungeon_challenge_6.gat",
//				"dungeon_challenge_7.gat",
//				"dungeon_challenge_8.gat",
//				"dungeon_challenge_10.gat",
//				"dungeon_end_1.gat",
//				"dungeon_end_2.gat",
//				"dungeon_door.gat",
//				"dungeon_end_key_1.gat",
//				"dungeon_end_key_fight_1.gat",
//				"dungeon_end_key_water_1.gat",
//				"dungeon_dmg_challenge1.gat",
//				"dungeon_vstair_1.gat",
//				"dungeon_vstair_2.gat",
//				"dungeon_vstair_3.gat",
//				"grand_hallway.gat",
			};
		
		//String[] compNames = {"sorcery_dungeon"};
		//final int dungeon_piece_count = 260;
		//String[][] compSubnames = new String[1][dungeon_piece_count + 1];
		
		InputStream[] files = new InputStream[fileNames.length];
		//InputStream[][] compDirs = new InputStream[compNames.length][]; // lel wrong
		
//		for (int i = 0; i <= dungeon_piece_count; i++) {
//			compSubnames[0][i] = (i == 0 ? "root.gat" : (compNames[0] + "_" + i + ".gat")); 
//		}
		
		boolean error = false;
		for (int i = 0; i < fileNames.length; i++) {
			String filename = fileNames[i];
			InputStream stream = this.getClass().getResourceAsStream("/rooms/" + filename);
			if (stream == null) {
				NostrumMagica.logger.fatal("Couldn't locate " + filename + " in resource files");
				error = true;
				break;
			} else {
				files[i] = stream;
			}
		}
		
		if (!error) {
//			for (int i = 0; i < compNames.length; i++) {
//				compDirs[i] = new InputStream[compSubnames[i].length];
//				for (int j = 0; j < compSubnames[i].length; j++) {
//					String subName = compSubnames[i][j];
//					String compName = compNames[i];
//					
//					InputStream stream = this.getClass().getResourceAsStream("/rooms/" + compName + "/" + subName);
//					if (stream == null) {
//						NostrumMagica.logger.fatal("Couldn't locate " + compName + "/" + subName + " in resource files");
//						error = true;
//						break;
//					} else {
//						compDirs[i][j] = stream;
//					}
//				}
//			}
		}
		
		if (error) {
			throw new RuntimeException("Failed to find one or more required files!");
		}
		
		for (int i = 0; i < fileNames.length; i++) {
			loadFromFileStream(fileNames[i], files[i]);
		}
		
//		for (int i = 0; i < compNames.length; i++) {
//			loadFromCompDirStreams(compNames[i], compSubnames[i], compDirs[i]);
//		}
	}
	
	private void loadFromFiles(List<File> files) {
		for (File file : files) {
			loadFromFile(file);
		}
	}
	
	private void loadFromCompDirs(List<File> dirs) {
		for (File comp : dirs) {
			loadFromCompDir(comp);
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
		
		int count = 0;
		long startTime = System.currentTimeMillis();
		loadRegistryFromBuiltin();
		
		DungeonRoomList list = map.get(INTERNAL_ALL_NAME);
		if (list != null)
			count = list.recordList.size();
		
		NostrumMagica.logger.info("Loaded " + count + " builtin rooms (" + (((double)(System.currentTimeMillis() - startTime) / 1000D)) + " seconds)");
		
		List<File> files = new LinkedList<>();
		List<File> compDirs = new LinkedList<>();
		findFiles(this.roomLoadFolder, files, compDirs);
		
		NostrumMagica.logger.info("Loading room cache overrides (" + files.size() + " simple rooms, " + compDirs.size() + " compound rooms)...");
		startTime = System.currentTimeMillis();
		loadFromFiles(files);
		
		loadFromCompDirs(compDirs);
		

		list = map.get(INTERNAL_ALL_NAME);
		if (list != null)
			count = list.recordList.size() - count;
		
		NostrumMagica.logger.info("Loaded " + count + " room overrides (" + (((double)(System.currentTimeMillis() - startTime) / 1000D)) + " seconds)");
	}
	
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
			NostrumMagica.logger.info("Loading dungeon room templates from {} resources", data.size());
			long start;
			long now;
			final DungeonRoomRegistry loader = DungeonRoomRegistry.instance();
			
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
			final DungeonRoomRegistry loader = DungeonRoomRegistry.instance();
			
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
			NostrumMagica.logger.info("Loading {} dungeon room compositions", data.size());
			int pieceCount = 0;
			
			for (Entry<ResourceLocation, Map<String, CompoundNBT>> entry : data.entrySet()) {
				final ResourceLocation comp = entry.getKey();
				final Map<String, CompoundNBT> compMap = entry.getValue();
				pieceCount += compMap.size();
				loadComp(comp, compMap, resourceManagerIn, profilerIn);
			}
			
			NostrumMagica.logger.info("Loaded {} dungeon room compositions from {} pieces", data.size(), pieceCount);
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
	
	public static class DungeonRoomReloadListener extends ReloadListener<ReloadListenerData> {
		
		private final RoomReloadListener roomListener;
		private final RoomCompReloadListener compListener;
		
		public DungeonRoomReloadListener(String folder) {
			roomListener = new RoomReloadListener(folder);
			compListener = new RoomCompReloadListener(folder);
		}

		@Override
		protected ReloadListenerData prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
			final ReloadListenerData data = new ReloadListenerData();
			final DungeonRoomRegistry loader = DungeonRoomRegistry.instance();
			
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
		}
	}
}
