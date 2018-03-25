package com.smanzana.nostrummagica.quests.objectives;

import net.minecraft.nbt.NBTTagCompound;

public interface IObjectiveState {
	
	public NBTTagCompound toNBT();
	public void fromNBT(NBTTagCompound nbt);
	
}
