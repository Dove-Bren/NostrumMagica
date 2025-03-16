package com.smanzana.nostrummagica.ritual;

import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.item.crafting.Ingredient;

public interface IRitualIngredients {
	
	/**
	 * Return whether this collection of ritual input ingredients has world supporting blocks (chalk)
	 * that are at or exceeding the provided tier.
	 * @param tier
	 * @return
	 */
	public boolean hasTierBlocks(int tier);

	/**
	 * Return whether the ingredients have a suitable center item
	 * @param ingredient
	 * @return
	 */
	public boolean hasCenterItem(Ingredient ingredient);
	
	
	public boolean hasReagents(Iterable<ReagentType> reagents);
	
	public boolean hasExtraItems(Iterable<Ingredient> ingredients);
	
	public boolean hasElement(EMagicElement element);
	
}
