package com.smanzana.nostrummagica.utils;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class ItemStacks {

	public static final boolean stacksMatch(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
		if (stack1.isEmpty() || stack2.isEmpty()) {
			return stack1.isEmpty() && stack2.isEmpty();
		}
		
		if (stack1.getItem() == stack2.getItem()
				&& Objects.equals(stack1.getTag(), stack2.getTag())) {
			if (stack1.getMetadata() == OreDictionary.WILDCARD_VALUE || stack2.getMetadata() == OreDictionary.WILDCARD_VALUE) {
				return true;
			}
			
			return stack1.getMetadata() == stack2.getMetadata(); 
		}
		
		return false;
	}
	
	public static final <T extends LivingEntity> ItemStack damageItem(@Nonnull ItemStack stack, T entity, Hand hand, int damage) {
		stack.damageItem(damage, entity, playerIn -> playerIn.sendBreakAnimation(hand));
		return stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

}
