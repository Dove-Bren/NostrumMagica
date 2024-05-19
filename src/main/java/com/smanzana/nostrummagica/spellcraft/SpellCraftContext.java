package com.smanzana.nostrummagica.spellcraft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpellCraftContext {

	public final PlayerEntity player;
	public final World world;
	public final BlockPos pos;
	
	public SpellCraftContext(PlayerEntity player, World world, BlockPos pos) {
		this.player = player;
		this.world = world;
		this.pos = pos;
	}
	
}
