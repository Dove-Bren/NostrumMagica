package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySprite;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderSprite extends MobRenderer<EntitySprite, ModelSpriteCore> {

	//private ModelSpriteArms armModel;
	
	public RenderSprite(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelSpriteCore(), shadowSizeIn);
		
		//this.armModel = new ModelSpriteArms();
	}

	@Override
	public ResourceLocation getEntityTexture(EntitySprite entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/sprite_core.png"
				);
	}
	
	@Override
	protected void renderModel(EntitySprite entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		super.renderModel(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
		
		//armModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
	}

}
