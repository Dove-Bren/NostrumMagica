package com.smanzana.nostrummagica.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IDragonWingRenderItem {

	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderDragonWings(ItemStack stack, PlayerEntity player);
	
	@OnlyIn(Dist.CLIENT)
	public int getDragonWingColor(ItemStack stack, PlayerEntity player);
	
}
