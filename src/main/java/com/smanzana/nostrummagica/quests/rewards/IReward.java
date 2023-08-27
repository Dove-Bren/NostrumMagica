package com.smanzana.nostrummagica.quests.rewards;

import net.minecraft.entity.player.PlayerEntity;

public interface IReward {
	
	public void award(PlayerEntity player);
	public String getDescription();
	
}
