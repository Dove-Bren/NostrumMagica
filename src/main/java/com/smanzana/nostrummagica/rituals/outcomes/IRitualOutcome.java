package com.smanzana.nostrummagica.rituals.outcomes;

import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IRitualOutcome {

	public void perform(World world, EntityPlayer player, BlockPos center, RitualRecipe recipe);
	
}
