package com.smanzana.nostrummagica.client.effects.modifiers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

public interface ClientEffectModifier {

	public void earlyApply(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks);
	
	public void apply(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks);
	
}
