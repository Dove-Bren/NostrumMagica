package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ArcaneWolfModel;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.client.render.layer.ArcaneWolfRunesLayer;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ArcaneWolfRenderer extends MobRenderer<ArcaneWolfEntity, ArcaneWolfModel> {

	private static final ResourceLocation ARCANE_WOLF_TEXTURE_BASE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/arcane_wolf/base.png");
	
	public ArcaneWolfRenderer(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ArcaneWolfModel(renderManagerIn.bakeLayer(NostrumModelLayers.ArcaneWolf)), shadowSizeIn);
		this.addLayer(new ArcaneWolfRunesLayer(this));
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
	public void render(ArcaneWolfEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
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
