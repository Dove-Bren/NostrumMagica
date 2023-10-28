package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss.PlantBossBody;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class RenderPlantBossBody extends EntityRenderer<EntityPlantBoss.PlantBossBody> {

	public RenderPlantBossBody(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}
	
	@Override
	public void doRender(EntityPlantBoss.PlantBossBody entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// Don't actually render anything
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected ResourceLocation getEntityTexture(PlantBossBody entity) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
