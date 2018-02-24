package com.smanzana.nostrummagica.rituals.outcomes;

import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IRitualOutcome {

	/**
	 * Actually perform the ritual and spawn outcomes, etc.
	 * @param world
	 * @param player
	 * @param centerItem
	 * @param otherItems
	 * @param center
	 * @param recipe
	 */
	public void perform(World world, EntityPlayer player, ItemStack centerItem, ItemStack otherItems[], BlockPos center, RitualRecipe recipe);
}
