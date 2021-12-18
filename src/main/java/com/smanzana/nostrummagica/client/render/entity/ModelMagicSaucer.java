package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class ModelMagicSaucer extends ModelOBJ {

	public ModelMagicSaucer() {
		super();
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
			new ResourceLocation(NostrumMagica.MODID, "entity/magic_saucer.obj")
		};
	}
	
	@Override
	protected int getColor(int i, Entity ent) {
		return EMagicElement.ENDER.getColor();
	}

	@Override
	protected boolean preRender(Entity entity, int model, BufferBuilder buffer, double x, double y, double z,
			float entityYaw, float partialTicks) {
		GlStateManager.scale(.5, .5, .5);
		GlStateManager.translate(0, entity.height, 0);
		GlStateManager.rotate(-90f, 1f, 0f, 0f);
		GlStateManager.color(1f, 0, 0);
		return true;
	}
}
