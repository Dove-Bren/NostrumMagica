package com.smanzana.nostrummagica.jei.categories;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.book.RitualRecipePage;
import com.smanzana.nostrummagica.jei.RitualOutcomeJEIRenderer;
import com.smanzana.nostrummagica.jei.RitualOutcomeWrapper;
import com.smanzana.nostrummagica.jei.wrappers.RitualRecipeWrapper;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.spells.EMagicElement;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
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
	
	public RitualRecipeCategory(IGuiHelper guiHelper) {
		title = I18n.format("nei.category.ritual.name", (Object[]) null);
		backgroundTier1 = guiHelper.createDrawable(TEXT_TIER1, 0, 0, BACK_WIDTH, BACK_HEIGHT, 10, 0, 0, 0);
		backgroundTier2 = guiHelper.createDrawable(TEXT_TIER2, 0, 0, BACK_WIDTH, BACK_HEIGHT, 10, 0, 0, 0);
		backgroundTier3 = guiHelper.createDrawable(TEXT_TIER3, 0, 0, BACK_WIDTH, BACK_HEIGHT, 10, 0, 0, 0);
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
		
		
		String title = recipeName;
		int len = minecraft.fontRendererObj.getStringWidth(title);
		minecraft.fontRendererObj.drawString(title, (BACK_WIDTH - len) / 2, 2, 0xFF000000);
	}

	@Override
	public void drawAnimations(Minecraft minecraft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Deprecated
	public void setRecipe(IRecipeLayout recipeLayout, RitualRecipeWrapper recipeWrapper) {
		//
		NostrumMagica.logger.warn("Using old interface for " + recipeWrapper.getRitual().getTitleKey());
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
				recipeLayout.getIngredientsGroup(RitualOutcomeWrapper.class);
		
		List<ItemStack> itemOuts = ingredients.getOutputs(ItemStack.class);
		if (itemOuts != null && itemOuts.size() > 0) {
			guiItemStacks.init(10, false, 132, 66);
		} else {
			guiOutcomes.init(11, false, RitualOutcomeJEIRenderer.instance(),
					132, 64, RitualRecipePage.TEXT_TABLET_WIDTH + 4, RitualRecipePage.TEXT_TABLET_WIDTH + 4, 2, 2);
		}
		
		guiItemStacks.set(ingredients);
		guiOutcomes.set(ingredients);
	}

	@Override
	public IDrawable getIcon() {
		return null;
	}

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		return new ArrayList<String>();
	}

}