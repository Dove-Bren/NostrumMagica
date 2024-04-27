package com.smanzana.nostrummagica.client.effects.modifiers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectModifierColor implements ClientEffectModifier {

	private float startRed;
	private float startGreen;
	private float startBlue;
	private float startAlpha;
	private float endRed;
	private float endGreen;
	private float endBlue;
	private float endAlpha;
	private float plateau; // Where to stop progressing and just wait
	
	public ClientEffectModifierColor(int startColor, int endColor) {
		this(startColor, endColor, .5f);
	}
	
	public ClientEffectModifierColor(int startColor, int endColor,
							float plateau) {
		startRed = 	 ((float) ((startColor >> 16) & 0xFF) / 256f);
		startGreen = ((float) ((startColor >> 8) & 0xFF) / 256f);
		startBlue =  ((float) ((startColor >> 0) & 0xFF) / 256f);
		startAlpha = ((float) ((startColor >> 24) & 0xFF) / 256f);
		endRed = 	 ((float) ((endColor >> 16) & 0xFF) / 256f);
		endGreen =	 ((float) ((endColor >> 8) & 0xFF) / 256f);
		endBlue =	 ((float) ((endColor >> 0) & 0xFF) / 256f);
		endAlpha =	 ((float) ((endColor >> 24) & 0xFF) / 256f);
		this.plateau = plateau;
	}
	
	@Override
	public void apply(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		if (progress < this.plateau) {
			// Stage 1
			final float frac = progress / plateau;
			detail.red *= startRed + ((endRed - startRed) * frac);
			detail.green *= startGreen + ((endGreen - startGreen) * frac);
			detail.blue *= startBlue + ((endBlue - startBlue) * frac);
			detail.alpha *= startAlpha + ((endAlpha - startAlpha) * frac);
		} else {
			detail.red *= endRed;
			detail.green *= endGreen;
			detail.blue *= endBlue;
			detail.alpha *= endAlpha;
		}
	}

	@Override
	public void earlyApply(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}

}
