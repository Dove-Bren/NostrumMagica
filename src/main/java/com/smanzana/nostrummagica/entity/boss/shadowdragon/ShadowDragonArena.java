package com.smanzana.nostrummagica.entity.boss.shadowdragon;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.util.JavaUtils;
import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.util.WorldUtil.IBlockWalker;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.LaserBlock;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.LaserBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

public class ShadowDragonArena {
	
	private static record Pillar(BlockPos topCenter, BoundingBox bounds) {}
	
	private static record ElementalLight(BlockPos source, EMagicElement element, BoundingBox bounds) {}
	
	protected final Level level;
	
	protected final AABB bounds;
	
	protected final Map<EMagicElement, ElementalLight> lights;
	
	protected final List<Pillar> pillars;
	
	protected final Pillar centerPillar;
	
	protected ShadowDragonArena(Level level, AABB bounds, Map<EMagicElement, ElementalLight> lights, List<Pillar> pillars, Pillar centerPillar) {
		this.level = level;
		this.bounds = bounds;
		this.lights = lights;
		this.pillars = pillars;
		this.centerPillar = centerPillar;
	}
	
	public static final ShadowDragonArena Capture(Level level, BlockPos scanStart) {
		// Large room, with a big central pillar. Several smaller pillars with (elemental) lasers on them.
		// Room floor block is a different type than pillars.
		// Boss spawns on top of central large pillar.
		List<Pillar> pillars = new ArrayList<>();
		Map<EMagicElement, ElementalLight> elementalLights = new EnumMap<>(EMagicElement.class);
		BlockPos min;
		BlockPos max;
		MutableBlockPos cursor = new MutableBlockPos();
		final BlockState pillarState;
		Pillar centerPillar;
		
		// Find bounding box
		{
			// Boss is on a platform. Find vertical space, then follow platform down by block type to find room base...
			// Except dungeon capture will only get shell of platform, so directly below boss is hollow. So walk pillar instead
			cursor.set(scanStart);
			
			// Find top of room above spawner
			while (!IsArenaEmptyBlock(level.getBlockState(cursor))) cursor.move(Direction.UP); // Move out of any pillar block to start with
			do { cursor.move(Direction.UP); } while (IsArenaEmptyBlock(level.getBlockState(cursor))); // Stops when not in an empty block anymore
			cursor.move(Direction.DOWN);
			final int yMax = cursor.getY(); // save for later
			
			// Scan. Can't just move down through pillar, since pillar will be hollow
			cursor.set(scanStart);
			while (IsArenaEmptyBlock(level.getBlockState(cursor))) cursor.move(Direction.DOWN); // move down out of air, if we started in air
			pillarState = level.getBlockState(cursor);
			BoundingBox startPillarBounds = new BoundingBox(cursor);
			WorldUtil.WalkConnectedBlocks(level, cursor.immutable(), new IBlockWalker() {

				@Override
				public boolean canVisit(BlockGetter arg0, BlockPos ignorePos, BlockState ignoreState, BlockPos pillarCheckPos, BlockState pillarCheckState, int dist) {
					return pillarCheckState == pillarState;
				}

				@SuppressWarnings("deprecation")
				@Override
				public WalkResult walk(BlockGetter arg0, BlockPos ignorePos, BlockState ignoreState, BlockPos pillarCheckPos, BlockState pillarCheckState, int dist, int walkCount, Consumer<BlockPos> addBlock) {
					startPillarBounds.encapsulate(pillarCheckPos);
					return WalkResult.CONTINUE;
				}
				
			}, 1024);
			
			// Record center pillar
			final BlockPos topCenter = startPillarBounds.getCenter().atY(startPillarBounds.maxY());
			centerPillar = new Pillar(topCenter, startPillarBounds);
			
			// get min Y from bounds
			final BlockPos bottomCenter = startPillarBounds.getCenter().atY(startPillarBounds.minY());
			cursor.set(bottomCenter);
			
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
					ElementalLight[] pillarLight = {null};
					WorldUtil.WalkConnectedBlocks(scanlevel, pos, new IBlockWalker() {

						@Override
						public boolean canVisit(BlockGetter arg0, BlockPos ignorePos, BlockState ignoreState, BlockPos pillarCheckPos, BlockState pillarCheckState, int dist) {
							return checkedBlocks.add(pillarCheckPos) && (pillarCheckState == pillarState || pillarCheckState.is(NostrumBlocks.laser));
						}

						@SuppressWarnings("deprecation")
						@Override
						public WalkResult walk(BlockGetter arg0, BlockPos ignorePos, BlockState ignoreState, BlockPos pillarCheckPos, BlockState pillarCheckState, int dist, int walkCount, Consumer<BlockPos> addBlock) {
							// Pillar, or lasers
							if (pillarCheckState.is(NostrumBlocks.laser)) {
								pillarLight[0] = new ElementalLight(pillarCheckPos.immutable(),
										((LaserBlock) pillarCheckState.getBlock()).getLaserElement(level, pillarCheckPos, pillarCheckState),
										BoundingBox.fromCorners(pillarCheckPos.above(), pillarCheckPos.atY(max.getY())));
								return WalkResult.CONTINUE;
							} else {
								pillarBounds.encapsulate(pillarCheckPos);
								return WalkResult.CONTINUE;
							}
						}
						
					}, 4096);
					
					final BlockPos topCenter = pillarBounds.getCenter().atY(pillarBounds.maxY());
					final Pillar pillar = new Pillar(topCenter, pillarBounds);
					
					// Record center block
					pillars.add(pillar);
					
					// If elemental, note it
					if (pillarLight[0] != null) {
						elementalLights.put(pillarLight[0].element(), pillarLight[0]);
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
//			System.out.println("Elemental lights:");
//				for (Map.Entry<EMagicElement, ElementalLight> entry : elementalLights.entrySet()) if (entry.getValue() != null) System.out.println("\t%s: %s".formatted(entry.getKey(), entry.getValue()));
//		}
		
		return new ShadowDragonArena(level, AABB.of(BoundingBox.fromCorners(min, max)), elementalLights, pillars, centerPillar);
	}
	
	public AABB getBounds() {
		return this.bounds;
	}
	
	public BlockPos getRandomPillarPos(Random rand) {
		return JavaUtils.GetRandom(this.pillars, rand).get().topCenter();
	}
	
	public BlockPos getCenterPillarFloatPos() {
		return this.centerPillar.topCenter().above(6);
	}
	
	public @Nullable EMagicElement getLaserElementBelow(Entity entity) {
		AABB entBounds = entity.getBoundingBox();
		
		for (EMagicElement element : EMagicElement.values()) {
			ElementalLight light = lights.get(element);
			if (light == null || !isLightActive(light)) {
				continue;
			}
			
			final BoundingBox bounds = light.bounds();
			if (entBounds.intersects(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX() + 1, bounds.maxY() + 1, bounds.maxZ() + 1)) {
				return light.element();
			}
		}
		return null;
	}
	
	public void activateFogClouds() {
		// Cloud above each light.
		// Height here should match height given to dragon for chain rendering
		for (ElementalLight light : lights.values()) {
			if (light == null) {
				continue;
			}
			
			BlockPos cloudCenter = light.source().above(Math.min(12, (light.bounds().maxY() - 3) - light.source().getY()));
			final int radius = 2;
			for (int i = -radius; i <= radius; i++)
			for (int j = -(radius-Math.abs(i)); j <= (radius-Math.abs(i)); j++)
			for (int k = -radius; k <= radius; k++) {
				level.setBlock(cloudCenter.offset(i, j, k), NostrumBlocks.fogEdgeBlock.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
			level.setBlockAndUpdate(cloudCenter.above(), Blocks.BEDROCK.defaultBlockState());
			level.setBlockAndUpdate(cloudCenter, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.DOWN));
		}
		
		// Put fog on dragon
		{
			final BlockPos center = centerPillar.topCenter().above();
			final int radius = 2;
			for (int i = -radius; i <= radius; i++)
			for (int j = -(radius-Math.abs(i)); j <= (radius-Math.abs(i)); j++)
			for (int k = -radius; k <= radius; k++) {
				final BlockPos pos = center.offset(i, k, j);
				if (level.isEmptyBlock(pos)) {
					level.setBlock(pos, NostrumBlocks.fogEdgeBlock.defaultBlockState(), Block.UPDATE_CLIENTS);
				}
			}
		}
	}
	
	public void removeFogClouds() {
		final IBlockWalker walker = new IBlockWalker() {
			@Override
			public boolean canVisit(BlockGetter arg0, BlockPos ignorePos, BlockState ignoreState, BlockPos walkPos, BlockState walkState, int dist) {
				return walkState.is(Blocks.BEDROCK)
						|| walkState.is(Blocks.AMETHYST_CLUSTER)
						|| walkState.is(NostrumBlocks.fogBlock)
						|| walkState.is(NostrumBlocks.fogEdgeBlock)
						|| walkState.is(NostrumBlocks.fogHiddenBlock)
						;
			}

			@Override
			public WalkResult walk(BlockGetter arg0, BlockPos ignorePos, BlockState ignoreState, BlockPos walkPos, BlockState walkState, int dist, int walkCount, Consumer<BlockPos> addBlock) {
				level.removeBlock(walkPos, false); 
				return WalkResult.CONTINUE;
			}
			
		};
		
		// Reverse what's in the setup
		for (ElementalLight light : lights.values()) {
			if (light == null) {
				continue;
			}
			
			BlockPos cloudCenter = light.source().above(Math.min(12, (light.bounds().maxY() - 3) - light.source().getY()));
			WorldUtil.WalkConnectedBlocks(level, cloudCenter, walker, 1024);
		}
		
		{
			final BlockPos center = centerPillar.topCenter().above().above(); // just one up is the spawner, and we don't want to get rid of that!
			WorldUtil.WalkConnectedBlocks(level, center, walker, 64);
		}
	}
	
	public void deactivateLights() {
		for (EMagicElement element : EMagicElement.values()) {
			deactivateLight(element);
		}
	}
	
	public void activateLights() {
		for (EMagicElement element : EMagicElement.values()) {
			activateLight(element);
		}
	}
	
	public void activateLight(EMagicElement element) {
		ElementalLight light = lights.get(element);
		if (light == null) {
			return;
		}
			
		this.setLightActive(light, true);
	}
	
	public void deactivateLight(EMagicElement element) {
		ElementalLight light = lights.get(element);
		if (light == null) {
			return;
		}
			
		this.setLightActive(light, false);
	}
	
	public void clearExtraBlocks() {
		// Get rid of elemental blocks above lasers, and remove any fog floor or other restriction if any are present
		for (ElementalLight light : lights.values()) {
			if (light == null) {
				continue;
			}
			
			level.removeBlock(light.source().above().above(), false);
		}
		
		removeFogClouds();
	}
	
	public void resetArena() {
		for (ElementalLight light : lights.values()) {
			if (light == null) {
				continue;
			}
			
			resetLightSource(light);
		}
		activateFogClouds();
	}
	
	protected static final boolean IsArenaEmptyBlock(BlockState state) {
		return state.isAir() || state.getFluidState().is(FluidTags.WATER);
	}
	
	protected void setLightActive(ElementalLight source, boolean active) {
		NostrumBlocks.laser.setLaserState(level, source.source(), level.getBlockState(source.source()), active);
	}
	
	protected boolean isLightActive(ElementalLight source) {
		return NostrumBlocks.laser.isLaserEnabled(level, level.getBlockState(source.source()), source.source());
	}
	
	protected void resetLightSource(ElementalLight source) {
		// Light should be activated, and block above it should be an elemental stone of a different kind.
		// When making the dungeon I think I rotated like ender blocked by fire, fire blocked by lightning, etc.
		// But we'll just be lazy and do opposite when resetting
		EMagicElement blockType = source.element.getOpposite();
		BlockState blockState = NostrumBlocks.elementalStone(blockType).defaultBlockState();
		level.setBlock(source.source().above().above(), blockState, Block.UPDATE_ALL);
				
		setLightActive(source, true);
	}

	public boolean areLightsUnblocked() {
		for (ElementalLight light : lights.values()) {
			if (light == null) {
				continue;
			}
			
			// Dupes logic to get Y level from fog creation method above
			int cloudCenterY = light.source().getY() + Math.min(12, (light.bounds().maxY() - 3) - light.source().getY());
			
			BlockEntity ent = level.getBlockEntity(light.source());
			if (ent == null || !(ent instanceof LaserBlockEntity laser)
					|| laser.getLaserWholeSegments() < (cloudCenterY - light.source().getY())) {
				return false;
			}
		}
		
		return true;
	}

}
