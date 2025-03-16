package com.smanzana.nostrummagica.capabilities;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class ManaArmorStorage implements IStorage<IManaArmor> {

	private static final String NBT_HAS_ARMOR = "has_armor";
	private static final String NBT_MANA_COST = "mana_cost";
	
	@Override
	public Tag writeNBT(Capability<IManaArmor> capability, IManaArmor instance, Direction side) {
		CompoundTag nbt = new CompoundTag();
		
		nbt.putBoolean(NBT_HAS_ARMOR, instance.hasArmor());
		nbt.putInt(NBT_MANA_COST, instance.getManaCost());
		
		return nbt;
	}

	@Override
	public void readNBT(Capability<IManaArmor> capability, IManaArmor instance, Direction side, Tag nbt) {
		CompoundTag tag = (CompoundTag) nbt;
		instance.deserialize(tag.getBoolean(NBT_HAS_ARMOR),
			tag.getInt(NBT_MANA_COST));
	}

}
