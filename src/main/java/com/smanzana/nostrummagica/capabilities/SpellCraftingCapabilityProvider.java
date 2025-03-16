package com.smanzana.nostrummagica.capabilities;

import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SpellCraftingCapabilityProvider implements ICapabilitySerializable<Tag> {

	@CapabilityInject(ISpellCrafting.class)
	public static Capability<ISpellCrafting> CAPABILITY = null;
	
	private ISpellCrafting instance = CAPABILITY.getDefaultInstance();
	
	public SpellCraftingCapabilityProvider() {
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
