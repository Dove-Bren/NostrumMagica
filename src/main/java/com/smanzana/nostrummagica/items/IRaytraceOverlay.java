package com.smanzana.nostrummagica.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IRaytraceOverlay {

	public boolean shouldTrace(World world, EntityPlayer player, ItemStack stack);
	
}
