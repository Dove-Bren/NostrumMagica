package com.smanzana.nostrummagica.inventory;

import java.util.Objects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class EquipmentSlotKey implements IInventorySlotKey<LivingEntity> {
	
	private final EquipmentSlot slot;
	
	public EquipmentSlotKey(EquipmentSlot slot) {
		this.slot = slot;
	}

	@Override
	public ItemStack getHeldStack(LivingEntity entity) {
		return entity.getItemBySlot(this.slot);
	}

	@Override
	public ItemStack setStack(LivingEntity entity, ItemStack newStack) {
		ItemStack existing = getHeldStack(entity);
		entity.setItemSlot(slot, newStack);
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
	
	public EquipmentSlot getSlotType() {
		return this.slot;
	}

}
