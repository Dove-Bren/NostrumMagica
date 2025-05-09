package com.smanzana.nostrummagica.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Specialized provider since NostrumMAgic is INostrumMagic which is also INostrumNana
 * @param <C>
 * @param <U>
 */
public class NostrumMagicProvider implements ICapabilitySerializable<CompoundTag> {
	
	private final NostrumMagic instance;
	
	public NostrumMagicProvider(NostrumMagic instance) {
		this.instance = instance;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
		if (capability == CapabilityHandler.CAPABILITY_MAGIC) {
			return CapabilityHandler.CAPABILITY_MAGIC.orEmpty(capability, LazyOptional.of(() -> instance));
		}
		if (capability == CapabilityHandler.CAPABILITY_MANA) {
			return CapabilityHandler.CAPABILITY_MANA.orEmpty(capability, LazyOptional.of(() -> instance));
		}
		if (capability == CapabilityHandler.CAPABILITY_INCANTATION_HOLDER) {
			return CapabilityHandler.CAPABILITY_INCANTATION_HOLDER.orEmpty(capability, LazyOptional.of(() -> instance));
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
