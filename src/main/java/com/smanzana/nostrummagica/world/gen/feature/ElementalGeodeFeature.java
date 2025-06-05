package com.smanzana.nostrummagica.world.gen.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Vanilla Geode, but with elemental crystal in the center
 */
public class ElementalGeodeFeature extends Feature<NoneFeatureConfiguration> {

	public ElementalGeodeFeature(Codec<NoneFeatureConfiguration> config) {
		super(config);
	}
	
	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		final BlockPos origin = context.origin();
		final Random rand = context.random();
		Predicate<BlockState> predicate = isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
		
		// Create encased empty sphere
		final int radius = rand.nextInt(6) + 6;
		carveSphere(world, origin, rand, radius, predicate);
		
		// Place actual elemental stone
		{
			final BlockState state = NostrumBlocks.elementalCrystal.setElement(NostrumBlocks.elementalCrystal.defaultBlockState(), EMagicElement.getRandom(rand)); 
			this.safeSetBlock(world, origin, state, predicate);
		}
		
		// Place water mini pool hint on surface
		final int topY = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
		final BlockPos topPos = new BlockPos(origin.getX(), topY, origin.getZ()).below();
		if (world.isWaterAt(topPos) || world.getBlockState(topPos).is(BlockTags.ICE)) {
			placeWaterSpout(world, topPos, rand, predicate);
		} else {
			carvePureWaterPond(world, topPos, rand, 2 + rand.nextInt(3), predicate);
		}
		
