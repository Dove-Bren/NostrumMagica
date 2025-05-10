package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.client.model.ModelCastingPlayer;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

public class RenderCastingPlayer extends PlayerRenderer {
	
	public RenderCastingPlayer(EntityRendererProvider.Context renderManager) {
		super(renderManager, false);
		
		// Overwrite model that parent set up
		this.model = new ModelCastingPlayer(renderManager.bakeLayer(ModelLayers.PLAYER));
	}
}
