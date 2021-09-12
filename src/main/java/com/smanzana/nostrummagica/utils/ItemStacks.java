package com.smanzana.nostrummagica.utils;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemStacks {

	public static final boolean stacksMatch(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
		if (stack1.isEmpty() || stack2.isEmpty()) {
			return stack1.isEmpty() && stack2.isEmpty();
		}
		
		if (stack1.getItem() == stack2.getItem()
				&& Objects.equals(stack1.getTagCompound(), stack2.getTagCompound())) {
			if (stack1.getMetadata() == OreDictionary.WILDCARD_VALUE || stack2.getMetadata() == OreDictionary.WILDCARD_VALUE) {
				return true;
			}
			
			return stack1.getMetadata() == stack2.getMetadata(); 
		}
		
		return false;
	}

}
