package com.smanzana.nostrummagica.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

/**
 * An entity model that's mode up of regular baked models like block models or obj models.
 * @param <T>
 */
public class BakedModel<T extends Entity> extends EntityModel<T> {
	
	protected List<PartBakedModel> children;
	
	public BakedModel(ResourceLocation ... models) {
		this(RenderType::entityCutoutNoCull, models);
	}
	
	public BakedModel(Function<ResourceLocation, RenderType> renderTypeMap, ResourceLocation ... models) {
		super(renderTypeMap);
		children = fetchModels(models);
	}

	protected @Nonnull List<PartBakedModel> fetchModels(@Nullable ResourceLocation[] modelLocations) {
		if (modelLocations == null || modelLocations.length == 0) {
			return new ArrayList<>();
		}
		
		List<PartBakedModel> list = new ArrayList<>(modelLocations.length);
		for (ResourceLocation loc : modelLocations) {
			list.add(new PartBakedModel(loc));
		}
		return list;
	}
	
	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		;
	}

	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		int i = 0;
		for (PartBakedModel child : children) {
			this.renderChild(child, i, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			i++;
		}
	}
	
	protected void renderChild(PartBakedModel child, int index, PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		child.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
}
