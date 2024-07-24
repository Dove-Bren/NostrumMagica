package com.smanzana.nostrummagica.inventory;

import net.minecraft.item.ItemStack;

public interface IInventorySlotKey<T> {

	public ItemStack getHeldStack(T inv);
	
	public ItemStack setStack(T inv, ItemStack newStack);
	
	@Override
	public boolean equals(Object o);
	
	@Override
	public int hashCode();
	
}
