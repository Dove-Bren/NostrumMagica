package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelDragonEgg;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonEgg;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderDragonEgg extends MobRenderer<EntityDragonEgg, ModelDragonEgg> {

	public RenderDragonEgg(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelDragonEgg(), shadowSizeIn);
	}

	@Override
	public ResourceLocation getEntityTexture(EntityDragonEgg entity) {
		
		// TODO maybe swap out texture depending on type of dragon?
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/dragon_egg_generic.png"
				);
	}
	
	@Override
	public void render(EntityDragonEgg entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		// Store on the model (instead of passing through as color D:) how cold the egg is
		final float coldScale = 1f - (entityIn.getHeat() / EntityDragonEgg.HEAT_MAX);
		this.entityModel.setColdScale(coldScale);
		
		// Continue with render
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
}
