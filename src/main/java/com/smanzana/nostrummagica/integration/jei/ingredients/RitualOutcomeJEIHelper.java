package com.smanzana.nostrummagica.integration.jei.ingredients;

import java.util.ArrayList;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.jei.RitualOutcomeWrapper;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;

public class RitualOutcomeJEIHelper implements IIngredientHelper<RitualOutcomeWrapper> {

//	@Override
//	public List<RitualOutcomeWrapper> expandSubtypes(List<RitualOutcomeWrapper> ingredients) {
//		return ingredients;
//	}

	@Override
	public RitualOutcomeWrapper getMatch(Iterable<RitualOutcomeWrapper> ingredients, RitualOutcomeWrapper ingredientToMatch, UidContext context) {
		return null;
	}

	@Override
	public String getDisplayName(RitualOutcomeWrapper ingredient) {
		return "nostrummagica.debug.outcome";
	}

	@Override
	public String getUniqueId(RitualOutcomeWrapper ingredient, UidContext context) {
		return ingredient.getClass().getName();
	}

	@Override
	public String getWildcardId(RitualOutcomeWrapper ingredient) {
		return getUniqueId(ingredient, UidContext.Ingredient);
	}

	@Override
	public String getModId(RitualOutcomeWrapper ingredient) {
		return NostrumMagica.MODID;
	}

	@Override
	public Iterable<Integer> getColors(RitualOutcomeWrapper ingredient) {
		return new ArrayList<>();
	}

	@Override
	public String getErrorInfo(RitualOutcomeWrapper ingredient) {
		return getUniqueId(ingredient, UidContext.Ingredient);
	}

	@Override
	public String getResourceId(RitualOutcomeWrapper ingredient) {
		return ingredient.getOutcome().getName();
	}

	@Override
	public RitualOutcomeWrapper copyIngredient(RitualOutcomeWrapper ingredient) {
		return new RitualOutcomeWrapper(ingredient.getOutcome());
	}

	@Override
	public IIngredientType<RitualOutcomeWrapper> getIngredientType() {
		return RitualOutcomeIngredientType.instance;
	}

}
