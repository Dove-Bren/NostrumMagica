package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.entity.plantboss.PlantBossEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PlantBossBodyRenderer extends InvisibleMultiPartEntityRenderer<PlantBossEntity.PlantBossBody> {

	public PlantBossBodyRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
	}
}
