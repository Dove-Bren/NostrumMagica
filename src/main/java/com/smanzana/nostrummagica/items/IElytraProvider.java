package com.smanzana.nostrummagica.items;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IElytraProvider {

	@SideOnly(Side.CLIENT)
	public boolean shouldRenderElyta(EntityLivingBase entity, ItemStack stack);
	
	public boolean isElytraFlying(EntityLivingBase entity, ItemStack stack);
	
}
