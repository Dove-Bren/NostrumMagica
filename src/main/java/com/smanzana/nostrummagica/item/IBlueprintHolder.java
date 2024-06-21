package com.smanzana.nostrummagica.item;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.world.blueprints.IBlueprint;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface IBlueprintHolder {

	public boolean hasBlueprint(PlayerEntity player, ItemStack stack);
	public boolean shouldDisplayBlueprint(PlayerEntity player, ItemStack stack, BlockPos pos);
	public @Nonnull IBlueprint getBlueprint(PlayerEntity player, ItemStack stack, BlockPos pos);
	
}
