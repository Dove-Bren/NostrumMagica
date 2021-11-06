package com.smanzana.nostrummagica.items;

import javax.annotation.Nonnull;

import com.enderio.core.common.interfaces.IElytraFlyingProvider;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.Interface(iface="com.enderio.core.common.interfaces.IElytraFlyingProvider",modid="enderio")
public interface IElytraProvider extends IElytraFlyingProvider {

	@SideOnly(Side.CLIENT)
	public boolean shouldRenderElyta(EntityLivingBase entity, ItemStack stack);
	
	public boolean isElytraFlying(EntityLivingBase entity, ItemStack stack);

	@Optional.Method(modid="enderio")
	@Override
	default public boolean isElytraFlying(@Nonnull EntityLivingBase entity, @Nonnull ItemStack itemstack, boolean shouldStop) {
		return isElytraFlying(entity, itemstack);
	}
	
}
