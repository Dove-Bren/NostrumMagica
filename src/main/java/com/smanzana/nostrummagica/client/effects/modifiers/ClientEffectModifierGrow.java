package com.smanzana.nostrummagica.client.effects.modifiers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectModifierGrow implements ClientEffectModifier {

	private float startScale;
	private float startAlpha;
	private float endScale;
	private float endAlpha;
	private float plateau; // Where to stop progressing and just wait
	
	public ClientEffectModifierGrow() {
		this(.5f, 0f, 1f, 1f, .5f);
	}
	
	public ClientEffectModifierGrow(float startScale, float startAlpha,
							float endScale, float endAlpha,
							float plateau) {
		this.startScale = startScale;
		this.startAlpha = startAlpha;
		this.endScale = endScale;
		this.endAlpha = endAlpha;
		this.plateau = plateau;
	}
	
	@Override
	public void apply(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		if (progress < this.plateau) {
			// Stage 1
			final float frac = progress / plateau;
			final float scale = startScale + ((endScale - startScale) * frac);
			final float alpha = startAlpha + ((endAlpha - startAlpha) * frac);
			matrixStackIn.scale(scale, scale, scale);
			detail.alpha *= alpha;
		} else {
			matrixStackIn.scale(endScale, endScale, endScale);
			detail.alpha *= endAlpha;
			
		}
	}

	@Override
	public void earlyApply(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}

}
