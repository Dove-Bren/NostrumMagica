package com.smanzana.nostrummagica.client.effects.modifiers;

import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

public interface ClientEffectModifier {

	public void earlyApply(ClientEffectRenderDetail detail, float progress, float partialTicks);
	
	public void apply(ClientEffectRenderDetail detail, float progress, float partialTicks);
	
}
