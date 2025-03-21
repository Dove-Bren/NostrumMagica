package com.smanzana.nostrummagica.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class AutoCapabilityProvider<C extends INBTSerializable<CompoundTag>, U extends C> implements ICapabilitySerializable<CompoundTag> {
	
	private final Capability<C> cap;
	private final U instance;
	
	public AutoCapabilityProvider(Capability<C> cap, U instance) {
		this.cap = cap;
		this.instance = instance;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
		if (capability == cap) {
			return cap.orEmpty(capability, LazyOptional.of(() -> instance));
		}
		
		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		return instance.serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		instance.deserializeNBT(nbt);
	}

}
