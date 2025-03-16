package com.smanzana.nostrummagica.inventory;

import java.util.Objects;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerInventorySlotKey implements IInventorySlotKey<Player> {
	
	private final int slotIdx;
	
	public PlayerInventorySlotKey(int slotIdx) {
		this.slotIdx = slotIdx;
	}

	@Override
	public ItemStack getHeldStack(Player player) {
		return player.getInventory().getItem(slotIdx);
	}

	@Override
	public ItemStack setStack(Player player, ItemStack newStack) {
		ItemStack existing = getHeldStack(player);
		player.getInventory().setItem(slotIdx, newStack);
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
