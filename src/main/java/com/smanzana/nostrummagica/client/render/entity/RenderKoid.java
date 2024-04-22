package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityKoid;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderKoid extends MobRenderer<EntityKoid, ModelKoid> {

	public RenderKoid(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelKoid(), shadowSizeIn);
	}

	@Override
	public ResourceLocation getEntityTexture(EntityKoid entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/koid.png"
				);
	}
	

}
