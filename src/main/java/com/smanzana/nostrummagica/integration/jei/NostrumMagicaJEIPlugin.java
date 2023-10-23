package com.smanzana.nostrummagica.integration.jei;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.jei.categories.RitualRecipeCategory;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeIngredientType;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeJEIHelper;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeJEIRenderer;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class NostrumMagicaJEIPlugin implements IModPlugin {
	
	private static final ResourceLocation pluginUID = new ResourceLocation(NostrumMagica.MODID, "jeiplugin");
	
	private List<RitualOutcomeWrapper> ritualOutcomes;
	
	@Override
	public void registerItemSubtypes(@Nonnull ISubtypeRegistration subtypeRegistry) {
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
	public void registerRecipes(IRecipeRegistration registry) {
		NostrumMagica.logger.info("Registering rituals with JEI...");
		
		registry.addRecipes(RitualRegistry.instance().getRegisteredRituals(), RitualRecipeCategory.UID);
		
		NostrumMagica.logger.info("Registered " + RitualRegistry.instance().getRegisteredRituals().size() + " rituals");
		
		// Hide our cool wrapper to outputs
		registry.getIngredientManager().removeIngredientsAtRuntime(RitualOutcomeIngredientType.instance, ritualOutcomes);
	}
	
	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(NostrumItems.altarItem), RitualRecipeCategory.UID);
	}

	@Override
	public ResourceLocation getPluginUid() {
		return pluginUID;
	}
	
}
