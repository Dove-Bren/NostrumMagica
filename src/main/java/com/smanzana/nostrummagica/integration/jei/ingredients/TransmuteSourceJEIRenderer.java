package com.smanzana.nostrummagica.integration.jei.ingredients;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.spell.component.Transmutation.TransmutationSource;
import com.smanzana.nostrummagica.util.RenderFuncs;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TransmuteSourceJEIRenderer implements IIngredientRenderer<TransmutationSource> {
	
	private static TransmuteSourceJEIRenderer instance = null;
	public static TransmuteSourceJEIRenderer instance() {
		if (instance == null) {
			instance = new TransmuteSourceJEIRenderer();
		}
		return instance;
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, int xPosition, int yPosition, TransmutationSource ingredient) {
		if (ingredient == null)
			return;
		
		// Note: we only use TransmutationSources as ingredients to mean MISSING/UNKNOWN sources.
		// So draw a "i dunno :3" icon
		
		RenderFuncs.drawRect(matrixStackIn, xPosition - 1, yPosition - 1, xPosition + 16 + 1, yPosition + 16 + 1, 0xFF000000);
		RenderFuncs.drawRect(matrixStackIn, xPosition, yPosition, xPosition + 16, yPosition + 16, 0xFF666666);
		
		final double iconPeriodMS = 3000;
		double alphaIcon1 = (0.0 + (((double) (System.currentTimeMillis() % iconPeriodMS)) / (double) iconPeriodMS)) % 1.0;
		double alphaIcon2 = (0.3 + (((double) (System.currentTimeMillis() % iconPeriodMS)) / (double) iconPeriodMS)) % 1.0;
		double alphaIcon3 = (0.6 + (((double) (System.currentTimeMillis() % iconPeriodMS)) / (double) iconPeriodMS)) % 1.0;
		
		// Map from 0-1 to -1 to 1 with sin curve
		alphaIcon1 = Math.sin(alphaIcon1 * 2 * Math.PI);
		alphaIcon2 = Math.sin(alphaIcon2 * 2 * Math.PI);
		alphaIcon3 = Math.sin(alphaIcon3 * 2 * Math.PI);
		
		// Put back to 0-1 but with curve
		alphaIcon1 = (alphaIcon1 + 1) / 2;
		alphaIcon2 = (alphaIcon2 + 1) / 2;
		alphaIcon3 = (alphaIcon3 + 1) / 2;
		
		final int alpha1 = ((int) (255 * alphaIcon1)) << 24;
		final int alpha2 = ((int) (255 * alphaIcon2)) << 24;
		final int alpha3 = ((int) (255 * alphaIcon3)) << 24;
		
		Minecraft mc = Minecraft.getInstance();
		RenderSystem.enableBlend();
		if (alpha1 > 0) mc.fontRenderer.drawString(matrixStackIn, "?", xPosition + 10, yPosition + 3, 0x00FF0000 | alpha1 | 0x4000000);
		if (alpha2 > 0) mc.fontRenderer.drawString(matrixStackIn, "?", xPosition + 1, yPosition + 1, 0x0000FF00 | alpha2 | 0x4000000);
		if (alpha3 > 0) mc.fontRenderer.drawString(matrixStackIn, "?", xPosition + 5, yPosition + 8, 0x000000FF | alpha3 | 0x4000000);
	}

	@Override
	public List<ITextComponent> getTooltip(TransmutationSource ingredient, ITooltipFlag flag) {
		return Lists.newArrayList(new TranslationTextComponent("info.jei.recipe.transmute.unknown"));
	}

	@Override
	public FontRenderer getFontRenderer(Minecraft minecraft, TransmutationSource ingredient) {
		return minecraft.fontRenderer;
	}
	
}
