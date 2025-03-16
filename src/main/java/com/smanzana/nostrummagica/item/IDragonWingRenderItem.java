package com.smanzana.nostrummagica.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IDragonWingRenderItem {

	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderDragonWings(ItemStack stack, Player player);
	
	@OnlyIn(Dist.CLIENT)
	public int getDragonWingColor(ItemStack stack, Player player);
	
}
