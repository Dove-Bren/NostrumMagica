package com.smanzana.nostrummagica.client.effects.modifiers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.effects.ClientEffect.ClientEffectRenderDetail;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectModifierFollow implements ClientEffectModifier {

	private LivingEntity entity;
	
	public ClientEffectModifierFollow(LivingEntity entity) {
		this.entity = entity;
	}
	
	@Override
	public void apply(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		;
	}

	@Override
	public void earlyApply(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		Vector3d pos = entity.getEyePosition(partialTicks).subtract(0, entity.getEyeHeight(), 0);
		matrixStackIn.translate(pos.x, pos.y, pos.z);
	}
}
