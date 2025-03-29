package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.resources.ResourceLocation;

public class ModelRendererBakedWithOffset extends ModelPartBaked {
	
	private float offsetX;
	private float offsetY;
	private float offsetZ;
	
	public ModelRendererBakedWithOffset(ResourceLocation model) {
		super(model);
	}
	
	public ModelRendererBakedWithOffset(ResourceLocation model, float offsetX, float offsetY, float offsetZ) {
		this(model);
		setOffsets(offsetX, offsetY, offsetZ);
	}
	
	public void setOffsets(float offsetX, float offsetY, float offsetZ) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
	}
	
	public float getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(float offsetX) {
		this.offsetX = offsetX;
	}

	public float getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(float offsetY) {
		this.offsetY = offsetY;
	}

	public float getOffsetZ() {
		return offsetZ;
	}

	public void setOffsetZ(float offsetZ) {
		this.offsetZ = offsetZ;
	}

	// Made public with AT :)
	@Override
	public void translateAndRotate(PoseStack matrixStackIn) {
		// Apply offset
		matrixStackIn.translate(offsetX, offsetY, offsetZ);
		super.translateAndRotate(matrixStackIn);

	}
}