package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

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
	
	/**
	 * Returns a unique identifier for this type of ritual outcome.
	 * This does not need to be readable and is not shown to the user.
	 * @return
	 */
	public String getName();
	
	/**
	 * Return a list of strings to serve as a description for this outcome.
	 * The description should already be translated
	 * @return
	 */
	public List<String> getDescription();
}
