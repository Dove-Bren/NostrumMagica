package com.smanzana.nostrummagica.client.render.entity;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class ModelBillboard extends Model {
	
	protected Vector3f offset;
	protected float radius;

	public ModelBillboard(Function<ResourceLocation, RenderType> renderTypeIn) {
		super(renderTypeIn);
	}
	
	public ModelBillboard() {
		this(RenderType::getEntityCutoutNoCull); // By default, do cutout instead of transparent
	}
	
	public ModelBillboard setRadius(float radius) {
		this.radius = radius;
		return this;
	}
	
	public ModelBillboard setOffset(@Nullable Vector3f offset) {
		if (offset == null) {
			offset = new Vector3f(0, 0, 0);
		}
		this.offset = offset;
		return this;
	}

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		final Minecraft mc = Minecraft.getInstance();
		RenderFuncs.renderSpaceQuadFacingCamera(matrixStackIn, bufferIn, mc.gameRenderer.getActiveRenderInfo(),
				offset.getX(), offset.getY(), offset.getZ(),
				radius,
				red, green, blue, alpha, packedLightIn);
	}

	
	
}
