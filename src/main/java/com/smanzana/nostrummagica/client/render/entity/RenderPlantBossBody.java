package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.entity.plantboss.PlantBossEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class RenderPlantBossBody extends RenderInvisibleMultiPartEntity<PlantBossEntity.PlantBossBody> {

	public RenderPlantBossBody(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
	}
}
