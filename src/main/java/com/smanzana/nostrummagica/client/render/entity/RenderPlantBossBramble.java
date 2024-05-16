package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelPlantBossBramble;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBossBramble;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class RenderPlantBossBramble extends EntityRenderer<EntityPlantBossBramble> {

	private static final ResourceLocation PLANT_BOSS_TEXTURE_BASE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/plant_boss_body.png");
	
	protected ModelPlantBossBramble mainModel;
	
	public RenderPlantBossBramble(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		
		mainModel = new ModelPlantBossBramble();
		
		// TODO shadow? nah
	}
	
	@Override
	public void render(EntityPlantBossBramble entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
//		if (entity.isWolfWet()) {
//			float f = entity.getBrightness() * entity.getShadingWhileWet(partialTicks);
//			GlStateManager.color4f(f, f, f);
//		}
		
		final float scale = (entityIn.getBrambleWidth() / 5f); // 5f is width of model before being stretched
		
		//this.mainModel = new ModelPlantBossBramble();
		matrixStackIn.push();
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180f - entityIn.rotationYaw));
		matrixStackIn.scale(-1, -1, 1);
		matrixStackIn.scale(scale, 1, 1);
		matrixStackIn.translate(0, -1.5f, 0);
		matrixStackIn.scale((1f / 16f), (1f / 16f) * 1, (1f / 16f) * 1);
		mainModel.render(matrixStackIn, bufferIn.getBuffer(mainModel.getRenderType(this.getEntityTexture(entityIn))), packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		
		matrixStackIn.pop();
		
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	@Override
	public ResourceLocation getEntityTexture(EntityPlantBossBramble entity) {
		return PLANT_BOSS_TEXTURE_BASE;
	}
	
}
