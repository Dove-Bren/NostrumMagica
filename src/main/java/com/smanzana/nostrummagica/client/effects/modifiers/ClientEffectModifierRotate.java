package com.smanzana.nostrummagica.client.effects.modifiers;

import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraft.client.renderer.GlStateManager;

public class ClientEffectModifierRotate implements ClientEffectModifier {

	private float rotationsX;
	private float rotationsY;
	private float rotationsZ;
	
	public ClientEffectModifierRotate(float rotationsX,
									float rotationsY,
									float rotationsZ) {
		this.rotationsX = rotationsX;
		this.rotationsY = rotationsY;
		this.rotationsZ = rotationsZ;
	}
	
	@Override
	public void apply(ClientEffectRenderDetail detail, float progress, float partialTicks) {
		float rotX = progress * rotationsX * 360f;
		float rotY = progress * rotationsY * 360f;
		float rotZ = progress * rotationsZ * 360f;
		GlStateManager.rotate(rotX % 360f, 1f, 0f, 0f);
		GlStateManager.rotate(rotY % 360f, 0f, 1f, 0f);
		GlStateManager.rotate(rotZ % 360f, 0f, 0f, 1f);
	}

	@Override
	public void earlyApply(ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}

}
