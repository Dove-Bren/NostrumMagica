package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.renderer.BufferBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class ModelOrb extends ModelOBJ {

	private static final ResourceLocation MODEL = new ResourceLocation(NostrumMagica.MODID, "entity/orb.obj");
	private float scale;
	private int color;
	
	public ModelOrb(float scale, int color) {
		this.scale = scale * .4f;
		this.color = color;
	}
	
	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
			MODEL
		};
	}
	
	@Override
	protected int getColor(int i, Entity ent) {
		return color;
	}

	@Override
	protected boolean preRender(Entity entity, int model, BufferBuilder buffer, double x, double y, double z,
			float entityYaw, float partialTicks) {
		GlStateManager.scalef(scale, scale, scale);
		GlStateManager.rotatef(-90f, 1f, 0f, 0f);
		return true;
	}
	
}
