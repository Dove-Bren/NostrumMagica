package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.client.model.ModelSpriteCore;
import com.smanzana.nostrummagica.entity.SpriteEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class RenderSprite extends MobRenderer<SpriteEntity, ModelSpriteCore> {

	public RenderSprite(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelSpriteCore(), shadowSizeIn);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getTextureLocation(SpriteEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
	
	@Override
	public void render(SpriteEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		this.model.setColor(0xFF75B589);
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	@Override
	protected void scale(SpriteEntity entityIn, PoseStack matrixStackIn, float partialTickTime) {
		matrixStackIn.translate(0, -.5, .75f);
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(90f));
	}

}
