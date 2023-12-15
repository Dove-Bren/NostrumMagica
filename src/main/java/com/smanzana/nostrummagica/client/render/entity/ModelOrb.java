package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class ModelOrb<T extends Entity> extends ModelOBJ<T> {

	private static final ModelResourceLocation MODEL = RenderFuncs.makeDefaultModelLocation(new ResourceLocation(NostrumMagica.MODID, "entity/orb"));
	private float scale;
	private int color;
	
	public ModelOrb(float scale, int color) {
		this.scale = scale * .4f;
		this.color = color;
	}
	
	@Override
	protected ModelResourceLocation[] getEntityModels() {
		return new ModelResourceLocation[] {
			MODEL
		};
	}
	
	@Override
	protected int getColor(int i, T ent) {
		return color;
	}

	@Override
	protected boolean preRender(T entity, int model, BufferBuilder buffer, double x, double y, double z,
			float entityYaw, float partialTicks, float scaleIn) {
		GlStateManager.scalef(this.scale, this.scale, this.scale);
		GlStateManager.rotatef(-90f, 1f, 0f, 0f);
		return true;
	}
	
}
