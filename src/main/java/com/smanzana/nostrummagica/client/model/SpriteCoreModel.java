package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.SpriteEntity;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.resources.ResourceLocation;

public class SpriteCoreModel extends BakedModel<SpriteEntity> {
	
	public static final ResourceLocation MODEL_CORE = new ResourceLocation(NostrumMagica.MODID, "entity/sprite_core");
	public static final ResourceLocation MODEL_ARMS = new ResourceLocation(NostrumMagica.MODID, "entity/sprite_arms");
	
	protected PartBakedModel core;
	protected PartBakedModel arms;
	
	protected float red;
	protected float green;
	protected float blue;
	protected float alpha;

	public SpriteCoreModel() {
		super(); // Only a child class to use LookupModel
		
		core = new PartBakedModel(MODEL_CORE);
		arms = new PartBakedModel(MODEL_ARMS);
		this.children.add(core);
		this.children.add(arms);
	}

	public void setColor(int color) {
		final float[] colors = ColorUtil.ARGBToColor(color);
		setColor(colors[0], colors[1], colors[2], colors[3]);
	}
	
	public void setColor(float red, float green, float blue, float alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}
	
	@Override
	protected void renderChild(PartBakedModel child, int index, PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		super.renderChild(child, index, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, 
				red * this.red, green * this.green, blue * this.blue, alpha * this.alpha);
	}

	@Override
	public void setupAnim(SpriteEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		
		final int intervalsPer = 2;
		float rate = 20f;
		boolean angry = false;
		
		if (entityIn instanceof SpriteEntity && ((SpriteEntity) entityIn).isAngry()) {
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
		arms.zRot = frac * (interval);
	}
}
