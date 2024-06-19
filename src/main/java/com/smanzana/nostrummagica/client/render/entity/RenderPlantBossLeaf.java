package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelPlantBossLeaf;
import com.smanzana.nostrummagica.entity.plantboss.PlantBossEntity;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class RenderPlantBossLeaf extends EntityRenderer<PlantBossEntity.PlantBossLeafLimb> {

	private static final ResourceLocation PLANT_BOSS_TEXTURE_BASE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/plant_boss_body.png");
	
	protected ModelPlantBossLeaf mainModel;
	
	public RenderPlantBossLeaf(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		
		mainModel = new ModelPlantBossLeaf();
	}
	
	@Override
	public void render(PlantBossEntity.PlantBossLeafLimb entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		PlantBossEntity plant = entityIn.getParent();
		if (plant == null || plant.getBody() == null) {
			return;
		}
		
		final int i = entityIn.getLeafIndex();
		//EntityPlantBoss.PlantBossLeafLimb leaf = plant.getLeafLimb(i);
		final double offsetRadius = (entityIn.getPitch() >= 85f) ? 1.25 : 1;
		final double offsetCenter = (i % 2 == 0 ? offsetRadius : offsetRadius * 1.25) * plant.getBody().getWidth();
		final double offset = offsetCenter - (3f/2f); // Model starts at 0, not center (for better rotation)
		
		// Previously, was changing offset to be to the parent, and then doing another offset
		// TODO does this look hacky now?
		matrixStackIn.push();
		
		// Standard transformation that LivingRenderer does
		matrixStackIn.scale(-1f, -1f, 1f);
		matrixStackIn.translate(0f, -1.5f, 0f);
		
		matrixStackIn.translate(0, plant.getBody().getHeight() / 2, 0);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180 + entityIn.getYawOffset()));
		matrixStackIn.translate(-offset + .5, -.001 * i, 0);
		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-entityIn.getPitch()));
		
		mainModel.render(matrixStackIn, bufferIn.getBuffer(mainModel.getRenderType(getEntityTexture(entityIn))), packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		
		matrixStackIn.pop();
	}
	
	@Override
	public ResourceLocation getEntityTexture(PlantBossEntity.PlantBossLeafLimb entity) {
		return PLANT_BOSS_TEXTURE_BASE;
	}
	
}
