package com.smanzana.nostrummagica.client.model;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.util.ModelUtils;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

/**
 * A ModelPart that supports rendering traditional baked models, like block models or objs.
 * 
 * Used to be a wrapper for OBJ files, and even wrapped it in a GL render list to speed rendering it up.
 * 
 * This class originally adapted from 
 * https://github.com/2piradians/Minewatch/blob/1.12.1/src/main/java/twopiradians/minewatch/client/render/entity/RenderOBJModel.java.
 * @author Skyler
 *
 * @param <T>
 */
public class PartBakedModel extends PartCloneModel {

	private final ResourceLocation modelLocation;
	private @Nullable BakedModel bakedModel;
	private boolean loaded;
	
	public PartBakedModel(ResourceLocation modelLocation) {
		super(Lists.newArrayList(MakeFauxCube()), new HashMap<>());
		
		this.modelLocation = modelLocation;
	}
	
	protected BakedModel loadModel(ResourceLocation location) {
		return ModelUtils.GetBakedModel(location);
	}
	
	protected void checkAndLoadModel() {
		if (!loaded) {
			loaded = true;
			this.bakedModel = loadModel(this.modelLocation);
		}
	}
	
	// Made public with AT :) TODO not anymore, right?
	@Override
	public void compile(PoseStack.Pose matrixEntryIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		checkAndLoadModel();
		
		// Render obj model
		RenderFuncs.RenderModel(matrixEntryIn, bufferIn, bakedModel, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		
		// Render boxes and children renderes that may have been set up
		super.compile(matrixEntryIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	private static final PartCloneModel.Cube MakeFauxCube() {
		//int u, int v, float x, float y, float z, float wx, float wy, float wz, float growX, float growY, float growZ, boolean mirror, float p_104355_, float p_104356_
		return new PartCloneModel.Cube(0, 0, 0, 0, 0, .1f, .1f, .1f, 0f, 0f, 0f, false, 1, 1); // last two params may need adjusting?
	}

}
