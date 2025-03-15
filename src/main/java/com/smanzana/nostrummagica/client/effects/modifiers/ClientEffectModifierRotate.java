package com.smanzana.nostrummagica.client.effects.modifiers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraft.util.math.vector.Vector3f;

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
	public void apply(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		float rotX = progress * rotationsX * 360f;
		float rotY = progress * rotationsY * 360f;
		float rotZ = progress * rotationsZ * 360f;
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(rotX % 360f));
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rotY % 360f));
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(rotZ % 360f));
	}

	@Override
	public void earlyApply(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}

}
