package com.smanzana.nostrummagica.world.blueprints;

import javax.annotation.Nullable;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

public class BlueprintSpawnContext {
	
	public final IWorld world;
	public final BlockPos at;
	public final Direction direction;
	public final boolean isWorldGen; // Whether this is currently operating during worldgen (which has implications for TE placing, etc.)
	public final @Nullable MutableBoundingBox bounds;
	public final @Nullable IBlueprintBlockPlacer placer; // Overriding block spawner interface
	
	public BlueprintSpawnContext(IWorld world, BlockPos pos, Direction direction, boolean isWorldGen, @Nullable MutableBoundingBox bounds, @Nullable IBlueprintBlockPlacer placer) {
		this.world = world;
		this.at = pos;
		this.direction = direction;
		this.bounds = bounds;
		this.placer = placer;
		this.isWorldGen = isWorldGen;
	}
	
	public BlueprintSpawnContext(IWorld world, BlockPos pos, Direction direction, boolean isWorldGen, @Nullable MutableBoundingBox bounds) {
		this(world, pos, direction, isWorldGen, bounds, null);
	}
}