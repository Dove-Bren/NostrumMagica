package com.smanzana.nostrummagica.utils;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemStacks {

	public static final boolean stacksMatch(@Nullable ItemStack stack1, @Nullable ItemStack stack2) {
		if (stack1 == null || stack2 == null) {
			return stack1 == null && stack2 == null;
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
