package com.smanzana.nostrummagica.inventory;

import java.util.Objects;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class InventorySlotKey implements IInventorySlotKey<Container> {
	
	private final int slotIdx;
	
	public InventorySlotKey(int slotIdx) {
		this.slotIdx = slotIdx;
	}

	@Override
	public ItemStack getHeldStack(Container inventory) {
		return inventory.getItem(slotIdx);
	}

	@Override
	public ItemStack setStack(Container inventory, ItemStack newStack) {
		ItemStack existing = getHeldStack(inventory);
		inventory.setItem(slotIdx, newStack);
		return existing;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof InventorySlotKey) {
			return ((InventorySlotKey) o).slotIdx == this.slotIdx;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(slotIdx);
	}

}