		return true;
	}
	
	protected void carveSphere(WorldGenLevel world, BlockPos origin, Random rand, int radius, Predicate<BlockState> safetyCheck) {
		for (int x = -radius; x <= radius; x++)
		for (int y = -radius; y <= radius; y++)
		for (int z = -radius; z <= radius; z++) {
			final double dist = Math.sqrt(x * x + y * y + z * z);
			if (dist > radius) {
				// ignore
			} else if (dist > radius - 2) {
				// edge?
				this.safeSetBlock(world, origin.offset(x, y, z), Blocks.BLACKSTONE.defaultBlockState(), safetyCheck);
			} else {
				this.safeSetBlock(world, origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), safetyCheck);
			}
		}
	}
	
	protected void carvePureWaterPond(WorldGenLevel world, BlockPos origin, Random rand, int radius, Predicate<BlockState> safetyCheck) {
		// Replace water with pure water. Mostly on surface level and then in a very narrow cone down a bit
		Set<BlockPos> visitted = new HashSet<>();
		Set<BlockPos> placed = new HashSet<>();
		List<BlockPos> next = new LinkedList<>();
		List<BlockPos> border = new ArrayList<>();
		Consumer<BlockPos> checkAndAdd = p -> {if (visitted.add(p)) next.add(p);};

		visitted.add(origin);
		next.add(origin);
		
		final float chanceBase = 1f + .2f * ((float) (radius-1)/2f); // extra .2 per half of radius
		final float chancePenalty = .2f; // 20% less chance of spawning more per distance from center.
		
		while (!next.isEmpty()) {
			final BlockPos pos = next.remove(0);
			final int hDistFromCenter = Math.abs(origin.getX() - pos.getX()) + Math.abs(origin.getZ() - pos.getZ());
			final int distFromCenter = pos.distManhattan(origin);
			final float placeChance;
			if (pos.getY() == origin.getY() && hDistFromCenter < (radius/2)) {
				placeChance = 1f;
			} else {
				placeChance = chanceBase - (chancePenalty * distFromCenter);
			}
			
			if (pos.getY() < world.getMinBuildHeight() || pos.getY() > world.getMaxBuildHeight()) {
				continue;
			}
			
			if (placeChance > 0f && rand.nextFloat() < placeChance) {
				// place, and mark neighbors as candidates
				this.safeSetBlock(world, pos, NostrumBlocks.pureWater.defaultBlockState(), safetyCheck);
				placed.add(pos);
				
				if (pos.getY() == origin.getY()) {
					checkAndAdd.accept(pos.north());
					checkAndAdd.accept(pos.south());
					checkAndAdd.accept(pos.east());
					checkAndAdd.accept(pos.west());
				} else {
					border.add(pos.north());
					border.add(pos.south());
					border.add(pos.east());
					border.add(pos.west());
				}
				
				checkAndAdd.accept(pos.below());
			} else {
				border.add(pos);
			}
		}
		
		// For each border pos, if it didn't end up becoming water make it a border piece
		for (BlockPos borderPos : border) {
			if (!placed.contains(borderPos)) {
				// take advantage of the fact that we build top down to guess that if we are going to place ablock above,
				// it will have already happened
				final BlockState state = (world.isEmptyBlock(borderPos.above()) ? Blocks.GRASS_BLOCK : Blocks.DIRT).defaultBlockState();
				this.safeSetBlock(world, borderPos, state, safetyCheck);
			}
		}
	}
	
	protected void placeWaterSpout(WorldGenLevel world, BlockPos origin, Random rand, Predicate<BlockState> safetyCheck) {
		// Replace water with pure water. Mostly on surface level and then in a very narrow cone down a bit
		Set<BlockPos> visitted = new HashSet<>();
		Set<BlockPos> placed = new HashSet<>();
		List<BlockPos> next = new LinkedList<>();
		List<BlockPos> border = new ArrayList<>();
		Consumer<BlockPos> checkAndAdd = p -> {if (visitted.add(p)) next.add(p);};
		
		visitted.clear();
		next.clear();
		visitted.add(origin);
		next.add(origin);
		
		final float chanceBase = 1f + .2f * ((float) (3-1)/2f); // extra .2 per half of radius
		final float chancePenalty = .2f; // 20% less chance of spawning more per distance from center.
		
		while (!next.isEmpty()) {
			final BlockPos pos = next.remove(0);
			final int hDistFromCenter = Math.abs(origin.getX() - pos.getX()) + Math.abs(origin.getZ() - pos.getZ());
			final int distFromCenter = pos.distManhattan(origin);
			
			if (!world.isWaterAt(pos) && !world.getBlockState(pos).is(BlockTags.ICE)) {
				continue;
			}
			
			if (pos.getY() < world.getMinBuildHeight() || pos.getY() > world.getMaxBuildHeight()) {
				continue;
			}
			
			final float placeChance;
			if (pos.getY() == origin.getY() && hDistFromCenter < 2) {
				placeChance = 1f;
			} else {
				placeChance = chanceBase - (chancePenalty * distFromCenter);
			}
			
			if (placeChance > 0f && rand.nextFloat() < placeChance) {
				// place, and mark neighbors as candidates
				this.safeSetBlock(world, pos, NostrumBlocks.pureWater.defaultBlockState(), safetyCheck);
				placed.add(pos);
				
				if (pos.getY() == origin.getY()) {
					checkAndAdd.accept(pos.north());
					checkAndAdd.accept(pos.south());
					checkAndAdd.accept(pos.east());
					checkAndAdd.accept(pos.west());
				} else {
					border.add(pos.north());
					border.add(pos.south());
					border.add(pos.east());
					border.add(pos.west());
				}
				checkAndAdd.accept(pos.below());
			} else {
				border.add(pos);
			}
		}
		
		// For each border pos, if it didn't end up becoming water make it a border piece
		for (BlockPos borderPos : border) {
			if (!placed.contains(borderPos) && world.isWaterAt(borderPos)) {
				// take advantage of the fact that we build top down to guess that if we are going to place ablock above,
				// it will have already happened
				final BlockState state = Blocks.BLUE_ICE.defaultBlockState();
				this.safeSetBlock(world, borderPos, state, safetyCheck);
			}
		}
		
		// Make bubble columns below for some random blocks
		List<BlockPos> allPos = border; // reuse
		allPos.clear();
		allPos.addAll(placed);
		Collections.shuffle(allPos);
		final int colCount = rand.nextInt(4) + 2;
		MutableBlockPos cursor = new MutableBlockPos();
		for (int i = 0; i < Math.min(colCount, allPos.size()); i++) {
			cursor.set(allPos.get(i));
			while (cursor.getY() > world.getMinBuildHeight() && (world.isWaterAt(cursor) || world.getBlockState(cursor).is(BlockTags.ICE))) {
				cursor.move(Direction.DOWN);
			}
			this.safeSetBlock(world, cursor.immutable(), Blocks.SOUL_SAND.defaultBlockState(), safetyCheck);
		}
	}

}
