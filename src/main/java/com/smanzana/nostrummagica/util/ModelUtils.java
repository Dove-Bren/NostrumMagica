package com.smanzana.nostrummagica.util;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;

public class ModelUtils {

	public static final BakedModel GetBakedModel(ResourceLocation loc) {
		final ModelManager manager = Minecraft.getInstance().getModelManager();
		BakedModel model = manager.getModel(loc);
		if (model == null || model == manager.getMissingModel()) {
			NostrumMagica.logger.error("Could not find model to match " + loc);
			model = manager.getMissingModel();
		}
		
		return model;
	}
	
}
