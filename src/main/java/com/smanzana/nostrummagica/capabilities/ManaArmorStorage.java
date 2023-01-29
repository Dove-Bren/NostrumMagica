package com.smanzana.nostrummagica.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class ManaArmorStorage implements IStorage<IManaArmor> {

	private static final String NBT_HAS_ARMOR = "has_armor";
	private static final String NBT_MANA_COST = "mana_cost";
	
	@Override
	public NBTBase writeNBT(Capability<IManaArmor> capability, IManaArmor instance, EnumFacing side) {
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setBoolean(NBT_HAS_ARMOR, instance.hasArmor());
		nbt.setInteger(NBT_MANA_COST, instance.getManaCost());
		
		return nbt;
	}

	@Override
	public void readNBT(Capability<IManaArmor> capability, IManaArmor instance, EnumFacing side, NBTBase nbt) {
		NBTTagCompound tag = (NBTTagCompound) nbt;
		instance.deserialize(tag.getBoolean(NBT_HAS_ARMOR),
			tag.getInteger(NBT_MANA_COST));
	}

}
