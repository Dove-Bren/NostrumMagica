package com.smanzana.nostrummagica.quests.rewards;

import net.minecraft.entity.player.EntityPlayer;

public interface IReward {
	
	public void award(EntityPlayer player);
	public String getDescription();
	
}
