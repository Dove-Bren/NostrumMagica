package com.smanzana.nostrummagica.ritual;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IRitualListener {

	public void onRitualPerformed(RitualRecipe ritual, Level world, Player player, BlockPos center);
	
}
