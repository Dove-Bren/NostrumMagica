package com.smanzana.nostrummagica.client.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;

/**
 * Effect made up of multiple copies of another effect
 * @author Skyler
 *
 */
public class ClientEffectMirrored extends ClientEffect {

	private int count;
	
	// Nice cached math
	private float dAngle;
	
	public ClientEffectMirrored(Vec3d origin, ClientEffectForm form, int ticks, int count) {
		super(origin, form, ticks);
		this.count = count;
		this.dAngle = (float) (360f) / (float) count;
	}
	
	public ClientEffectMirrored(Vec3d origin, ClientEffectForm form, long ms, int count) {
		super(origin, form, ms);
		this.count = count;
		this.dAngle = (float) (360f) / (float) count;
	}

	@Override
	protected void drawForm(Minecraft mc, float progress, float partialTicks) {
		for (int i = 0; i < this.count; i++) {
			GlStateManager.pushMatrix();
			GlStateManager.rotate(dAngle * (float) i, 0f, 1f, 0f);
			super.drawForm(mc, progress, partialTicks);
			GlStateManager.popMatrix();
		}
		
	}
	
}
