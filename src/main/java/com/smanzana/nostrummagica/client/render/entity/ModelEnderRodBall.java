package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityEnderRodBall;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

public class ModelEnderRodBall extends ModelOBJ<EntityEnderRodBall> {

	protected final float alpha;
	
	public ModelEnderRodBall(float alpha) {
		super();
		this.alpha = alpha;
	}

	@Override
	protected ModelResourceLocation[] getEntityModels() {
		return new ModelResourceLocation[] {
			RenderFuncs.makeDefaultModelLocation(new ResourceLocation(NostrumMagica.MODID, "entity/koid"))
		};
	}
	
	@Override
	protected int getColor(int i, EntityEnderRodBall ent) {
		final int a = (int) ((float) 0xFF * this.alpha) << 24; 
		int color = 0x008650B9 | a;
		return color;
	}

	@Override
	protected boolean preRender(EntityEnderRodBall entity, int model, BufferBuilder buffer, double x, double y, double z,
			float entityYaw, float partialTicks, float scale) {
		final float time = (entity.ticksExisted + partialTicks); 
		GlStateManager.translatef(0, entity.getHeight()/2, 0);
		
		GlStateManager.scalef(scale, scale, scale);
		
		float frac = time / (20f * 3.0f);
		GlStateManager.rotatef(360f * frac, 0, 1f, 0);
		frac = (entity.ticksExisted + partialTicks) / (20f * 10f);
		GlStateManager.rotatef(360f * frac, 1f, 0, 0);
		
		return true;
	}
}
