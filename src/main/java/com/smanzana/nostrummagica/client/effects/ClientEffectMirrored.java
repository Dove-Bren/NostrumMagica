package com.smanzana.nostrummagica.client.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Effect made up of multiple copies of another effect
 * @author Skyler
 *
 */
@SideOnly(Side.CLIENT)
public class ClientEffectMirrored extends ClientEffect {

	private int count;
	private Vec3d eulers;
	
	// Nice cached math
	private float dAngle;
	
	public ClientEffectMirrored(Vec3d origin, ClientEffectForm form, int ticks, int count) {
		this(origin, form, ticks, count, new Vec3d(0.0, 1.0, 0.0));
	}
	
	public ClientEffectMirrored(Vec3d origin, ClientEffectForm form, int ticks, int count, Vec3d angles) {
		super(origin, form, ticks);
		this.count = count;
		this.dAngle = (float) (360f) / (float) count;
		this.eulers = angles;
	}
	
	public ClientEffectMirrored(Vec3d origin, ClientEffectForm form, long ms, int count) {
		this(origin, form, ms, count, new Vec3d(0, 1, 0));
	}
	
	public ClientEffectMirrored(Vec3d origin, ClientEffectForm form, long ms, int count, Vec3d angles) {
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
			GlStateManager.rotate(dAngle * (float) i, (float) eulers.x, (float) eulers.y, (float) eulers.z);
			super.drawForm(newDetail, mc, progress, partialTicks);
			GlStateManager.popMatrix();
		}
		
	}
	
}
