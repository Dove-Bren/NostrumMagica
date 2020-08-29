package com.smanzana.nostrummagica.utils;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

public class ItemStacks {

	public static final boolean stacksMatch(@Nullable ItemStack stack1, @Nullable ItemStack stack2) {
		if (stack1 == null || stack2 == null) {
			return stack1 == null && stack2 == null;
		}
		
		return stack1.getItem() == stack2.getItem()
	    		&& stack1.getMetadata() == stack2.getMetadata()
	    		&& Objects.equals(stack1.getTagCompound(), stack2.getTagCompound());
	}

}
