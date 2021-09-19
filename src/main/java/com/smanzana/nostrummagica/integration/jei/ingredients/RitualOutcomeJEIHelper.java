package com.smanzana.nostrummagica.integration.jei.ingredients;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.jei.RitualOutcomeWrapper;

import mezz.jei.api.ingredients.IIngredientHelper;

public class RitualOutcomeJEIHelper implements IIngredientHelper<RitualOutcomeWrapper> {

	@Override
	public List<RitualOutcomeWrapper> expandSubtypes(List<RitualOutcomeWrapper> ingredients) {
		return ingredients;
	}

	@Override
	public RitualOutcomeWrapper getMatch(Iterable<RitualOutcomeWrapper> ingredients, RitualOutcomeWrapper ingredientToMatch) {
		return null;
	}

	@Override
	public String getDisplayName(RitualOutcomeWrapper ingredient) {
		return "nostrummagica.debug.outcome";
	}

	@Override
	public String getUniqueId(RitualOutcomeWrapper ingredient) {
		return ingredient.getClass().getName();
	}

	@Override
	public String getWildcardId(RitualOutcomeWrapper ingredient) {
		return getUniqueId(ingredient);
	}

	@Override
	public String getModId(RitualOutcomeWrapper ingredient) {
		return NostrumMagica.MODID;
	}

	@Override
	public Iterable<Color> getColors(RitualOutcomeWrapper ingredient) {
		return new ArrayList<>();
	}

	@Override
	public String getErrorInfo(RitualOutcomeWrapper ingredient) {
		return getUniqueId(ingredient);
	}

	@Override
	public String getResourceId(RitualOutcomeWrapper ingredient) {
		return ingredient.getOutcome().getName();
	}

	@Override
	public RitualOutcomeWrapper copyIngredient(RitualOutcomeWrapper ingredient) {
		return new RitualOutcomeWrapper(ingredient.getOutcome());
	}

}
