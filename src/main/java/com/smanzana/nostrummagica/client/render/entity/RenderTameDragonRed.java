package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;

import net.minecraft.client.renderer.entity.EntityRendererManager;

public class RenderTameDragonRed extends RenderDragonRed<EntityTameDragonRed> {

	public RenderTameDragonRed(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelTameDragonRed(), shadowSizeIn);
	}
	
	@Override
	protected void preRenderCallback(EntityTameDragonRed entityIn, MatrixStack matrixStackIn, float partialTicks) {
		super.preRenderCallback(entityIn, matrixStackIn, partialTicks);
		
		final float age = entityIn.getGrowingAge();
		final float growthMod = .6f + .4f * age;
		
		matrixStackIn.scale(.6f, .6f, .6f);
		matrixStackIn.scale(growthMod, growthMod, growthMod);
		
		// 1f makes full-grown touch the ground.
		// .4 is 40%, which is max we shrink by.
		// 2.76 is size of full-grown tamed red ragons.
		matrixStackIn.translate(0f, 1f + ((.4f * entityIn.getHeight()) * (1f - age)), 0f);
	}

}
