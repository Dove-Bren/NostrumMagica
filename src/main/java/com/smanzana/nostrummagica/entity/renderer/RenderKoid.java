package com.smanzana.nostrummagica.entity.renderer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityKoid;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderKoid extends RenderOBJModel<EntityKoid> {

	public RenderKoid(RenderManager renderManagerIn) {
		super(renderManagerIn);
		this.shadowSize = .3f;
	}

	@Override
	protected ResourceLocation[] getEntityModels() {
		return new ResourceLocation[] {
			new ResourceLocation(NostrumMagica.MODID, "entity/koid.obj")
		};
	}
	
	@Override
	protected int getColor(int i, EntityKoid koid) {
		final int bright = 0x00202020;
		
		int color = 0x00;
		
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
		
		return color;
	}

	@Override
	protected boolean preRender(EntityKoid entity, int model, VertexBuffer buffer, double x, double y, double z,
			float entityYaw, float partialTicks) {
		GlStateManager.translate(0, -.5, 0);
		float frac = (entity.ticksExisted + partialTicks) / (20f * 3.0f);
		GlStateManager.rotate(360f * frac, 0, 1f, 0);
		frac = (entity.ticksExisted + partialTicks) / (20f * 10f);
		GlStateManager.rotate(360f * frac, 1f, 0, 0);
		
		return true;
	}

}
