package com.smanzana.nostrummagica.client.effects.modifiers;

import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectModifierFollow implements ClientEffectModifier {

	private LivingEntity entity;
	
	public ClientEffectModifierFollow(LivingEntity entity) {
		this.entity = entity;
	}
	
	@Override
	public void apply(ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}

	@Override
	public void earlyApply(ClientEffectRenderDetail detail, float progress, float partialTicks) {
		Vec3d pos = entity.getEyePosition(partialTicks).subtract(0, entity.getEyeHeight(), 0);
		GlStateManager.translated(pos.x, pos.y, pos.z);
	}
}
