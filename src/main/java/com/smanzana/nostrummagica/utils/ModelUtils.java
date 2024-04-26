package com.smanzana.nostrummagica.utils;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;

public class ModelUtils {

	public static final IBakedModel GetBakedModel(ModelResourceLocation loc) {
		final ModelManager manager = Minecraft.getInstance().getModelManager();
		IBakedModel model = manager.getModel(loc);
		if (model == null || model == manager.getMissingModel()) {
			NostrumMagica.logger.error("Could not find model to match " + loc);
			model = manager.getMissingModel();
		}
		
		return model;
	}
	
}
