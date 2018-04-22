package com.smanzana.nostrummagica.client.effects.modifiers;

import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;

public class ClientEffectModifierFollow implements ClientEffectModifier {

	private EntityLivingBase entity;
	
	public ClientEffectModifierFollow(EntityLivingBase entity) {
		this.entity = entity;
	}
	
	@Override
	public void apply(ClientEffectRenderDetail detail, float progress) {
		;
	}

	@Override
	public void earlyApply(ClientEffectRenderDetail detail, float progress) {
		GlStateManager.translate(entity.posX, entity.posY, entity.posZ);
	}
}
