package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelArcaneWolf;
import com.smanzana.nostrummagica.client.render.layer.LayerArcaneWolfRunes;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderArcaneWolf extends MobRenderer<EntityArcaneWolf, ModelArcaneWolf> {

	private static final ResourceLocation ARCANE_WOLF_TEXTURE_BASE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/arcane_wolf/base.png");
	
	public RenderArcaneWolf(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelArcaneWolf(), shadowSizeIn);
		this.addLayer(new LayerArcaneWolfRunes(this));
	}
	
	/**
	 * Defines what float the third param in setRotationAngles of EntityModel is
	 * @param livingBase
	 * @param partialTicks
	 * @return
	 */
	@Override
	protected float handleRotationFloat(EntityArcaneWolf livingBase, float partialTicks) {
		return livingBase.getTailRotation();
	}
	
	@Override
	public void render(EntityArcaneWolf entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		if (entity.isWolfWet()) {
			//float f = entity.getBrightness() * entity.getShadingWhileWet(partialTicks);
			float f = entity.getShadingWhileWet(partialTicks);
			this.entityModel.setTint(f, f, f);
		}
		
		//this.entityModel = new ModelArcaneWolf();
		super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		// Vanilla sets this back every time. Not sure why
		if (entity.isWolfWet()) {
			this.entityModel.setTint(1f, 1f, 1f);
		}
	}
	
	@Override
	public ResourceLocation getEntityTexture(EntityArcaneWolf entity) {
		return ARCANE_WOLF_TEXTURE_BASE;
	}
	
}
