package com.smanzana.nostrummagica.integration.jei.ingredients;

import java.util.ArrayList;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.components.Transmutation.TransmutationSource;

import mezz.jei.api.ingredients.IIngredientHelper;

public class TransmuteSourceJEIHelper implements IIngredientHelper<TransmutationSource> {

//	@Override
//	public List<TransmutationSource> expandSubtypes(List<TransmutationSource> ingredients) {
//		return ingredients;
//	}

	@Override
	public TransmutationSource getMatch(Iterable<TransmutationSource> ingredients, TransmutationSource ingredientToMatch) {
		return null;
	}

	@Override
	public String getDisplayName(TransmutationSource ingredient) {
		return "nostrummagica.transmutationsource";
	}

	@Override
	public String getUniqueId(TransmutationSource ingredient) {
		return ingredient.getClass().getName();
	}

	@Override
	public String getWildcardId(TransmutationSource ingredient) {
		return getUniqueId(ingredient);
	}

	@Override
	public String getModId(TransmutationSource ingredient) {
		return NostrumMagica.MODID;
	}

	@Override
	public Iterable<Integer> getColors(TransmutationSource ingredient) {
		return new ArrayList<>();
	}

	@Override
	public String getErrorInfo(TransmutationSource ingredient) {
		return getUniqueId(ingredient);
	}

	@Override
	public String getResourceId(TransmutationSource ingredient) {
		return ingredient.getName();
	}

	@Override
	public TransmutationSource copyIngredient(TransmutationSource ingredient) {
		return ingredient.copy();
	}

}
