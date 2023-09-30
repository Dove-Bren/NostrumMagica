package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityCyclerSpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer.Vector;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderMagicSaucer extends Render<EntitySpellSaucer> {
	
	private ModelMagicSaucer mainModel;

	public RenderMagicSaucer(RenderManager renderManagerIn) {
		super(renderManagerIn);
		mainModel = new ModelMagicSaucer();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySpellSaucer entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/magic_blade.png"
				);
	}
	
	@Override
	public void doRender(EntitySpellSaucer entity, double x, double y, double z, float entityYaw, float partialTicks) {
		
		GlStateManager.pushMatrix();
		
        GlStateManager.disableCull();
        GlStateManager.enableAlphaTest();
        
        
        if (entity instanceof EntityCyclerSpellSaucer) {
        	EntityCyclerSpellSaucer cycler = (EntityCyclerSpellSaucer) entity;
        	
        	// Instead of rendering real position, render where we should basically be
        	//Vector vec = cycler.getTargetOffsetLoc(partialTicks); // TODO should this just be the target pos? Otherwise it cycles the player even if it's actually on another player
        	Vector vec = cycler.getTargetLoc(partialTicks);
        	vec.subtract(NostrumMagica.instance.proxy.getPlayer().getPositionVector());
        	
        	GlStateManager.translatef(vec.x, vec.y, vec.z);
        } else {
            // Render at actual positional offset from player
        	GlStateManager.translatef(x, y, z);
            GlStateManager.rotatef(entity.rotationPitch, 1, 0, 0);
        }
        
        
        mainModel.render(entity, 0f, 0f, 0f, 0f, 0f, 1f);
		
		GlStateManager.popMatrix();
		
	}
}
