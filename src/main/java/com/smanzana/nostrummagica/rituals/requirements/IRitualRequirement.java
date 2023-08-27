package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.entity.player.PlayerEntity;

public interface IRitualRequirement {
	
	public abstract boolean matches(PlayerEntity player, INostrumMagic attr);
	
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
