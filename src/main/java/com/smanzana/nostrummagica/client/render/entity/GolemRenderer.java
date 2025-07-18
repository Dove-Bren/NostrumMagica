package com.smanzana.nostrummagica.client.render.entity;

import java.util.HashMap;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.GolemModel;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.entity.golem.MagicGolemEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GolemRenderer<T extends MagicGolemEntity> extends MobRenderer<T, GolemModel<T>> {

	private Map<String, ResourceLocation> texCache;
	
	public GolemRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn, new GolemModel<>(renderManagerIn.bakeLayer(NostrumModelLayers.Golem)), .8f);
		texCache = new HashMap<>();
	}
	
	@Override
	public ResourceLocation getTextureLocation(T entity) {
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
