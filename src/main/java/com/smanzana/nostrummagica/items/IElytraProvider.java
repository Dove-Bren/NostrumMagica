package com.smanzana.nostrummagica.items;

import javax.annotation.Nonnull;

import com.enderio.core.common.transform.EnderCoreMethods.IElytraFlyingProvider2;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IElytraProvider extends IElytraFlyingProvider2 {

	@SideOnly(Side.CLIENT)
	public boolean shouldRenderElyta(EntityLivingBase entity, ItemStack stack);
	
	public boolean isElytraFlying(EntityLivingBase entity, ItemStack stack);
	
	@Override
	default public boolean isElytraFlying(@Nonnull EntityLivingBase entity, @Nonnull ItemStack itemstack, boolean shouldStop) {
		return isElytraFlying(entity, itemstack);
	}
	
}
