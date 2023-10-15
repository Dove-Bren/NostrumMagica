package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderSwitchTrigger extends LivingRenderer<EntitySwitchTrigger, ModelSwitchTrigger> {

	public RenderSwitchTrigger(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new ModelSwitchTrigger(), .1f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySwitchTrigger entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/blocks/spawner.png"
				);
	}
	
}
