package com.smanzana.nostrummagica.client.effects;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Effect made up of multiple copies of another effect
 * @author Skyler
 *
 */
@OnlyIn(Dist.CLIENT)
public class ClientEffectMirrored extends ClientEffect {

	private int count;
	private Vector3d eulers;
	
	// Nice cached math
	private float dAngle;
	
	public ClientEffectMirrored(Vector3d origin, ClientEffectForm form, int ticks, int count) {
		this(origin, form, ticks, count, new Vector3d(0.0, 1.0, 0.0));
	}
	
	public ClientEffectMirrored(Vector3d origin, ClientEffectForm form, int ticks, int count, Vector3d angles) {
		super(origin, form, ticks);
		this.count = count;
		this.dAngle = (float) (360f) / (float) count;
		this.eulers = angles;
	}
	
	public ClientEffectMirrored(Vector3d origin, ClientEffectForm form, long ms, int count) {
		this(origin, form, ms, count, new Vector3d(0, 1, 0));
	}
	
	public ClientEffectMirrored(Vector3d origin, ClientEffectForm form, long ms, int count, Vector3d angles) {
		super(origin, form, ms);
		this.count = count;
		this.dAngle = (float) (360f) / (float) count;
		this.eulers = angles;
	}
	
	@Override
	protected void drawForm(ClientEffectRenderDetail detail, Minecraft mc, float progress, float partialTicks) {
		for (int i = 0; i < this.count; i++) {
			ClientEffectRenderDetail newDetail = new ClientEffectRenderDetail();
			newDetail.alpha = detail.alpha;
			newDetail.red = detail.red;
			newDetail.green = detail.green;
			newDetail.blue = detail.blue;
			
			GlStateManager.pushMatrix();
			GlStateManager.rotatef(dAngle * (float) i, (float) eulers.x, (float) eulers.y, (float) eulers.z);
			super.drawForm(newDetail, mc, progress, partialTicks);
			GlStateManager.popMatrix();
		}
		
	}
	
}
