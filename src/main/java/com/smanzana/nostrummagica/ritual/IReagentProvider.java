package com.smanzana.nostrummagica.ritual;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IReagentProvider<T> {

	public @Nullable ReagentType getPresentReagentType(T provider, World world, BlockPos pos);
	
	public boolean consumeReagentType(T provider, World world, BlockPos pos, ReagentType type);
	
}
