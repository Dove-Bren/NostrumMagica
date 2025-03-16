package com.smanzana.nostrummagica.ritual;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IReagentProvider<T> {

	public @Nullable ReagentType getPresentReagentType(T provider, Level world, BlockPos pos);
	
	public boolean consumeReagentType(T provider, Level world, BlockPos pos, ReagentType type);
	
}
