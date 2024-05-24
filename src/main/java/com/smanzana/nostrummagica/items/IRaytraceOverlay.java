package com.smanzana.nostrummagica.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IRaytraceOverlay {

	public boolean shouldTrace(World world, PlayerEntity player, ItemStack stack);
	
	public double getTraceRange(World world, PlayerEntity player, ItemStack stack);
	
}
