package com.smanzana.nostrummagica.world;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumShrineDungeon;
import com.smanzana.nostrummagica.world.dungeon.room.RoomChallenge1;
import com.smanzana.nostrummagica.world.dungeon.room.RoomEnd1;
import com.smanzana.nostrummagica.world.dungeon.room.RoomEnd2;
import com.smanzana.nostrummagica.world.dungeon.room.RoomHallway;
import com.smanzana.nostrummagica.world.dungeon.room.RoomJail1;
import com.smanzana.nostrummagica.world.dungeon.room.RoomLongHallway;
import com.smanzana.nostrummagica.world.dungeon.room.RoomTee1;
import com.smanzana.nostrummagica.world.dungeon.room.RoomVHallway;
import com.smanzana.nostrummagica.world.dungeon.room.ShrineRoom;
import com.smanzana.nostrummagica.world.dungeon.room.StartRoom;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class NostrumShrineGenerator implements IWorldGenerator {
	
	private static class ShapeComponent implements NostrumShrineDungeon.ComponentGenerator {

		@Override
		public SpellComponentWrapper getRandom() {
			if (!queuedWrappers.isEmpty()) {
				if (queuedWrappers.get(0).isShape())
					return queuedWrappers.remove(0);
			}
			
			Collection<SpellShape> shapes = SpellShape.getAllShapes();
			int pos = NostrumMagica.rand.nextInt(shapes.size());
			for (SpellShape shape : shapes) {
				if (pos == 0)
					return new SpellComponentWrapper(shape);
				pos--;
			}
			
			// Error condition. Oh well.
			return new SpellComponentWrapper(shapes.iterator().next());
		}
		
	}
	
	private static class TriggerComponent implements NostrumShrineDungeon.ComponentGenerator {
		
		@Override
		public SpellComponentWrapper getRandom() {
			if (!queuedWrappers.isEmpty()) {
				if (queuedWrappers.get(0).isTrigger())
					return queuedWrappers.remove(0);
			}
			
			Collection<SpellTrigger> triggers = SpellTrigger.getAllTriggers();
			// AI trigger should be skipped.
			int pos = NostrumMagica.rand.nextInt(triggers.size() - 1);
			for (SpellTrigger trigger : triggers) {
				if (trigger == AITargetTrigger.instance())
					continue;
				if (pos == 0)
					return new SpellComponentWrapper(trigger);
				pos--;
			}
			
			// Error condition. Oh well.
			return new SpellComponentWrapper(triggers.iterator().next());
		}
	}
	
	private static class ElementComponent implements NostrumShrineDungeon.ComponentGenerator {
		
		@Override
		public SpellComponentWrapper getRandom() {
			if (!queuedWrappers.isEmpty()) {
				if (queuedWrappers.get(0).isElement())
					return queuedWrappers.remove(0);
			}
			
			return new SpellComponentWrapper(EMagicElement.values()[
			       NostrumMagica.rand.nextInt(EMagicElement.values().length)]);
		}
	}
	
//	private static class AlterationComponent implements NostrumShrineDungeon.ComponentGenerator {
//		
//		@Override
//		public SpellComponentWrapper getRandom() {
//			return new SpellComponentWrapper(EAlteration.values()[
//			       NostrumMagica.rand.nextInt(EAlteration.values().length)]);
//		}
//	}

	private static class WorldGenNostrumShrine extends WorldGenerator {
		
		private NostrumDungeon dungeon;
		
		public WorldGenNostrumShrine(NostrumDungeon dungeon) {
			this.dungeon = dungeon;
		}
		
		@Override
		public boolean generate(World worldIn, Random rand, BlockPos position)
	    {
	        dungeon.spawn(worldIn,
	        		new NostrumDungeon.DungeonExitPoint(position, 
	        				EnumFacing.HORIZONTALS[rand.nextInt(4)]
	        				));
	        
	        return true;
	    }
	}
	
	private static enum DungeonGen {
		SHAPE(new WorldGenNostrumShrine(new NostrumShrineDungeon(
				// Shape dungeon
				new ShapeComponent(),
				new StartRoom(),
				new ShrineRoom()
				).add(new RoomHallway())
				 .add(new RoomHallway())
				 .add(new RoomLongHallway())
				 .add(new RoomEnd1(true, false))
				 .add(new RoomEnd1(false, false))
				 .add(new RoomEnd1(false, false))
				 .add(new RoomEnd1(false, true))
				 .add(new RoomEnd2(false))
				 .add(new RoomEnd2(true))
				 .add(new RoomVHallway())
				 .add(new RoomTee1())
				 .add(new RoomJail1())
				 .add(new RoomChallenge1())), 20, 50),
		TRIGGER(new WorldGenNostrumShrine(new NostrumShrineDungeon(
				// Trigger dungeon
				new TriggerComponent(),
				new StartRoom(),
				new ShrineRoom()
				).add(new RoomHallway())
				 .add(new RoomHallway())
				 .add(new RoomLongHallway())
				 .add(new RoomEnd1(true, false))
				 .add(new RoomEnd1(false, false))
				 .add(new RoomEnd1(false, false))
				 .add(new RoomEnd1(false, true))
				 .add(new RoomEnd2(false))
				 .add(new RoomEnd2(true))
				 .add(new RoomVHallway())
				 .add(new RoomTee1())
				 .add(new RoomJail1())
				 .add(new RoomChallenge1())), 30, 50),
		ELEMENT(new WorldGenNostrumShrine(new NostrumShrineDungeon(
				// Trigger dungeon
				new ElementComponent(),
				new StartRoom(),
				new ShrineRoom()
				).add(new RoomHallway())
				 .add(new RoomHallway())
				 .add(new RoomLongHallway())
				 .add(new RoomEnd1(true, false))
				 .add(new RoomEnd1(false, false))
				 .add(new RoomEnd1(false, false))
				 .add(new RoomEnd1(false, true))
				 .add(new RoomEnd2(false))
				 .add(new RoomEnd2(true))
				 .add(new RoomVHallway())
				 .add(new RoomTee1())
				 .add(new RoomJail1())
				 .add(new RoomChallenge1())), 30, 50);
		
		private WorldGenerator gen;
		private int minY;
		private int maxY;
		
		private DungeonGen(WorldGenerator gen, int minY, int maxY) {
			this.gen = gen;
			this.minY = minY;
			this.maxY = maxY;
		}
		
		public WorldGenerator getGenerator() {
			return gen;
		}

		public int getMinY() {
			return minY;
		}

		public int getMaxY() {
			return maxY;
		}
		
		public boolean chanceSpawn(Random random, World world, int chunkX, int chunkZ) {
			if (this == ELEMENT) {
				if (chunkX % 32 == 0 && chunkZ % 24 == 0) {
					return random.nextBoolean() && random.nextBoolean();
				}
			}
			
			int count = 1;
			if (this == ELEMENT)
				count = 4;
			else if (this == SHAPE)
				count = 1;
			else if (this == TRIGGER)
				count = 4;
			
			
			return random.nextInt(9000) < count;
		}
	}
	
	// Some items can queue a certain type of shrine to be spawned next.
	// This list is the queue
	// We don't persist it, though :shrug:
	private static List<SpellComponentWrapper> queuedWrappers = new LinkedList<>();
	private static int forceTimer = -1;
	
	public NostrumShrineGenerator() {
		
	}
	
	public static void enqueueShrineRequest(SpellComponentWrapper type) {
		queuedWrappers.add(type);
	}
	
	public static void forceSpawn(int chunks) {
		forceTimer = Math.max(0, chunks);
	}
	
	private List<DungeonGen> list;
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (world.provider.getDimension() != 0)
			return;
		
		if (list == null) {
			list = new LinkedList<DungeonGen>();
			for (DungeonGen gen : DungeonGen.values())
				list.add(gen);
		}
		Collections.shuffle(list);
		
		if (forceTimer == 0) {
			DungeonGen gen = list.get(0);
			runGenerator(gen.getGenerator(), world, random, chunkX, chunkZ,
					gen.getMinY(), gen.getMaxY());
			forceTimer = -1;
			return;
		}
		
		if (forceTimer > 0)
			forceTimer--;
		
		for (DungeonGen gen : list) {
			if (gen.chanceSpawn(random, world, chunkX, chunkZ)) {
				runGenerator(gen.getGenerator(), world, random, chunkX, chunkZ,
					gen.getMinY(), gen.getMaxY());
				break;
			}
		}
		
	}

	/**
	 * Taken from bedrockminer's worldgen tutorial
	 * http://bedrockminer.jimdo.com/modding-tutorials/basic-modding-1-8/world-generation/
	 * @param generator
	 * @param world
	 * @param rand
	 * @param chunk_X
	 * @param chunk_Z
	 * @param minHeight
	 * @param maxHeight
	 */
	private void runGenerator(WorldGenerator generator, World world, Random rand,
			int chunk_X, int chunk_Z, int minHeight, int maxHeight) {
	    if (minHeight < 0 || maxHeight > 256 || minHeight > maxHeight)
	        throw new IllegalArgumentException("Illegal Height Arguments for WorldGenerator");

	    int heightDiff = maxHeight - minHeight + 1;
        int x = chunk_X * 16 + rand.nextInt(16);
        int z = chunk_Z * 16 + rand.nextInt(16);
        int y = minHeight + rand.nextInt(heightDiff);
        
        BlockPos pos =  new BlockPos(x, y, z);
        		
       	generator.generate(world, rand, pos);
	}
	
}
