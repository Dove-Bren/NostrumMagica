package com.smanzana.nostrummagica.client.model;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Custom player model that just slightly tweaks arm animation for players that are casting
 */
public abstract class ModelCastingPlayer extends PlayerModel<AbstractClientPlayer> {

	private ModelCastingPlayer(ModelPart root) {
		super(root, false);
	}
	
	//@Override called via mixin
	public static <T extends LivingEntity> void setupAnim(HumanoidModel<T>self, T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		if (NostrumMagica.spellChargeTracker.getCharge(entityIn) != null) {
			self.rightArm.z = 0.0F;
			self.rightArm.x = -5.0F;
			self.leftArm.z = 0.0F;
			self.leftArm.x = 5.0F;
			self.rightArm.xRot = -2f + Mth.cos(ageInTicks * 0.22F) * 0.25F;
			self.leftArm.xRot = -2f + Mth.cos(ageInTicks * 0.22F) * 0.25F;
			self.rightArm.zRot = Mth.cos(ageInTicks * 0.22F) * 0.25F;
			self.leftArm.zRot = -Mth.cos(ageInTicks * 0.22F) * 0.25F;
			self.rightArm.yRot = Mth.sin(ageInTicks * 0.32F) * 0.25F;
			self.leftArm.yRot = -Mth.sin(ageInTicks * 0.32F) * 0.25F;
		}
	}

}
