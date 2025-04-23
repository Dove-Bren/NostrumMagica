package com.smanzana.nostrummagica.integration.jei.categories;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.jei.ingredients.TransmuteSourceIngredientType;
import com.smanzana.nostrummagica.spell.component.Transmutation.TransmutationRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class TransmutationItemCategory implements IRecipeCategory<TransmutationRecipe> {

	private static final ResourceLocation TEXT_BACK = new ResourceLocation(NostrumMagica.MODID, "textures/gui/nei/transmute.png");
	private static final int BACK_WIDTH = 99;
	private static final int BACK_HEIGHT = 32;
	private static final int ICON_HOFFSET = 0;
	private static final int ICON_VOFFSET = 64;
	
	public static final ResourceLocation UID_ITEMS = new ResourceLocation(NostrumMagica.MODID, "transmutation_item_recipe");
	public static final ResourceLocation UID_BLOCKS = new ResourceLocation(NostrumMagica.MODID, "transmutation_block_recipe");
	public static final RecipeType<TransmutationRecipe> TYPE_ITEMS = new RecipeType<>(UID_ITEMS, TransmutationRecipe.class);
	public static final RecipeType<TransmutationRecipe> TYPE_BLOCKS = new RecipeType<>(UID_BLOCKS, TransmutationRecipe.class);
	
	private Component title;
	private IDrawable background;
	private IDrawable icon;
	private final ResourceLocation UID;
	private final RecipeType<TransmutationRecipe> type;
	
	public TransmutationItemCategory(IGuiHelper guiHelper, boolean blocks) {
		background = guiHelper.drawableBuilder(TEXT_BACK, 0, 0, BACK_WIDTH, BACK_HEIGHT).addPadding(0, 0, 0, 0).build();
		
		if (blocks) {
			title = new TranslatableComponent("nei.category.transmutation.block.name");
			icon = guiHelper.drawableBuilder(TEXT_BACK, ICON_HOFFSET + 16, ICON_VOFFSET, 16, 16).build();
			UID = UID_BLOCKS;
			type = TYPE_BLOCKS;
		} else {
			title = new TranslatableComponent("nei.category.transmutation.item.name");
			icon = guiHelper.drawableBuilder(TEXT_BACK, ICON_HOFFSET, ICON_VOFFSET, 16, 16).build();
			UID = UID_ITEMS;
			type = TYPE_ITEMS;
		}
	}
	
	@Override
	public RecipeType<TransmutationRecipe> getRecipeType() {
		return type;
	}
	
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public ResourceLocation getUid() {
		return UID;
	}
	
	@Override
	@Deprecated(forRemoval = true, since = "9.5.0")
	public Class<TransmutationRecipe> getRecipeClass() {
		return TransmutationRecipe.class;
	}

	@Override
	public Component getTitle() {
		return title;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void draw(TransmutationRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStackIn, double mouseX, double mouseY) {
		// Only thing to draw is the transmutation jump power
		
		final int jump = recipe.getLevel();
		final Minecraft minecraft = Minecraft.getInstance();
		final String label = "" + jump;
		minecraft.font.drawShadow(matrixStackIn, label, (BACK_WIDTH/2) - 4, BACK_HEIGHT/2 - 4, 0xFFFFFFFF);
	}
	
	@Override
	public void setRecipe(IRecipeLayoutBuilder recipeLayout, TransmutationRecipe recipe, IFocusGroup focuses) {
		/*
		 * Input is either an item stack or a source. Output is always an item stack.
		 */
		// We will always have an output, but may have an input or may have a hidden one
		final @Nonnull ItemStack output = recipe.getOutput();
		final @Nullable Ingredient input = recipe.getRevealedIngredient();
		
		IRecipeSlotBuilder slot = recipeLayout.addSlot(RecipeIngredientRole.INPUT, 14, 8);
		if (input == null || input.isEmpty()) {
			// undiscovered mapping so add a source ingredient
			slot.addIngredient(TransmuteSourceIngredientType.instance, recipe.getSource());
		} else {
			slot.addIngredients(input);
		}
		
		recipeLayout.addSlot(RecipeIngredientRole.OUTPUT, 69, 8)
			.addIngredient(VanillaTypes.ITEM_STACK, output);
	}
	
	@Override
	public IDrawable getIcon() {
		return icon;
	}
}