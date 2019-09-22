package com.smanzana.nostrummagica.entity.renderer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityDragonEgg;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDragonEgg extends RenderLiving<EntityDragonEgg> {

	public RenderDragonEgg(RenderManager renderManagerIn, float shadowSizeIn) {
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
