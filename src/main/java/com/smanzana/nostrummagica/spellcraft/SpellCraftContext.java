package com.smanzana.nostrummagica.spellcraft;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SpellCraftContext {
	
	public static final SpellCraftContext DUMMY = new SpellCraftContext(null, null, null, null);
	
	public final @Nonnull Player player;
	public final @Nonnull Level world;
	public final @Nonnull BlockPos pos;
	public final @Nonnull INostrumMagic magic;
	
	public SpellCraftContext(@Nonnull Player player, @Nonnull Level world, @Nonnull BlockPos pos) {
		this(player, world, pos, NostrumMagica.getMagicWrapper(player));
	}
	
	private SpellCraftContext(@Nonnull Player player, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull INostrumMagic magic) {
		this.player = player;
		this.world = world;
		this.pos = pos;
		this.magic = magic;
	}
	
	public final boolean isValid() {
		return !isDummy();
	}
	
	public final boolean isDummy() {
		return this == DUMMY;
	}
	
}
