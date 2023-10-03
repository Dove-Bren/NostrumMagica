package com.smanzana.nostrummagica.integration.jei;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.jei.categories.RitualRecipeCategory;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeIngredientType;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeJEIHelper;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeJEIRenderer;
import com.smanzana.nostrummagica.integration.jei.wrappers.RitualRecipeWrapper;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class NostrumMagicaJEIPlugin implements IModPlugin {
	
	private List<RitualOutcomeWrapper> ritualOutcomes;
	
	@Override
	public void registerItemSubtypes(@Nonnull ISubtypeRegistry subtypeRegistry) {
		subtypeRegistry.registerSubtypeInterpreter(NostrumItems.spellRune, new ISubtypeRegistry.ISubtypeInterpreter() {
			
			@Override
			@Nullable
			public String getSubtypeInfo(ItemStack itemStack) {
				return apply(itemStack); // remove once removed
			}

			@Override
			public String apply(ItemStack itemStack) {
				return SpellRune.toComponentWrapper(itemStack).getKeyString();
			}
		});
		
		subtypeRegistry.useNbtForSubtypes(NostrumItems.spellTomePage);
	}

	@Override
	public void registerIngredients(IModIngredientRegistration ingredientRegistry) {
		ritualOutcomes = new LinkedList<>();
		for (RitualRecipe ritual : RitualRegistry.instance().getRegisteredRituals()) {
			ritualOutcomes.add(new RitualOutcomeWrapper(ritual.getOutcome()));
		}
		
		RitualOutcomeJEIHelper helper = new RitualOutcomeJEIHelper();
		RitualOutcomeJEIRenderer renderer = RitualOutcomeJEIRenderer.instance();
		
		ingredientRegistry.register(RitualOutcomeIngredientType.instance,
				ritualOutcomes,
				helper,
				renderer);
	}
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new RitualRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
	}
	
	@Override
	public void register(IModRegistry registry) {
		NostrumMagica.logger.info("Registering rituals with JEI...");
		
		registry.handleRecipes(RitualRecipe.class, RitualRecipeWrapper::new, RitualRecipeCategory.UID);
		
		registry.addRecipes(RitualRegistry.instance().getRegisteredRituals(), RitualRecipeCategory.UID);
		
		NostrumMagica.logger.info("Registered " + RitualRegistry.instance().getRegisteredRituals().size() + " rituals");
		
		registry.addRecipeCatalyst(new ItemStack(NostrumItems.altarItem), RitualRecipeCategory.UID);
		
		
		// Hide our cool wrapper to outputs
		IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
		for (RitualOutcomeWrapper wrapper : ritualOutcomes) {
			blacklist.addIngredientToBlacklist(wrapper);
		}
		//blacklist.addIngredientToBlacklist(new ItemStack(DungeonBlock.instance()));
		//blacklist.addIngredientToBlacklist(new ItemStack(NostrumSingleSpawner.instance()));
		int unused; // hmmm above doesn't work? is block registering right?
		
	}
	
}
