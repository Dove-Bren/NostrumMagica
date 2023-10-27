package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBossBramble;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class RenderPlantBossBramble extends EntityRenderer<EntityPlantBossBramble> {

	private static final ResourceLocation PLANT_BOSS_TEXTURE_BASE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/plant_boss_body.png");
	
	protected ModelPlantBossBramble mainModel;
	
	public RenderPlantBossBramble(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		
		mainModel = new ModelPlantBossBramble();
		
		// TODO shadow? nah
	}
	
	@Override
	public void doRender(EntityPlantBossBramble entity, double x, double y, double z, float entityYaw, float partialTicks) {
//		if (entity.isWolfWet()) {
//			float f = entity.getBrightness() * entity.getShadingWhileWet(partialTicks);
//			GlStateManager.color4f(f, f, f);
//		}
		
		this.mainModel = new ModelPlantBossBramble();
		
		this.bindEntityTexture(entity);
		
		final float scale = (entity.getBrambleWidth() / 5f); // 5f is width of model before being stretched
		
		GlStateManager.pushMatrix();
		
		// Copied from renderliving
		GlStateManager.translated(x, y, z);

		// yaw 0 is south, z+
		GlStateManager.rotatef(180.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
		
		GlStateManager.enableRescaleNormal();
		GlStateManager.scalef(-1.0F, -1.0F, 1.0F);

		GlStateManager.scalef(scale, 1, 1);
		GlStateManager.translatef(0, - 1.5f, 0);
		GlStateManager.scalef((1f / 16f), (1f / 16f) * 1, (1f / 16f) * 1);
		mainModel.render(entity, partialTicks, 0f, 0f, entityYaw, 0f, 1f);
		GlStateManager.popMatrix();
		
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityPlantBossBramble entity) {
		return PLANT_BOSS_TEXTURE_BASE;
	}
	
}
