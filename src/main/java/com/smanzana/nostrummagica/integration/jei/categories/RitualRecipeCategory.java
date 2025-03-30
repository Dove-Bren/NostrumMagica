package com.smanzana.nostrummagica.integration.jei.categories;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.integration.jei.RitualOutcomeWrapper;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeIngredientType;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeJEIRenderer;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.progression.requirement.IRequirement;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.ritual.outcome.IItemRitualOutcome;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.util.TextUtils;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class RitualRecipeCategory implements IRecipeCategory<RitualRecipe> {

	private static final ResourceLocation TEXT_TIER1 = new ResourceLocation(NostrumMagica.MODID, "textures/gui/nei/tier_1.png");
	private static final ResourceLocation TEXT_TIER2 = new ResourceLocation(NostrumMagica.MODID, "textures/gui/nei/tier_2.png");
	private static final ResourceLocation TEXT_TIER3 = new ResourceLocation(NostrumMagica.MODID, "textures/gui/nei/tier_3.png");
	private static final ResourceLocation TEXT_RING = new ResourceLocation(NostrumMagica.MODID, "textures/gui/nei/ring.png");
	private static final int BACK_WIDTH = 162;
	private static final int BACK_HEIGHT = 124;
	private static final int RING_WIDTH = 62;
	private static final int RING_HEIGHT = 62;
	
	public static final ResourceLocation UID = new ResourceLocation(NostrumMagica.MODID, "ritual_recipe");
	
	private Component title;
	private IDrawable backgroundTier1;
	private IDrawable backgroundTier2;
	private IDrawable backgroundTier3;
	private EMagicElement recipeFlavor;
	private int recipeTier;
	private String recipeName;
	
	public RitualRecipeCategory(IGuiHelper guiHelper) {
		title = new TranslatableComponent("nei.category.ritual.name");
		backgroundTier1 = guiHelper.drawableBuilder(TEXT_TIER1, 0, 0, BACK_WIDTH, BACK_HEIGHT).addPadding(10, 0, 0, 0).build();
		backgroundTier2 = guiHelper.drawableBuilder(TEXT_TIER2, 0, 0, BACK_WIDTH, BACK_HEIGHT).addPadding(10, 0, 0, 0).build();
		backgroundTier3 = guiHelper.drawableBuilder(TEXT_TIER3, 0, 0, BACK_WIDTH, BACK_HEIGHT).addPadding(10, 0, 0, 0).build();
		recipeFlavor = null;
		recipeTier = 0;
	}
	
	@Override
	public ResourceLocation getUid() {
		return UID;
	}
	
	@Override
	public Class<RitualRecipe> getRecipeClass() {
		return RitualRecipe.class;
	}

	@Override
	public Component getTitle() {
		return title;
	}

	@Override
	public IDrawable getBackground() {
		switch (recipeTier) {
		case 0:
		default:
			return backgroundTier1;
		case 1:
			return backgroundTier2;
		case 2:
			return backgroundTier3;
		}
	}

	@Override
	public void draw(RitualRecipe recipe, PoseStack matrixStackIn, double mouseX, double mouseY) {
		final Minecraft minecraft = Minecraft.getInstance();
		matrixStackIn.pushPose();
		
		float red, green, blue, alpha, angle;
		alpha = 1f;
		angle = 0f;
		
		if (recipeFlavor == null)
			recipeFlavor = EMagicElement.PHYSICAL;
		
		float frac = (float) (System.currentTimeMillis() % 3000L) / 3000f;
		angle = frac * 360f;
		alpha = .8f + .2f * (float) Math.sin(frac * 2 * Math.PI);
		
		int color = recipeFlavor.getColor(); // ARGB
		red = ((float) ((color >> 16) & 0xFF) / 255f);
		green = ((float) ((color >> 8) & 0xFF) / 255f);
		blue = ((float) (color & 0xFF) / 255f);
		matrixStackIn.translate(48, 70, 0);
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(angle));
		matrixStackIn.translate(-RING_WIDTH / 2, -RING_HEIGHT/2, 0);
		
		RenderSystem.enableBlend();
		RenderSystem.setShaderTexture(0, TEXT_RING);
		RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, 0, 0, 0,
				0, RING_WIDTH, RING_HEIGHT, RING_WIDTH, RING_HEIGHT,
				red, green, blue, alpha);
		
		matrixStackIn.popPose();
		
		if (!canPerform(recipe)) {
			minecraft.font.draw(matrixStackIn, ChatFormatting.BOLD + "x" + ChatFormatting.RESET, 108, 70, 0xFFAA0000);
		}
		
		
		String title = recipeName;
		int len = minecraft.font.width(title);
		minecraft.font.draw(matrixStackIn, title, (BACK_WIDTH - len) / 2, 2, 0xFF000000);
	}
	
	@Override
	public void setIngredients(RitualRecipe ritual, IIngredients ingredients) {
		List<Ingredient> stackInputs = new ArrayList<>();
		
		// Add flavor gem
		stackInputs.add(Ingredient.of(InfusedGemItem.getGemItem(ritual.getElement())));
		Ingredient reagent2, reagent3, reagent4;
		ReagentType reagents[] = ritual.getTypes();
		stackInputs.add(Ingredient.of(ReagentItem.GetItem(reagents[0])));
		if (reagents.length > 1) {
			reagent2 = Ingredient.of(ReagentItem.GetItem(reagents[1]));
			reagent3 = Ingredient.of(ReagentItem.GetItem(reagents[2]));
			reagent4 = Ingredient.of(ReagentItem.GetItem(reagents[3]));
		} else {
			reagent2 = reagent3 = reagent4 = Ingredient.EMPTY;
		}
		stackInputs.add(reagent2);
		stackInputs.add(reagent3);
		stackInputs.add(reagent4);
		
		stackInputs.add(ritual.getCenterItem());
		NonNullList<Ingredient> extras = ritual.getExtraItems();
		Ingredient extra1, extra2, extra3, extra4;
		extra1 = extra2 = extra3 = extra4 = Ingredient.EMPTY;
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
		stackInputs.add(extra1 == null ? Ingredient.EMPTY : extra1);
		stackInputs.add(extra2 == null ? Ingredient.EMPTY : extra2);
		stackInputs.add(extra3 == null ? Ingredient.EMPTY : extra3);
		stackInputs.add(extra4 == null ? Ingredient.EMPTY : extra4);
		
//		if (ritual.getCenterItem() != Ingredient.EMPTY && !ritual.getCenterItem().hasNoMatchingItems()) {
//			stackInputs.add(Lists.newArrayList(ritual.getCenterItem().getMatchingStacks()));
//		}
//		for (Ingredient ing : ritual.getExtraItems()) {
//			if (ing == Ingredient.EMPTY || ing.hasNoMatchingItems()) {
//				stackInputs.add(new ArrayList<>()); // should be null?
//			} else {
//				stackInputs.add(Lists.newArrayList(ing.getMatchingStacks()));
//			}
//		}
//		
		ingredients.setInputIngredients(stackInputs);
		if (ritual.getOutcome() instanceof IItemRitualOutcome) {
			ingredients.setOutput(VanillaTypes.ITEM, ((IItemRitualOutcome) ritual.getOutcome()).getResult());
		} else {
			ingredients.setOutput(RitualOutcomeIngredientType.instance, new RitualOutcomeWrapper(ritual.getOutcome()));
		}
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, RitualRecipe ritual, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		/*
		 * reagents are first. 0, 1, 2, 3 are itemstacks for reagents
		 * center item next
		 * extra items next
		 * nulls exist!
		 */
		
		recipeFlavor = ritual.getElement();
		recipeTier = ritual.getTier();
		recipeName = I18n.get("ritual." + ritual.getTitleKey() + ".name", (Object[]) null);

		guiItemStacks.init(0, true, 102, 35);
		if (ritual.getTier() == 0) {
			guiItemStacks.init(1, true, 41, 54);
		} else if (ritual.getTier() == 1) {
			// reagents
			guiItemStacks.init(1, true, 6, 26);
			guiItemStacks.init(2, true, 74, 26);
			guiItemStacks.init(3, true, 6, 97);
			guiItemStacks.init(4, true, 74, 97);
			
			// center item
			guiItemStacks.init(5, true, 41, 54);
		} else {
			// reagents
			guiItemStacks.init(1, true, 6, 26);
			guiItemStacks.init(2, true, 74, 26);
			guiItemStacks.init(3, true, 6, 97);
			guiItemStacks.init(4, true, 74, 97);
			
			// center item
			guiItemStacks.init(5, true, 41, 54);
			
			// extra items
			guiItemStacks.init(6, true, 5, 69);
			guiItemStacks.init(7, true, 41, 13);
			guiItemStacks.init(8, true, 41, 98);
			guiItemStacks.init(9, true, 76, 69);
		}
		
		IGuiIngredientGroup<RitualOutcomeWrapper> guiOutcomes =
				recipeLayout.getIngredientsGroup(RitualOutcomeIngredientType.instance);
		
		List<List<ItemStack>> itemOuts = ingredients.getOutputs(VanillaTypes.ITEM);
		if (itemOuts != null && itemOuts.size() > 0) {
			guiItemStacks.init(10, false, 132, 66);
		} else {
			guiOutcomes.init(11, false, RitualOutcomeJEIRenderer.instance(),
					132, 64, RitualOutcomeJEIRenderer.RITUAL_TEXT_TABLET_WIDTH + 4, RitualOutcomeJEIRenderer.RITUAL_TEXT_TABLET_WIDTH + 4, 2, 2);
		}
		
		guiItemStacks.set(ingredients);
		guiOutcomes.set(ingredients);
	}

	@Override
	public IDrawable getIcon() {
		return null;
	}
	
	private List<Component> tooltipEmpty = new ArrayList<>();

	@Override
	public List<Component> getTooltipStrings(RitualRecipe ritual, double mouseX, double mouseY) {
		//108, 70
		if (!canPerform(ritual)
				&& mouseX > 101 && mouseX < 124
				&& mouseY > 66 && mouseY < 85) {
			List<Component> tooltip = TextUtils.GetTranslatedList("info.jei.recipe.ritual.invalid", ChatFormatting.BOLD + "" + ChatFormatting.RED, ChatFormatting.BLACK);
			List<Component> extras = ritual.getRequirement() == null ? null : ritual.getRequirement().getDescription(NostrumMagica.instance.proxy.getPlayer());
			if (extras != null) {
				extras.forEach(t -> tooltip.add(new TextComponent(" - ").append(t)));
			}
			return tooltip;
		}
			
		return tooltipEmpty;
	}
	
	protected boolean canPerform(RitualRecipe ritual) {
		// Check whether this ritual can be performed
		boolean canPerform = true;
		Player player = NostrumMagica.instance.proxy.getPlayer();
		if (player != null) {
			// Client side, so check if player has unlocked the ritual
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (!attr.isUnlocked()) {
				canPerform = false;
			} else if (ritual.getRequirement() != null) {
				IRequirement req = ritual.getRequirement();
				if (!req.matches(player)) {
					canPerform = false;
				}
			}
		}
		return canPerform;
	}

}