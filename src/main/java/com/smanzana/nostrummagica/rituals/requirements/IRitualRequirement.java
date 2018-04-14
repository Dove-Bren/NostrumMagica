package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.entity.player.EntityPlayer;

public interface IRitualRequirement {
	
	public abstract boolean matches(EntityPlayer player, INostrumMagic attr);
	
}
