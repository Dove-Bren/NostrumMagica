package com.smanzana.nostrummagica.client.effects.modifiers;

import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

public interface ClientEffectModifier {

	public void apply(ClientEffectRenderDetail detail, float progress);
	
}
