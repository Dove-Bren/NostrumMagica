package com.smanzana.nostrummagica.client.render.entity;

import java.util.HashMap;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.golem.EntityGolem;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderGolem extends RenderLiving<EntityGolem> {

	private Map<String, ResourceLocation> texCache;
	
	public RenderGolem(RenderManager renderManagerIn, EntityModel modelBaseIn, float shadowSizeIn) {
		super(renderManagerIn, modelBaseIn, shadowSizeIn);
		texCache = new HashMap<>();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityGolem entity) {
		ResourceLocation loc = texCache.get(entity.getTextureKey());
		if (loc == null) {
			loc = new ResourceLocation(NostrumMagica.MODID,
					"textures/entity/golem_" + entity.getTextureKey() + ".png"
					);
			texCache.put(entity.getTextureKey(), loc);
		}
		return loc;
	}
	
}
