package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.LayerArcaneWolfRunes;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderArcaneWolf extends RenderLiving<EntityArcaneWolf> {

	private static final ResourceLocation ARCANE_WOLF_TEXTURE_BASE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/arcane_wolf/base.png");
	
	public RenderArcaneWolf(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelArcaneWolf(), shadowSizeIn);
		this.addLayer(new LayerArcaneWolfRunes(this));
	}
	
	/**
	 * Defines what float the third param in setRotationAngles of ModelBase is
	 * @param livingBase
	 * @param partialTicks
	 * @return
	 */
	@Override
	protected float handleRotationFloat(EntityArcaneWolf livingBase, float partialTicks) {
		return livingBase.getTailRotation();
	}
	
	@Override
	public void doRender(EntityArcaneWolf entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if (entity.isWolfWet()) {
			float f = entity.getBrightness() * entity.getShadingWhileWet(partialTicks);
			GlStateManager.color(f, f, f);
		}
		
		//this.mainModel = new ModelArcaneWolf();
		
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityArcaneWolf entity) {
		return ARCANE_WOLF_TEXTURE_BASE;
	}
	
}
