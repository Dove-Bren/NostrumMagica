package com.smanzana.nostrummagica.integration.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.IJEIAwareGuiContainer;
import com.smanzana.nostrummagica.client.gui.container.MasterSpellCreationGui;
import com.smanzana.nostrummagica.client.gui.container.RuneShaperGui;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui;
import com.smanzana.nostrummagica.integration.jei.categories.RitualRecipeCategory;
import com.smanzana.nostrummagica.integration.jei.categories.TransmutationItemCategory;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeIngredientType;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeJEIHelper;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeJEIRenderer;
import com.smanzana.nostrummagica.integration.jei.ingredients.TransmuteSourceIngredientType;
import com.smanzana.nostrummagica.integration.jei.ingredients.TransmuteSourceJEIHelper;
import com.smanzana.nostrummagica.integration.jei.ingredients.TransmuteSourceJEIRenderer;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.ritual.RitualRegistry;
import com.smanzana.nostrummagica.spell.component.Transmutation.TransmutationRecipe;
import com.smanzana.nostrummagica.spell.component.Transmutation.TransmutationSource;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

@JeiPlugin
public class NostrumMagicaJEIPlugin implements IModPlugin {
	
	private static final ResourceLocation pluginUID = new ResourceLocation(NostrumMagica.MODID, "jeiplugin");
	
	private static NostrumMagicaJEIPlugin lastCreated = null;
	
	private List<RitualOutcomeWrapper> ritualOutcomes;
	private List<TransmutationSource> transmuteSources;
	private List<TransmutationRecipe> transmuteItemRecipes;
	private List<TransmutationRecipe> transmuteBlockRecipes;
	private IJeiRuntime runtime = null;
	
	public NostrumMagicaJEIPlugin() {
		if (lastCreated != null) {
			lastCreated.discardRefs();
		}
		lastCreated = this;
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	protected void discardRefs() {
		runtime = null; // Avoid keeping whole runtime alive
	}
	
	@SubscribeEvent
	public void onTagsUpdated(TagsUpdatedEvent event) {
		// This is so so so so so stupid. JEI responds to recipes being updated, which
		// is done right before tags are sent down from the server. So all recipes with tags
		// don't get updated?
		
		if (this != lastCreated) {
			return;
		}
		
		NostrumMagica.logger.warn("Forcing JEI reload after receiving tag update :(");
		
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			Ingredient.invalidateAll();
			@Nullable Player player = NostrumMagica.Proxy.getPlayer();
			if (player != null && player.level.isClientSide() && player instanceof AbstractClientPlayer) {
				ClientPacketListener handler = Minecraft.getInstance().getConnection();
				ForgeHooksClient.onRecipesUpdated(handler.getRecipeManager());
			}
		});
		
		
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
			transmuteSources = new ArrayList<>(TransmutationSource.GetAll());
			
			ingredientRegistry.register(TransmuteSourceIngredientType.instance,
					transmuteSources,
					helper,
					renderer);
		}
	}
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new RitualRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new TransmutationItemCategory(registry.getJeiHelpers().getGuiHelper(), false));
		registry.addRecipeCategories(new TransmutationItemCategory(registry.getJeiHelpers().getGuiHelper(), true));
	}
	
	@Override
	public void registerRecipes(IRecipeRegistration registry) {
		NostrumMagica.logger.info("Registering rituals with JEI...");
		registry.addRecipes(RitualRecipeCategory.Type, RitualRegistry.instance().getRegisteredRituals());
		NostrumMagica.logger.info("Registered " + RitualRegistry.instance().getRegisteredRituals().size() + " rituals");
		
		
		NostrumMagica.logger.info("Registering transmutations with JEI...");
		transmuteItemRecipes = TransmutationRecipe.GetItemRecipes();
		Collections.shuffle(transmuteItemRecipes, new Random(442)); // Shuffle so the same input isn't grouped together :P
		registry.addRecipes(TransmutationItemCategory.TYPE_ITEMS, transmuteItemRecipes);
		
		transmuteBlockRecipes = TransmutationRecipe.GetBlocksRecipes();
		Collections.shuffle(transmuteBlockRecipes, new Random(442)); // Shuffle so the same input isn't grouped together :P
		registry.addRecipes(TransmutationItemCategory.TYPE_BLOCKS, transmuteBlockRecipes);
		NostrumMagica.logger.info("Registered " + ((transmuteBlockRecipes.size() + transmuteItemRecipes.size())/2) + " transmutations");
		
		
		// JEI wiki says to do this here, but it fires an exception
		// Hide our cool wrapper to outputs
		//registry.getIngredientManager().removeIngredientsAtRuntime(RitualOutcomeIngredientType.instance, ritualOutcomes);
	}
	
	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(NostrumItems.altarItem), RitualRecipeCategory.Type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		List<Class<? extends IJEIAwareGuiContainer>> list = Lists.newArrayList(
				SpellCreationGui.SpellGui.class,
				MasterSpellCreationGui.SpellGui.class,
				RuneShaperGui.RuneShaperGuiContainer.class
		);
		
		for (Class<? extends AbstractContainerScreen<?>> clazz : list.toArray(new Class[0])) {
			registration.addGenericGuiContainerHandler(clazz, new IGuiContainerHandler<AbstractContainerScreen<?>>() {
				@Override
				public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> containerScreen) {
					return ((IJEIAwareGuiContainer) containerScreen).getGuiExtraAreas();
				}
			});
		}
	}

	@Override
	public ResourceLocation getPluginUid() {
		return pluginUID;
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		this.runtime = jeiRuntime;
		refreshTransmuteRecipes(null);
		
		if (ritualOutcomes != null && !ritualOutcomes.isEmpty()) {
			jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(RitualOutcomeIngredientType.instance, ritualOutcomes);
			//jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(RitualOutcomeIngredientType.instance, ritualOutcomes);
		}
	}
	
	public void refreshTransmuteRecipes(@Nullable Player player) {
		// Hide and unhide transmutation recipes based on whether a player has seen a given
		if (runtime != null) {
			IRecipeManager manager = runtime.getRecipeManager();
			List<TransmutationRecipe> visible = new ArrayList<>(transmuteItemRecipes.size());
			List<TransmutationRecipe> hidden = new ArrayList<>(transmuteItemRecipes.size());
			for (TransmutationRecipe recipe : transmuteItemRecipes) {
				if (recipe.isRevealed(player)) {
					visible.add(recipe);
				} else {
					hidden.add(recipe);
				}
			}
			manager.unhideRecipes(TransmutationItemCategory.TYPE_ITEMS, visible);
			manager.hideRecipes(TransmutationItemCategory.TYPE_ITEMS, hidden);

			visible.clear();
			hidden.clear();
			for (TransmutationRecipe recipe : transmuteBlockRecipes) {
				if (recipe.isRevealed(player)) {
					visible.add(recipe);
				} else {
					hidden.add(recipe);
				}
			}
			manager.unhideRecipes(TransmutationItemCategory.TYPE_BLOCKS, visible);
			manager.hideRecipes(TransmutationItemCategory.TYPE_BLOCKS, hidden);
		}
	}
	
	public static void RefreshTransmuteRecipes(@Nullable Player player) {
		if (lastCreated != null) {
			lastCreated.refreshTransmuteRecipes(player);
		}
	}
}
