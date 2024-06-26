package com.smanzana.nostrummagica.spellcraft;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpellCraftContext {
	
	public final @Nonnull PlayerEntity player;
	public final @Nonnull World world;
	public final @Nonnull BlockPos pos;
	public final @Nonnull INostrumMagic magic;
	
	public SpellCraftContext(@Nonnull PlayerEntity player, @Nonnull World world, @Nonnull BlockPos pos) {
		this.player = player;
		this.world = world;
		this.pos = pos;
		this.magic = NostrumMagica.getMagicWrapper(player);
	}
	
}
