package com.smanzana.nostrummagica.entity.boss.primalmage;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.util.WorldUtil.IBlockWalker;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PrimalMageArena {
	
	private static record Pillar(BlockPos topCenter, BoundingBox bounds, BoundingBox onBounds) {}
	
	private static record ElementalPillar(Pillar pillar, BlockPos topBlock) {}

	protected final AABB bounds;
	
	protected final List<Pillar> pillars;
	
	protected final Map<EMagicElement, ElementalPillar> elementalPillars;
	
	protected PrimalMageArena(AABB bounds, List<Pillar> pillars, Map<EMagicElement, ElementalPillar> elementalPillars) {
		this.bounds = bounds;
		this.pillars = pillars;
		this.elementalPillars = elementalPillars;
	}
	
	public static final PrimalMageArena Capture(Level level, BlockPos scanStart) {
		// Large room with recessed walls on some axes. Pillar platforms, including one that the boss is spawned in at.
		// Some of the pillars have elemental stone attached to them, and are considered elemental platforms.
		// There are some smaller pillars whos base does not touch the ground that are for player movement, but not
		// intended for boss mechanics.
		
		List<Pillar> pillars = new ArrayList<>();
		Map<EMagicElement, ElementalPillar> elementalPillars = new EnumMap<>(EMagicElement.class);
		BlockPos min;
		BlockPos max;
		MutableBlockPos cursor = new MutableBlockPos();
		final BlockState pillarState;
		
		// Found bounding box (min, max)
		{
		
			// Boss is on a platform. Find vertical space, then follow platform down by block type to find room base
			cursor.set(scanStart);
			
			// Find top of room above spawner
			while (!IsArenaEmptyBlock(level.getBlockState(cursor))) cursor.move(Direction.UP); // Move out of any pillar block to start with
			do { cursor.move(Direction.UP); } while (IsArenaEmptyBlock(level.getBlockState(cursor))); // Stops when not in an empty block anymore
			cursor.move(Direction.DOWN);
			final int yMax = cursor.getY(); // save for later
			
			
			// Move down from spawn pos till we are out of pillar
			cursor.set(scanStart);
			while (IsArenaEmptyBlock(level.getBlockState(cursor))) cursor.move(Direction.DOWN); // move down out of air, if we started in air
			pillarState = level.getBlockState(scanStart);
			do { cursor.move(Direction.DOWN); } while (level.getBlockState(cursor) == pillarState);
	
			// This is min y
			cursor.move(Direction.UP);
			final BlockPos bottomCenter = cursor.immutable(); // copy for restoring later
			
			// Walk west/north to get min coord, looking for a wall
			do { cursor.move(Direction.WEST); } while (level.getBlockState(cursor) == pillarState || IsArenaEmptyBlock(level.getBlockState(cursor))); cursor.move(Direction.EAST);
			do { cursor.move(Direction.NORTH); } while (level.getBlockState(cursor) == pillarState || IsArenaEmptyBlock(level.getBlockState(cursor))); cursor.move(Direction.SOUTH);
			
			min = new BlockPos(cursor.immutable());
			
			cursor.set(bottomCenter);
					
			// Walk east/south to get max coord, looking for a wall
			do { cursor.move(Direction.EAST); } while (level.getBlockState(cursor) == pillarState || IsArenaEmptyBlock(level.getBlockState(cursor))); cursor.move(Direction.WEST);
			do { cursor.move(Direction.SOUTH); } while (level.getBlockState(cursor) == pillarState || IsArenaEmptyBlock(level.getBlockState(cursor))); cursor.move(Direction.NORTH);
			
			max = new BlockPos(cursor.getX(), yMax, cursor.getZ());
		}
		
		// Scan for pillars. Optimize by only scanning bottom layer, since we want mechanic pillars to touch the ground
		Set<BlockPos> checkedBlocks = new HashSet<>();
		WorldUtil.ScanBlocks(level, min, max.atY(min.getY()), (scanlevel, pos) -> {
			// Occasionally we run subroutines that scan more blocks, so optimize and don't double check those
			if (checkedBlocks.add(pos)) {
				BlockState state = scanlevel.getBlockState(pos);
				if (state == pillarState) {
					BoundingBox pillarBounds = new BoundingBox(pos);
					List<BlockPos> pillarBlocks = new ArrayList<>();
					pillarBlocks.add(pos);
					EMagicElement[] pillarElement = {null};
					MutableBlockPos topElementalBlock = new MutableBlockPos(); topElementalBlock.set(pos);
					WorldUtil.WalkConnectedBlocks(scanlevel, pos, new IBlockWalker() {

						@Override
						public boolean canVisit(BlockGetter arg0, BlockPos ignorePos, BlockState ignoreState, BlockPos pillarCheckPos, BlockState pillarCheckState, int dist) {
							return checkedBlocks.add(pillarCheckPos) && (pillarCheckState == pillarState || getStoneElement(arg0, pillarCheckPos, pillarCheckState) != null);
						}

						@SuppressWarnings("deprecation")
						@Override
						public WalkResult walk(BlockGetter arg0, BlockPos ignorePos, BlockState ignoreState, BlockPos pillarCheckPos, BlockState pillarCheckState, int dist, int walkCount, Consumer<BlockPos> addBlock) {
							// Pillar, or elemental crystal blocks come here
							@Nullable EMagicElement element = getStoneElement(arg0, pillarCheckPos, pillarCheckState);
							if (element != null) {
								pillarElement[0] = element;
								if (pillarCheckPos.getY() > topElementalBlock.getY()) {
									topElementalBlock.set(pillarCheckPos);
								}
								return WalkResult.CONTINUE;
							} else {
								pillarBlocks.add(pillarCheckPos);
								pillarBounds.encapsulate(pillarCheckPos);
								return WalkResult.CONTINUE;
							}
						}
						
					}, 4096);
					
					final BlockPos topCenter = pillarBounds.getCenter().atY(pillarBounds.maxY());
					final Pillar pillar = new Pillar(topCenter, pillarBounds, pillarBounds.moved(0, 2, 0));
					
					// Record center block
					pillars.add(pillar);
					
					// If elemental, note it
					if (pillarElement[0] != null) {
						elementalPillars.put(pillarElement[0], new ElementalPillar(pillar, topElementalBlock));
					}
				}
			}
			
			// Keep scanning
			return true;
		});
		
//		// Debug print
//		{
//			System.out.println("Discovered arena with bounds %s  <->  %s".formatted(min, max));
//			System.out.println("Found %d pillars: ".formatted(pillars.size()));
//				for (Pillar pillar : pillars) System.out.println("\t%s".formatted(pillar));
//			System.out.println("Elemental pillars:");
//				for (Map.Entry<EMagicElement, ElementalPillar> entry : elementalPillars.entrySet()) if (entry.getValue() != null) System.out.println("\t%s: %s".formatted(entry.getKey(), entry.getValue()));
//		}
		
		return new PrimalMageArena(AABB.of(BoundingBox.fromCorners(min, max)), pillars, elementalPillars);
		
	}
	
	protected static final boolean IsArenaEmptyBlock(BlockState state) {
		return state.isAir() || state.getFluidState().is(FluidTags.WATER);
	}
	
	protected static final @Nullable EMagicElement getStoneElement(BlockGetter level, BlockPos pos, BlockState state) {
		if (state.is(NostrumBlocks.fireStone)) {
			return EMagicElement.FIRE;
		}
		if (state.is(NostrumBlocks.iceStone)) {
			return EMagicElement.ICE;
		}
		if (state.is(NostrumBlocks.windStone)) {
			return EMagicElement.WIND;
		}
		if (state.is(NostrumBlocks.earthStone)) {
			return EMagicElement.EARTH;
		}
		if (state.is(NostrumBlocks.lightningStone)) {
			return EMagicElement.LIGHTNING;
		}
		if (state.is(NostrumBlocks.enderStone)) {
			return EMagicElement.ENDER;
		}
		
		if (state.is(NostrumBlocks.summonGhostBlock)) {
			BlockState innerState = NostrumBlocks.summonGhostBlock.getGhostState(state, level, pos);
			if (innerState != state) {
				return getStoneElement(level, pos, innerState);
			}
		}
		return null;
	}
	
	public AABB getBounds() {
		return this.bounds;
	}
	
	public boolean isOnElementalPlatform(Vec3 position, EMagicElement element) {
		@Nullable ElementalPillar pillar = this.elementalPillars.get(element);
		if (pillar != null) {
			return pillar.pillar.onBounds.isInside(new BlockPos(position));
		}
		return false;
	}
	
	public @Nullable BlockPos getElementalCrystal(EMagicElement element) {
		@Nullable ElementalPillar pillar = this.elementalPillars.get(element);
		if (pillar != null) {
			return pillar.topBlock;
		}
		return null;
	}
	
	public @Nullable BlockPos getElementalCrystalFloatPos(EMagicElement element) {
		@Nullable ElementalPillar pillar = this.elementalPillars.get(element);
		if (pillar != null) {
			final BlockPos pillarTop = pillar.pillar.topCenter;
			final int yDiff = Math.max(2, Math.min((((int)this.bounds.maxY - pillarTop.getY()) - 2), 6));
			return pillarTop.above(yDiff);
		}
		return null;
	}
	
	public BlockPos getRandomPillarCenter(Random rand) {
		if (pillars.isEmpty()) {
			return null;
		}
		
		final int index = rand.nextInt(pillars.size());
		return pillars.get(index).topCenter;
		
	}
	
	public BlockPos getRandomPillarFloatPos(Random rand) {
		if (pillars.isEmpty()) {
			return null;
		}
		
		final int index = rand.nextInt(pillars.size());
		final Pillar pillar = pillars.get(index);
		final BlockPos pillarTop = pillar.topCenter;
		final int yDiff = Math.max(2, Math.min((((int)this.bounds.maxY - pillarTop.getY()) - 2), 6));
		return pillarTop.above(yDiff);
	}
}
