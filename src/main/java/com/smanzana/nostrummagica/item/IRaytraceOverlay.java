package com.smanzana.nostrummagica.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IRaytraceOverlay {

	public boolean shouldTrace(Level world, Player player, ItemStack stack);
	
	public double getTraceRange(Level world, Player player, ItemStack stack);
	
	public default boolean shouldOutline(Level world, Player player, ItemStack stack) {
		return true;
	}
	
}
