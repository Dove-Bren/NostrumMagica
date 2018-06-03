package com.smanzana.nostrummagica.jei;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellTomePage;
import com.smanzana.nostrummagica.jei.categories.RitualRecipeCategory;
import com.smanzana.nostrummagica.jei.handlers.RitualRecipeHandler;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class NostrumMagicaJEIPlugin extends BlankModPlugin {
	
	private List<RitualOutcomeWrapper> ritualOutcomes;
	
	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
		subtypeRegistry.registerSubtypeInterpreter(SpellRune.instance(), new ISubtypeRegistry.ISubtypeInterpreter() {
			@Override
			public String getSubtypeInfo(ItemStack itemStack) {
				return SpellRune.toComponentWrapper(itemStack).getKeyString();
			}
		});
		
		subtypeRegistry.useNbtForSubtypes(SpellTomePage.instance());
	}

	@Override
	public void registerIngredients(IModIngredientRegistration ingredientRegistry) {
		ritualOutcomes = new LinkedList<>();
		for (RitualRecipe ritual : RitualRegistry.instance().getRegisteredRituals()) {
			ritualOutcomes.add(new RitualOutcomeWrapper(ritual.getOutcome()));
		}
		
		RitualOutcomeJEIHelper helper = new RitualOutcomeJEIHelper();
		RitualOutcomeJEIRenderer renderer = RitualOutcomeJEIRenderer.instance();
		
		ingredientRegistry.register(RitualOutcomeWrapper.class,
				ritualOutcomes,
				helper,
				renderer);
	}
	
	@Override
	public void register(IModRegistry registry) {
		NostrumMagica.logger.info("Registering rituals with JEI...");
		registry.addRecipeCategories(
				new RitualRecipeCategory(registry.getJeiHelpers().getGuiHelper())
				);
		registry.addRecipeHandlers(new RitualRecipeHandler());
		
		registry.addRecipes(RitualRegistry.instance().getRegisteredRituals());
		
		NostrumMagica.logger.info("Registered " + RitualRegistry.instance().getRegisteredRituals().size()
				+ " rituals");
		
		registry.addRecipeCategoryCraftingItem(new ItemStack(AltarItem.instance()),
				RitualRecipeCategory.UID);
		
		
		// Hide our cool wrapper to outputs
		for (RitualOutcomeWrapper wrapper : ritualOutcomes) {
			registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(wrapper);
		}
	}
	
}
