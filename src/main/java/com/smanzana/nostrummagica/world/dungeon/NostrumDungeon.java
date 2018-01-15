package com.smanzana.nostrummagica.world.dungeon;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoom;

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
	
	public NostrumDungeon(IDungeonRoom ending, IDungeonRoom starting) {
		self = this;
		rooms = new LinkedList<>();
		this.ending = ending;
		this.starting = starting;
		
		if (starting.getNumExits() <= 0)
			NostrumMagica.logger.warn("Dungeon created with 0-exit starting. This will not work.");
	}
	
	public NostrumDungeon add(IDungeonRoom room) {
		rooms.add(room);
		return this;
	}
	
	public void spawn(World world, DungeonExitPoint start) {
		starting.spawn(this, world, start);
		// Select a subpath to have the ending
		int index = rand.nextInt(starting.getNumExits());
		
		for (DungeonExitPoint exit : starting.getExits(start)) {
			Path path = new Path(rand.nextInt(10) + 1,
					index-- == 0 ? ending : null);
			path.spawn(world, exit);
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
			 * 3) Get random room with >0 doors. call spawn at it's end with remaining - 1;
			 */
			if (remaining == 0) {
				if (ending != null) {
					ending.spawn(self, world, entry);
					return;
				}
				
				List<IDungeonRoom> singles = new LinkedList<>();
				IDungeonRoom selected = null;
				for (IDungeonRoom room : rooms) {
					if (room.getNumExits() == 0)
						singles.add(room);
				}
				
				if (singles.size() == 0) {
					selected = rooms.get(rand.nextInt(rooms.size()));
				} else {
					selected = singles.get(rand.nextInt(singles.size()));
				}
				
				selected.spawn(self, world, entry);
				return;
			} else {
				List<IDungeonRoom> list = new LinkedList<>();
				IDungeonRoom selected = null;
				for (IDungeonRoom room : rooms) {
					if (room.getNumExits() > 0) {
						list.add(room);
					}
				}
				
				if (list.isEmpty()) {
					selected = rooms.get(rand.nextInt(rooms.size()));
				} else {
					selected = list.get(rand.nextInt(list.size()));
				}
				
				selected.spawn(self, world, entry);
				
				// Select a subpath to have the ending
				int index = rand.nextInt(selected.getNumExits());
				
				for (DungeonExitPoint exit : selected.getExits(entry)) {
					Path path = new Path(remaining - 1,
							index-- == 0 ? ending : null);
					path.spawn(world, exit);
				}
			}
		}
	}
	
}
