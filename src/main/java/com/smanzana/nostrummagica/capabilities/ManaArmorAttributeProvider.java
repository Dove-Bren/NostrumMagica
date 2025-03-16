package com.smanzana.nostrummagica.capabilities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ManaArmorAttributeProvider implements ICapabilitySerializable<Tag> {

	@CapabilityInject(IManaArmor.class)
	public static Capability<IManaArmor> CAPABILITY = null;
	
	private IManaArmor instance = CAPABILITY.getDefaultInstance();
	private Entity entity;
	
	public ManaArmorAttributeProvider(Entity object) {
		this.entity = object;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if (capability == CAPABILITY) {
			if (entity instanceof LivingEntity)
				this.instance.provideEntity((LivingEntity) entity);
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
