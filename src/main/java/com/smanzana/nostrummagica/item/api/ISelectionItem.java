package com.smanzana.nostrummagica.item.api;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ISelectionItem {

	public boolean shouldRenderSelection(Player player, ItemStack stack);
	
	public @Nullable BlockPos getAnchor(Player player, ItemStack stack);
	
	public @Nullable BlockPos getBoundingPos(Player player, ItemStack stack);

	public boolean isSelectionValid(Player player, ItemStack selectionStack);
	
}
