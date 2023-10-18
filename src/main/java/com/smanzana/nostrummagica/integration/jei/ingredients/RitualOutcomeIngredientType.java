package com.smanzana.nostrummagica.integration.jei.ingredients;

import com.smanzana.nostrummagica.integration.jei.RitualOutcomeWrapper;

import mezz.jei.api.ingredients.IIngredientType;

public class RitualOutcomeIngredientType implements IIngredientType<RitualOutcomeWrapper> {

	public static RitualOutcomeIngredientType instance = new RitualOutcomeIngredientType();
	
	@Override
	public Class<? extends RitualOutcomeWrapper> getIngredientClass() {
		return RitualOutcomeWrapper.class;
	}

}
