package com.smanzana.nostrummagica.world;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class NostrumFlowerGenerator implements IWorldGenerator {

	private static class WorldGenNostrumFlowers extends WorldGenerator {
		
		private BlockState flowerState;
		
		public WorldGenNostrumFlowers(NostrumMagicaFlower.Type type) {
			flowerState = NostrumMagicaFlower.instance().getState(type);
		}
		
		@Override
		public boolean generate(World worldIn, Random rand, BlockPos position)
	    {
	        for (int i = 0; i < 64; ++i)
	        {
	            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

	            if (NostrumMagica.isBlockLoaded(worldIn, blockpos) && worldIn.isAirBlock(blockpos) && (worldIn.provider.hasSkyLight() || blockpos.getY() < 255) && NostrumMagicaFlower.instance().canBlockStay(worldIn, blockpos, flowerState))
	            {
	                worldIn.setBlockState(blockpos, flowerState, 2);
	            }
	        }

	        return true;
	    }
	}
	
	private static enum FlowerGen {
		CRYSTABLOOM(new WorldGenNostrumFlowers(NostrumMagicaFlower.Type.CRYSTABLOOM), 50, 100, 0.5f, 2f),
		MIDNIGHT_IRIS(new WorldGenNostrumFlowers(NostrumMagicaFlower.Type.MIDNIGHT_IRIS), 30, 80, -5f, 1f);
		
		private WorldGenerator gen;
		private int minY;
		private int maxY;
		private float minTemp;
		private float maxTemp;
		
		private FlowerGen(WorldGenerator gen, int minY, int maxY, float minTemp, float maxTemp) {
			this.gen = gen;
			this.minY = minY;
			this.maxY = maxY;
			this.minTemp = minTemp;
			this.maxTemp = maxTemp;
		}
		
		public float getMinTemp() {
			return minTemp;
		}

		public float getMaxTemp() {
			return maxTemp;
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
	
	public NostrumFlowerGenerator() {
		
	}
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (world.provider.getDimension() != 0)
			return;
		
		for (FlowerGen gen : FlowerGen.values())
			if (random.nextBoolean() && random.nextBoolean())
			runGenerator(gen.getGenerator(), world, random, chunkX, chunkZ,
					gen.getMinY(), gen.getMaxY(),
					gen.getMinTemp(), gen.getMaxTemp());
		
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
			int chunk_X, int chunk_Z, int minHeight, int maxHeight,
			float minTemp, float maxTemp) {
	    if (minHeight < 0 || maxHeight > 256 || minHeight > maxHeight)
	        throw new IllegalArgumentException("Illegal Height Arguments for WorldGenerator");

	    Biome b = world.getBiome(new BlockPos(chunk_X, 0, chunk_Z));
	    
	    int heightDiff = maxHeight - minHeight + 1;
        int x = chunk_X * 16 + rand.nextInt(16);
        int y = minHeight + rand.nextInt(heightDiff);
        int z = chunk_Z * 16 + rand.nextInt(16);
        
        BlockPos pos =  new BlockPos(x, y, z);
        float temp = b.getTemperature(pos);
        		
        if (temp >= minTemp && temp <= maxTemp)
        	generator.generate(world, rand, pos);
	}
	
}
