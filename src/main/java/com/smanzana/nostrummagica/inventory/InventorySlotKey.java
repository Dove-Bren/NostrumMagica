package com.smanzana.nostrummagica.inventory;

import java.util.Objects;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventorySlotKey implements IInventorySlotKey<IInventory> {
	
	private final int slotIdx;
	
	public InventorySlotKey(int slotIdx) {
		this.slotIdx = slotIdx;
	}

	@Override
	public ItemStack getHeldStack(IInventory inventory) {
		return inventory.getStackInSlot(slotIdx);
	}

	@Override
	public ItemStack setStack(IInventory inventory, ItemStack newStack) {
		ItemStack existing = getHeldStack(inventory);
		inventory.setInventorySlotContents(slotIdx, newStack);
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
