package com.smanzana.nostrummagica.item;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.item.ItemStack;

/**
 * Item can be enchanted by an Elemental Crystal block 
 */
public interface ICrystalEnchantableItem {
	
	public static final class Result {
		public final boolean success;
		public final @Nonnull ItemStack resultItem;
		
		public Result(boolean success, @Nonnull ItemStack resultItem) {
			this.success = success;
			this.resultItem = resultItem;
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

	public @Nonnull Result attemptEnchant(ItemStack stack, EMagicElement element);
	
}
