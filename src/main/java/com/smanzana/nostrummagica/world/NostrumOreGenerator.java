package com.smanzana.nostrummagica.world;

import java.util.Random;

import com.smanzana.nostrummagica.blocks.EssenceOre;
import com.smanzana.nostrummagica.blocks.ManiOre;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class NostrumOreGenerator implements IWorldGenerator {

	private static enum OreGen {
		MANI(new WorldGenMinable(ManiOre.instance().getDefaultState(), 9), 20, 1, 240),
		ESS(new WorldGenMinable(EssenceOre.instance().getDefaultState(), 3), 10, 20, 60);
		
		private WorldGenerator gen;
		
		private int chancesToSpawn;
		
		private int minY;
		
		private int maxY;
		
		private OreGen(WorldGenerator gen) {
			this(gen, 4, 1, 254);
		}
		
		private OreGen(WorldGenerator gen, int chances, int minY, int maxY) {
			this.gen = gen;
			this.chancesToSpawn = chances;
			this.minY = minY;
			this.maxY = maxY;
		}
		
		public WorldGenerator getGenerator() {
			return gen;
		}

		public int getChancesToSpawn() {
			return chancesToSpawn;
		}

		public int getMinY() {
			return minY;
		}

		public int getMaxY() {
			return maxY;
		}
	}
	
	public NostrumOreGenerator() {
		
	}
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (world.provider.getDimension() != 0)
			return;
		
		for (OreGen gen : OreGen.values())
			runGenerator(gen.getGenerator(), world, random, chunkX, chunkZ, gen.getChancesToSpawn(), gen.getMinY(), gen.getMaxY());
		
	}

	/**
	 * Taken from bedrockminer's worldgen tutorial
	 * http://bedrockminer.jimdo.com/modding-tutorials/basic-modding-1-8/world-generation/
	 * @param generator
	 * @param world
	 * @param rand
	 * @param chunk_X
	 * @param chunk_Z
	 * @param chancesToSpawn
	 * @param minHeight
	 * @param maxHeight
	 */
	private void runGenerator(WorldGenerator generator, World world, Random rand, int chunk_X, int chunk_Z, int chancesToSpawn, int minHeight, int maxHeight) {
	    if (minHeight < 0 || maxHeight > 256 || minHeight > maxHeight)
	        throw new IllegalArgumentException("Illegal Height Arguments for WorldGenerator");

	    int heightDiff = maxHeight - minHeight + 1;
	    for (int i = 0; i < chancesToSpawn; i ++) {
	        int x = chunk_X * 16 + rand.nextInt(16);
	        int y = minHeight + rand.nextInt(heightDiff);
	        int z = chunk_Z * 16 + rand.nextInt(16);
	        generator.generate(world, rand, new BlockPos(x, y, z));
	    }
	}
	
}
