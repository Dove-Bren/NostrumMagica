package com.smanzana.nostrummagica.client.model;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

/**
 * Hacky model that allows for renderer-level rendering after all the transformations that models are rendered at.
 * Replicates rendering that used to be done in renderModel()
 * @author Skyler
 *
 */
public class RenderShivModel<T extends Entity> extends EntityModel<T> {

	public static interface RenderRunnable {
		public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
				float red, float green, float blue, float alpha);
	}
	
	private RenderRunnable payload;
	
	public RenderShivModel() {
		this(RenderType::entityCutoutNoCull);
	}
	
	public RenderShivModel(Function<ResourceLocation, RenderType> renderTypeMap) {
		super(renderTypeMap);
		payload = null;
	}
	
	public void setPayload(RenderRunnable payload) {
		this.payload = payload;
	}

	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		;
	}

	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		if (this.payload != null) {
			this.payload.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			this.payload = null;
		}
	}
}
