package com.smanzana.nostrummagica.ritual;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IRitualListener {

	public void onRitualPerformed(RitualRecipe ritual, World world, PlayerEntity player, BlockPos center);
	
}
