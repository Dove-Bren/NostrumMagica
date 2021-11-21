package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRecipe.RitualMatchInfo;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
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
	public void perform(World world, EntityPlayer player, ItemStack centerItem, @Nullable NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe);
	
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
	
	/**
	 * Check last minute if outcome agrees ritual can be performed.,
	 * When this is called, ritual ingredients have already been checked. This is intended for
	 * outcomes that have spacial requirements to do that check before consuming ingredients.
	 * @param world
	 * @param player
	 * @param center
	 * @return
	 */
	default public boolean canPerform(World world, EntityPlayer player, BlockPos center, RitualMatchInfo ingredients) { return true; }
}
