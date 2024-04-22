package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityEnderRodBall;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class RenderEnderRodBall extends EntityRenderer<EntityEnderRodBall> {
	
	protected ModelEnderRodBall center;
	protected ModelEnderRodBall edge;
	protected ModelEnderRodBall glow;

	public RenderEnderRodBall(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		this.center = new ModelEnderRodBall(1f);
		this.edge = new ModelEnderRodBall(.4f);
		this.glow = new ModelEnderRodBall(.2f);
	}

	@Override
	public ResourceLocation getEntityTexture(EntityEnderRodBall entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/koid.png"
				);
	}
	
	@Override
	public void doRender(EntityEnderRodBall entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		
        GlStateManager.disableCull();
        GlStateManager.enableAlphaTest();
        
        
    	GlStateManager.translated(x, y, z);
        GlStateManager.rotatef(entity.rotationPitch, 1, 0, 0);
        
        final float time = entity.ticksExisted + partialTicks;
        float frac = time / (20 * 1.5f);
		float scale = 1.3f + .1f * (float) (Math.sin(Math.PI * 2 * frac));
        center.render(entity, 0f, 0f, 0f, 0f, 0f, scale);
        
        scale += .2f;
        edge.render(entity, 0f, 0f, 0f, 0f, 0f, scale);
        
        frac = time / (20f * .6f);
        scale = 2f * (1 - (frac % 1f));
        glow.render(entity, 0f, 0f, 0f, 0f, 0f, scale);
		
		GlStateManager.popMatrix();
	}
}
