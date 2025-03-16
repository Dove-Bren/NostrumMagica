package com.smanzana.nostrummagica.progression.reward;

import net.minecraft.world.entity.player.Player;

public interface IReward {
	
	public void award(Player player);
	public String getDescription();
	
}
