package com.smanzana.nostrummagica.item;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface ISelectionItem {

	public boolean shouldRenderSelection(PlayerEntity player, ItemStack stack);
	
	public @Nullable BlockPos getAnchor(PlayerEntity player, ItemStack stack);
	
	public @Nullable BlockPos getBoundingPos(PlayerEntity player, ItemStack stack);

	public boolean isSelectionValid(PlayerEntity player, ItemStack selectionStack);
	
}
