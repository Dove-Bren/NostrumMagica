package com.smanzana.nostrummagica.entity.renderer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityWillo;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderWillo extends RenderLiving<EntityWillo> {

	public RenderWillo(RenderManager renderManagerIn, float scale) {
		super(renderManagerIn, new ModelWillo(), .33f);
	}
	
	@Override
	public void doRender(EntityWillo entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.color(.65f, 1f, .7f, 1f);
		GlStateManager.disableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableCull();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GlStateManager.enableCull();
		GlStateManager.color(1f, 1f, 1f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityWillo entity) {
		return new ResourceLocation(NostrumMagica.MODID, "textures/entity/willo.png");
	}
	
}
