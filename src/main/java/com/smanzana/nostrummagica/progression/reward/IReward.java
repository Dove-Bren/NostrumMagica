package com.smanzana.nostrummagica.progression.reward;

import net.minecraft.entity.player.PlayerEntity;

public interface IReward {
	
	public void award(PlayerEntity player);
	public String getDescription();
	
}
