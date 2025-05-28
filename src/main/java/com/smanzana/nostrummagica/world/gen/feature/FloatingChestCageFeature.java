package com.smanzana.nostrummagica.world.gen.feature;

import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.dungeon.MagicBreakableContainerBlock;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class FloatingChestCageFeature extends Feature<NoneFeatureConfiguration> {
	
	protected final ResourceLocation lootTable;

	public FloatingChestCageFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
		lootTable = NostrumMagica.Loc("nostrum_shrine_room");
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		Random rand = context.random();
		BlockPos position = context.origin();
		
		if (!world.isEmptyBlock(position)) {
			return false;
		}
		
		System.out.println("At " + position);
		
		final BlockState[] baseMaterials = decideMaterials(world, position, rand);
		
		placeGroundRubble(world, position, 5, rand, baseMaterials);
		
		placeBaseRing(world, position, 5, rand, baseMaterials);
		placeUpperWall(world, position, 5, rand, baseMaterials);
		placeLowerWall(world, position, 5, rand, baseMaterials);
		
		placeChest(world, position.above(), rand);
		
		return true;
	}
	
	protected BlockState[] decideMaterials(WorldGenLevel world, BlockPos center, Random rand) {
		return new BlockState[] {
			Blocks.STONE_BRICKS.defaultBlockState()	
		};
	}
	
	protected void placeBaseRing(WorldGenLevel world, BlockPos center, int radius, Random rand, BlockState[] materials) {
		// r^2 = x^2 + y^2
		final int radsqr = radius * radius;
		
		for (int x = -radius; x <= radius; x++)
		for (int z = -radius; z <= radius; z++) {
			// Check if in circle
			final int sum = x * x + z * z;
			if (sum <= radsqr) {
				BlockState state = materials[rand.nextInt(materials.length)];
				world.setBlock(center.offset(x, 0, z), state, 2);
			}
		}
	}
	
	protected void placeUpperWall(WorldGenLevel world, BlockPos center, int radius, Random rand, BlockState[] materials) {
		// square of radius-2
		if (radius > 3) {
			radius = radius - 2;
			for (int x = -radius; x <= radius; x++)
			for (int z = -radius; z <= radius; z++) {
				if (x == -radius || x == radius || z == -radius || z == radius) {
					@Nullable Direction dir;
					if (x == 0) {
						dir = z == -radius ? Direction.NORTH : Direction.SOUTH;
					} else if (z == 0) {
						dir = x == -radius ? Direction.WEST : Direction.EAST;
					} else {
						dir = null;
					}
					genUpperColumn(world, center.offset(x, 1, z), rand, 3, materials, dir);
				}
			}
		}
	}
	
	protected void placeLowerWall(WorldGenLevel world, BlockPos center, int radius, Random rand, BlockState[] materials) {
		// r^2 = x^2 + y^2
		final int radsqr = radius * radius;
		center = center.below();
		
		for (int x = -radius; x <= radius; x++)
		for (int z = -radius; z <= radius; z++) {
			// Check if in circle
			final int sum = x * x + z * z;
			if (sum <= radsqr) {
				// get outside border-ish with manhattan distance
				if (Math.abs(x) + Math.abs(z) >= radius) {
					genLowerColumn(world, center.offset(x, 0, z), rand, 3, materials);
				}
			}
		}
	}
	
	protected void placeGroundRubble(WorldGenLevel world, BlockPos center, int radius, Random rand, BlockState[] materials) {
		// place in small circle on ground below
		if (radius > 3) {
			radius = radius - 2;
			
			// r^2 = x^2 + y^2
			final int radsqr = radius * radius;
			center = center.below();
			
			for (int x = -radius; x <= radius; x++)
			for (int z = -radius; z <= radius; z++) {
				// Check if in circle
				final int sum = x * x + z * z;
				if (sum <= radsqr) {
					final BlockPos pos = center.offset(x, 0, z);
					BlockPos top = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, pos);
					if (world.isWaterAt(top.below())) {
						top = world.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, pos);
					}
					if (world.isEmptyBlock(top) || world.getBlockState(top).getCollisionShape(world, top).isEmpty()) {
						if (rand.nextBoolean() && rand.nextBoolean()) {
							final BlockState state = materials[rand.nextInt(materials.length)];
							world.setBlock(top, state, 2);
						}
					}
				}
			}
		}
	}
	
	protected int genUpperColumn(WorldGenLevel world, BlockPos center, Random rand, int max, BlockState[] materials, @Nullable Direction doorDirection) {
		final int total = rand.nextInt(max + 1);
		int count = 0;
		while (count < total) {
			BlockState state;
			if (doorDirection != null && count <= 1) {
				state = Blocks.IRON_BARS.defaultBlockState()
						.setValue(IronBarsBlock.NORTH, doorDirection.getAxis() == Axis.X)
						.setValue(IronBarsBlock.SOUTH, doorDirection.getAxis() == Axis.X)
						.setValue(IronBarsBlock.EAST, doorDirection.getAxis() == Axis.Z)
						.setValue(IronBarsBlock.WEST, doorDirection.getAxis() == Axis.Z)
						;
			} else {
				state = materials[rand.nextInt(materials.length)];
			}
			world.setBlock(center.offset(0, count, 0), state, 2);
			
			count++;
		}
		return count;
	}
	
	protected int genLowerColumn(WorldGenLevel world, BlockPos center, Random rand, int max, BlockState[] materials) {
		final int total = rand.nextInt(max + 1);
		int count = 0;
		while (count < total) {
			BlockState state = materials[rand.nextInt(materials.length)];
			world.setBlock(center.offset(0, -count, 0), state, 2);
			count++;
		}
		return count;
	}
	
	protected boolean placeCandle(WorldGenLevel world, BlockPos at) {
		return false;
	}
	
	protected void placeChest(WorldGenLevel world, BlockPos at, Random rand) {
		final EMagicElement element = EMagicElement.getRandom(rand);
		MagicBreakableContainerBlock.PlaceWrappedLootChest(world, at, lootTable, rand, element);
	}

}
