package com.smanzana.nostrummagica.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ITriggeredBlock {

	public void trigger(Level world, BlockPos blockPos, BlockState state, BlockPos triggerPos);
	
}
