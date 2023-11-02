package com.smanzana.nostrummagica.integration.jei.categories;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.jei.ingredients.TransmuteSourceIngredientType;
import com.smanzana.nostrummagica.spells.components.Transmutation.TransmutationRecipe;
import com.smanzana.nostrummagica.spells.components.Transmutation.TransmutationSource;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

public class TransmutationCategory implements IRecipeCategory<TransmutationRecipe> {

	private static final ResourceLocation TEXT_BACK = new ResourceLocation(NostrumMagica.MODID, "textures/gui/nei/transmute.png");
	private static final int BACK_WIDTH = 99;
	private static final int BACK_HEIGHT = 32;
	private static final int ICON_HOFFSET = 0;
	private static final int ICON_VOFFSET = 64;
	
	public static final ResourceLocation UID = new ResourceLocation(NostrumMagica.MODID, "transmutation_recipe");
	
	private String title;
	private IDrawable background;
	private IDrawable icon;
	
	public TransmutationCategory(IGuiHelper guiHelper) {
		title = I18n.format("nei.category.transmutation.name", (Object[]) null);
		background = guiHelper.drawableBuilder(TEXT_BACK, 0, 0, BACK_WIDTH, BACK_HEIGHT).addPadding(0, 0, 0, 0).build();
		icon = guiHelper.drawableBuilder(TEXT_BACK, ICON_HOFFSET, ICON_VOFFSET, 16, 16).build();
	}
	
	@Override
	public ResourceLocation getUid() {
		return UID;
	}
	
	@Override
	public Class<TransmutationRecipe> getRecipeClass() {
		return TransmutationRecipe.class;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void draw(TransmutationRecipe recipe, double mouseX, double mouseY) {
		// Only thing to draw is the transmutation jump power
		
		final int jump = recipe.getLevel();
		final Minecraft minecraft = Minecraft.getInstance();
		final String label = "" + jump;
		minecraft.fontRenderer.drawStringWithShadow(label, (BACK_WIDTH/2) - 4, BACK_HEIGHT/2 - 4, 0xFFFFFFFF);
	}
	
	@Override
	public void setIngredients(TransmutationRecipe recipe, IIngredients ingredients) {
		
		// We will always have an output, but may have an input or may have a hidden one
		final @Nonnull ItemStack output;
		final @Nullable List<ItemStack> input;
		
		output = recipe.getOutput();
		Ingredient inputIng = recipe.getRevealedIngredient();
		if (inputIng == null || inputIng == Ingredient.EMPTY || inputIng.hasNoMatchingItems()) {
			input = null;
		} else {
			input = Lists.newArrayList(inputIng.getMatchingStacks());
		}
		
		ingredients.setOutput(VanillaTypes.ITEM, output);
		if (input != null) {
			List<List<ItemStack>> inputs = new ArrayList<List<ItemStack>>();
			inputs.add(input);
			ingredients.setInputLists(VanillaTypes.ITEM, inputs);
		} else {
			ingredients.setInput(TransmuteSourceIngredientType.instance, recipe.getSource());
		}
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, TransmutationRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		IGuiIngredientGroup<TransmutationSource> sourceStacks = recipeLayout.getIngredientsGroup(TransmuteSourceIngredientType.instance);
		
		/*
		 * Input is either an item stack or a source. Output is always an item stack.
		 * Idk how this maps slots to items in the ingredients list...
		 */
		
		if (recipe.getRevealedIngredient() != null) {
			// Assume two itemstacks
			guiItemStacks.init(0, true, 13, 7);
			guiItemStacks.init(1, false, 68, 7);
		} else {
			// Input is a source
			sourceStacks.init(0, true, 13, 7);
			guiItemStacks.init(1, false, 68, 7);
		}
		
		guiItemStacks.set(ingredients);
		sourceStacks.set(ingredients);
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}
}