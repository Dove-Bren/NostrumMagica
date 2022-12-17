package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySpellMortar;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RenderSpellMortar extends Render<EntitySpellMortar> {
	
	private static final ResourceLocation LOC_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/effects/glow_orb.png");
	
	private final float scale;

	public RenderSpellMortar(RenderManager renderManager, float scale) {
		super(renderManager);
		this.scale = scale;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySpellMortar entity) {
		return LOC_TEXT;
	}
	
	public void doRender(EntitySpellMortar entity, double x, double y, double z, float entityYaw, float partialTicks) {
		
		final int color = entity.getElement().getColor();
		final float brightness = 1f;
		
		GlStateManager.pushMatrix();
		this.bindEntityTexture(entity);
		GlStateManager.translate((float)x, (float)y, (float)z);
		GlStateManager.enableRescaleNormal();
		GlStateManager.scale(1.5 * this.scale, 1.5 * this.scale, 1.5 * this.scale);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

		if (this.renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.enableOutlineMode(this.getTeamColor(entity));
		}
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(516, 0);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.disableTexture2D();
		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.color(
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
			GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.color(1f, 1f, 1f, 1f);
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
	
}