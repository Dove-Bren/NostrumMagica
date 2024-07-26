package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.ResourceLocation;

public class ModelRendererBakedWithOffset extends ModelRendererBaked {
	
	private float offsetX;
	private float offsetY;
	private float offsetZ;
	
	public ModelRendererBakedWithOffset(Model base, ResourceLocation model) {
		super(base, model);
	}
	
	public ModelRendererBakedWithOffset(ResourceLocation model) {
		super(model);
	}
	
	public ModelRendererBakedWithOffset(Model base, ResourceLocation model, float offsetX, float offsetY, float offsetZ) {
		super(base, model);
		setOffsets(offsetX, offsetY, offsetZ);
	}
	
	public ModelRendererBakedWithOffset(ResourceLocation model, float offsetX, float offsetY, float offsetZ) {
		super(model);
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
	public void translateRotate(MatrixStack matrixStackIn) {
		// Apply offset
		matrixStackIn.translate(offsetX, offsetY, offsetZ);
		super.translateRotate(matrixStackIn);

	}
}