package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class RenderPlantBossLeaf extends EntityRenderer<EntityPlantBoss.PlantBossLeafLimb> {

	private static final ResourceLocation PLANT_BOSS_TEXTURE_BASE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/plant_boss_body.png");
	
	protected ModelPlantBossLeaf mainModel;
	
	public RenderPlantBossLeaf(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		
		mainModel = new ModelPlantBossLeaf();
		
		// TODO shadow? nah
	}
	
	@Override
	public void doRender(EntityPlantBoss.PlantBossLeafLimb entity, double x, double y, double z, float entityYaw, float partialTicks) {
		float f = 0.0625F;
//		if (entity.isWolfWet()) {
//			float f = entity.getBrightness() * entity.getShadingWhileWet(partialTicks);
//			GlStateManager.color4f(f, f, f);
//		}
		
		//this.mainModel = new ModelPlantBoss();
//		GlStateManager.pushMatrix();
//		GlStateManager.translated(x, y, z);
//		
//		GlStateManager.rotatef(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
//		
//		GlStateManager.enableRescaleNormal();
//		GlStateManager.scalef(-1.0F, -1.0F, 1.0F);
//		GlStateManager.scalef(.125f, .125f, .125f);
//		//this.preRenderCallback(entitylivingbaseIn, partialTicks);
//		GlStateManager.translatef(0.0F, -1.501F, 0.0F);
//	    //return 0.0625F;
//		this.bindEntityTexture(entity);
//		mainModel.render(entity, partialTicks, 0f, 0f, entityYaw, 0f, f);
//		GlStateManager.popMatrix();
		
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		EntityPlantBoss plant = entity.getParent();
		if (plant == null || plant.getBody() == null) {
			return;
		}
		
		this.bindEntityTexture(entity);
		GlStateManager.pushMatrix();
		
		// Offset to body center
		Vec3d cameraOffsetToParent = plant.getPositionVec().subtract(
				TileEntityRendererDispatcher.staticPlayerX,
				TileEntityRendererDispatcher.staticPlayerY,
				TileEntityRendererDispatcher.staticPlayerZ
				);
		GlStateManager.translated(cameraOffsetToParent.x, cameraOffsetToParent.y, cameraOffsetToParent.z);
		
		// Standard transformations that LivingRenderer does
		GlStateManager.scalef(-1.0F, -1.0F, 1.0F);
		GlStateManager.translatef(0.0F, -1.501F, 0.0F);
		
		//final float existingRotation = RenderFuncs.interpolateRotation(plant.prevRenderYawOffset, plant.renderYawOffset, entity.ticksExisted % 1);
		//GlStateManager.rotatef((180.0F + existingRotation), 0, 1, 0); // undo existing rotation
		GlStateManager.translatef(0, plant.getBody().getHeight()/2, 0);
		//for (int i = 0; i < leafModels.length; i++) {
		final int i = entity.getLeafIndex();
			//EntityPlantBoss.PlantBossLeafLimb leaf = plant.getLeafLimb(i);
			final double offsetRadius = (entity.getPitch() >= 85f) ? 1.25 : 1;
			final double offsetCenter = (i % 2 == 0 ? offsetRadius : offsetRadius * 1.25) * plant.getBody().getWidth();
			final double offset = offsetCenter - (3f/2f); // Model starts at 0, not center (for better rotation)
			
			GlStateManager.pushMatrix();
			GlStateManager.rotatef(180 + entity.getYawOffset(), 0, 1, 0);
			GlStateManager.translated(offset, -.001 * i, 0);
			GlStateManager.rotatef(-entity.getPitch(), 0, 0, 1);
			//GlStateManager.scalef(1f/16f, 1f/16f, 1f/16f);
			mainModel.render(entity, 0f, 0f, entity.ticksExisted, 0f, 0f, f);
			GlStateManager.popMatrix();
		//}
		GlStateManager.popMatrix();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityPlantBoss.PlantBossLeafLimb entity) {
		return PLANT_BOSS_TEXTURE_BASE;
	}
	
}
