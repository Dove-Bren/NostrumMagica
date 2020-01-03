package com.smanzana.nostrummagica.entity.renderer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderSwitchTrigger extends RenderLiving<EntitySwitchTrigger> {

	public RenderSwitchTrigger(RenderManager renderManagerIn) {
		super(renderManagerIn, new ModelSwitchTrigger(), .1f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySwitchTrigger entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/blocks/spawner.png"
				);
	}
	
}
