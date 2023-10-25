package com.smanzana.nostrummagica.world.dungeon;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.utils.WorldUtil;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoom;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Constants.NBT;

public class NostrumDungeon {
	
	public static class DungeonExitPoint {
		private Direction facing;
		private BlockPos pos;
		
		public DungeonExitPoint(BlockPos pos, Direction facing) {
			this.pos = pos;
			this.facing = facing;
		}

		public Direction getFacing() {
			return facing;
		}

		public BlockPos getPos() {
			return pos;
		}
		
		@Override
		public String toString() {
			return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")[" + facing.name() + "]";
		}
		
		private static final String NBT_POS = "pos";
		private static final String NBT_DIR = "facing";
		
		
		public CompoundNBT toNBT() {
			CompoundNBT tag = new CompoundNBT();
			tag.put(NBT_POS, NBTUtil.writeBlockPos(pos));
			tag.putByte(NBT_DIR, (byte) facing.getHorizontalIndex());
			return tag;
		}
		
		public static DungeonExitPoint fromNBT(CompoundNBT nbt) {
			final BlockPos pos;
			
			if (nbt.contains(NBT_POS, NBT.TAG_LONG)) {
				// Legacy
				// 1.13/1.14 changed BlockPos.fromLong, so have to use old version
				pos = WorldUtil.blockPosFromLong1_12_2(nbt.getLong(NBT_POS));
			} else {
				pos = NBTUtil.readBlockPos(nbt.getCompound(NBT_POS));
			}
			
			Direction facing = Direction.byHorizontalIndex(nbt.getByte(NBT_DIR));
			return new DungeonExitPoint(pos, facing);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof DungeonExitPoint) {
				DungeonExitPoint other = (DungeonExitPoint) o;
				return other.facing == this.facing && other.pos.equals(this.pos);
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			return this.facing.hashCode() * 91 + this.pos.hashCode();
		}
	}

	private static Random rand = new Random();
	private int pathLen;
	private int pathRand;
	private List<IDungeonRoom> rooms;
	protected IDungeonRoom ending;
	protected IDungeonRoom starting;
	protected NostrumDungeon self;
	
	// Cached subsets 
	private List<IDungeonRoom> endRooms;
	private List<IDungeonRoom> contRooms;
	private List<IDungeonRoom> keyRooms;
	private List<IDungeonRoom> doorRooms;
	
	// Puzzle sets per spawn run
//	private List<Path> doorPoints;
//	private List<Path> keyPoints; // Potential keys, that is
	
	public NostrumDungeon(IDungeonRoom starting, IDungeonRoom ending) {
		this(starting, ending, 2, 3);
	}
	
	public NostrumDungeon(IDungeonRoom starting, IDungeonRoom ending, int minPath, int randPath) {
		self = this;
		rooms = new LinkedList<>();
		endRooms = new LinkedList<>();
		contRooms = new LinkedList<>();
		keyRooms = new LinkedList<>();
		doorRooms = new LinkedList<>();
		this.ending = ending;
		this.starting = starting;
		this.pathLen = minPath; // minimum length of paths
		this.pathRand = randPath; // add rand(0, (pathRand-1)) to the length of paths
		
		if (starting.getNumExits() <= 0)
			NostrumMagica.logger.warn("Dungeon created with 0-exit starting. This will not work.");
	}
	
	public NostrumDungeon add(IDungeonRoom room) {
		rooms.add(room);
		
		// invalidate cache
		endRooms.clear();
		contRooms.clear();
		keyRooms.clear();
		doorRooms.clear();
		return this;
	}
	
	public void clearRooms() {
		rooms.clear();
		// invalidate cache
		endRooms.clear();
		contRooms.clear();
		keyRooms.clear();
		doorRooms.clear();
	}
	
	// Generates a dungeon, and returns a list of all the instances that were generated.
	// These can be used to spawn the dungeon in the world.
	public List<DungeonRoomInstance> generate(DungeonExitPoint start) {
		// Calculate caches
		if (endRooms.isEmpty()) {
			for (IDungeonRoom room : rooms) {
				// any doors can be key room. door rooms cannot be key.
				
				if (room.getNumExits() == 0)
					endRooms.add(room);
				else
					contRooms.add(room);
				if (room.supportsKey())
					keyRooms.add(room);
				
				if (room.supportsDoor() && !room.supportsKey())
					doorRooms.add(room);
			}
		}
		
		if (contRooms.isEmpty()) {
			NostrumMagica.logger.error("No continuation rooms found in dungeon. Aborting spawn...");
			return new ArrayList<>();
		}
		
		Path startPath = new Path(new DungeonRoomInstance(start, this.starting, false)); // Note: false means starting won't ever have key
		
		startPath.generateChildren(rand, pathLen + rand.nextInt(pathRand), ending);
		
		return startPath.getInstances();
	}
	
