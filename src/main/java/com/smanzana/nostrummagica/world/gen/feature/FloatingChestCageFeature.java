package com.smanzana.nostrummagica.world.gen.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.CandleBlock;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.MagicBreakableContainerBlock;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.Tags;

public class FloatingChestCageFeature extends Feature<NoneFeatureConfiguration> {
	
	protected final ResourceLocation lootTable;

	public FloatingChestCageFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
		lootTable = NostrumMagica.Loc("chests/floating_cage");
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		Random rand = context.random();
		BlockPos position = context.origin();
		
		if (!world.isEmptyBlock(position)) {
			return false;
		}
		
		final BlockState[] baseMaterials = decideMaterials(world, position, rand);
		
		placeGroundRubble(world, position, 5, rand, baseMaterials);
		
		placeBaseRing(world, position, 5, rand, baseMaterials);
		placeUpperWall(world, position, 5, rand, baseMaterials);
		placeLowerWall(world, position, 5, rand, baseMaterials);
		
		placeChest(world, position.above(), rand);
		
		return true;
	}
	
	protected BlockState[] decideMaterials(WorldGenLevel world, BlockPos center, Random rand) {
		final Holder<Biome> biome = world.getBiome(center);
		if (biome.containsTag(Tags.Biomes.IS_DRY_OVERWORLD)) {
			return new BlockState[] {
					Blocks.SANDSTONE.defaultBlockState(),
					Blocks.SANDSTONE.defaultBlockState(),
					Blocks.CUT_SANDSTONE.defaultBlockState(),
					Blocks.CHISELED_SANDSTONE.defaultBlockState(),
					Blocks.SMOOTH_SANDSTONE.defaultBlockState(),
				};
		}
		
		if (biome.containsTag(Tags.Biomes.IS_WATER)) {
			return new BlockState[] {
					Blocks.PRISMARINE_BRICKS.defaultBlockState(),
					Blocks.PRISMARINE_BRICKS.defaultBlockState(),
					Blocks.PRISMARINE_BRICKS.defaultBlockState(),
					Blocks.PRISMARINE.defaultBlockState(),
					Blocks.DARK_PRISMARINE.defaultBlockState(),
				};
		}
		
		return new BlockState[] {
			Blocks.STONE_BRICKS.defaultBlockState(),
			Blocks.STONE_BRICKS.defaultBlockState(),
			Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),
			Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
			Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
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
		
		if (rand.nextBoolean()) {
			// one free mani crystal below center
			world.setBlock(center.below(), NostrumBlocks.maniCrystalBlock.defaultBlockState(), 2);
		}
	}
	
	protected void placeUpperWall(WorldGenLevel world, BlockPos center, int radius, Random rand, BlockState[] materials) {
		Set<BlockPos> candleSpots = new HashSet<>();
		
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
					final BlockPos at = center.offset(x, 1, z);
					if (genUpperColumn(world, at, rand, 3, materials, dir) >= 2) {
						// candle candidate
						if (dir == null // not a 'door/gate' spot
								&& !((x == -radius || x == radius) && (z == -radius || z == radius)) // not a corner
								) {
							// figure out which direction is in
							BlockPos pos;
							if (x == -radius) {
								pos = at.east();
							} else if (x == radius) {
								pos = at.west();
							} else if (z == -radius) {
								pos = at.south();
							} else {
								pos = at.north();
							}
							candleSpots.add(pos.above());
						}
					}
				}
			}
		}
		
		if (!candleSpots.isEmpty()) {
			int count = rand.nextInt(4);
			List<BlockPos> spots = new ArrayList<>(candleSpots);
			Collections.shuffle(spots);
			while (!spots.isEmpty() && count-- > 0) {
				final BlockPos at = spots.remove(spots.size() - 1);
				placeCandle(world, at);
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
		if (doorDirection != null) {
			this.markAboveForPostProcessing(world, center.below());
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
		BlockState blockstate = NostrumBlocks.candle.defaultBlockState().setValue(CandleBlock.LIT, true);
		
		for(Direction direction : Direction.Plane.HORIZONTAL) {
			direction = direction.getOpposite();
			if (CandleBlock.FACING.getPossibleValues().contains(direction)) {
				Direction direction1 = direction;
				blockstate = blockstate.setValue(CandleBlock.FACING, direction1);
				if (blockstate.canSurvive(world, at)) {
					world.setBlock(at, blockstate, 2);
					return true;
				}
			}
		}
		return false;
	}
	
	protected void placeChest(WorldGenLevel world, BlockPos at, Random rand) {
		final EMagicElement element = EMagicElement.getRandom(rand);
		MagicBreakableContainerBlock.PlaceWrappedLootChest(world, at, lootTable, rand, element);
	}

}
