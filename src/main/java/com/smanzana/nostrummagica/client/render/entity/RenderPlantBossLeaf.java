package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderPlantBossLeaf extends Render<EntityPlantBoss.PlantBossLeafLimb> {

	private static final ResourceLocation PLANT_BOSS_TEXTURE_BASE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/plant_boss_body.png");
	
	protected ModelPlantBossLeaf mainModel;
	
	public RenderPlantBossLeaf(RenderManager renderManagerIn) {
		super(renderManagerIn);
		
		mainModel = new ModelPlantBossLeaf();
		
		// TODO shadow? nah
	}
	
	@Override
	public void doRender(EntityPlantBoss.PlantBossLeafLimb entity, double x, double y, double z, float entityYaw, float partialTicks) {
//		if (entity.isWolfWet()) {
//			float f = entity.getBrightness() * entity.getShadingWhileWet(partialTicks);
//			GlStateManager.color(f, f, f);
//		}
		
		//this.mainModel = new ModelPlantBoss();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		mainModel.render(entity, partialTicks, 0f, 0f, entityYaw, 0f, 1f);
		GlStateManager.popMatrix();
		
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityPlantBoss.PlantBossLeafLimb entity) {
		return PLANT_BOSS_TEXTURE_BASE;
	}
	
}
