package com.smanzana.nostrummagica.world;

import java.util.Random;

import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoom;
import com.smanzana.nostrummagica.world.dungeon.room.ShrineRoom;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class NostrumShrineGenerator implements IWorldGenerator {

	private static class WorldGenNostrumShrine extends WorldGenerator {
		
		//private NostrumDungeon dungeon;
		private IDungeonRoom room;
		
		public WorldGenNostrumShrine(IDungeonRoom room) {
			this.room = room;
		}
		
		@Override
		public boolean generate(World worldIn, Random rand, BlockPos position)
	    {
	        room.spawn(null, worldIn,
	        		new NostrumDungeon.DungeonExitPoint(position, 
	        				EnumFacing.random(rand)
	        				));

	        return true;
	    }
	}
	
	private static enum DungeonGen {
		DUNG1(new WorldGenNostrumShrine(new ShrineRoom()), 30, 80);
		
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
	}
	
	public NostrumShrineGenerator() {
		
	}
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (world.provider.getDimension() != 0)
			return;
		
		if (random.nextInt(20) != 0)
			return;
		
		for (DungeonGen gen : DungeonGen.values())
			runGenerator(gen.getGenerator(), world, random, chunkX, chunkZ,
					gen.getMinY(), gen.getMaxY());
		
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
        int y = minHeight + rand.nextInt(heightDiff);
        int z = chunk_Z * 16 + rand.nextInt(16);
        
        BlockPos pos =  new BlockPos(x, y, z);
        		
       	generator.generate(world, rand, pos);
	}
	
}
