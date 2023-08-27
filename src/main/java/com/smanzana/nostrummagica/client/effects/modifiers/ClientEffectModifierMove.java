package com.smanzana.nostrummagica.client.effects.modifiers;

import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectModifierMove implements ClientEffectModifier {

	private Vec3d startPos;
	private Vec3d endPos;
	private float startTime;
	private float endTime;
	
	public ClientEffectModifierMove(Vec3d start, Vec3d end) {
		this(start, end, 0f, 1f);
	}
	
	public ClientEffectModifierMove(Vec3d start, Vec3d end, float startTime, float endTime) {
		this.startPos = start;
		this.endPos = end;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	@Override
	public void apply(ClientEffectRenderDetail detail, float progress, float partialTicks) {
		if (progress < this.startTime) {
			GlStateManager.translated(startPos.x, startPos.y, startPos.z);
		} else if (progress < this.endTime) {
			// Stage 1
			final float frac = (progress - startTime) / (endTime - startTime);
			GlStateManager.translated(
					startPos.x + frac * (endPos.x - startPos.x),
					startPos.y + frac * (endPos.y - startPos.y),
					startPos.z + frac * (endPos.z - startPos.z)
					);
		} else {
			GlStateManager.translated(endPos.x, endPos.y, endPos.z);
			
		}
	}

	@Override
	public void earlyApply(ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}

}
