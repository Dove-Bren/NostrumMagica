package com.smanzana.nostrummagica.client.effects.modifiers;

import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraft.client.renderer.GlStateManager;

public class ClientEffectModifierTranslate implements ClientEffectModifier {

	private final float x;
	private final float y;
	private final float z;
	private final float pitch;
	private final float yaw;
	
	public ClientEffectModifierTranslate(float x,
			float y,
			float z,
			float pitch,
			float yaw) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
	}
	
	public ClientEffectModifierTranslate(float x,
									float y,
									float z) {
		this(x, y, z, 0f, 0f);
	}
	
	@Override
	public void apply(ClientEffectRenderDetail detail, float progress, float partialTicks) {
		if (yaw != 0f) {
			GlStateManager.rotate(yaw, 0f, 1f, 0f);
		}
		if (pitch != 0f) {
			GlStateManager.rotate(pitch, 1f, 0f, 0f);
		}
		GlStateManager.translate(x, y, z);
	}

	@Override
	public void earlyApply(ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}
}
