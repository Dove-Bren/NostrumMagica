package com.smanzana.nostrummagica.integration.jei.ingredients;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.jei.RitualOutcomeWrapper;
import com.smanzana.nostrummagica.ritual.outcome.IItemRitualOutcome;
import com.smanzana.nostrummagica.ritual.outcome.IRitualOutcome;
import com.smanzana.nostrummagica.util.RenderFuncs;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

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
	public void render(MatrixStack matrixStackIn, int xPosition, int yPosition, RitualOutcomeWrapper ingredient) {
		if (ingredient == null)
			return;
		Minecraft.getInstance().getTextureManager().bindTexture(RITUAL_TEXTURE);
		ItemStack item = fetchItem(ingredient.getOutcome());
		RenderSystem.enableBlend();
		if (item != null) {
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, xPosition,
					yPosition,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_TABLET_VOFFSET,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_WIDTH, RITUAL_TEXT_HEIGHT);
			
			Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(item, xPosition + 1, yPosition + 1);
		} else {
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, xPosition,
					yPosition,
					0,
					RITUAL_TEXT_TABLET_VOFFSET,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_WIDTH, RITUAL_TEXT_HEIGHT);
		}
	}

	@Override
	public List<ITextComponent> getTooltip(RitualOutcomeWrapper ingredient, ITooltipFlag flag) {
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
