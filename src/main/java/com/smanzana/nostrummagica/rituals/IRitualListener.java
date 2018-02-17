package com.smanzana.nostrummagica.rituals;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IRitualListener {

	public void onRitualPerformed(RitualRecipe ritual, World world, EntityPlayer player, BlockPos center);
	
}
