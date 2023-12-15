package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySprite;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

public class ModelSpriteCore extends ModelOBJ<EntitySprite> {

	public ModelSpriteCore() {
		super();
	}

	@Override
	protected ModelResourceLocation[] getEntityModels() {
		return new ModelResourceLocation[] {
			RenderFuncs.makeDefaultModelLocation(new ResourceLocation(NostrumMagica.MODID, "entity/sprite_core")),
			RenderFuncs.makeDefaultModelLocation(new ResourceLocation(NostrumMagica.MODID, "entity/sprite_arms"))
		};
	}
	
	@Override
	protected int getColor(int i, EntitySprite ent) {
		final int bright = 0x00202020;
		
		int color = 0xFF75B589;
		
		if (i > 0) {
			color |= bright;
		}
		
		return color;
	}

	@Override
	protected boolean preRender(EntitySprite entity, int model, BufferBuilder buffer, double x, double y, double z,
			float entityYaw, float partialTicks, float scaleIn) {
		GlStateManager.scalef(.5f, .5f, .5f);
		GlStateManager.translatef(0, entity.getHeight(), 0);
		GlStateManager.rotatef(-90f, 1f, 0f, 0f);
		
		if (model > 0) {
			final int intervalsPer = 2;
			final float ticks = entity.ticksExisted + partialTicks;
			float rate = 20f;
			boolean angry = false;
			
			if (entity instanceof EntitySprite && ((EntitySprite) entity).isAngry()) {
				angry = true;
			}
			
			// Get full rotate interval
			float interval = ((int) (ticks / rate) * intervalsPer);
			
			if (angry) {
				interval += (intervalsPer * (float) (ticks % rate) / rate);
			} else {
				// Add movement effect
				// Ease inout
				float change = Math.min(1f, (float) (ticks % rate) / 10f);
				if (change <= .5f) {
					change *= 2f;
					change = .5f * change * change;
				} else {
					change = (change - .5f) * 2;
					change = 1f + (float) Math.pow(change-1, 3);
					change *= .5f;
					change += .5f;
				}
				interval += intervalsPer * change;
			}
			
			final float frac = 360f / 8;
			GlStateManager.rotatef(frac * (interval), 0f, 0f, 1f);
		}
		//frac = (entity.ticksExisted + partialTicks) / (20f * 10f);
		//GlStateManager.rotatef(360f * frac, 1f, 0, 0);
		
		GlStateManager.color3f(1f, 0, 0);
		
		return true;
	}
}
