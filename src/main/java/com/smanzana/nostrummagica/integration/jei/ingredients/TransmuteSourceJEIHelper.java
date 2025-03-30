package com.smanzana.nostrummagica.integration.jei.ingredients;

import java.util.ArrayList;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.component.Transmutation.TransmutationSource;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;

public class TransmuteSourceJEIHelper implements IIngredientHelper<TransmutationSource> {

//	@Override
//	public List<TransmutationSource> expandSubtypes(List<TransmutationSource> ingredients) {
//		return ingredients;
//	}

	@Override
	public TransmutationSource getMatch(Iterable<TransmutationSource> ingredients, TransmutationSource ingredientToMatch, UidContext context) {
		return null;
	}

	@Override
	public String getDisplayName(TransmutationSource ingredient) {
		return "nostrummagica.transmutationsource";
	}

	@Override
	public String getUniqueId(TransmutationSource ingredient, UidContext context) {
		return ingredient.getClass().getName();
	}

	@Override
	public String getWildcardId(TransmutationSource ingredient) {
		return getUniqueId(ingredient, UidContext.Ingredient);
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
		return getUniqueId(ingredient, UidContext.Ingredient);
	}

	@Override
	public String getResourceId(TransmutationSource ingredient) {
		return ingredient.getName();
	}

	@Override
	public TransmutationSource copyIngredient(TransmutationSource ingredient) {
		return ingredient.copy();
	}

	@Override
	public IIngredientType<TransmutationSource> getIngredientType() {
		return TransmuteSourceIngredientType.instance;
	}

}
