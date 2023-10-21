package com.smanzana.nostrummagica.client.render.entity;

import java.util.HashMap;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.golem.EntityGolem;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderGolem<T extends EntityGolem> extends MobRenderer<T, ModelGolem<T>> {

	private Map<String, ResourceLocation> texCache;
	
	public RenderGolem(EntityRendererManager renderManagerIn, ModelGolem<T> modelBaseIn, float shadowSizeIn) {
		super(renderManagerIn, modelBaseIn, shadowSizeIn);
		texCache = new HashMap<>();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(T entity) {
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
