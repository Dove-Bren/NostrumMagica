package com.smanzana.nostrummagica.stats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;

public class PlayerStats {

	
	public PlayerStats() {
		
	}
	
	public @Nonnull CompoundNBT toNBT(@Nullable CompoundNBT nbt) {
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		
		
		return nbt;
	}
	
	public static final PlayerStats FromNBT(@Nonnull CompoundNBT nbt) {
		PlayerStats stats = new PlayerStats();
		
		
		return stats;
	}
	
}
