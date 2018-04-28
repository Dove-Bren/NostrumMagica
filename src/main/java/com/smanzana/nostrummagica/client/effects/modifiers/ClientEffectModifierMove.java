package com.smanzana.nostrummagica.client.effects.modifiers;

import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;

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
	public void apply(ClientEffectRenderDetail detail, float progress) {
		if (progress < this.startTime) {
			GlStateManager.translate(startPos.xCoord, startPos.yCoord, startPos.zCoord);
		} else if (progress < this.endTime) {
			// Stage 1
			final float frac = (progress - startTime) / (endTime - startTime);
			GlStateManager.translate(
					startPos.xCoord + frac * (endPos.xCoord - startPos.xCoord),
					startPos.yCoord + frac * (endPos.yCoord - startPos.yCoord),
					startPos.zCoord + frac * (endPos.zCoord - startPos.zCoord)
					);
		} else {
			GlStateManager.translate(endPos.xCoord, endPos.yCoord, endPos.zCoord);
			
		}
	}

	@Override
	public void earlyApply(ClientEffectRenderDetail detail, float progress) {
		;
	}

}
