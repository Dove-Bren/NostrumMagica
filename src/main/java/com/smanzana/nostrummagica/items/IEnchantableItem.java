package com.smanzana.nostrummagica.items;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface IEnchantableItem {
	
	public static final class Result {
		public final boolean success;
		public final @Nonnull ItemStack resultItem;
		public final boolean consumeInput;
		
		public Result(boolean success, @Nonnull ItemStack resultItem, boolean consumeInput) {
			this.success = success;
			this.resultItem = resultItem;
			this.consumeInput = consumeInput;
		}
		
		public Result(boolean success, @Nonnull ItemStack resultItem) {
			this(success, resultItem, true);
		}
		
		public Result(boolean success) {
			this(success, ItemStack.EMPTY);
		}
	}
	
	/**
	 * Return whether this itemstack is generally enchantable.
	 * Used for tooltips, and should check any restrictions on the type based on stored element, etc.
	 * @param stack
	 * @return
	 */
	public boolean canEnchant(ItemStack stack);

	public @Nonnull Result attemptEnchant(ItemStack stack, LivingEntity entity, EMagicElement element, int power);
	
}
