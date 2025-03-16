package com.smanzana.nostrummagica.client.effects.modifiers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

public interface ClientEffectModifier {

	public void earlyApply(PoseStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks);
	
	public void apply(PoseStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks);
	
}
