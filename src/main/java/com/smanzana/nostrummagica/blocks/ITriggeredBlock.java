package com.smanzana.nostrummagica.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITriggeredBlock {

	public void trigger(World world, BlockPos blockPos, BlockState state, BlockPos triggerPos);
	
}