	// Generates and then spawns a dungeon in the world.
	public void spawn(IWorld world, DungeonExitPoint start) {
		List<DungeonRoomInstance> dungeonInstances = generate(start);
		
		// Iterate and spawn instances
		// TODO I used to make sure to spawn the 'end room' last so it didn't get stomped.
		// Do that again? Or inforce bounds checking? Its not that expensive.
		for (DungeonRoomInstance instance : dungeonInstances) {
			instance.spawn(world);
		}
		
//		
//		starting.spawn(this, world, start);
//		
//		
//		
//		// Clear out puzzle info
////		doorPoints = new LinkedList<>();
////		keyPoints = new LinkedList<>();
//		
//		// Select a subpath to have the ending and another to have the key
//		int index = rand.nextInt(starting.getNumExits());
//		int key = rand.nextInt(starting.getNumExits());
//		if (index == key)
//			key = (key + 1) % starting.getNumExits();
//		IDungeonRoom inEnd;
//
//		int shrineroom = index;
//		List<DungeonExitPoint> exits = starting.getExits(start);
//		for (DungeonExitPoint exit : exits) {
//			inEnd = null;
//			
//
//			if (index == 0) {
//				; // Skip this one, so we can do it last outside the loop
//			} else {
//				
//				Path path = new Path(null, pathLen + rand.nextInt(pathRand));
//				if (key == 0) {
//					if (!keyRooms.isEmpty() && !doorRooms.isEmpty())
//						path.hasKey();
//				}
//				
//				path.spawn(world, exit, inEnd);
//			}
//			index -= 1;
//			key -= 1;
//		}
//		
//		{
//			DungeonExitPoint last = exits.get(shrineroom);
//			Path path = new Path(null, pathLen + rand.nextInt(pathRand));
//			inEnd = ending;
//			if (!keyRooms.isEmpty() && !doorRooms.isEmpty())
//				path.hasDoor();
//			
//			path.spawn(world, last, inEnd);
//		}
		
	}
	
	public static class DungeonRoomInstance {
		private final DungeonExitPoint entry;
		private final IDungeonRoom template;
		private final boolean hasKey; // whether the key should be in this room
		
		public DungeonRoomInstance(DungeonExitPoint entry, IDungeonRoom template, boolean hasKey) {
			this.entry = entry;
			this.template = template;
			this.hasKey = hasKey;
		}

		public MutableBoundingBox getBounds() {
			return template.getBounds(this.entry);
		}
		
		public void spawn(IWorld world) {
			spawn(world, null);
		}
		
		public void spawn(IWorld world, MutableBoundingBox bounds) {
			// Spawn room template
			template.spawn(world, this.entry, bounds);
			
			// If we have a key, do special key placement
			if (this.hasKey) {
				DungeonExitPoint keyLoc = template.getKeyLocation(this.entry);
				if (bounds == null || bounds.isVecInside(keyLoc.pos)) {
					spawnKey(world, keyLoc);
				}
			}
		}

		private void spawnKey(IWorld world, DungeonExitPoint keyLocation) {
			NonNullList<ItemStack> loot = NonNullList.withSize(27, ItemStack.EMPTY);
			for (int i = 0; i < 27; i++) {
				if (rand.nextFloat() < .2) {
					loot.set(i, new ItemStack(Items.ARROW, rand.nextInt(3) + 1));
				} else if (rand.nextFloat() < .5) {
					loot.set(i, ReagentItem.CreateStack(
							ReagentType.values()[rand.nextInt(ReagentType.values().length)],
							rand.nextInt(10) + 1));
				} else if (rand.nextFloat() < .5) {
					loot.set(i, ReagentItem.CreateStack(
							ReagentType.values()[rand.nextInt(ReagentType.values().length)],
							rand.nextInt(20) + 1));
				}
			}
			
			loot.set(rand.nextInt(27), new ItemStack(Items.GOLDEN_APPLE)); // FIXME should be key
			LootUtil.createLoot(world, keyLocation.getPos(), keyLocation.getFacing(),
					loot);
		}
		
