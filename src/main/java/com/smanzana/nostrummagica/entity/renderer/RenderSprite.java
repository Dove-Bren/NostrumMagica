package com.smanzana.nostrummagica.entity.renderer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySprite;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderSprite extends RenderLiving<EntitySprite> {

	//private ModelSpriteArms armModel;
	
	public RenderSprite(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelSpriteCore(), shadowSizeIn);
		
		//this.armModel = new ModelSpriteArms();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySprite entity) {
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
