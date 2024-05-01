package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySpellMortar;
import com.smanzana.nostrummagica.utils.ColorUtil;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class RenderSpellMortar extends EntityRenderer<EntitySpellMortar> {
	
	private static final ResourceLocation LOC_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/effects/glow_orb.png");
	
	private final float scale;

	public RenderSpellMortar(EntityRendererManager renderManager, float scale) {
		super(renderManager);
		this.scale = scale;
	}

	@Override
	public ResourceLocation getEntityTexture(EntitySpellMortar entity) {
		return LOC_TEXT;
	}
	
	@Override
	public void render(EntitySpellMortar entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		final float[] color = ColorUtil.ARGBToColor(entityIn.getElement().getColor());
		
		// Copied from DragonFireballRenderer.
		// Just render a billboard
		matrixStackIn.push();
		matrixStackIn.scale(1.5f * this.scale, 1.5f * this.scale, 1.5f * this.scale);
		matrixStackIn.rotate(this.renderManager.getCameraOrientation());
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
		Matrix4f transform = matrixStackIn.getLast().getMatrix();
		Matrix3f normal = matrixStackIn.getLast().getNormal();
		IVertexBuilder buffer = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(getEntityTexture(entityIn)));
		buffer.pos(transform, -0.5f, -0.25f, 0.0f).color(color[0], color[1], color[2], color[3]).tex(0, 1f).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		buffer.pos(transform, 0.5f, -0.25f, 0.0f).color(color[0], color[1], color[2], color[3]).tex(1f, 1f).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		buffer.pos(transform, 0.5f, 0.75f, 0.0f).color(color[0], color[1], color[2], color[3]).tex(1f, 0f).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		buffer.pos(transform, -0.5f, 0.75f, 0.0f).color(color[0], color[1], color[2], color[3]).tex(0f, 0f).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		matrixStackIn.pop();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }
	
}
