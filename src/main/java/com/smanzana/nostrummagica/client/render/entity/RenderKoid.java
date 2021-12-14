package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityKoid;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderKoid extends RenderLiving<EntityKoid> {

	public RenderKoid(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelKoid(), shadowSizeIn);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityKoid entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/koid.png"
				);
	}
	

}
