package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonEgg;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderDragonEgg extends LivingRenderer<EntityDragonEgg, ModelDragonEgg> {

	public RenderDragonEgg(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelDragonEgg(), shadowSizeIn);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDragonEgg entity) {
		
		// TODO maybe swap out texture depending on type of dragon?
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/dragon_egg_generic.png"
				);
	}
	
}
