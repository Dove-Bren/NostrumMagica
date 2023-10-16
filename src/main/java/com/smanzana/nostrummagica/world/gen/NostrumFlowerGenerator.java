package com.smanzana.nostrummagica.world.gen;

import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.world.gen.NostrumFlowerGenerator.NostrumFlowerConfig;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class NostrumFlowerGenerator extends Feature<NostrumFlowerConfig> {
	
	public static final class NostrumFlowerConfig implements IFeatureConfig {
		
		public static NostrumFlowerConfig deserialize(Dynamic<?> dynamic) {
			return new NostrumFlowerConfig();
		}

		@Override
		public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
			return new Dynamic<>(ops);
		}
		
		public boolean allowedInDimension(DimensionType dimension) {
			return dimension == DimensionType.OVERWORLD;
		}
		
		public boolean allowedAtPos(IWorld world, BlockPos pos) {
			return true;
		}
		
	}

	private static class WorldGenNostrumFlowers {
		
		private BlockState flowerState;
		
		public WorldGenNostrumFlowers(BlockState flowerState) {
			this.flowerState = flowerState;
		}
		
		public boolean generate(IWorld worldIn, Random rand, BlockPos position) {
	        for (int i = 0; i < 64; ++i) {
	            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

	            if (/*NostrumMagica.isBlockLoaded(worldIn, blockpos) &&*/ worldIn.isAirBlock(blockpos) && (worldIn.getDimension().hasSkyLight() || blockpos.getY() < 255) && flowerState.isValidPosition(worldIn, blockpos)) {
	                worldIn.setBlockState(blockpos, flowerState, 2);
	            }
	        }

	        return true;
	    }
	}
	
	private static enum FlowerGen {
		CRYSTABLOOM(new WorldGenNostrumFlowers(NostrumBlocks.crystabloom.getDefaultState()), 50, 100, 0.5f, 2f),
		MIDNIGHT_IRIS(new WorldGenNostrumFlowers(NostrumBlocks.midnightIris.getDefaultState()), 30, 80, -5f, 1f);
		
		private WorldGenNostrumFlowers gen;
		private int minY;
		private int maxY;
		private float minTemp;
		private float maxTemp;
		
		private FlowerGen(WorldGenNostrumFlowers gen, int minY, int maxY, float minTemp, float maxTemp) {
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

		public WorldGenNostrumFlowers getGenerator() {
			return gen;
		}

		public int getMinY() {
			return minY;
		}

		public int getMaxY() {
			return maxY;
		}
	}
	
	public NostrumFlowerGenerator(Function<Dynamic<?>, ? extends NostrumFlowerConfig> configFactoryIn) {
		super(configFactoryIn);
	}
	
	@Override
	public boolean place(@Nonnull IWorld world, @Nonnull ChunkGenerator<? extends GenerationSettings> generator, Random random, BlockPos pos, NostrumFlowerConfig config) {
		if (!config.allowedInDimension(world.getDimension().getType())) {
			return false;
		}
		
		if (!config.allowedAtPos(world, pos)) {
			return false;
		}
		
		boolean generated = false;
		for (FlowerGen gen : FlowerGen.values()) {
			if (random.nextBoolean() && random.nextBoolean()) {
				runGenerator(gen.getGenerator(), world, random, pos.getX(), pos.getZ(),
					gen.getMinY(), gen.getMaxY(),
					gen.getMinTemp(), gen.getMaxTemp());
				generated = true;
			}
		}
		
		return generated;
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
	private void runGenerator(WorldGenNostrumFlowers generator, IWorld world, Random rand,
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
