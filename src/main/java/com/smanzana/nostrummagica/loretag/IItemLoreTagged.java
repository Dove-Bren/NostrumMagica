package com.smanzana.nostrummagica.loretag;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Subclass of ILoreTagged for blocks that can't be actually set up as ILoreTagged.
 * @author Skyler
 *
 */
public interface IItemLoreTagged extends ILoreTagged {
	
	public Item getItem();
	
	public default ItemStack makeStack() {
		return new ItemStack(getItem());
	}
}
