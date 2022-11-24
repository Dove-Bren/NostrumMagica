package com.smanzana.nostrummagica.client.render.entity;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss.PlantBossLeafLimb;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss.PlantBossTreeType;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RenderPlantBoss extends RenderLiving<EntityPlantBoss> {

	private static final ResourceLocation PLANT_BOSS_TEXTURE_BASE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/plant_boss_body.png");
	
	protected ModelPlantBossLeaf leafModels[];
	
	public RenderPlantBoss(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelPlantBoss(), shadowSizeIn);
		
		leafModels = new ModelPlantBossLeaf[EntityPlantBoss.NumberOfLeaves];
		for (int i = 0; i < leafModels.length; i++) {
			leafModels[i] = new ModelPlantBossLeaf();
		}
	}
	
	/**
	 * Defines what float the third param in setRotationAngles of ModelBase is
	 * @param livingBase
	 * @param partialTicks
	 * @return
	 */
	@Override
	protected float handleRotationFloat(EntityPlantBoss livingBase, float partialTicks) {
		return super.handleRotationFloat(livingBase, partialTicks);
		//return livingBase.getTailRotation();
	}
	
	@Override
	public void doRender(EntityPlantBoss plant, double x, double y, double z, float entityYaw, float partialTicks) {
//		if (entity.isWolfWet()) {
//			float f = entity.getBrightness() * entity.getShadingWhileWet(partialTicks);
//			GlStateManager.color(f, f, f);
//		}
		
		//this.mainModel = new ModelPlantBoss();
		
//		for (int i = 0; i < leafModels.length; i++) {
//			leafModels[i] = new ModelPlantBossLeaf();
//		}
		
		super.doRender(plant, x, y, z, entityYaw, partialTicks);
	}
	
	protected void renderTrimming(EntityPlantBoss plant, float partialTicks) {
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, -plant.height/2, 0);
		GlStateManager.color(1f, 1f, 1f, 1f);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		
		// 4 faces
		final double uBase = 0;
		final double vBase = (96f / 256f);
		final double uLen = (48f) / 256f;
		final double vLen = (16f) / 256f;
		
		final double hMin = -1.5;
		final double hMax = 1.5;
		final double yMin = -1;
		final double yMax = 0;
		
		double umin;
		double umax;
		double vmin = vBase;
		double vmax = vBase + vLen;
		
		// North
		umin = uBase;
		umax = umin + uLen;
		buffer.pos(hMin, yMax, hMin).tex(umin,vmax).normal(0, 0, -1).endVertex();
		buffer.pos(hMin, yMin, hMin).tex(umin,vmin).normal(0, 0, -1).endVertex();
		buffer.pos(hMax, yMin, hMin).tex(umax,vmin).normal(0, 0, -1).endVertex();
		buffer.pos(hMax, yMax, hMin).tex(umax,vmax).normal(0, 0, -1).endVertex();
		
		// East
		umin += uLen;
		umax += uLen;
		buffer.pos(hMax, yMax, hMin).tex(umin,vmax).normal(1, 0, 0).endVertex();
		buffer.pos(hMax, yMin, hMin).tex(umin,vmin).normal(1, 0, 0).endVertex();
		buffer.pos(hMax, yMin, hMax).tex(umax,vmin).normal(1, 0, 0).endVertex();
		buffer.pos(hMax, yMax, hMax).tex(umax,vmax).normal(1, 0, 0).endVertex();
		
		// South
		umin += uLen;
		umax += uLen;
		buffer.pos(hMax, yMax, hMax).tex(umin,vmax).normal(0, 0, 1).endVertex();
		buffer.pos(hMax, yMin, hMax).tex(umin,vmin).normal(0, 0, 1).endVertex();
		buffer.pos(hMin, yMin, hMax).tex(umax,vmin).normal(0, 0, 1).endVertex();
		buffer.pos(hMin, yMax, hMax).tex(umax,vmax).normal(0, 0, 1).endVertex();
		
		// West
		umin += uLen;
		umax += uLen;
		buffer.pos(hMin, yMax, hMax).tex(umin,vmax).normal(-1, 0, 0).endVertex();
		buffer.pos(hMin, yMin, hMax).tex(umin,vmin).normal(-1, 0, 0).endVertex();
		buffer.pos(hMin, yMin, hMin).tex(umax,vmin).normal(-1, 0, 0).endVertex();
		buffer.pos(hMin, yMax, hMin).tex(umax,vmax).normal(-1, 0, 0).endVertex();
			
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}
	
	protected void renderHeadTree(EntityPlantBoss plant, float ageInTicks) {
		
		final PlantBossTreeType treeType = plant.getTreeType();
		
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, -plant.height/2, 0);
		GlStateManager.color(1f, 1f, 1f, 1f);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		
		final double uBase = (192f / 256f);
		final double vBase = 0;
		final double uLen = (16f) / 256f;
		final double vLen = (48f) / 256f;
		
		final double uOrbBase = uBase + uLen * 3;
		
		final double hMin = -.5;
		final double hMax = .5;
		final double yMin = -2;
		final double yMax = 0;
		
		double umin = 0;
		double umax;
		double vmin = vBase;
		double vmax = vBase + vLen;
		
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
		
		// Two panes centered but orthog like regular mc plants.
		// For each pane, do base tree, and maybe elemental orb
		
		// North
		buffer.pos(hMin, yMax, 0).tex(umin,vmax).normal(0, 0, -1).endVertex();
		buffer.pos(hMin, yMin, 0).tex(umin,vmin).normal(0, 0, -1).endVertex();
		buffer.pos(hMax, yMin, 0).tex(umax,vmin).normal(0, 0, -1).endVertex();
		buffer.pos(hMax, yMax, 0).tex(umax,vmax).normal(0, 0, -1).endVertex();
		
		// East
		buffer.pos(0, yMax, hMin).tex(umin,vmax).normal(1, 0, 0).endVertex();
		buffer.pos(0, yMin, hMin).tex(umin,vmin).normal(1, 0, 0).endVertex();
		buffer.pos(0, yMin, hMax).tex(umax,vmin).normal(1, 0, 0).endVertex();
		buffer.pos(0, yMax, hMax).tex(umax,vmax).normal(1, 0, 0).endVertex();

		Tessellator.getInstance().draw();
		
		if (treeType == PlantBossTreeType.ELEMENTAL) {
			final EMagicElement element = plant.getTreeElement();
			final int color = element.getColor();
			final float brightness = 1f;
			GlStateManager.color(
					brightness * (float)((color >> 16) & 0xFF) / 255f,
					brightness * (float)((color >> 8) & 0xFF) / 255f,
					brightness * (float)((color >> 0) & 0xFF) / 255f,
					1f
					);
			
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
			
			// North
			umin = uOrbBase;
			umax = umin + uLen;
			buffer.pos(hMin, yMax, 0).tex(umin,vmax).normal(0, 0, -1).endVertex();
			buffer.pos(hMin, yMin, 0).tex(umin,vmin).normal(0, 0, -1).endVertex();
			buffer.pos(hMax, yMin, 0).tex(umax,vmin).normal(0, 0, -1).endVertex();
			buffer.pos(hMax, yMax, 0).tex(umax,vmax).normal(0, 0, -1).endVertex();
			
			// East
			umin = uOrbBase;
			umax = umin + uLen;
			buffer.pos(0, yMax, hMin).tex(umin,vmax).normal(1, 0, 0).endVertex();
			buffer.pos(0, yMin, hMin).tex(umin,vmin).normal(1, 0, 0).endVertex();
			buffer.pos(0, yMin, hMax).tex(umax,vmin).normal(1, 0, 0).endVertex();
			buffer.pos(0, yMax, hMax).tex(umax,vmax).normal(1, 0, 0).endVertex();

			Tessellator.getInstance().draw();
			GlStateManager.color(1f, 1f, 1f, 1f);
		}
		
		GlStateManager.popMatrix();
	}
	
	@Override
	protected void renderModel(EntityPlantBoss plant, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		super.renderModel(plant, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
		
		// Render top of main body as 4 double-sided quads
		renderTrimming(plant, ageInTicks);
		
		// Render tree at top
		renderHeadTree(plant, ageInTicks);
		
		// Also render leaf models. Do it here so I don't have to repeat all the rotations and scaling
		GlStateManager.pushMatrix();
		final float existingRotation = this.interpolateRotation(plant.prevRenderYawOffset, plant.renderYawOffset, ageInTicks % 1);
		GlStateManager.rotate((180.0F - existingRotation), 0, 1, 0); // undo existing rotation
		GlStateManager.translate(0, plant.height/2, 0);
		for (int i = 0; i < leafModels.length; i++) {
			PlantBossLeafLimb leaf = plant.getLeafLimb(i);
			final double offsetCenter = (i % 2 == 0 ? 1.25 : 1.5) * plant.width;
			final double offset = offsetCenter - (leaf.width/2); // Model starts at 0, not center (for better rotation)
			
			GlStateManager.pushMatrix();
			GlStateManager.rotate(leaf.getYawOffset(), 0, 1, 0);
			GlStateManager.translate(offset, -.001 * i, 0);
			GlStateManager.rotate(-leaf.getPitch(), 0, 0, 1);
			leafModels[i].render(leaf, 0f, 0f, ageInTicks, 0f, 0f, scaleFactor);
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityPlantBoss entity) {
		return PLANT_BOSS_TEXTURE_BASE;
	}
	
}
