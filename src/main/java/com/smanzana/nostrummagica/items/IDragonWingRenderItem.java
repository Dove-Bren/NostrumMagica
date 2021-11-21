package com.smanzana.nostrummagica.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IDragonWingRenderItem {

	public boolean shouldRenderDragonWings(ItemStack stack, EntityPlayer player);
	
	public int getDragonWingColor(ItemStack stack, EntityPlayer player);
	
}
