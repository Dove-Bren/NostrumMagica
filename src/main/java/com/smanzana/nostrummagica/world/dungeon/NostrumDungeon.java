package com.smanzana.nostrummagica.world.dungeon;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoom;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NostrumDungeon {
	
	public static class DungeonExitPoint {
		private EnumFacing facing;
		private BlockPos pos;
		
		public DungeonExitPoint(BlockPos pos, EnumFacing facing) {
			this.pos = pos;
			this.facing = facing;
		}

		public EnumFacing getFacing() {
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
		
		
		public NBTTagCompound toNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setLong(NBT_POS, this.pos.toLong());
			tag.setByte(NBT_DIR, (byte) facing.getHorizontalIndex());
			return tag;
		}
		
		public static DungeonExitPoint fromNBT(NBTTagCompound nbt) {
			BlockPos pos = BlockPos.fromLong(nbt.getLong(NBT_POS));
			EnumFacing facing = EnumFacing.getHorizontal(nbt.getByte(NBT_DIR));
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
	
	public void spawn(World world, DungeonExitPoint start) {
		starting.spawn(this, world, start);
		
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
			return;
		}
		
		// Clear out puzzle info
//		doorPoints = new LinkedList<>();
//		keyPoints = new LinkedList<>();
		
		// Select a subpath to have the ending and another to have the key
		int index = rand.nextInt(starting.getNumExits());
		int key = rand.nextInt(starting.getNumExits());
		if (index == key)
			key = (key + 1) % starting.getNumExits();
		IDungeonRoom inEnd;

		int shrineroom = index;
		List<DungeonExitPoint> exits = starting.getExits(start);
		for (DungeonExitPoint exit : exits) {
			inEnd = null;
			

			if (index == 0) {
				; // Skip this one, so we can do it last outside the loop
			} else {
				
				Path path = new Path(null, pathLen + rand.nextInt(pathRand));
				if (key == 0) {
					if (!keyRooms.isEmpty() && !doorRooms.isEmpty())
						path.hasKey();
				}
				
				path.spawn(world, exit, inEnd);
			}
			index -= 1;
			key -= 1;
		}
		
		{
			DungeonExitPoint last = exits.get(shrineroom);
			Path path = new Path(null, pathLen + rand.nextInt(pathRand));
			inEnd = ending;
			if (!keyRooms.isEmpty() && !doorRooms.isEmpty())
				path.hasDoor();
			
			path.spawn(world, last, inEnd);
		}
		
	}
	
	private class Path {
		
		private int remaining;
		
		// Head info
//		private IDungeonRoom firstRoom;
//		private List<Path> children;
//		private Path parent;
		private boolean hasKey; // whether this path will have a key when spawned
		private boolean hasDoor; // Whether the door should be spawned on this path
		
		
//		private int doorKey; // if is a door room, set to the key that's needed to unlock
//		// If is a key supporting room, set to the key that we have. -1 is none
//		private int numKeys; // Number of key-supporting rooms we have to have
		
		public Path(Path parent, int remaining) {
			this.remaining = remaining;
//			this.parent = parent;
//			this.firstRoom = room;
			this.hasKey = false;
			this.hasDoor = false;
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
		
		public void spawn(World world, DungeonExitPoint entry, IDungeonRoom ending) {
			spawn(world, entry, remaining, ending);
		}
		
		private void spawn(World world, DungeonExitPoint entry, int remaining, IDungeonRoom ending) {
			/*
			 * 0) If remaining is 0, spawn end if we have it. Otherwise, if we
			 *    somehow still have the key, spawn a keyroom. Otherwise, try to
			 *    find a room that has 0 exists and spawn taht. Otherwise, spawn
			 *    whatever.
			 * 2) If we have a door or key, roll to spawn that. If we have both,
			 *    roll for key first. Otherwise place regular >0 door room.
			 */
			if (remaining == 0) {
				if (ending != null) {
					ending.spawn(self, world, entry);
					return;
				}
				

				IDungeonRoom selected = null;
				if (hasKey && !hasDoor) {
					// If we have both, just drop it. We missed our chance
					// Since we're here, though, we get to spawn a key
					selected = keyRooms.get(rand.nextInt(keyRooms.size()));
					hasKey = false;
				} else if (endRooms.isEmpty()) {
					selected = rooms.get(rand.nextInt(rooms.size()));
				} else {
					selected = endRooms.get(rand.nextInt(endRooms.size()));
				}
				
				selected.spawn(self, world, entry);
				return;
			} else {
				// If we have door or key, try those first
				// If we have both, roll for key first
				// Do not do door if we have both and key didn'tt succeed
				IDungeonRoom selected = null;
				boolean doingKey = false;
				if (hasKey) {
					if (rand.nextFloat() < 1.0f / ((float) remaining + 1)) {
						selected = keyRooms.get(rand.nextInt(keyRooms.size()));
						hasKey = false;
						doingKey = true;
					}
				} else if (hasDoor) {
					if (rand.nextFloat() < 1.0f / ((float) remaining + 1)) {
						selected = doorRooms.get(rand.nextInt(doorRooms.size()));
						hasDoor = false;
					}
				}
				
				if (selected == null) {
					selected = contRooms.get(rand.nextInt(contRooms.size()));
				}
				
				selected.spawn(self, world, entry);
				if (doingKey)
					spawnKey(world, selected.getKeyLocation(null));
				
				// Select a subpath to have the ending
				int keyI = -1;
				int doorI = -1;
				IDungeonRoom inEnd;
				
				if (hasKey) {
					keyI = rand.nextInt(selected.getNumExits());
				}
				if (hasDoor || ending != null) {
					doorI = rand.nextInt(selected.getNumExits());
				}
				
				if (keyI != -1 && doorI != -1 && keyI == doorI) {
					doorI = (doorI + 1) % selected.getNumExits();
				}
				
				for (DungeonExitPoint exit : selected.getExits(entry)) {
					Path path = new Path(this, remaining - 1);
					inEnd = null;
					if (doorI == 0) {
						if (hasDoor)
							path.hasDoor();
						inEnd = ending; // just set to null again if we don't have one 
					}
					if (keyI == 0) {
						path.hasKey();
					}
					
					path.spawn(world, exit, inEnd);
					keyI -= 1;
					doorI -= 1;
				}
			}
		}

		private void spawnKey(World world, DungeonExitPoint keyLocation) {
			NonNullList<ItemStack> loot = NonNullList.withSize(27, ItemStack.EMPTY);
			for (int i = 0; i < 27; i++) {
				if (rand.nextFloat() < .2) {
					loot.set(i, new ItemStack(Items.ARROW, rand.nextInt(3) + 1));
				} else if (rand.nextFloat() < .5) {
					loot.set(i, ReagentItem.instance().getReagent(
							ReagentType.values()[rand.nextInt(ReagentType.values().length)],
							rand.nextInt(10) + 1));
				} else if (rand.nextFloat() < .5) {
					loot.set(i, ReagentItem.instance().getReagent(
							ReagentType.values()[rand.nextInt(ReagentType.values().length)],
							rand.nextInt(20) + 1));
				}
			}
			
			loot.set(rand.nextInt(27), new ItemStack(Items.GOLDEN_APPLE)); // FIXME should be key
			LootUtil.createLoot(world, keyLocation.getPos(), keyLocation.getFacing(),
					loot);
		}
	}
	
	public static DungeonExitPoint asRotated(DungeonExitPoint start, BlockPos offset, EnumFacing facing) {
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
		EnumFacing out = start.facing;
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
