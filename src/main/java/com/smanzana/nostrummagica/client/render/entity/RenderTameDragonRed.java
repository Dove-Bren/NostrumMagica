package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.model.ModelTameDragonRed;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class RenderTameDragonRed extends RenderDragonRed<TameRedDragonEntity> {

	public RenderTameDragonRed(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelTameDragonRed(), shadowSizeIn);
	}
	
	@Override
	protected void scale(TameRedDragonEntity entityIn, PoseStack matrixStackIn, float partialTicks) {
		super.scale(entityIn, matrixStackIn, partialTicks);
		
		final float age = entityIn.getGrowingAge();
		final float growthMod = .6f + .4f * age;
		
		matrixStackIn.scale(.6f, .6f, .6f);
		matrixStackIn.scale(growthMod, growthMod, growthMod);
		
		// 1f makes full-grown touch the ground.
		// .4 is 40%, which is max we shrink by.
		// 2.76 is size of full-grown tamed red ragons.
		//matrixStackIn.translate(0f, 1f + ((.4f * entityIn.getHeight()) * (1f - age)), 0f);
	}

}
