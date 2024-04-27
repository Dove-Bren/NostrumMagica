package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
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
	private Vector3f eulers;
	
	// Nice cached math
	private float dAngle;
	
	public ClientEffectMirrored(Vector3d origin, ClientEffectForm form, int ticks, int count) {
		this(origin, form, ticks, count, new Vector3f(0, 1, 0));
	}
	
	public ClientEffectMirrored(Vector3d origin, ClientEffectForm form, int ticks, int count, Vector3f angles) {
		super(origin, form, ticks);
		this.count = count;
		this.dAngle = (float) (360f) / (float) count;
		this.eulers = angles;
	}
	
	public ClientEffectMirrored(Vector3d origin, ClientEffectForm form, long ms, int count) {
		this(origin, form, ms, count, new Vector3f(0, 1, 0));
	}
	
	public ClientEffectMirrored(Vector3d origin, ClientEffectForm form, long ms, int count, Vector3f angles) {
		super(origin, form, ms);
		this.count = count;
		this.dAngle = (float) (360f) / (float) count;
		this.eulers = angles;
	}
	
	@Override
	protected void drawForm(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, Minecraft mc, float progress, float partialTicks) {
		for (int i = 0; i < this.count; i++) {
			ClientEffectRenderDetail newDetail = new ClientEffectRenderDetail();
			newDetail.alpha = detail.alpha;
			newDetail.red = detail.red;
			newDetail.green = detail.green;
			newDetail.blue = detail.blue;
			
			matrixStackIn.push();
			matrixStackIn.rotate(this.eulers.rotationDegrees(dAngle * (float) i));
			super.drawForm(matrixStackIn, newDetail, mc, progress, partialTicks);
			matrixStackIn.pop();
		}
		
	}
	
}
