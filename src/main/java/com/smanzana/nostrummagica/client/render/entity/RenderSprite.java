package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.model.ModelSpriteCore;
import com.smanzana.nostrummagica.entity.SpriteEntity;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class RenderSprite extends MobRenderer<SpriteEntity, ModelSpriteCore> {

	public RenderSprite(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelSpriteCore(), shadowSizeIn);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getEntityTexture(SpriteEntity entity) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}
	
	@Override
	public void render(SpriteEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		this.entityModel.setColor(0xFF75B589);
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	@Override
	protected void preRenderCallback(SpriteEntity entityIn, MatrixStack matrixStackIn, float partialTickTime) {
		matrixStackIn.translate(0, -.5, .75f);
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90f));
	}

}
