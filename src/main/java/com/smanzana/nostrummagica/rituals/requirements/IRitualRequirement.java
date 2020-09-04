package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.entity.player.EntityPlayer;

public interface IRitualRequirement {
	
	public abstract boolean matches(EntityPlayer player, INostrumMagic attr);
	
	public static IRitualRequirement AND(IRitualRequirement ... requirements) {
		return (player, attr) -> {
			for (IRitualRequirement req : requirements) {
				if (!req.matches(player, attr)) {
					return false;
				}
			}
			return true;
		};
	}
	
}
