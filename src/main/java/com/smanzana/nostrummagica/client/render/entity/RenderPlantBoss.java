package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelPlantBoss;
import com.smanzana.nostrummagica.client.model.ModelRenderShiv;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss.PlantBossTreeType;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;

public class RenderPlantBoss extends MobRenderer<EntityPlantBoss, ModelRenderShiv<EntityPlantBoss>> {

	private static final ResourceLocation PLANT_BOSS_TEXTURE_BASE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/plant_boss_body.png");
	
	private ModelPlantBoss mainModel;
	
	public RenderPlantBoss(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelRenderShiv<>(), shadowSizeIn);
		this.mainModel = new ModelPlantBoss();
	}
	
	/**
	 * Defines what float the third param in setRotationAngles of EntityModel is
	 * @param livingBase
	 * @param partialTicks
	 * @return
	 */
	@Override
	protected float handleRotationFloat(EntityPlantBoss livingBase, float partialTicks) {
		return super.handleRotationFloat(livingBase, partialTicks);
	}
	
	@Override
	public void render(EntityPlantBoss entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		this.entityModel.setPayload((deferredStack, deferredBufferIn, deferredPackedLightIn, packedOverlayIn, red, green, blue, alpha) -> {
			// Could pass through bufferIn to allow access to different buffer types, but only need the base one
			this.renderModel(entityIn, deferredStack, deferredBufferIn, deferredPackedLightIn, packedOverlayIn, red, green, blue, alpha);
		});
		//
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	protected void renderTrimming(EntityPlantBoss plant, MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn,
			float red, float green, float blue, float alpha) {
		if (plant.getBody() == null) {
			return;
		}
		
		// 4 faces
		final float uBase = 0;
		final float vBase = (96f / 256f);
		final float uLen = (48f) / 256f;
		final float vLen = (16f) / 256f;
		
		// Scaled back to this from unit cube
//		final double hMin = -1.5;
//		final double hMax = 1.5;
//		final double yMin = -1;
//		final double yMax = 0;
		
		float umin;
		float umax;
		float vmin = vBase;
		float vmax = vBase + vLen;
		
		// Copied from RenderFuncs' unit box
		final float mind = -1.5f;
		final float maxd = 1.5f;
		
		final float minn = -.5773f;
		final float maxn = .5773f;
		
		matrixStackIn.push();
		matrixStackIn.translate(0, -plant.getBody().getHeight()/2, 0);
		
		// Adapt to 3 wide (from -1.5 to 1.5) and 1 tall (from -1 to 0)
		matrixStackIn.scale(.99f, (float) (1.0/3.0), .99f);
		matrixStackIn.translate(0, -.5, 0);
		
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		final Matrix3f normal = matrixStackIn.getLast().getNormal();
		
		// North
		umin = uBase;
		umax = umin + uLen;
		buffer.pos(transform, maxd, maxd, mind).color(red, green, blue, alpha).tex(umin,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		buffer.pos(transform, maxd, mind, mind).color(red, green, blue, alpha).tex(umin,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		buffer.pos(transform, mind, mind, mind).color(red, green, blue, alpha).tex(umax,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, minn, minn, minn).endVertex();
		buffer.pos(transform, mind, maxd, mind).color(red, green, blue, alpha).tex(umax,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		
		// East
		umin += uLen;
		umax += uLen;
		buffer.pos(transform, maxd, maxd, maxd).color(red, green, blue, alpha).tex(umin,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		buffer.pos(transform, maxd, mind, maxd).color(red, green, blue, alpha).tex(umin,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		buffer.pos(transform, maxd, mind, mind).color(red, green, blue, alpha).tex(umax,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		buffer.pos(transform, maxd, maxd, mind).color(red, green, blue, alpha).tex(umax,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		
		// South
		umin += uLen;
		umax += uLen;
		buffer.pos(transform, mind, maxd, maxd).color(red, green, blue, alpha).tex(umin,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		buffer.pos(transform, mind, mind, maxd).color(red, green, blue, alpha).tex(umin,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
		buffer.pos(transform, maxd, mind, maxd).color(red, green, blue, alpha).tex(umax,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		buffer.pos(transform, maxd, maxd, maxd).color(red, green, blue, alpha).tex(umax,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		
		// West
		umin += uLen;
		umax += uLen;
		buffer.pos(transform, mind, maxd, mind).color(red, green, blue, alpha).tex(umin,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		buffer.pos(transform, mind, mind, mind).color(red, green, blue, alpha).tex(umin,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, minn, minn, minn).endVertex();
		buffer.pos(transform, mind, mind, maxd).color(red, green, blue, alpha).tex(umax,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
		buffer.pos(transform, mind, maxd, maxd).color(red, green, blue, alpha).tex(umax,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		
//		// North
//		buffer.pos(hMin, yMax, hMin).tex(umin,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(0, 0, -1).endVertex();
//		buffer.pos(hMin, yMin, hMin).tex(umin,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(0, 0, -1).endVertex();
//		buffer.pos(hMax, yMin, hMin).tex(umax,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(0, 0, -1).endVertex();
//		buffer.pos(hMax, yMax, hMin).tex(umax,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(0, 0, -1).endVertex();
//		
//		// East
//		buffer.pos(hMax, yMax, hMin).tex(umin,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(1, 0, 0).endVertex();
//		buffer.pos(hMax, yMin, hMin).tex(umin,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(1, 0, 0).endVertex();
//		buffer.pos(hMax, yMin, hMax).tex(umax,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(1, 0, 0).endVertex();
//		buffer.pos(hMax, yMax, hMax).tex(umax,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(1, 0, 0).endVertex();
//		
//		// South
//		buffer.pos(hMax, yMax, hMax).tex(umin,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(0, 0, 1).endVertex();
//		buffer.pos(hMax, yMin, hMax).tex(umin,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(0, 0, 1).endVertex();
//		buffer.pos(hMin, yMin, hMax).tex(umax,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(0, 0, 1).endVertex();
//		buffer.pos(hMin, yMax, hMax).tex(umax,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(0, 0, 1).endVertex();
//		
//		// West
//		buffer.pos(hMin, yMax, hMax).tex(umin,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(-1, 0, 0).endVertex();
//		buffer.pos(hMin, yMin, hMax).tex(umin,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(-1, 0, 0).endVertex();
//		buffer.pos(hMin, yMin, hMin).tex(umax,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(-1, 0, 0).endVertex();
//		buffer.pos(hMin, yMax, hMin).tex(umax,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(-1, 0, 0).endVertex();
			
		matrixStackIn.pop();
	}
	
	protected void renderHeadTree(EntityPlantBoss plant, MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn,
			float red, float green, float blue, float alpha) {
		if (plant.getBody() == null) {
			return;
		}
		
		final PlantBossTreeType treeType = plant.getTreeType();
		final float uBase = (192f / 256f);
		final float vBase = 0;
		final float uLen = (16f) / 256f;
		final float vLen = (48f) / 256f;
		
		final float uOrbBase = uBase + uLen * 3;
		
		final float hMin = -.5f;
		final float hMax = .5f;
		final float yMin = -2f;
		final float yMax = 0f;
		
		float umin = 0;
		float umax;
		float vmin = vBase;
		float vmax = vBase + vLen;
		
		switch (treeType) {
		case NORMAL:
			umin = uBase;
			break;
		case COVERED:
			umin = uBase + uLen;
			break;
		case ELEMENTAL:
			umin = uBase + uLen + uLen;
			break;
		
		}
		umax = umin + uLen;
		
		matrixStackIn.push();
		matrixStackIn.translate(0, -plant.getBody().getHeight()/2, 0);
		
		// Two panes centered but orthog like regular mc plants.
		// For each pane, do base tree, and maybe elemental orb
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		final Matrix3f normal = matrixStackIn.getLast().getNormal();
		// NOTE: nromals are wrong
		
		// North
		buffer.pos(transform, hMin, yMax, 0).color(red, green, blue, alpha).tex(umin,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0, 0, -1).endVertex();
		buffer.pos(transform, hMin, yMin, 0).color(red, green, blue, alpha).tex(umin,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0, 0, -1).endVertex();
		buffer.pos(transform, hMax, yMin, 0).color(red, green, blue, alpha).tex(umax,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0, 0, -1).endVertex();
		buffer.pos(transform, hMax, yMax, 0).color(red, green, blue, alpha).tex(umax,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0, 0, -1).endVertex();
		
		// East
		buffer.pos(transform, 0, yMax, hMin).color(red, green, blue, alpha).tex(umin,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 1, 0, 0).endVertex();
		buffer.pos(transform, 0, yMin, hMin).color(red, green, blue, alpha).tex(umin,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 1, 0, 0).endVertex();
		buffer.pos(transform, 0, yMin, hMax).color(red, green, blue, alpha).tex(umax,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 1, 0, 0).endVertex();
		buffer.pos(transform, 0, yMax, hMax).color(red, green, blue, alpha).tex(umax,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 1, 0, 0).endVertex();

		
		if (treeType == PlantBossTreeType.ELEMENTAL) {
			final EMagicElement element = plant.getTreeElement();
			final float color[] = ColorUtil.ARGBToColor(element.getColor());
			
			// North
			umin = uOrbBase;
			umax = umin + uLen;
			buffer.pos(transform, hMin, yMax, 0).color(color[0], color[1], color[2], color[3]).tex(umin,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0, 0, -1).endVertex();
			buffer.pos(transform, hMin, yMin, 0).color(color[0], color[1], color[2], color[3]).tex(umin,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0, 0, -1).endVertex();
			buffer.pos(transform, hMax, yMin, 0).color(color[0], color[1], color[2], color[3]).tex(umax,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0, 0, -1).endVertex();
			buffer.pos(transform, hMax, yMax, 0).color(color[0], color[1], color[2], color[3]).tex(umax,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0, 0, -1).endVertex();
			
			// East
			umin = uOrbBase;
			umax = umin + uLen;
			buffer.pos(transform, 0, yMax, hMin).color(color[0], color[1], color[2], color[3]).tex(umin,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 1, 0, 0).endVertex();
			buffer.pos(transform, 0, yMin, hMin).color(color[0], color[1], color[2], color[3]).tex(umin,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 1, 0, 0).endVertex();
			buffer.pos(transform, 0, yMin, hMax).color(color[0], color[1], color[2], color[3]).tex(umax,vmin).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 1, 0, 0).endVertex();
			buffer.pos(transform, 0, yMax, hMax).color(color[0], color[1], color[2], color[3]).tex(umax,vmax).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 1, 0, 0).endVertex();
		}
		
		matrixStackIn.pop();
	}
	
	protected void renderModel(EntityPlantBoss entityIn, MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn,
			int packedOverlayIn, float red, float green, float blue, float alpha) {
		
		this.mainModel.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		if (entityIn.getBody() == null) {
			return;
		}
		
		// Render top of main body as 4 double-sided quads
		renderTrimming(entityIn, matrixStackIn, bufferIn, packedLightIn, red, green, blue, alpha);
		
		// Render tree at top
		renderHeadTree(entityIn, matrixStackIn, bufferIn, packedLightIn, red, green, blue, alpha);
		
		// Also render leaf models. Do it here so I don't have to repeat all the rotations and scaling
//		GlStateManager.pushMatrix();
//		final float existingRotation = RenderFuncs.interpolateRotation(plant.prevRenderYawOffset, plant.renderYawOffset, ageInTicks % 1);
//		GlStateManager.rotatef((180.0F - existingRotation), 0, 1, 0); // undo existing rotation
//		GlStateManager.translatef(0, plant.getBody().getHeight()/2, 0);
//		for (int i = 0; i < leafModels.length; i++) {
//			PlantBossLeafLimb leaf = plant.getLeafLimb(i);
//			final double offsetCenter = (i % 2 == 0 ? 1.25 : 1.5) * plant.getBody().getWidth();
//			final double offset = offsetCenter - (3f/2f); // Model starts at 0, not center (for better rotation)
//			
//			GlStateManager.pushMatrix();
//			GlStateManager.rotatef(180 + leaf.getYawOffset(), 0, 1, 0);
//			GlStateManager.translated(offset, -.001 * i, 0);
//			GlStateManager.rotatef(-leaf.getPitch(), 0, 0, 1);
//			leafModels[i].render(leaf, 0f, 0f, ageInTicks, 0f, 0f, scaleFactor);
//			GlStateManager.popMatrix();
//		}
//		GlStateManager.popMatrix();
	}
	
	@Override
	public ResourceLocation getEntityTexture(EntityPlantBoss entity) {
		return PLANT_BOSS_TEXTURE_BASE;
	}
	
}
