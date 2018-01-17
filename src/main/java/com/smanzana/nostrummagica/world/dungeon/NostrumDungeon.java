package com.smanzana.nostrummagica.world.dungeon;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.dungeon.room.HallwayRoom;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoom;
import com.smanzana.nostrummagica.world.dungeon.room.ShrineRoom;
import com.smanzana.nostrummagica.world.dungeon.room.StartRoom;

import net.minecraft.util.EnumFacing;
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
	}

	private static Random rand = new Random();
	private List<IDungeonRoom> rooms;
	private IDungeonRoom ending;
	private IDungeonRoom starting;
	protected NostrumDungeon self;
	
	// Cached subsets 
	private List<IDungeonRoom> endPoints;
	private List<IDungeonRoom> contPoints;
	
	public NostrumDungeon(IDungeonRoom starting, IDungeonRoom ending) {
		self = this;
		rooms = new LinkedList<>();
		endPoints = new LinkedList<>();
		contPoints = new LinkedList<>();
		this.ending = ending;
		this.starting = starting;
		
		if (starting.getNumExits() <= 0)
			NostrumMagica.logger.warn("Dungeon created with 0-exit starting. This will not work.");
	}
	
	public NostrumDungeon add(IDungeonRoom room) {
		rooms.add(room);
		
		// invalidate cache
		endPoints.clear();
		contPoints.clear();
		return this;
	}
	
	public void spawn(World world, DungeonExitPoint start) {
		starting.spawn(this, world, start);
		
		// Calculate caches
		if (endPoints.isEmpty()) {
			for (IDungeonRoom room : rooms) {
				if (room.getNumExits() == 0)
					endPoints.add(room);
				else
					contPoints.add(room);
			}
		}
		
		// Select a subpath to have the ending
		int index = rand.nextInt(starting.getNumExits());
		IDungeonRoom inEnd;
		
		for (DungeonExitPoint exit : starting.getExits(start)) {
			inEnd = null;
			if (index == 0) {
				inEnd = ending; 
			}
			Path path = new Path(2,//rand.nextInt(10) + 1,
					inEnd);
			path.spawn(world, exit);

			index -= 1;
		}
	}
	
	private class Path {
		
		//private List<DungeonExitPoint> doors;
		private IDungeonRoom ending;
		private int remaining;
		
		public Path(int remaining, IDungeonRoom ending) {
			this.remaining = remaining;
			this.ending = ending;
		}
		
		public void spawn(World world, DungeonExitPoint entry) {
			spawn(world, entry, remaining);
		}
		
		private void spawn(World world, DungeonExitPoint entry, int remaining) {
			/*
			 * 1) Base case. Remaining == 0? 
			 *   If we have a set ending, use that.
			 *   Otherwise, If we can find one with no doors, use that. Else use random one
			 * 2) Get random room with >0 doors. call spawn at it's end with remaining - 1;
			 */
			if (remaining == 0) {
				if (ending != null) {
					ending.spawn(self, world, entry);
					return;
				}
				
				IDungeonRoom selected = null;
				
				if (endPoints.size() == 0) {
					selected = rooms.get(rand.nextInt(rooms.size()));
				} else {
					selected = endPoints.get(rand.nextInt(endPoints.size()));
				}
				
				selected.spawn(self, world, entry);
				return;
			} else {
				IDungeonRoom selected = null;
				
				if (contPoints.isEmpty()) {
					selected = rooms.get(rand.nextInt(rooms.size()));
				} else {
					selected = contPoints.get(rand.nextInt(contPoints.size()));
				}
				
				selected.spawn(self, world, entry);
				
				// Select a subpath to have the ending
				int index = rand.nextInt(selected.getNumExits());
				IDungeonRoom inEnd;
				
				for (DungeonExitPoint exit : selected.getExits(entry)) {
					inEnd = null;
					if (index == 0) {
						inEnd = ending; 
					}
					Path path = new Path(remaining - 1, inEnd);
					path.spawn(world, exit);
					index -= 1;
				}
			}
		}
	}
	
	public static NostrumDungeon temp = new NostrumDungeon(
			new StartRoom(),
			new ShrineRoom()
			).add(new HallwayRoom());
	
	public static DungeonExitPoint asRotated(DungeonExitPoint start, BlockPos offset, EnumFacing facing) {
		System.out.println("Start: " + start.pos.toString() + " w/ " + start.facing.name());
		System.out.println("offset: " + offset.toString() + " w/ " + facing.name());
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
			
		System.out.println("out: " + pos.toString() + " w/ " + out.name());
		return new DungeonExitPoint(pos, out);
	}
}
