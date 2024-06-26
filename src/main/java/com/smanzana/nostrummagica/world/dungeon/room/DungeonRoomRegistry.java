package com.smanzana.nostrummagica.world.dungeon.room;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint.INBTGenerator;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class DungeonRoomRegistry {
	
	public static final class DungeonRoomRecord {
		public final String name;
		public final RoomBlueprint blueprint;
		protected final int weight;
		protected final int cost;
		
		public DungeonRoomRecord(String name, RoomBlueprint blueprint, int weight, int cost) {
			this.blueprint = blueprint;
			this.weight = weight;
			this.name = name;
			this.cost = cost;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof DungeonRoomRecord) {
				DungeonRoomRecord other = (DungeonRoomRecord) o;
				return other.name.equalsIgnoreCase(name);
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			return this.name.hashCode() * 17;
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
				
				NostrumMagica.logger.info("Overriding DungeonRoom registration for entry " + record.name);
				
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
	private static final String ROOM_ROOT_NAME = "root.gat";
	private static final String ROOM_COMPRESSED_EXT = "gat";
	private static final String ROOM_ROOT_NAME_COMP = "root." + ROOM_COMPRESSED_EXT;
	
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
	
	public void register(String name, RoomBlueprint blueprint, int weight, int cost, List<String> tags) {
		DungeonRoomRecord record = new DungeonRoomRecord(name, blueprint, weight, cost);
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
		DungeonRoomRecord record = getRoomRecord(roomName);
		if (record != null) {
			return record.blueprint;
		} else {
			return null;
		}
	}
	
	@Nullable
	public DungeonRoomRecord getRoomRecord(String roomName) {
		DungeonRoomRecord ret = null;
		DungeonRoomList list = map.get(INTERNAL_ALL_NAME);
		if (list != null) {
			for (DungeonRoomRecord record : list.recordList) {
				if (record.name.equalsIgnoreCase(roomName)) {
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
	
	private final RoomBlueprint loadFromNBT(CompoundNBT nbt, boolean doRegister) {
		String name = nbt.getString(NBT_NAME);
		int weight = nbt.getInt(NBT_WEIGHT);
		
		if (name.isEmpty() || weight <= 0) {
			return null;
		}
		
		int cost = nbt.contains(NBT_COST) ? nbt.getInt(NBT_COST) : 1;
		
		RoomBlueprint blueprint = RoomBlueprint.fromNBT(nbt.getCompound(NBT_BLUEPRINT));
		if (blueprint == null) {
			return null;
		}
		
		// TODO join to master if this is a piece?
		
		List<String> tags = new LinkedList<>();
		ListNBT list = nbt.getList(NBT_TAGS, NBT.TAG_STRING);
		
		int tagCount = list.size();
		for (int i = 0; i < tagCount; i++) {
			tags.add(list.getString(i));
		}
		
		if (doRegister) {
			this.register(name, blueprint, weight, cost, tags);
		}
		
		// For version bumping
//		int unusedWarning;
//		writeRoomAsFile(blueprint, name, weight, tags);
		
		return blueprint;
	}
	
	public final RoomBlueprint loadFromNBT(CompoundNBT nbt) {
		return this.loadFromNBT(nbt, false);
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
				loadFromNBT(nbt, true);
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
				loadFromNBT(nbt, true);
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
					
					root = loadFromNBT(nbt, true);
					
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
				
				RoomBlueprint blueprint = loadFromNBT(nbt, false);
				
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
				
				RoomBlueprint blueprint = loadFromNBT(nbt, root == null);
				
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
		
		String[] fileNames = {"grand_hallway.gat",
				"portal_room.gat",
				"sorcery_lobby.gat",
				"plant_boss_room.gat",
				"dungeon_challenge_1.gat",
				"dungeon_challenge_2.gat",
				"dungeon_challenge_3.gat",
				"dungeon_challenge_4.gat",
				"dungeon_challenge_5.gat",
				"dungeon_challenge_6.gat",
				"dungeon_challenge_7.gat",
				"dungeon_challenge_8.gat",
				"dungeon_challenge_10.gat",
				"dungeon_end_1.gat",
				"dungeon_end_2.gat",
				"plantboss_dungeon_entrance.gat",
				"plantboss_lobby.gat",
				"portal_entrance.gat",
				"portal_lobby.gat",
			};
		
		String[] compNames = {"sorcery_dungeon"};
		final int dungeon_piece_count = 260;
		String[][] compSubnames = new String[1][dungeon_piece_count + 1];
		
		InputStream[] files = new InputStream[fileNames.length];
		InputStream[][] compDirs = new InputStream[compNames.length][]; // lel wrong
		
		for (int i = 0; i <= dungeon_piece_count; i++) {
			compSubnames[0][i] = (i == 0 ? "root.gat" : (compNames[0] + "_" + i + ".gat")); 
		}
		
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
			for (int i = 0; i < compNames.length; i++) {
				compDirs[i] = new InputStream[compSubnames[i].length];
				for (int j = 0; j < compSubnames[i].length; j++) {
					String subName = compSubnames[i][j];
					String compName = compNames[i];
					
					InputStream stream = this.getClass().getResourceAsStream("/rooms/" + compName + "/" + subName);
					if (stream == null) {
						NostrumMagica.logger.fatal("Couldn't locate " + compName + "/" + subName + " in resource files");
						error = true;
						break;
					} else {
						compDirs[i][j] = stream;
					}
				}
			}
		}
		
		if (error) {
			throw new RuntimeException("Failed to find one or more required files!");
		}
		
		for (int i = 0; i < fileNames.length; i++) {
			loadFromFileStream(fileNames[i], files[i]);
		}
		
		for (int i = 0; i < compNames.length; i++) {
			loadFromCompDirStreams(compNames[i], compSubnames[i], compDirs[i]);
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
		for (File file : files) {
			loadFromFile(file);
		}
		
		for (File comp : compDirs) {
			loadFromCompDir(comp);
		}
		

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
}
