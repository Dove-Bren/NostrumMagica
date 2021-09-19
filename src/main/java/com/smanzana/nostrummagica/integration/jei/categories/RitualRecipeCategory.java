package com.smanzana.nostrummagica.integration.jei.categories;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.integration.jei.RitualOutcomeWrapper;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeIngredientType;
import com.smanzana.nostrummagica.integration.jei.ingredients.RitualOutcomeJEIRenderer;
import com.smanzana.nostrummagica.integration.jei.wrappers.RitualRecipeWrapper;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.requirements.IRitualRequirement;
import com.smanzana.nostrummagica.spells.EMagicElement;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RitualRecipeCategory implements IRecipeCategory<RitualRecipeWrapper> {

	private static final ResourceLocation TEXT_TIER1 = new ResourceLocation(NostrumMagica.MODID, "textures/gui/nei/tier_1.png");
	private static final ResourceLocation TEXT_TIER2 = new ResourceLocation(NostrumMagica.MODID, "textures/gui/nei/tier_2.png");
	private static final ResourceLocation TEXT_TIER3 = new ResourceLocation(NostrumMagica.MODID, "textures/gui/nei/tier_3.png");
	private static final ResourceLocation TEXT_RING = new ResourceLocation(NostrumMagica.MODID, "textures/gui/nei/ring.png");
	private static final int BACK_WIDTH = 162;
	private static final int BACK_HEIGHT = 124;
	private static final int RING_WIDTH = 62;
	private static final int RING_HEIGHT = 62;
	
	public static String UID = "nostrummagica:ritual_recipe";
	
	private String title;
	private IDrawable backgroundTier1;
	private IDrawable backgroundTier2;
	private IDrawable backgroundTier3;
	private EMagicElement recipeFlavor;
	private int recipeTier;
	private String recipeName;
	private boolean canPerform;
	
	public RitualRecipeCategory(IGuiHelper guiHelper) {
		title = I18n.format("nei.category.ritual.name", (Object[]) null);
		backgroundTier1 = guiHelper.drawableBuilder(TEXT_TIER1, 0, 0, BACK_WIDTH, BACK_HEIGHT).addPadding(10, 0, 0, 0).build();
		backgroundTier2 = guiHelper.drawableBuilder(TEXT_TIER2, 0, 0, BACK_WIDTH, BACK_HEIGHT).addPadding(10, 0, 0, 0).build();
		backgroundTier3 = guiHelper.drawableBuilder(TEXT_TIER3, 0, 0, BACK_WIDTH, BACK_HEIGHT).addPadding(10, 0, 0, 0).build();
		recipeFlavor = null;
		recipeTier = 0;
	}
	
	@Override
	public String getUid() {
		return UID;
	}

	@Override
	public String getTitle() {
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
	public void drawExtras(Minecraft minecraft) {
		GlStateManager.pushMatrix();
		
		float red, green, blue, alpha, angle;
		alpha = 1f;
		angle = 0f;
		
		if (recipeFlavor == null)
			recipeFlavor = EMagicElement.PHYSICAL;
		
		float frac = (float) (Minecraft.getSystemTime() % 3000L) / 3000f;
		angle = frac * 360f;
		alpha = .8f + .2f * (float) Math.sin(frac * 2 * Math.PI);
		
		int color = recipeFlavor.getColor(); // ARGB
		red = ((float) ((color >> 16) & 0xFF) / 255f);
		green = ((float) ((color >> 8) & 0xFF) / 255f);
		blue = ((float) (color & 0xFF) / 255f);
		GlStateManager.translate(48, 70, 0);
		GlStateManager.rotate(angle, 0, 0, 1f);
		GlStateManager.translate(-RING_WIDTH / 2, -RING_HEIGHT/2, 0);
		
		GlStateManager.color(red, green, blue, alpha);
		GlStateManager.enableBlend();
		minecraft.getTextureManager().bindTexture(TEXT_RING);
		Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0,
				RING_WIDTH, RING_HEIGHT, RING_WIDTH, RING_HEIGHT);
		
		GlStateManager.popMatrix();
		
		if (!canPerform) {
			minecraft.fontRenderer.drawString(ChatFormatting.BOLD + "x" + ChatFormatting.RESET, 108, 70, 0xFFAA0000);
		}
		
		
		String title = recipeName;
		int len = minecraft.fontRenderer.getStringWidth(title);
		minecraft.fontRenderer.drawString(title, (BACK_WIDTH - len) / 2, 2, 0xFF000000);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, RitualRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		/*
		 * reagents are first. 0, 1, 2, 3 are itemstacks for reagents
		 * center item next
		 * extra items next
		 * nulls exist!
		 */
		
		RitualRecipe ritual = recipeWrapper.getRitual();
		
		recipeFlavor = ritual.getElement();
		recipeTier = ritual.getTier();
		recipeName = I18n.format("ritual." + ritual.getTitleKey() + ".name", (Object[]) null);

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
		
		int unused; // why don't I have to change this after changing its type?
		List<List<ItemStack>> itemOuts = ingredients.getOutputs(VanillaTypes.ITEM);
		if (itemOuts != null && itemOuts.size() > 0) {
			guiItemStacks.init(10, false, 132, 66);
		} else {
			guiOutcomes.init(11, false, RitualOutcomeJEIRenderer.instance(),
					132, 64, RitualOutcomeJEIRenderer.RITUAL_TEXT_TABLET_WIDTH + 4, RitualOutcomeJEIRenderer.RITUAL_TEXT_TABLET_WIDTH + 4, 2, 2);
		}
		
		guiItemStacks.set(ingredients);
		guiOutcomes.set(ingredients);
		
		// Check whether this ritual can be performed
		canPerform = true;
		EntityPlayer player = NostrumMagica.proxy.getPlayer();
		if (player != null) {
			// Client side, so check if player has unlocked the ritual
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (!attr.isUnlocked()) {
				canPerform = false;
			} else if (ritual.getRequirement() != null) {
				IRitualRequirement req = ritual.getRequirement();
				if (!req.matches(player, attr)) {
					canPerform = false;
				}
			}
		}
	}

	@Override
	public IDrawable getIcon() {
		return null;
	}
	
	private String tooltipInvalidKey = null;
	private List<String> tooltipInvalid = null;
	private List<String> tooltipEmpty = new ArrayList<>();

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		//108, 70
		if (!this.canPerform
				&& mouseX > 101 && mouseX < 124
				&& mouseY > 66 && mouseY < 85) {
			if (tooltipInvalidKey == null || !tooltipInvalidKey.equals(I18n.format("info.jei.recipe.ritual.invalid", ChatFormatting.BOLD + "" + ChatFormatting.RED, ChatFormatting.BLACK))) {
				tooltipInvalidKey = I18n.format("info.jei.recipe.ritual.invalid", ChatFormatting.BOLD + "" + ChatFormatting.RED, ChatFormatting.BLACK);;
				tooltipInvalid = Lists.newArrayList(tooltipInvalidKey.split("\\|"));
			}
			return tooltipInvalid;
		}
			
		return tooltipEmpty;
	}

	@Nonnull
	@Override
	public String getModName() {
		return NostrumMagica.MODID;
	}

}