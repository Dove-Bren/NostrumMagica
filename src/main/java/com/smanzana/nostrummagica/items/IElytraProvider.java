package com.smanzana.nostrummagica.items;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface IElytraProvider {

	public boolean isElytraFlying(EntityLivingBase entity, ItemStack stack);
	
}
