package com.smanzana.nostrummagica.world.blueprints;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public interface IBlueprintBlockPlacer {
	
	/**
	 * Possible place a blueprint block manually instead of using built-in blueprint placing.
	 * @param context
	 * @param pos
	 * @param direction
	 * @param block
	 * @return whether the block has been handled manually. If false, blueprint placement happens like normal.
	 */
	public boolean spawnBlock(BlueprintSpawnContext context, BlockPos pos, Direction direction, BlueprintBlock block);
	
	/**
	 * Called after block placement has happened (regardless of whether spawnBlock returned true or false)
	 * @param context
	 * @param pos
	 * @param placedState
	 * @param te 
	 * @param direction
	 * @param block
	 */
	public void finalizeBlock(BlueprintSpawnContext context, BlockPos pos, BlockState placedState, @Nullable TileEntity te, Direction direction, BlueprintBlock block);
}