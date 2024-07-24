package com.smanzana.nostrummagica.inventory;

import java.util.Objects;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class PlayerInventorySlotKey implements IInventorySlotKey<PlayerEntity> {
	
	private final int slotIdx;
	
	public PlayerInventorySlotKey(int slotIdx) {
		this.slotIdx = slotIdx;
	}

	@Override
	public ItemStack getHeldStack(PlayerEntity player) {
		return player.inventory.getStackInSlot(slotIdx);
	}

	@Override
	public ItemStack setStack(PlayerEntity player, ItemStack newStack) {
		ItemStack existing = getHeldStack(player);
		player.inventory.setInventorySlotContents(slotIdx, newStack);
		return existing;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof PlayerInventorySlotKey) {
			return ((PlayerInventorySlotKey) o).slotIdx == this.slotIdx;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 4561 * Objects.hash(slotIdx);
	}

}
