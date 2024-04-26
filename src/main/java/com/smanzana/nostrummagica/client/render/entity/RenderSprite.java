package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySprite;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class RenderSprite extends MobRenderer<EntitySprite, ModelSpriteCore> {

	private static final ResourceLocation TEXTURE = NostrumMagica.Loc("textures/entity/sprite_core.png");
	
	public RenderSprite(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelSpriteCore(), shadowSizeIn);
	}

	@Override
	public ResourceLocation getEntityTexture(EntitySprite entity) {
		return TEXTURE;
	}
	
	@Override
	public void render(EntitySprite entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		// The core and arms used to be different colors than white
		//final int bright = 0x00202020;
		//int color = 0xFF75B589;
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	@Override
	protected void preRenderCallback(EntitySprite entityIn, MatrixStack matrixStackIn, float partialTickTime) {
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.translate(0, entityIn.getHeight(), 0);
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90f));
	}

}
