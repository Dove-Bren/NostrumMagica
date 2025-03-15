package com.smanzana.nostrummagica.crafting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface ISpellCraftingInventory extends IInventory {

	public int getScrollSlotIndex();
	
	public int getRuneSlotStartingIndex();
	
	public int getRuneSlotCount();
	
	public default ItemStack getScrollSlotContents() {
		return this.getItem(getScrollSlotIndex());
	}
	
	public default ItemStack getRuneSlotContents(int runeIndex) {
		return this.getItem(runeIndex + this.getRuneSlotStartingIndex());
	}
	
	public default void setScrollSlotContents(ItemStack newStack) {
		this.setItem(getScrollSlotIndex(), newStack);
	}
	
	public default void setRuneSlotContents(int runeIndex, ItemStack newStack) {
		this.setItem(getRuneSlotStartingIndex() + runeIndex, newStack);
	}
	
	public default void clearRunes() {
		for (int i = 0; i < this.getRuneSlotCount(); i++) {
			this.setItem(i + this.getRuneSlotStartingIndex(), ItemStack.EMPTY);
		}
	}
	
	public default void clearSpellBoard() {
		this.clearRunes();
		this.setScrollSlotContents(ItemStack.EMPTY);
	}
	
	public int getMaxWeight(PlayerEntity crafter);
	
}
