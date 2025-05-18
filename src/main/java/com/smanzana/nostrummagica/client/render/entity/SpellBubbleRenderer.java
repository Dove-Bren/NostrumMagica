package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.SpellProjectileEntity;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SpellBubbleRenderer extends EntityRenderer<SpellProjectileEntity> {
	
	private static final ResourceLocation LOC_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/entity/spell_bubble.png");
	
	private final float scale;

	public SpellBubbleRenderer(EntityRendererProvider.Context renderManager, float scale) {
		super(renderManager);
		this.scale = scale;
	}

	@Override
	public ResourceLocation getTextureLocation(SpellProjectileEntity entity) {
		return LOC_TEXT;
	}
	
	@Override
	public void render(SpellProjectileEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		final float[] color = ColorUtil.ARGBToColor(entityIn.getElement().getColor());
		final float modelScale = .25f;
		
		// Copied from DragonFireballRenderer.
		// Just render a billboard
		matrixStackIn.pushPose();
		matrixStackIn.scale(this.scale * modelScale, this.scale * modelScale, this.scale * modelScale);
		matrixStackIn.mulPose(this.entityRenderDispatcher.cameraOrientation());
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F));
		Matrix4f transform = matrixStackIn.last().pose();
		Matrix3f normal = matrixStackIn.last().normal();
		VertexConsumer buffer = bufferIn.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entityIn), true));
		buffer.vertex(transform, -0.5f, -0.25f, 0.0f).color(color[0], color[1], color[2], color[3]).uv(0, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		buffer.vertex(transform, 0.5f, -0.25f, 0.0f).color(color[0], color[1], color[2], color[3]).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		buffer.vertex(transform, 0.5f, 0.75f, 0.0f).color(color[0], color[1], color[2], color[3]).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		buffer.vertex(transform, -0.5f, 0.75f, 0.0f).color(color[0], color[1], color[2], color[3]).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
    }
	
}
