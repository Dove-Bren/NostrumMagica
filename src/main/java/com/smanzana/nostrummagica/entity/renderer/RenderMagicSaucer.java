package com.smanzana.nostrummagica.entity.renderer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityCyclerSpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer.Vector;

import net.minecraft.client.renderer.GlStateManager;
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
        GlStateManager.enableAlpha();
        
        
        if (entity instanceof EntityCyclerSpellSaucer) {
        	EntityCyclerSpellSaucer cycler = (EntityCyclerSpellSaucer) entity;
        	
        	// Instead of rendering real position, render where we should basically be
        	Vector vec = cycler.getTargetOffsetLoc(partialTicks);
        	GlStateManager.translate(vec.x, vec.y, vec.z);
        } else {
            // Render at actual positional offset from player
        	GlStateManager.translate(x, y, z);
            GlStateManager.rotate(entity.rotationPitch, 1, 0, 0);
        }
        
        
        mainModel.render(entity, 0f, 0f, 0f, 0f, 0f, 1f);
		
		GlStateManager.popMatrix();
		
	}
}
