package com.smanzana.nostrummagica.capabilities;

import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class BonusJumpCapabilityProvider implements ICapabilitySerializable<Tag> {

	@CapabilityInject(IBonusJumpCapability.class)
	public static Capability<IBonusJumpCapability> CAPABILITY = null;
	
	private IBonusJumpCapability instance = CAPABILITY.getDefaultInstance();
	
	public BonusJumpCapabilityProvider() {
		;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if (capability == CAPABILITY) {
			return CAPABILITY.orEmpty(capability, LazyOptional.of(() -> instance));
		}
		
		return LazyOptional.empty();
	}

	@Override
	public Tag serializeNBT() {
		return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(Tag nbt) {
		CAPABILITY.getStorage().readNBT(CAPABILITY, instance, null, nbt);
	}

}