		@Override
		public String toString() {
			return "[" + this.entry.pos + "] " + this.template.getRoomID() + ": " + this.getBounds();
		}
		
		private static final String NBT_ENTRY = "entry";
		private static final String NBT_TEMPLATE = "template";
		private static final String NBT_HASKEY = "hasKey";
		
		public @Nonnull CompoundNBT toNBT(@Nullable CompoundNBT tag) {
			if (tag == null) {
				tag = new CompoundNBT();
			}
			
			tag.put(NBT_ENTRY, this.entry.toNBT());
			tag.putString(NBT_TEMPLATE, this.template.getRoomID());
			tag.putBoolean(NBT_HASKEY, this.hasKey);
			
			return tag;
		}
		
		public static DungeonRoomInstance fromNBT(CompoundNBT tag) {
			final DungeonExitPoint entry = DungeonExitPoint.fromNBT(tag.getCompound(NBT_ENTRY));
			final IDungeonRoom template = IDungeonRoom.GetRegisteredRoom(tag.getString(NBT_TEMPLATE));
			final boolean hasKey = tag.getBoolean(NBT_HASKEY);
			
			return new DungeonRoomInstance(entry, template, hasKey);
		}
	}
	
	private class Path {
		
		private List<Path> children;
		//private final Path parent;
		private boolean hasKey; // whether this path will have a key when spawned
		private boolean hasDoor; // Whether the door should be spawned on this path
		
		private DungeonRoomInstance myRoom;
		
//		private int doorKey; // if is a door room, set to the key that's needed to unlock
//		// If is a key supporting room, set to the key that we have. -1 is none
//		private int numKeys; // Number of key-supporting rooms we have to have
		
		public Path(Path parent) {
//			this.remaining = remaining;
//			this.parent = parent;
//			this.firstRoom = room;
			this.hasKey = false;
			this.hasDoor = false;
			this.children = new ArrayList<>();
		}
		
		public Path(DungeonRoomInstance startingRoom) {
			this((Path) null);
			this.myRoom = startingRoom;
		}
		
		public void hasKey() {
			this.hasKey = true;
		}
		
		public void hasDoor() {
			this.hasDoor = true;
		}
		
//		public int getDoorKey() {
//			return doorKey;
//		}
		
//		/**
//		 * Return keys behind this room
//		 * @return
//		 */
//		public List<Integer> getHiddenKeys() {
//			List<Integer> keys = null;
//			if (children.isEmpty()) {
//				keys = new LinkedList<>();
//			} else {
//				for (Path child : children) {
//					if (keys == null)
//						keys = child.getHiddenKeys();
//					else
//						keys.addAll(child.getHiddenKeys());
//				}
//			}
//			
//			if (this.doorKey != -1)
//				keys.add(doorKey);
//			
//			return keys;
//		}
		
//		public Path getDoor(int key) {
//			for (Path path : doorPoints) {
//				if (path.getDoorKey() == key)
//					return path;
//			}
//			
//			return null;
//		}
		
		protected @Nonnull IDungeonRoom pickRandomContRoom(Random rand) {
			return contRooms.get(rand.nextInt(contRooms.size()));
		}
		
		protected @Nonnull IDungeonRoom pickRandomEndRoom(Random rand) {
			if (endRooms.isEmpty()) {
				return rooms.get(rand.nextInt(rooms.size()));
			} else {
				return endRooms.get(rand.nextInt(endRooms.size()));
			}
		}
		
		protected @Nonnull IDungeonRoom pickRandomKeyRoom(Random rand) {
			return keyRooms.get(rand.nextInt(keyRooms.size()));
		}
		
		protected @Nonnull IDungeonRoom pickRandomDoorRoom(Random rand) {
			return doorRooms.get(rand.nextInt(doorRooms.size()));
		}
		
