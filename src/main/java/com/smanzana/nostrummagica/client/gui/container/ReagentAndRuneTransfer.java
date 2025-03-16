package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.item.equipment.ReagentBag;
import com.smanzana.nostrummagica.item.equipment.RuneBag;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ReagentAndRuneTransfer {
	
	public static final boolean ShouldAddTo(Player player, AbstractContainerMenu container) {
		// Disallow when it's the player inventory container
		if (container == player.inventoryMenu) {
			return false;
		}
		if (player.level.isClientSide() && container instanceof ItemPickerMenu) {
			return false;
		}
		if (container instanceof ReagentBagGui.BagContainer) {
			return false;
		}
		if (container instanceof RuneBagGui.BagContainer) {
			return false;
		}
		
		boolean foundPlayerInv = false; // Make sure player inventory is represented on the screen in some form
		for (Slot slot : container.slots) {
			if (slot.container == player.inventory) {
				final int slotIdx = slot.getSlotIndex();
				if (Inventory.isHotbarSlot(slotIdx) || slotIdx >= 36) {
					continue; // hotbar or armor slot
				}
				
				foundPlayerInv = true;
				break;
			}
		}
		
		if (!foundPlayerInv) {
			return false;
		}
		
		// Make sure player has an applicable bag
		List<ItemStack> reagentBags = FindReagentBags(player);
		if (!reagentBags.isEmpty()) {
			return true;
		}
		
		List<ItemStack> runeBags = FindRuneBags(player);
		if (!runeBags.isEmpty()) {
			return true;
		}
		
		return false;
	}
	
	public static final List<ItemStack> FindReagentBags(Player player) {
		List<ItemStack> ret = new ArrayList<>();
		
		for (ItemStack item : player.inventory.items) {
			if (!item.isEmpty() && item.getItem() instanceof ReagentBag) {
				ret.add(item);
			}
		}
		for (ItemStack item : player.getAllSlots()) {
			if (!item.isEmpty() && item.getItem() instanceof ReagentBag) {
				ret.add(item);
			}
		}
		
		Container curios = NostrumMagica.instance.curios.getCurios(player);
		if (curios != null) {
			for (int i = 0; i < curios.getContainerSize(); i++) {
				ItemStack equip = curios.getItem(i);
				if (equip.isEmpty()) {
					continue;
				}
				
				if (equip.getItem() instanceof ReagentBag) {
					ret.add(equip);
				}
			}
		}
		
		return ret;
	}
	
	public static final List<ItemStack> FindRuneBags(Player player) {
		List<ItemStack> ret = new ArrayList<>();
		
		for (ItemStack item : player.inventory.items) {
			if (!item.isEmpty() && item.getItem() instanceof RuneBag) {
				ret.add(item);
			}
		}
		for (ItemStack item : player.getAllSlots()) {
			if (!item.isEmpty() && item.getItem() instanceof RuneBag) {
				ret.add(item);
			}
		}
		
		Container curios = NostrumMagica.instance.curios.getCurios(player);
		if (curios != null) {
			for (int i = 0; i < curios.getContainerSize(); i++) {
				ItemStack equip = curios.getItem(i);
				if (equip.isEmpty()) {
					continue;
				}
				
				if (equip.getItem() instanceof RuneBag) {
					ret.add(equip);
				}
			}
		}
		
		return ret;
	}
	
	public static final void ProcessContainerItems(Player player, AbstractContainerMenu container) {
		List<ItemStack> reagentBags = FindReagentBags(player);
		List<ItemStack> runeBags = FindRuneBags(player);
		
		for (Slot slot : container.slots) {
			if (slot.container != player.inventory
					&& slot.hasItem() && !slot.getItem().isEmpty()) {
				if (slot.getItem().getItem() instanceof ReagentItem) {
					ItemStack toAdd = slot.getItem().copy();
					for (ItemStack bag : reagentBags) {
						toAdd = ReagentBag.addItem(bag, toAdd);
						if (toAdd.isEmpty()) {
							break;
						}
					}
					slot.set(toAdd);
				} else if (slot.getItem().getItem() instanceof SpellRune) {
					ItemStack toAdd = slot.getItem().copy();
					for (ItemStack bag : runeBags) {
						toAdd = RuneBag.addItem(bag, toAdd);
						if (toAdd.isEmpty()) {
							break;
						}
					}
					slot.set(toAdd);
				}
			}
		}
	}
}
