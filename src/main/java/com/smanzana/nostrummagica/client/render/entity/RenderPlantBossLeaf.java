package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelPlantBossLeaf;
import com.smanzana.nostrummagica.entity.plantboss.PlantBossEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RenderPlantBossLeaf extends EntityRenderer<PlantBossEntity.PlantBossLeafLimb> {

	private static final ResourceLocation PLANT_BOSS_TEXTURE_BASE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/plant_boss_body.png");
	
	protected ModelPlantBossLeaf mainModel;
	
	public RenderPlantBossLeaf(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
		
		mainModel = new ModelPlantBossLeaf();
	}
	
	@Override
	public void render(PlantBossEntity.PlantBossLeafLimb entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		PlantBossEntity plant = entityIn.getParent();
		if (plant == null || plant.getBody() == null) {
			return;
		}
		
		final int i = entityIn.getLeafIndex();
		//EntityPlantBoss.PlantBossLeafLimb leaf = plant.getLeafLimb(i);
		final double offsetRadius = (entityIn.getPitch() >= 85f) ? 1.25 : 1;
		final double offsetCenter = (i % 2 == 0 ? offsetRadius : offsetRadius * 1.25) * plant.getBody().getBbWidth();
		final double offset = offsetCenter - (3f/2f); // Model starts at 0, not center (for better rotation)
		
		// Previously, was changing offset to be to the parent, and then doing another offset
		// TODO does this look hacky now?
		matrixStackIn.pushPose();
		
		// Standard transformation that LivingRenderer does
		matrixStackIn.scale(-1f, -1f, 1f);
		matrixStackIn.translate(0f, -1.5f, 0f);
		
		matrixStackIn.translate(0, plant.getBody().getBbHeight() / 2, 0);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180 + entityIn.getYawOffset()));
		matrixStackIn.translate(-offset + .5, -.001 * i, 0);
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(-entityIn.getPitch()));
		
		mainModel.renderToBuffer(matrixStackIn, bufferIn.getBuffer(mainModel.renderType(getTextureLocation(entityIn))), packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		
		matrixStackIn.popPose();
	}
	
	@Override
	public ResourceLocation getTextureLocation(PlantBossEntity.PlantBossLeafLimb entity) {
		return PLANT_BOSS_TEXTURE_BASE;
	}
	
}
