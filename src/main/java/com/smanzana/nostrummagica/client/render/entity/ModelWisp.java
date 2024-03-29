package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.client.renderer.entity.model.EntityModel;

public class ModelWisp extends EntityModel<EntityWisp> {
	
	private ModelOrb<EntityWisp> core;
	private ModelOrb<EntityWisp> fringe;
	
	private static final int makeColor(int alpha, int color) {
		int ret = (color & 0x00FFFFFF);
		ret |= (0xFF000000 & (alpha << 24));
		
		return ret;
	}
	
	public ModelWisp(EMagicElement element, float scale) {
		int color = (element == null ? EMagicElement.PHYSICAL : element).getColor();
		core = new ModelOrb<>(scale * .4f, makeColor(0xFF, color));
		fringe = new ModelOrb<>(scale, makeColor(0x30, color));
	}
	
	@Override
	public void render(EntityWisp entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		
		final float ticks = 3 * 20;
		float frac = (((float) entity.ticksExisted + time) % ticks) / ticks;
		float adjustedScale = (float) (Math.sin(frac * Math.PI * 2) * .1) + 1f;
		
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, entity.getHeight() / 2, 0);
		core.render(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
		GlStateManager.scalef(adjustedScale, adjustedScale, adjustedScale);
		GlStateManager.depthMask(false);
		fringe.render(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
	}
	
}
