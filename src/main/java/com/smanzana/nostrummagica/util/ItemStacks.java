package com.smanzana.nostrummagica.util;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class ItemStacks {

	public static final boolean stacksMatch(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
		if (stack1.isEmpty() || stack2.isEmpty()) {
			return stack1.isEmpty() && stack2.isEmpty();
		}
		
		if (stack1.getItem() == stack2.getItem()
				&& Objects.equals(stack1.getTag(), stack2.getTag())) {
			return true;
		}
		
		return false;
	}
	
	public static final <T extends LivingEntity> ItemStack damageItem(@Nonnull ItemStack stack, T entity, Hand hand, int damage) {
		stack.hurtAndBreak(damage, entity, playerIn -> playerIn.broadcastBreakEvent(hand));
		return stack.isEmpty() ? ItemStack.EMPTY : stack;
	}
	
	public static final <T extends LivingEntity> ItemStack damageEquippedArmor(@Nonnull ItemStack stack, T entity, EquipmentSlotType slot, int damage) {
		stack.hurtAndBreak(damage, entity, playerIn -> playerIn.broadcastBreakEvent(slot));
		return stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

}
