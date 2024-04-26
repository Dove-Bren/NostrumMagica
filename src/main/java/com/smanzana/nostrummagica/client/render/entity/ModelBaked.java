package com.smanzana.nostrummagica.client.render.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.utils.ModelUtils;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class ModelBaked<T extends Entity> extends EntityModel<T> {
	
	protected List<ModelRendererBaked> children;
	
	public ModelBaked(ModelResourceLocation ... models) {
		this(RenderType::getEntityCutoutNoCull, models);
	}
	
	public ModelBaked(Function<ResourceLocation, RenderType> renderTypeMap, ModelResourceLocation ... models) {
		super(renderTypeMap);
		this.children = fetchModels(models);
	}

	protected @Nonnull List<ModelRendererBaked> fetchModels(@Nullable ModelResourceLocation[] modelLocations) {
		if (modelLocations == null || modelLocations.length == 0) {
			return new ArrayList<>();
		}
		
		List<ModelRendererBaked> list = new ArrayList<>(modelLocations.length);
		for (ModelResourceLocation loc : modelLocations) {
			list.add(new ModelRendererBaked(this, ModelUtils.GetBakedModel(loc)));
		}
		return list;
	}
	
	@Override
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		;
	}

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		children.forEach(child -> child.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha));
	}
}
