package com.smanzana.nostrummagica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ITriggeredBlock {

	public void trigger(Level world, BlockPos blockPos, BlockState state, BlockPos triggerPos);
	
}
