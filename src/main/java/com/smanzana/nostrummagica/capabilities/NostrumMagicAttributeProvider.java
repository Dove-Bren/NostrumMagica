package com.smanzana.nostrummagica.capabilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class NostrumMagicAttributeProvider implements ICapabilitySerializable<INBT> {

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private INostrumMagic instance = CAPABILITY.getDefaultInstance();
	private Entity entity;
	
	public NostrumMagicAttributeProvider(Entity object) {
		this.entity = object;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if (capability == CAPABILITY) {
			if (entity instanceof LivingEntity)
				this.instance.provideEntity((LivingEntity) entity);
			return CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this.instance));
		}
		
		return null;
	}

	@Override
	public INBT serializeNBT() {
		return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(INBT nbt) {
		CAPABILITY.getStorage().readNBT(CAPABILITY, instance, null, nbt);
	}

}
