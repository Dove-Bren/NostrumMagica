package com.smanzana.nostrummagica.client.render.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class EffectGemRenderer extends EffectBubbleRenderer {
	
	private static final float width = .1f;
	
	public EffectGemRenderer(float yOffset, float orbitSpeed, float orbitOffset, int color) {
		super(yOffset, orbitSpeed, orbitOffset, color);
	}
	
	public EffectGemRenderer(float yExtraOffset, MobEffect effect) {
		this(yExtraOffset + GetDefaultOffset(effect.getCategory()), GetDefaultOrbit(effect.getCategory()), GetDefaultOrbitOffset(effect), effect.getColor());
	}
	
	public EffectGemRenderer(MobEffect effect) {
		this(0f, effect);
	}
	
	@Override
	protected float getWidth() {
		return width;
	}
	
	@Override
	public void renderEffectOnEntity(MobEffectInstance effect, PoseStack stack, MultiBufferSource typeBuffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		super.renderEffectOnEntity(effect, stack, typeBuffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
	}
	
	protected void renderOrb(PoseStack stack, MultiBufferSource typeBuffer, int packedLight, float width, float red, float green, float blue, float alpha) {
		final VertexConsumer buffer = typeBuffer.getBuffer(NostrumRenderTypes.SWITCH_TRIGGER_BASE);
		final int packedOverlay = OverlayTexture.NO_OVERLAY;
		
		RenderFuncs.renderDiamond(stack, buffer, width, width, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