		// Fill out this path, including a room for this node and spawning any children that are needed.
		protected void generate(Random rand, int remaining, DungeonExitPoint entry, IDungeonRoom ending) {
			Validate.isTrue(this.myRoom == null); // If room is already set, only generate children!
			
			/*
			 * 0) If remaining is 0, spawn end if we have it. Otherwise, if we
			 *    somehow still have the key, spawn a keyroom. Otherwise, try to
			 *    find a room that has 0 exists and spawn taht. Otherwise, spawn
			 *    whatever.
			 * 2) If we have a door or key, roll to spawn that. If we have both,
			 *    roll for key first. Otherwise place regular >0 door room.
			 */
			if (remaining == 0) {
				// Terminal
				if (ending != null) {
					this.myRoom = new DungeonRoomInstance(entry, ending, false);
				} else if (this.hasKey) {
					this.myRoom = new DungeonRoomInstance(entry, pickRandomKeyRoom(rand), true);
				} else {
					this.myRoom = new DungeonRoomInstance(entry, pickRandomEndRoom(rand), false);
				}
			} else {
				// If we have door or key, try those first
				// If we have both, roll for key first
				// Do not do door if we have both and key didn'tt succeed
				myRoom = null;
				if (hasKey) {
					if (rand.nextFloat() < 1.0f / ((float) remaining + 1)) {
						myRoom = new DungeonRoomInstance(entry, pickRandomKeyRoom(rand), true);
						hasKey = false;
					}
				} else if (hasDoor) {
					if (rand.nextFloat() < 1.0f / ((float) remaining + 1)) {
						myRoom = new DungeonRoomInstance(entry, pickRandomDoorRoom(rand), false);
						hasDoor = false;
					}
				}
				
				if (myRoom == null) {
					myRoom = new DungeonRoomInstance(entry, pickRandomContRoom(rand), false);
				}
				
				this.generateChildren(rand, remaining - 1, ending);
			}
		}
		
		// Fill out this path's children
		protected void generateChildren(Random rand, int remaining, IDungeonRoom ending) {
			// Select a subpath to have the ending
			int keyI = -1;
			int doorI = -1;
			IDungeonRoom inEnd;
			
			if (hasKey) {
				keyI = rand.nextInt(myRoom.template.getNumExits());
			}
			if (hasDoor || ending != null) {
				doorI = rand.nextInt(myRoom.template.getNumExits());
			}
			
			if (keyI != -1 && doorI != -1 && keyI == doorI) {
				doorI = (doorI + 1) % myRoom.template.getNumExits();
			}

			// Add subpaths based on doors
			for (DungeonExitPoint door : myRoom.template.getExits(myRoom.entry)) {
				Path path = new Path(this);
				inEnd = null;
				if (doorI == 0) {
					if (hasDoor)
						path.hasDoor();
					inEnd = ending; // just set to null again if we don't have one 
				}
				if (keyI == 0) {
					path.hasKey();
				}
				
				// TODO evaluate making 'remaining' be random to be like 1-remaining
				path.generate(rand, remaining, door, inEnd);
				keyI -= 1;
				doorI -= 1;
				this.children.add(path);
			}
		}
		
		public List<DungeonRoomInstance> getInstances() {
			return getInstances(new ArrayList<>());
		}
		
		public List<DungeonRoomInstance> getInstances(@Nonnull List<DungeonRoomInstance> list) {
			list.add(this.myRoom);
			
			for (Path child : this.children) {
				child.getInstances(list);
			}
			
			return list;
		}
	}
	
	public static DungeonExitPoint asRotated(DungeonExitPoint start, BlockPos offset, Direction facing) {
		int modX = 1;
		int modZ = 1;
		boolean swap = false;
		switch (start.getFacing()) {
		case EAST:
			swap = true;
			modX = -1;
			break;
		case SOUTH:
			modX = -1;
			modZ = -1;
			break;
		case NORTH: // -z
		default:
			break;
		case WEST: // -x
			swap = true;
			modZ = -1;
			break;
		}
		

		BlockPos pos = start.getPos();
		int x = offset.getX();
		int z = offset.getZ();
		if (swap) {
			int t = x;
			x = z;
			z = t;
		}
		x *= modX;
		z *= modZ;
		
		pos = new BlockPos(pos.getX() + x, pos.getY() + offset.getY(), pos.getZ() + z);
		
		int rot;
		Direction out = start.facing;
		switch (facing) {
		case NORTH:
		default:
			rot = 0;
			break;
		case EAST:
			rot = 1;
			break;
		case SOUTH:
			rot = 2;
			break;
		case WEST:
			rot = 3;
			break;
		}
		
		while (rot-- > 0)
			out = out.rotateY();
			
		return new DungeonExitPoint(pos, out);
	}
}
