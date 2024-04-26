package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

public class ModelMagicSaucer<T extends EntitySpellSaucer> extends ModelBaked<T> {

	public ModelMagicSaucer() {
		super();
	}

	@Override
	protected ModelResourceLocation[] getEntityModels() {
		return new ModelResourceLocation[] {
			RenderFuncs.makeDefaultModelLocation(new ResourceLocation(NostrumMagica.MODID, "entity/magic_saucer"))
		};
	}
	
	@Override
	protected int getColor(int i, T ent) {
		return EMagicElement.ENDER.getColor();
	}

	@Override
	protected boolean preRender(T entity, int model, BufferBuilder buffer, double x, double y, double z,
			float entityYaw, float partialTicks, float scaleIn) {
		GlStateManager.scalef(.5f, .5f, .5f);
		GlStateManager.translatef(0, entity.getHeight(), 0);
		GlStateManager.rotatef(-90f, 1f, 0f, 0f);
		GlStateManager.color3f(1f, 0, 0);
		return true;
	}
}
