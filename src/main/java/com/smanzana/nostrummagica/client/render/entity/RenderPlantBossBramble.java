package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelPlantBossBramble;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.entity.plantboss.PlantBossBrambleEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RenderPlantBossBramble extends EntityRenderer<PlantBossBrambleEntity> {

	private static final ResourceLocation PLANT_BOSS_TEXTURE_BASE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/plant_boss_body.png");
	
	protected ModelPlantBossBramble mainModel;
	
	public RenderPlantBossBramble(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
		
		mainModel = new ModelPlantBossBramble(renderManagerIn.bakeLayer(NostrumModelLayers.PlantbossBramble));
		
		// TODO shadow? nah
	}
	
	@Override
	public void render(PlantBossBrambleEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
//		if (entity.isWolfWet()) {
//			float f = entity.getBrightness() * entity.getShadingWhileWet(partialTicks);
//			GlStateManager.color4f(f, f, f);
//		}
		
		final float scale = (entityIn.getBrambleWidth() / 5f); // 5f is width of model before being stretched
		
		//this.mainModel = new ModelPlantBossBramble();
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180f - entityIn.getYRot()));
		matrixStackIn.scale(-1, -1, 1);
		matrixStackIn.scale(scale, 1, 1);
		matrixStackIn.translate(0, -1.5f, 0);
		matrixStackIn.scale((1f / 16f), (1f / 16f) * 1, (1f / 16f) * 1);
		mainModel.renderToBuffer(matrixStackIn, bufferIn.getBuffer(mainModel.renderType(this.getTextureLocation(entityIn))), packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		
		matrixStackIn.popPose();
		
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	@Override
	public ResourceLocation getTextureLocation(PlantBossBrambleEntity entity) {
		return PLANT_BOSS_TEXTURE_BASE;
	}
	
}
