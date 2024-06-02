package com.smanzana.nostrummagica.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// If in an equipment slot, may choose to turn on elytra rendering.
// See NostrumElytraWrapper for what is considered and will have this called.
public interface IElytraRenderer {

	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderElyta(LivingEntity entity, ItemStack stack);
	
}
