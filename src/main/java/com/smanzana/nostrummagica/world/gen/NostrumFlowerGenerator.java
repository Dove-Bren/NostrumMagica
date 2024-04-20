package com.smanzana.nostrummagica.world.gen;

import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.DefaultFlowersFeature;

public class NostrumFlowerGenerator extends DefaultFlowersFeature {
	
	int unused; // DELETE?
	public NostrumFlowerGenerator() {
		super(BlockClusterFeatureConfig.field_236587_a_);
	}

//	@Override
//	public BlockState getFlowerToPlace(Random random, BlockPos pos, BlockClusterFeatureConfig config) {
//		final Block[] FLOWERS = new Block[] {NostrumBlocks.crystabloom, NostrumBlocks.midnightIris};
//		
//		double d0 = MathHelper.clamp((1.0D + Biome.INFO_NOISE.getValue((double)pos.getX() / 48.0D, (double)pos.getZ() / 48.0D)) / 2.0D, 0.0D, 0.9999D);
//		Block block = FLOWERS[(int)(d0 * (double)FLOWERS.length)];
//		return block == Blocks.BLUE_ORCHID ? Blocks.POPPY.getDefaultState() : block.getDefaultState();
//	}

//	public static final class NostrumFlowerConfig implements IFeatureConfig {
//		
//		public static NostrumFlowerConfig deserialize(Dynamic<?> dynamic) {
//			return new NostrumFlowerConfig();
//		}
//
//		@Override
//		public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
//			return new Dynamic<>(ops);
//		}
//		
//		public boolean allowedInDimension(DimensionType dimension) {
//			return dimension == DimensionType.OVERWORLD;
//		}
//		
//		public boolean allowedAtPos(IWorld world, BlockPos pos) {
//			return true;
//		}
//		
//	}
//
//	private static class WorldGenNostrumFlowers {
//		
//		private BlockState flowerState;
//		
//		public WorldGenNostrumFlowers(BlockState flowerState) {
//			this.flowerState = flowerState;
//		}
//		
//		public boolean generate(IWorld worldIn, Random rand, BlockPos position) {
//	        for (int i = 0; i < 16; ++i) {
//	        	final int x = position.getX() + rand.nextInt(16);
//	        	final int z = position.getZ() + rand.nextInt(16);
//	        	final int y = worldIn.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
//	            BlockPos blockpos = new BlockPos(x, y, z);
//
//	            if (/*NostrumMagica.isBlockLoaded(worldIn, blockpos) &&*/ worldIn.isAirBlock(blockpos) && (worldIn.getDimension().hasSkyLight() || blockpos.getY() < 255) && flowerState.isValidPosition(worldIn, blockpos)) {
//	                worldIn.setBlockState(blockpos, flowerState, 2);
//	            }
//	        }
//
//	        return true;
//	    }
//	}
//	
//	private static enum FlowerGen {
//		CRYSTABLOOM(new WorldGenNostrumFlowers(NostrumBlocks.crystabloom.getDefaultState()), 50, 100, 0.5f, 2f),
//		MIDNIGHT_IRIS(new WorldGenNostrumFlowers(NostrumBlocks.midnightIris.getDefaultState()), 30, 80, -5f, 1f);
//		
//		private WorldGenNostrumFlowers gen;
//		private int minY;
//		private int maxY;
//		private float minTemp;
//		private float maxTemp;
//		
//		private FlowerGen(WorldGenNostrumFlowers gen, int minY, int maxY, float minTemp, float maxTemp) {
//			this.gen = gen;
//			this.minY = minY;
//			this.maxY = maxY;
//			this.minTemp = minTemp;
//			this.maxTemp = maxTemp;
//		}
//		
//		public float getMinTemp() {
//			return minTemp;
//		}
//
//		public float getMaxTemp() {
//			return maxTemp;
//		}
//
//		public WorldGenNostrumFlowers getGenerator() {
//			return gen;
//		}
//
//		public int getMinY() {
//			return minY;
//		}
//
//		public int getMaxY() {
//			return maxY;
//		}
//	}
//	
//	public NostrumFlowerGenerator(Function<Dynamic<?>, ? extends NostrumFlowerConfig> configFactoryIn) {
//		super(configFactoryIn);
//	}
//	
//	@Override
//	public boolean place(@Nonnull IWorld world, @Nonnull ChunkGenerator<? extends GenerationSettings> generator, Random random, BlockPos pos, NostrumFlowerConfig config) {
//		if (!config.allowedInDimension(world.getDimension().getType())) {
//			return false;
//		}
//		
//		if (!config.allowedAtPos(world, pos)) {
//			return false;
//		}
//		
//		boolean generated = false;
//		for (FlowerGen gen : FlowerGen.values()) {
//			if (random.nextBoolean() && random.nextBoolean()) {
//				runGenerator(gen.getGenerator(), world, random, pos.getX(), pos.getZ(),
//					gen.getMinY(), gen.getMaxY(),
//					gen.getMinTemp(), gen.getMaxTemp());
//				generated = true;
//			}
//		}
//		
//		return generated;
//	}
//
//	/**
//	 * Taken from bedrockminer's worldgen tutorial
//	 * http://bedrockminer.jimdo.com/modding-tutorials/basic-modding-1-8/world-generation/
//	 * @param generator
//	 * @param world
//	 * @param rand
//	 * @param chunk_X
//	 * @param chunk_Z
//	 * @param minHeight
//	 * @param maxHeight
//	 */
//	private void runGenerator(WorldGenNostrumFlowers generator, IWorld world, Random rand,
//			int chunk_X, int chunk_Z, int minHeight, int maxHeight,
//			float minTemp, float maxTemp) {
//	    if (minHeight < 0 || maxHeight > 256 || minHeight > maxHeight)
//	        throw new IllegalArgumentException("Illegal Height Arguments for WorldGenerator");
//
//	    Biome b = world.getBiome(new BlockPos(chunk_X, 0, chunk_Z));
//	    
//	    int heightDiff = maxHeight - minHeight + 1;
//        int x = chunk_X * 16 + rand.nextInt(16);
//        int y = minHeight + rand.nextInt(heightDiff);
//        int z = chunk_Z * 16 + rand.nextInt(16);
//        
//        BlockPos pos =  new BlockPos(x, y, z);
//        float temp = b.getTemperature(pos);
//        		
//        if (temp >= minTemp && temp <= maxTemp)
//        	generator.generate(world, rand, pos);
//	}
	
}
