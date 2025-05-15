package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

/**
 * Effect made by displaying multiple other effects with slight time offsets
 * @author Skyler
 *
 */
public class ClientEffectEchoed extends ClientEffect {

	private int count;
	private float span;
	private ClientEffect effect;
	
	public ClientEffectEchoed(Vec3 origin, ClientEffect effect, int ticks, int count, float span) {
		super(origin, null, ticks);
		this.count = count;
		this.span = span;
		this.effect = effect;
	}
	
	public ClientEffectEchoed(Vec3 origin, ClientEffect effect, long ms, int count, float span) {
		super(origin, null, ms);
		this.count = count;
		this.span = span;
		this.effect = effect;
	}
	
	@Override
	protected void drawForm(PoseStack matrixStackIn, ClientEffectRenderDetail detail, Minecraft mc, MultiBufferSource buffersIn, float progress, float partialTicks) {
		// Span is centered on actual progress.
		// So at start and at end there will be part chopped off
		// Calculate that part now and ignore in loop
		int min, max;
		float progDiff = span / (float) count;
		min = Math.max(0, (count / 2) - (int)(progress / progDiff));
		max = Math.min(count, (count / 2) + (int)((1f - progress) / progDiff));
		final int center = count / 2;
		for (int i = min; i < max; i++) {
			ClientEffectRenderDetail newDetail = new ClientEffectRenderDetail();
			newDetail.alpha = detail.alpha;
			newDetail.red = detail.red;
			newDetail.green = detail.green;
			newDetail.blue = detail.blue;
			
			float diff = (float) (i - center) * progDiff;
			
			matrixStackIn.pushPose();
			if (!this.modifiers.isEmpty())
				for (ClientEffectModifier mod : modifiers) {
					mod.apply(matrixStackIn, newDetail, progress + diff, partialTicks);
				}
			
			effect.drawForm(matrixStackIn, newDetail, mc, buffersIn, progress + diff, partialTicks);
			matrixStackIn.popPose();
		}
		
	}
	
}
