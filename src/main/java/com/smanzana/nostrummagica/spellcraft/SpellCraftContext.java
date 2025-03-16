package com.smanzana.nostrummagica.spellcraft;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SpellCraftContext {
	
	public final @Nonnull Player player;
	public final @Nonnull Level world;
	public final @Nonnull BlockPos pos;
	public final @Nonnull INostrumMagic magic;
	
	public SpellCraftContext(@Nonnull Player player, @Nonnull Level world, @Nonnull BlockPos pos) {
		this.player = player;
		this.world = world;
		this.pos = pos;
		this.magic = NostrumMagica.getMagicWrapper(player);
	}
	
}
