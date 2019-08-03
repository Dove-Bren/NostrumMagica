package com.smanzana.nostrummagica.entity.renderer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityDragonRedBase;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDragonRed extends RenderLiving<EntityDragonRedBase> {

	public RenderDragonRed(RenderManager renderManagerIn, float shadowSizeIn) {
		this(renderManagerIn, new ModelDragonRed(), shadowSizeIn);
	}
	
	protected RenderDragonRed(RenderManager renderManagerIn, ModelDragonRed modelBase, float shadowSizeIn) {
		super(renderManagerIn, modelBase, shadowSizeIn);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDragonRedBase entity) {
		// TODO fixme?
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/koid.png"
				);
	}

}
