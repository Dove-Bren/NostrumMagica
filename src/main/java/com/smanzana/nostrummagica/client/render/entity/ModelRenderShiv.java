package com.smanzana.nostrummagica.client.render.entity;

import java.util.function.Function;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * Hacky model that allows for renderer-level rendering after all the transformations that models are rendered at.
 * Replicates rendering that used to be done in renderModel()
 * @author Skyler
 *
 */
public class ModelRenderShiv<T extends Entity> extends EntityModel<T> {

	public static interface RenderRunnable {
		public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
				float red, float green, float blue, float alpha);
	}
	
	private RenderRunnable payload;
	
	public ModelRenderShiv() {
		this(RenderType::getEntityCutoutNoCull);
	}
	
	public ModelRenderShiv(Function<ResourceLocation, RenderType> renderTypeMap) {
		super(renderTypeMap);
		payload = null;
	}
	
	public void setPayload(RenderRunnable payload) {
		this.payload = payload;
	}

	@Override
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		;
	}

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		if (this.payload != null) {
			this.payload.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			this.payload = null;
		}
	}
}
