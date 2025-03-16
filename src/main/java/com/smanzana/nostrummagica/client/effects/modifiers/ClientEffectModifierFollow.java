package com.smanzana.nostrummagica.client.effects.modifiers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectModifierFollow implements ClientEffectModifier {

	private LivingEntity entity;
	
	public ClientEffectModifierFollow(LivingEntity entity) {
		this.entity = entity;
	}
	
	@Override
	public void apply(PoseStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}

	@Override
	public void earlyApply(PoseStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		Vec3 pos = entity.getEyePosition(partialTicks).subtract(0, entity.getEyeHeight(), 0);
		matrixStackIn.translate(pos.x, pos.y, pos.z);
	}
}
