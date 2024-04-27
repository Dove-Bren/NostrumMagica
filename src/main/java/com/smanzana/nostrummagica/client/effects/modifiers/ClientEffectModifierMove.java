package com.smanzana.nostrummagica.client.effects.modifiers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectModifierMove implements ClientEffectModifier {

	private Vector3d startPos;
	private Vector3d endPos;
	private float startTime;
	private float endTime;
	
	public ClientEffectModifierMove(Vector3d start, Vector3d end) {
		this(start, end, 0f, 1f);
	}
	
	public ClientEffectModifierMove(Vector3d start, Vector3d end, float startTime, float endTime) {
		this.startPos = start;
		this.endPos = end;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	@Override
	public void apply(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		if (progress < this.startTime) {
			matrixStackIn.translate(startPos.x, startPos.y, startPos.z);
		} else if (progress < this.endTime) {
			// Stage 1
			final float frac = (progress - startTime) / (endTime - startTime);
			matrixStackIn.translate(
					startPos.x + frac * (endPos.x - startPos.x),
					startPos.y + frac * (endPos.y - startPos.y),
					startPos.z + frac * (endPos.z - startPos.z)
					);
		} else {
			matrixStackIn.translate(endPos.x, endPos.y, endPos.z);
		}
	}

	@Override
	public void earlyApply(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}

}
