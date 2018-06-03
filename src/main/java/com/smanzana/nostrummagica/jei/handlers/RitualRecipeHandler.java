package com.smanzana.nostrummagica.jei.handlers;

import com.smanzana.nostrummagica.jei.categories.RitualRecipeCategory;
import com.smanzana.nostrummagica.jei.wrappers.RitualRecipeWrapper;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class RitualRecipeHandler implements IRecipeHandler<RitualRecipe> {

	@Override
	public Class<RitualRecipe> getRecipeClass() {
		return RitualRecipe.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return RitualRecipeCategory.UID;
	}

	@Override
	public String getRecipeCategoryUid(RitualRecipe recipe) {
		return RitualRecipeCategory.UID;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(RitualRecipe recipe) {
		return new RitualRecipeWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(RitualRecipe recipe) {
		return true;
	}

}
