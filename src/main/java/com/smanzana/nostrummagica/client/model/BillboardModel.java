package com.smanzana.nostrummagica.client.model;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;

public class BillboardModel extends Model {
	
	protected float radius;

	public BillboardModel(Function<ResourceLocation, RenderType> renderTypeIn) {
		super(renderTypeIn);
		this.radius = .5f;
	}
	
	public BillboardModel() {
		this(RenderType::entityCutoutNoCull); // By default, do cutout instead of transparent
	}
	
	public BillboardModel setRadius(float radius) {
		this.radius = radius;
		return this;
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		final Minecraft mc = Minecraft.getInstance();
		RenderFuncs.renderSpaceQuadFacingCamera(matrixStackIn, bufferIn, mc.gameRenderer.getMainCamera(),
				radius,
				packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	
	
}
