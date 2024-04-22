package com.smanzana.nostrummagica.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.entity.ModelArcaneWolf;
import com.smanzana.nostrummagica.client.render.entity.RenderArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class LayerArcaneWolfRunes extends LayerRenderer<EntityArcaneWolf, ModelArcaneWolf> {

	private static final ResourceLocation RUNE_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/entity/arcane_wolf/overlay.png");
	private final RenderArcaneWolf wolfRenderer;
	
	public LayerArcaneWolfRunes(RenderArcaneWolf wolfRenderer) {
		super(wolfRenderer);
		this.wolfRenderer = wolfRenderer;
	}
	
	@Override
	public void render(MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, EntityArcaneWolf wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!wolf.isInvisible()) {
			
			final IVertexBuilder buffer = typeBuffer.getBuffer(RenderType.getEntityTranslucentCull(RUNE_LOC));
			final float brightPeriod = 40f;
			final float brightness = .8f + .2f * (float) Math.sin(Math.PI * 2 * ((partialTicks + wolf.ticksExisted) % brightPeriod) / brightPeriod);
			final int ARGB = wolf.getRuneColor();
			final float alpha = (float)((ARGB >> 24) & 0xFF) / 255f;
			final float red = (float)((ARGB >> 16) & 0xFF) / 255f;
			final float green = (float)((ARGB >> 8) & 0xFF) / 255f;
			final float blue = (float)((ARGB >> 0) & 0xFF) / 255f;
			
			//renderCopyCutoutModel?
			// Might need to call setRotation stuff again?
			wolfRenderer.getEntityModel().render(stack, buffer, packedLight, OverlayTexture.NO_OVERLAY, red * brightness, green * brightness, blue * brightness, alpha);
		}
	}
}
