package com.smanzana.nostrummagica.entity.renderer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityGolem;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderGolem extends RenderLiving<EntityGolem> {

	public RenderGolem(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
		super(renderManagerIn, modelBaseIn, shadowSizeIn);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGolem entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/golem_" + entity.getTextureKey() + ".png"
				);
	}
	
}
