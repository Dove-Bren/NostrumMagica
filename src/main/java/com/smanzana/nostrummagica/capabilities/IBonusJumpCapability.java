package com.smanzana.nostrummagica.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IBonusJumpCapability extends INBTSerializable<CompoundTag> {
	
	public int getCount();
	
	public void incrCount();
	
	public void resetCount();
	
	public void copy(IBonusJumpCapability source);

}
