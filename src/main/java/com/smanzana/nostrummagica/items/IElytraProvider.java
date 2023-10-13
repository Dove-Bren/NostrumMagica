package com.smanzana.nostrummagica.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//@Optional.Interface(iface="com.enderio.core.common.interfaces.IElytraFlyingProvider",modid="enderio")
public interface IElytraProvider /*extends IElytraFlyingProvider*/ {

	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderElyta(LivingEntity entity, ItemStack stack);
	
	public boolean isElytraFlying(LivingEntity entity, ItemStack stack);

//	@Optional.Method(modid="enderio")
//	@Override
//	default public boolean isElytraFlying(@Nonnull LivingEntity entity, @Nonnull ItemStack itemstack, boolean shouldStop) {
//		return isElytraFlying(entity, itemstack);
//	}
	
}
