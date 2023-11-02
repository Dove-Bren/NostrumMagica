package com.smanzana.nostrummagica.integration.jei;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.jei.categories.RitualRecipeCategory;
import com.smanzana.nostrummagica.integration.jei.categories.TransmutationCategory;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeIngredientType;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeJEIHelper;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeJEIRenderer;
import com.smanzana.nostrummagica.integration.jei.ingredients.TransmuteSourceIngredientType;
import com.smanzana.nostrummagica.integration.jei.ingredients.TransmuteSourceJEIHelper;
import com.smanzana.nostrummagica.integration.jei.ingredients.TransmuteSourceJEIRenderer;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.spells.components.Transmutation.TransmutationRecipe;
import com.smanzana.nostrummagica.spells.components.Transmutation.TransmutationSource;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class NostrumMagicaJEIPlugin implements IModPlugin {
	
	private static final ResourceLocation pluginUID = new ResourceLocation(NostrumMagica.MODID, "jeiplugin");
	
	private static NostrumMagicaJEIPlugin lastCreated = null;
	
	private List<RitualOutcomeWrapper> ritualOutcomes;
	private List<TransmutationRecipe> transmuteRecipes;
	private IJeiRuntime runtime = null;
	
	public NostrumMagicaJEIPlugin() {
		lastCreated = this;
	}
	
	@Override
	public void registerItemSubtypes(@Nonnull ISubtypeRegistration subtypeRegistry) {
		subtypeRegistry.useNbtForSubtypes(NostrumItems.spellTomePage);
	}

	@Override
	public void registerIngredients(IModIngredientRegistration ingredientRegistry) {
		
		// Ritual Outcomes
		{
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
		
		// Transmutation source ingredients
		{
			TransmuteSourceJEIHelper helper = new TransmuteSourceJEIHelper();
			TransmuteSourceJEIRenderer renderer = new TransmuteSourceJEIRenderer();
			
			ingredientRegistry.register(TransmuteSourceIngredientType.instance,
					TransmutationSource.GetAll(),
					helper,
					renderer);
		}
	}
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new RitualRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new TransmutationCategory(registry.getJeiHelpers().getGuiHelper()));
	}
	
	@Override
	public void registerRecipes(IRecipeRegistration registry) {
		NostrumMagica.logger.info("Registering rituals with JEI...");
		registry.addRecipes(RitualRegistry.instance().getRegisteredRituals(), RitualRecipeCategory.UID);
		NostrumMagica.logger.info("Registered " + RitualRegistry.instance().getRegisteredRituals().size() + " rituals");
		
		
		NostrumMagica.logger.info("Registering transmutations with JEI...");
		transmuteRecipes = TransmutationRecipe.GetAll();
		Collections.shuffle(transmuteRecipes, new Random(442)); // Shuffle so the same input isn't grouped together :P
		registry.addRecipes(transmuteRecipes, TransmutationCategory.UID);
		NostrumMagica.logger.info("Registered " + (transmuteRecipes.size()/2) + " transmutations");
		
		
		// JEI wiki says to do this here, but it fires an exception
		// Hide our cool wrapper to outputs
		//registry.getIngredientManager().removeIngredientsAtRuntime(RitualOutcomeIngredientType.instance, ritualOutcomes);
	}
	
	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(NostrumItems.altarItem), RitualRecipeCategory.UID);
	}

	@Override
	public ResourceLocation getPluginUid() {
		return pluginUID;
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(RitualOutcomeIngredientType.instance, ritualOutcomes);
		//jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(RitualOutcomeIngredientType.instance, ritualOutcomes);
		
		this.runtime = jeiRuntime;
		refreshTransmuteRecipes(null);
	}
	
	public void refreshTransmuteRecipes(@Nullable PlayerEntity player) {
		// Hide and unhide transmutation recipes based on whether a player has seen a given
		if (runtime != null) {
			IRecipeManager manager = runtime.getRecipeManager();
			for (TransmutationRecipe recipe : transmuteRecipes) {
				if (recipe.isRevealed(player)) {
					manager.unhideRecipe(recipe, TransmutationCategory.UID);
				} else {
					manager.hideRecipe(recipe, TransmutationCategory.UID);
				}
			}
		}
	}
	
	public static void RefreshTransmuteRecipes(@Nullable PlayerEntity player) {
		if (lastCreated != null) {
			lastCreated.refreshTransmuteRecipes(player);
		}
	}
}
