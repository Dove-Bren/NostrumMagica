package com.smanzana.nostrummagica.entity.renderer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityDragonRed;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDragonRed extends RenderLiving<EntityDragonRed> {

	public RenderDragonRed(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelDragonRed(), shadowSizeIn);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDragonRed entity) {
		// TODO fixme?
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/koid.png"
				);
	}

}
