package com.smanzana.nostrummagica.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITriggeredBlock {

	public void trigger(World world, BlockPos blockPos, IBlockState state, BlockPos triggerPos);
	
}
