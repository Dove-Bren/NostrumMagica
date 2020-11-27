package com.smanzana.nostrummagica.client.effects.modifiers;

import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

public class ClientEffectModifierFollow implements ClientEffectModifier {

	private EntityLivingBase entity;
	
	public ClientEffectModifierFollow(EntityLivingBase entity) {
		this.entity = entity;
	}
	
	@Override
	public void apply(ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}

	@Override
	public void earlyApply(ClientEffectRenderDetail detail, float progress, float partialTicks) {
		Vec3d pos = entity.getPositionEyes(partialTicks).subtract(0, entity.getEyeHeight(), 0);
		GlStateManager.translate(pos.xCoord, pos.yCoord, pos.zCoord);
	}
}
