package com.smanzana.nostrummagica.ritual.outcome;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

public interface IItemRitualOutcome extends IRitualOutcome {

	public @Nonnull ItemStack getResult();
	
}
