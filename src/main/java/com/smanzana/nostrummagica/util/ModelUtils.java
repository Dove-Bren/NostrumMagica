package com.smanzana.nostrummagica.util;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.util.ResourceLocation;

public class ModelUtils {

	public static final IBakedModel GetBakedModel(ResourceLocation loc) {
		final ModelManager manager = Minecraft.getInstance().getModelManager();
		IBakedModel model = manager.getModel(loc);
		if (model == null || model == manager.getMissingModel()) {
			NostrumMagica.logger.error("Could not find model to match " + loc);
			model = manager.getMissingModel();
		}
		
		return model;
	}
	
}
