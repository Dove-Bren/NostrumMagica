package com.smanzana.nostrummagica.progression.quests.objectives;

import net.minecraft.nbt.CompoundNBT;

public interface IObjectiveState {
	
	public CompoundNBT toNBT();
	public void fromNBT(CompoundNBT nbt);
	
}
