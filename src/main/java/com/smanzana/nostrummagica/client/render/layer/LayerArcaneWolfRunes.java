package com.smanzana.nostrummagica.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelArcaneWolf;
import com.smanzana.nostrummagica.client.render.entity.RenderArcaneWolf;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class LayerArcaneWolfRunes extends RenderLayer<ArcaneWolfEntity, ModelArcaneWolf> {

	private static final ResourceLocation RUNE_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/entity/arcane_wolf/overlay.png");
	private final RenderArcaneWolf wolfRenderer;
	
	public LayerArcaneWolfRunes(RenderArcaneWolf wolfRenderer) {
		super(wolfRenderer);
		this.wolfRenderer = wolfRenderer;
	}
	
	@Override
	public void render(PoseStack stack, MultiBufferSource typeBuffer, int packedLight, ArcaneWolfEntity wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!wolf.isInvisible()) {
			
			final VertexConsumer buffer = typeBuffer.getBuffer(RenderType.entityTranslucentCull(RUNE_LOC));
			final float brightPeriod = 40f;
			final float brightness = .8f + .2f * (float) Math.sin(Math.PI * 2 * ((partialTicks + wolf.tickCount) % brightPeriod) / brightPeriod);
			final int ARGB = wolf.getRuneColor();
			final float alpha = (float)((ARGB >> 24) & 0xFF) / 255f;
			final float red = (float)((ARGB >> 16) & 0xFF) / 255f;
			final float green = (float)((ARGB >> 8) & 0xFF) / 255f;
			final float blue = (float)((ARGB >> 0) & 0xFF) / 255f;
			
			//renderCopyCutoutModel?
			// Might need to call setRotation stuff again?
			wolfRenderer.getModel().renderToBuffer(stack, buffer, packedLight, OverlayTexture.NO_OVERLAY, red * brightness, green * brightness, blue * brightness, alpha);
		}
	}
}
