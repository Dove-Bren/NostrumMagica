package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

public class ModelKoid extends ModelOBJ<EntityKoid> {

	public ModelKoid() {
		super();
	}

	@Override
	protected ModelResourceLocation[] getEntityModels() {
		return new ModelResourceLocation[] {
			RenderFuncs.makeDefaultModelLocation(new ResourceLocation(NostrumMagica.MODID, "entity/koid"))
		};
	}
	
	@Override
	protected int getColor(int i, EntityKoid ent) {
		final int bright = 0x00202020;
		
		int color = 0x00;
		
		if (ent instanceof EntityKoid) {
			EntityKoid koid = (EntityKoid) ent;
			switch (koid.getElement()) {
			case EARTH:
				color = 0xFF704113;
				break;
			case ENDER:
				color = 0xFF663099;
				break;
			case FIRE:
				color = 0xFFD10F00;
				break;
			case ICE:
				color = 0xFF23D9EA;
				break;
			case LIGHTNING:
				color = 0xFFEAEA2C;
				break;
			case PHYSICAL:
				color = 0xFFFFFFFF;
				break;
			case WIND:
				color = 0xFF18CC59;
				break;
			}
			
			if (koid.getAttackTarget() != null)
				color += bright;
		
		}
		
		return color;
	}

	@Override
	protected boolean preRender(EntityKoid entity, int model, BufferBuilder buffer, double x, double y, double z,
			float entityYaw, float partialTicks) {
		GlStateManager.translatef(0, entity.getHeight(), 0);
		float frac = (entity.ticksExisted + partialTicks) / (20f * 3.0f);
		GlStateManager.rotatef(360f * frac, 0, 1f, 0);
		frac = (entity.ticksExisted + partialTicks) / (20f * 10f);
		GlStateManager.rotatef(360f * frac, 1f, 0, 0);
		
		return true;
	}
}
