package com.smanzana.nostrummagica.client.effects.modifiers;

import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraft.client.renderer.GlStateManager;

public class ClientEffectModifierTranslate implements ClientEffectModifier {

	private float x;
	private float y;
	private float z;
	
	public ClientEffectModifierTranslate(float x,
									float y,
									float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public void apply(ClientEffectRenderDetail detail, float progress) {
		GlStateManager.translate(x, y, z);
	}

}
