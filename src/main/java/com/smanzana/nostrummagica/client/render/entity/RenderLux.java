package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityLux;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderLux extends MobRenderer<EntityLux, ModelLux> {

	public RenderLux(EntityRendererManager renderManagerIn, float scale) {
		super(renderManagerIn, new ModelLux(), .33f);
	}
	
	@Override
	public void doRender(EntityLux entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.color3f(.65f, 1f, .7f);
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GlStateManager.color3f(1f, 1f, 1f);
	}

	@Override
	public ResourceLocation getEntityTexture(EntityLux entity) {
		return new ResourceLocation(NostrumMagica.MODID, "textures/entity/sprite_core.png");
	}
	
}
