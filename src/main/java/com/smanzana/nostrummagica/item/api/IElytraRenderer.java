package com.smanzana.nostrummagica.item.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// If in an equipment slot, may choose to turn on elytra rendering.
// See NostrumElytraWrapper for what is considered and will have this called.
public interface IElytraRenderer {

	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderElyta(LivingEntity entity, ItemStack stack);
	
}
