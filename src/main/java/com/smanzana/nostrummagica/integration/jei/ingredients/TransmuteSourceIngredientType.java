package com.smanzana.nostrummagica.integration.jei.ingredients;

import com.smanzana.nostrummagica.spells.components.Transmutation.TransmutationSource;

import mezz.jei.api.ingredients.IIngredientType;

/**
 * Originally: An item/block that can be transmuted into the result, although what item/block may be hidden!
 * Now: an effectively 'singleton' ingredient that means the transmutation hasn't been seen yet. That way,
 *      input ingredients can work as item stacks. So you can right-click a pumpkin and see you can transmute it
 *      
 * @author Skyler
 *
 */
public class TransmuteSourceIngredientType implements IIngredientType<TransmutationSource> {

	public static TransmuteSourceIngredientType instance = new TransmuteSourceIngredientType();
	
	@Override
	public Class<? extends TransmutationSource> getIngredientClass() {
		return TransmutationSource.class;
	}
}