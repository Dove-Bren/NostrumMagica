package com.smanzana.nostrummagica.client.effects.modifiers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import com.mojang.math.Vector3f;

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
	public void apply(PoseStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		if (yaw != 0f) {
			matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(yaw));
		}
		if (pitch != 0f) {
			matrixStackIn.mulPose(Vector3f.XP.rotation(pitch));
		}
		matrixStackIn.translate(x, y, z);
	}

	@Override
	public void earlyApply(PoseStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}
}
