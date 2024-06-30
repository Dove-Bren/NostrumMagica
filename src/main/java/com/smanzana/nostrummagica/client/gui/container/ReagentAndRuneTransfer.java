package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.item.equipment.ReagentBag;
import com.smanzana.nostrummagica.item.equipment.RuneBag;

import net.minecraft.client.gui.screen.inventory.CreativeScreen.CreativeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public final class ReagentAndRuneTransfer {
	
	public static final boolean ShouldAddTo(PlayerEntity player, Container container) {
		// Disallow when it's the player inventory container
		if (container == player.container) {
			return false;
		}
		if (container instanceof CreativeContainer) {
			return false;
		}
		if (container instanceof ReagentBagGui.BagContainer) {
			return false;
		}
		if (container instanceof RuneBagGui.BagContainer) {
			return false;
		}
		
		boolean foundPlayerInv = false; // Make sure player inventory is represented on the screen in some form
		for (Slot slot : container.inventorySlots) {
			if (slot.inventory == player.inventory) {
				final int slotIdx = slot.getSlotIndex();
				if (PlayerInventory.isHotbar(slotIdx) || slotIdx >= 36) {
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
	
	public static final List<ItemStack> FindReagentBags(PlayerEntity player) {
		List<ItemStack> ret = new ArrayList<>();
		
		for (ItemStack item : player.inventory.mainInventory) {
			if (!item.isEmpty() && item.getItem() instanceof ReagentBag) {
				ret.add(item);
			}
		}
		for (ItemStack item : player.getEquipmentAndArmor()) {
			if (!item.isEmpty() && item.getItem() instanceof ReagentBag) {
				ret.add(item);
			}
		}
		
		IInventory curios = NostrumMagica.instance.curios.getCurios(player);
		if (curios != null) {
			for (int i = 0; i < curios.getSizeInventory(); i++) {
				ItemStack equip = curios.getStackInSlot(i);
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
	
	public static final List<ItemStack> FindRuneBags(PlayerEntity player) {
		List<ItemStack> ret = new ArrayList<>();
		
		for (ItemStack item : player.inventory.mainInventory) {
			if (!item.isEmpty() && item.getItem() instanceof RuneBag) {
				ret.add(item);
			}
		}
		for (ItemStack item : player.getEquipmentAndArmor()) {
			if (!item.isEmpty() && item.getItem() instanceof RuneBag) {
				ret.add(item);
			}
		}
		
		IInventory curios = NostrumMagica.instance.curios.getCurios(player);
		if (curios != null) {
			for (int i = 0; i < curios.getSizeInventory(); i++) {
				ItemStack equip = curios.getStackInSlot(i);
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
	
	public static final void ProcessContainerItems(PlayerEntity player, Container container) {
		List<ItemStack> reagentBags = FindReagentBags(player);
		List<ItemStack> runeBags = FindRuneBags(player);
		
		for (Slot slot : container.inventorySlots) {
			if (slot.inventory != player.inventory
					&& slot.getHasStack() && !slot.getStack().isEmpty()) {
				if (slot.getStack().getItem() instanceof ReagentItem) {
					ItemStack toAdd = slot.getStack().copy();
					for (ItemStack bag : reagentBags) {
						toAdd = ReagentBag.addItem(bag, toAdd);
						if (toAdd.isEmpty()) {
							break;
						}
					}
					slot.putStack(toAdd);
				} else if (slot.getStack().getItem() instanceof SpellRune) {
					ItemStack toAdd = slot.getStack().copy();
					for (ItemStack bag : runeBags) {
						toAdd = RuneBag.addItem(bag, toAdd);
						if (toAdd.isEmpty()) {
							break;
						}
					}
					slot.putStack(toAdd);
				}
			}
		}
	}
}
