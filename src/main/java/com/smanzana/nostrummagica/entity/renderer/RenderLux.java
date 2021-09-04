package com.smanzana.nostrummagica.entity.renderer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityLux;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderLux extends RenderLiving<EntityLux> {

	public RenderLux(RenderManager renderManagerIn, float scale) {
		super(renderManagerIn, new ModelLux(), .33f);
	}
	
	@Override
	public void doRender(EntityLux entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.color(.65f, 1f, .7f);
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GlStateManager.color(1f, 1f, 1f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityLux entity) {
		return new ResourceLocation(NostrumMagica.MODID, "textures/entity/sprite_core.png");
	}
	
}
