package com.smanzana.nostrummagica.client.effects.modifiers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectModifierMove implements ClientEffectModifier {

	private Vec3 startPos;
	private Vec3 endPos;
	private float startTime;
	private float endTime;
	
	public ClientEffectModifierMove(Vec3 start, Vec3 end) {
		this(start, end, 0f, 1f);
	}
	
	public ClientEffectModifierMove(Vec3 start, Vec3 end, float startTime, float endTime) {
		this.startPos = start;
		this.endPos = end;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	@Override
	public void apply(PoseStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
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
	public void earlyApply(PoseStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}

}
