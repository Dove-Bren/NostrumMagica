package com.smanzana.nostrummagica.inventory;

import java.util.Objects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class EquipmentSlotKey implements IInventorySlotKey<LivingEntity> {
	
	private final EquipmentSlotType slot;
	
	public EquipmentSlotKey(EquipmentSlotType slot) {
		this.slot = slot;
	}

	@Override
	public ItemStack getHeldStack(LivingEntity entity) {
		return entity.getItemStackFromSlot(this.slot);
	}

	@Override
	public ItemStack setStack(LivingEntity entity, ItemStack newStack) {
		ItemStack existing = getHeldStack(entity);
		entity.setItemStackToSlot(slot, newStack);
		return existing;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof EquipmentSlotKey) {
			return ((EquipmentSlotKey) o).slot == this.slot;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(slot);
	}

}
