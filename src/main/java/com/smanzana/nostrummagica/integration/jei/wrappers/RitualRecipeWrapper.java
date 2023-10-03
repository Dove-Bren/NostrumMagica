package com.smanzana.nostrummagica.integration.jei.wrappers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.integration.jei.RitualOutcomeWrapper;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeIngredientType;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.outcomes.IItemRitualOutcome;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

public class RitualRecipeWrapper implements IRecipeWrapper {

	private RitualRecipe ritual;
	private List<Ingredient> inputs;
	
	public RitualRecipeWrapper(RitualRecipe ritual) {
		this.ritual = ritual;
		inputs = new ArrayList<>();

		// Add flavor gem
		inputs.add(Ingredient.fromItems(InfusedGemItem.getGemItem(ritual.getElement())));
		Ingredient reagent2, reagent3, reagent4;
		ReagentType reagents[] = ritual.getTypes();
		inputs.add(Ingredient.fromItems(ReagentItem.GetItem(reagents[0])));
		if (reagents.length > 1) {
			reagent2 = Ingredient.fromItems(ReagentItem.GetItem(reagents[1]));
			reagent3 = Ingredient.fromItems(ReagentItem.GetItem(reagents[2]));
			reagent4 = Ingredient.fromItems(ReagentItem.GetItem(reagents[3]));
		} else {
			reagent2 = reagent3 = reagent4 = null;
		}
		inputs.add(reagent2);
		inputs.add(reagent3);
		inputs.add(reagent4);
		inputs.add(ritual.getCenterItem());
		NonNullList<Ingredient> extras = ritual.getExtraItems();
		Ingredient extra1, extra2, extra3, extra4;
		extra1 = extra2 = extra3 = extra4 = null;
		if (extras != null) {
			int len = extras.size();
			if (len > 0)
				extra1 = extras.get(0);
			if (len > 1)
				extra2 = extras.get(1);
			if (len > 2)
				extra3 = extras.get(2);
			if (len > 3)
				extra4 = extras.get(3);
		}
		inputs.add(extra1);
		inputs.add(extra2);
		inputs.add(extra3);
		inputs.add(extra4);
		
			
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) {
		List<List<ItemStack>> stackInputs = new ArrayList<>();
		for (Ingredient ing : inputs) {
			if (ing == Ingredient.EMPTY || ing.hasNoMatchingItems()) {
				stackInputs.add(new ArrayList<>()); // should be null?
			} else {
				stackInputs.add(Lists.newArrayList(ing.getMatchingStacks()));
			}
		}
		
		ingredients.setInputLists(VanillaTypes.ITEM, stackInputs);
		if (ritual.getOutcome() instanceof IItemRitualOutcome) {
			ingredients.setOutput(VanillaTypes.ITEM, ((IItemRitualOutcome) ritual.getOutcome()).getResult());
		} else {
			ingredients.setOutput(RitualOutcomeIngredientType.instance, new RitualOutcomeWrapper(ritual.getOutcome()));
		}
	}
	
	public RitualRecipe getRitual() {
		return ritual;
	}
	
}
