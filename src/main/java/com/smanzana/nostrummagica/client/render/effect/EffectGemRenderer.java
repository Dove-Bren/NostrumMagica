package com.smanzana.nostrummagica.client.render.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;

public class EffectGemRenderer extends EffectBubbleRenderer {
	
	private static final ResourceLocation TEX_GEM = NostrumMagica.Loc("textures/models/crystal_blank.png");
	private static final float width = .1f;
	
	public EffectGemRenderer(float yOffset, float orbitSpeed, float orbitOffset, int color) {
		super(yOffset, orbitSpeed, orbitOffset, color);
	}
	
	public EffectGemRenderer(float yExtraOffset, Effect effect) {
		this(yExtraOffset + GetDefaultOffset(effect.getEffectType()), GetDefaultOrbit(effect.getEffectType()), GetDefaultOrbitOffset(effect), effect.getLiquidColor());
	}
	
	public EffectGemRenderer(Effect effect) {
		this(0f, effect);
	}
	
	@Override
	protected float getWidth() {
		return width;
	}
	
	@Override
	public void renderEffectOnEntity(EffectInstance effect, MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		super.renderEffectOnEntity(effect, stack, typeBuffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
	}
	
	protected void renderOrb(MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, float width, float red, float green, float blue, float alpha) {
		final IVertexBuilder buffer = typeBuffer.getBuffer(NostrumRenderTypes.SWITCH_TRIGGER_BASE);
		final int packedOverlay = OverlayTexture.NO_OVERLAY;
		
		RenderFuncs.renderDiamond(stack, buffer, width, width, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
