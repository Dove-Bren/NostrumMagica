package com.smanzana.nostrummagica.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface IDragonWingRenderItem {

	public boolean shouldRenderDragonWings(ItemStack stack, PlayerEntity player);
	
	public int getDragonWingColor(ItemStack stack, PlayerEntity player);
	
}
