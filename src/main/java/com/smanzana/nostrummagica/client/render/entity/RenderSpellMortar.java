package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySpellMortar;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

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
	
	public void doRender(EntitySpellMortar entity, double x, double y, double z, float entityYaw, float partialTicks) {
		
		final int color = entity.getElement().getColor();
		final float brightness = 1f;
		
		GlStateManager.pushMatrix();
		this.bindEntityTexture(entity);
		GlStateManager.translatef((float)x, (float)y, (float)z);
		GlStateManager.enableRescaleNormal();
		GlStateManager.scalef(1.5f * this.scale, 1.5f * this.scale, 1.5f * this.scale);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.rotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

		if (this.renderOutlines) {
			GlStateManager.enableColorMaterial();
			//GlStateManager.enableOutlineMode(this.getTeamColor(entity));
			GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
		}
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(516, 0);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlphaTest();
		GlStateManager.disableTexture();
		GlStateManager.enableTexture();
		GlStateManager.depthMask(false);
		GlStateManager.color4f(
				brightness * (float)((color >> 16) & 0xFF) / 255f,
				brightness * (float)((color >> 8) & 0xFF) / 255f,
				brightness * (float)((color >> 0) & 0xFF) / 255f,
				1f
				);

		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
		bufferbuilder.pos(-0.5D, -0.25D, 0.0D).tex(0, 1f).normal(0.0F, 1.0F, 0.0F).endVertex();
		bufferbuilder.pos(0.5D, -0.25D, 0.0D).tex(1f, 1f).normal(0.0F, 1.0F, 0.0F).endVertex();
		bufferbuilder.pos(0.5D, 0.75D, 0.0D).tex(1f, 0f).normal(0.0F, 1.0F, 0.0F).endVertex();
		bufferbuilder.pos(-0.5D, 0.75D, 0.0D).tex(0f, 0f).normal(0.0F, 1.0F, 0.0F).endVertex();
		tessellator.draw();

		if (this.renderOutlines) {
			//GlStateManager.disableOutlineMode();
			GlStateManager.tearDownSolidRenderingTextureCombine();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
	
}
