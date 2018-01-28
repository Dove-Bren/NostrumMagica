package com.smanzana.nostrummagica.world;

import java.util.Collection;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
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
	
	private static class ShapeComponent extends SpellComponentWrapper {
		
		public ShapeComponent() {
			super(SingleShape.instance());
		}
		
		@Override
		public SpellShape getShape() {
			Collection<SpellShape> shapes = SpellShape.getAllShapes();
			int pos = NostrumMagica.rand.nextInt(shapes.size());
			for (SpellShape shape : shapes) {
				if (pos == 0)
					return shape;
				pos--;
			}
			
			// Error condition. Oh well.
			return shapes.iterator().next();
		}
	}
	
	private static class TriggerComponent extends SpellComponentWrapper {
		
		public TriggerComponent() {
			super(SelfTrigger.instance());
		}
		
		@Override
		public SpellTrigger getTrigger() {
			Collection<SpellTrigger> triggers = SpellTrigger.getAllTriggers();
			int pos = NostrumMagica.rand.nextInt(triggers.size());
			for (SpellTrigger trigger : triggers) {
				if (pos == 0)
					return trigger;
				pos--;
			}
			
			// Error condition. Oh well.
			return triggers.iterator().next();
		}
	}
	
	private static class ElementComponent extends SpellComponentWrapper {
		
		public ElementComponent() {
			super(EMagicElement.PHYSICAL);
		}
		
		@Override
		public EMagicElement getElement() {
			return EMagicElement.values()[
			       NostrumMagica.rand.nextInt(EMagicElement.values().length)];
		}
	}
	
	private static class AlterationComponent extends SpellComponentWrapper {
		
		public AlterationComponent() {
			super(EAlteration.INFLICT);
		}
		
		@Override
		public EAlteration getAlteration() {
			return EAlteration.values()[
			       NostrumMagica.rand.nextInt(EAlteration.values().length)];
		}
	}

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
		SHAPE(new WorldGenNostrumShrine(new NostrumDungeon(
				// Shape dungeon
				new StartRoom(new ShapeComponent()),
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
		TRIGGER(new WorldGenNostrumShrine(new NostrumDungeon(
				// Trigger dungeon
				new StartRoom(new TriggerComponent()),
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
		ELEMENT(new WorldGenNostrumShrine(new NostrumDungeon(
				// Trigger dungeon
				new StartRoom(new ElementComponent()),
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
			
			return random.nextInt(8000) == 0;
		}
	}
	
	public NostrumShrineGenerator() {
		
	}
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (world.provider.getDimension() != 0)
			return;
		
		for (DungeonGen gen : DungeonGen.values()) {
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
