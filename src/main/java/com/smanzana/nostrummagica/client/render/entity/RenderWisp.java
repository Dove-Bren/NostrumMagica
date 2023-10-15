package com.smanzana.nostrummagica.client.render.entity;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class RenderWisp extends EntityRenderer<EntityWisp> {

	private Map<EMagicElement, ModelWisp> modelCache;
	private float scale;
	
	public RenderWisp(EntityRendererManager renderManagerIn, float scale) {
		super(renderManagerIn);
		modelCache = new EnumMap<>(EMagicElement.class);
		this.scale = scale;
	}
	
	@Override
	public void doRender(EntityWisp entity, double x, double y, double z, float entityYaw, float partialTicks) {
		ModelWisp model = modelCache.get(entity.getElement());
		if (model == null) {
			model = new ModelWisp(entity.getElement(), scale);
			modelCache.put(entity.getElement(), model);
		}
		
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		model.render(entity, partialTicks, 0, 0, 0, 0, this.scale);
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityWisp entity) {
		return null;
	}
	
}
