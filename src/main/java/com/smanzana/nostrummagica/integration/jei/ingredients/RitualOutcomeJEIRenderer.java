package com.smanzana.nostrummagica.integration.jei.ingredients;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.jei.RitualOutcomeWrapper;
import com.smanzana.nostrummagica.rituals.outcomes.IItemRitualOutcome;
import com.smanzana.nostrummagica.rituals.outcomes.IRitualOutcome;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RitualOutcomeJEIRenderer implements IIngredientRenderer<RitualOutcomeWrapper> {
	
	public static final ResourceLocation RITUAL_TEXTURE = new ResourceLocation(NostrumMagica.MODID, "textures/gui/book_extras.png");
	public static final int RITUAL_TEXT_WIDTH = 256;
	public static final int RITUAL_TEXT_HEIGHT = 128;
	public static final int RITUAL_TEXT_TABLET_VOFFSET = 64;
	public static final int RITUAL_TEXT_TABLET_WIDTH = 18;


	private static RitualOutcomeJEIRenderer instance = null;
	public static RitualOutcomeJEIRenderer instance() {
		if (instance == null) {
			instance = new RitualOutcomeJEIRenderer();
		}
		return instance;
	}
	
	@Override
	public void render(Minecraft minecraft, int xPosition, int yPosition, RitualOutcomeWrapper ingredient) {
		if (ingredient == null)
			return;
		minecraft.getTextureManager().bindTexture(RITUAL_TEXTURE);
		ItemStack item = fetchItem(ingredient.getOutcome());
		GlStateManager.enableBlend();
		GlStateManager.color(1f, 1f, 1f, 1f);
		if (item != null) {
			Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_TABLET_VOFFSET,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_WIDTH,
					RITUAL_TEXT_HEIGHT);
			
			minecraft.getRenderItem().renderItemIntoGUI(item, xPosition + 1, yPosition + 1);
		} else {
			Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition,
					0,
					RITUAL_TEXT_TABLET_VOFFSET,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_WIDTH,
					RITUAL_TEXT_HEIGHT);
		}
	}

	@Override
	public List<String> getTooltip(Minecraft minecraft, RitualOutcomeWrapper ingredient) {
		if (ingredient == null)
			return new LinkedList<>();
		
		ItemStack item = fetchItem(ingredient.getOutcome());
		if (item == null)
			return ingredient.getOutcome().getDescription();
		else
			return Lists.newArrayList(item.getDisplayName());
	}

	@Override
	public FontRenderer getFontRenderer(Minecraft minecraft, RitualOutcomeWrapper ingredient) {
		if (ingredient == null)
			return minecraft.fontRenderer;
		
		FontRenderer render = null;
		ItemStack stack = fetchItem(ingredient.getOutcome());
		if (stack != null)
			render = stack.getItem().getFontRenderer(stack);
		
		if (render == null)
			render = minecraft.fontRenderer;
		
		return render;
	}
	
	private static ItemStack fetchItem(IRitualOutcome outcome) {
		if (outcome != null && outcome instanceof IItemRitualOutcome) {
			return ((IItemRitualOutcome) outcome).getResult();
		}
		
		return null;
	}

}
