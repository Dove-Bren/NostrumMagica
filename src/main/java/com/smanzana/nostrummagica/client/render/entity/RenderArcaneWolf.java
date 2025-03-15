package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelArcaneWolf;
import com.smanzana.nostrummagica.client.render.layer.LayerArcaneWolfRunes;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderArcaneWolf extends MobRenderer<ArcaneWolfEntity, ModelArcaneWolf> {

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
	protected float getBob(ArcaneWolfEntity livingBase, float partialTicks) {
		return livingBase.getTailAngle();
	}
	
	@Override
	public void render(ArcaneWolfEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		if (entity.isWet()) {
			//float f = entity.getBrightness() * entity.getShadingWhileWet(partialTicks);
			float f = entity.getWetShade(partialTicks);
			this.model.setColor(f, f, f);
		}
		
		//this.entityModel = new ModelArcaneWolf();
		super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		// Vanilla sets this back every time. Not sure why
		if (entity.isWet()) {
			this.model.setColor(1f, 1f, 1f);
		}
	}
	
	@Override
	public ResourceLocation getTextureLocation(ArcaneWolfEntity entity) {
		return ARCANE_WOLF_TEXTURE_BASE;
	}
	
}
