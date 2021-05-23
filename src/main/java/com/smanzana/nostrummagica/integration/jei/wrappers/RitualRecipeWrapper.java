package com.smanzana.nostrummagica.integration.jei.wrappers;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.integration.jei.RitualOutcomeWrapper;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.outcomes.IItemRitualOutcome;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

public class RitualRecipeWrapper extends BlankRecipeWrapper {

	private RitualRecipe ritual;
	private List<ItemStack> inputs;
	
	public RitualRecipeWrapper(RitualRecipe ritual) {
		this.ritual = ritual;
		inputs = new ArrayList<>();

		// Add flavor gem
		inputs.add(InfusedGemItem.instance().getGem(ritual.getElement(), 1));
		ItemStack reagent2, reagent3, reagent4;
		ReagentType reagents[] = ritual.getTypes();
		inputs.add(ReagentItem.instance().getReagent(reagents[0], 1));
		if (reagents.length > 1) {
			reagent2 = ReagentItem.instance().getReagent(reagents[1], 1);
			reagent3 = ReagentItem.instance().getReagent(reagents[2], 1);
			reagent4 = ReagentItem.instance().getReagent(reagents[3], 1);
		} else {
			reagent2 = reagent3 = reagent4 = null;
		}
		inputs.add(reagent2);
		inputs.add(reagent3);
		inputs.add(reagent4);
		inputs.add(ritual.getCenterItem());
		ItemStack extras[] = ritual.getExtraItems();
		ItemStack extra1, extra2, extra3, extra4;
		extra1 = extra2 = extra3 = extra4 = null;
		if (extras != null) {
			int len = extras.length;
			if (len > 0)
				extra1 = extras[0];
			if (len > 1)
				extra2 = extras[1];
			if (len > 2)
				extra3 = extras[2];
			if (len > 3)
				extra4 = extras[3];
		}
		inputs.add(extra1);
		inputs.add(extra2);
		inputs.add(extra3);
		inputs.add(extra4);
		
			
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(ItemStack.class, inputs);
		if (ritual.getOutcome() instanceof IItemRitualOutcome) {
			ingredients.setOutput(ItemStack.class, ((IItemRitualOutcome) ritual.getOutcome()).getResult());
		} else {
			ingredients.setOutput(RitualOutcomeWrapper.class, new RitualOutcomeWrapper(ritual.getOutcome()));
		}
	}
	
	public RitualRecipe getRitual() {
		return ritual;
	}
	
}
