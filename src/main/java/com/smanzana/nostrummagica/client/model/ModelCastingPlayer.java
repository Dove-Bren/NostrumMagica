package com.smanzana.nostrummagica.client.model;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

/**
 * Custom player model that just slightly tweaks arm animation for players that are casting
 */
public class ModelCastingPlayer extends PlayerModel<AbstractClientPlayer> {

	public ModelCastingPlayer(ModelPart root) {
		super(root, false);
	}
	
	@Override
	public void setupAnim(AbstractClientPlayer entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		
		if (NostrumMagica.spellChargeTracker.getCharge(entityIn) != null) {
			this.rightArm.z = 0.0F;
			this.rightArm.x = -5.0F;
			this.leftArm.z = 0.0F;
			this.leftArm.x = 5.0F;
			this.rightArm.xRot = -2f + Mth.cos(ageInTicks * 0.22F) * 0.25F;
			this.leftArm.xRot = -2f + Mth.cos(ageInTicks * 0.22F) * 0.25F;
			this.rightArm.zRot = Mth.cos(ageInTicks * 0.22F) * 0.25F;
			this.leftArm.zRot = -Mth.cos(ageInTicks * 0.22F) * 0.25F;
			this.rightArm.yRot = Mth.sin(ageInTicks * 0.32F) * 0.25F;
			this.leftArm.yRot = -Mth.sin(ageInTicks * 0.32F) * 0.25F;
		}
	}

}
