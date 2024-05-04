package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySprite;

import net.minecraft.util.ResourceLocation;

public class ModelSpriteCore extends ModelBaked<EntitySprite> {
	
	public static final ResourceLocation MODEL_CORE = new ResourceLocation(NostrumMagica.MODID, "entity/sprite_core");
	public static final ResourceLocation MODEL_ARMS = new ResourceLocation(NostrumMagica.MODID, "entity/sprite_arms");
	
	protected ModelRendererBaked core;
	protected ModelRendererBaked arms;

	public ModelSpriteCore() {
		super(); // Only a child class to use LookupModel
		
		core = new ModelRendererBaked(this, MODEL_CORE);
		arms = new ModelRendererBaked(this, MODEL_ARMS);
		this.children.add(core);
		this.children.add(arms);
	}

//	@Override
//	protected int getColor(int i, EntitySprite ent) {
//		final int bright = 0x00202020;
//		
//		int color = 0xFF75B589;
//		
//		if (i > 0) {
//			color |= bright;
//		}
//		
//		return color;
//	}

	@Override
	public void setRotationAngles(EntitySprite entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		
		final int intervalsPer = 2;
		float rate = 20f;
		boolean angry = false;
		
		if (entityIn instanceof EntitySprite && ((EntitySprite) entityIn).isAngry()) {
			angry = true;
		}
		
		// Get full rotate interval
		float interval = ((int) (ageInTicks / rate) * intervalsPer);
		
		if (angry) {
			interval += (intervalsPer * (float) (ageInTicks % rate) / rate);
		} else {
			// Add movement effect
			// Ease inout
			float change = Math.min(1f, (float) (ageInTicks % rate) / 10f);
			if (change <= .5f) {
				change *= 2f;
				change = .5f * change * change;
			} else {
				change = (change - .5f) * 2;
				change = 1f + (float) Math.pow(change-1, 3);
				change *= .5f;
				change += .5f;
			}
			interval += intervalsPer * change;
		}
		
		final float frac = (float) (Math.PI * (2.0/8.0)); // (2 * Math.pi) / 8
		
		// This is Z because it's tilted on it's side in the obj?
		arms.rotateAngleZ = frac * (interval);
	}
}
