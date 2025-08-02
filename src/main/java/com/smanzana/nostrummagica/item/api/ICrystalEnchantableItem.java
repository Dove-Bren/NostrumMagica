package com.smanzana.nostrummagica.item.api;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.ElementalCrystalBlockEntity;

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
	
	public static boolean isEnchantable(ItemStack stack) {
		final var enchantability = ElementalCrystalBlockEntity.GetEnchantability(stack);
		return enchantability != null && enchantability.canEnchant(stack, null);
	}
	
	/**
	 * Return whether this itemstack is generally enchantable.
	 * Used for tooltips, and should check any restrictions on the type based on stored element, etc.
	 * @param stack
	 * @param element TODO
	 * @return
	 */
	public boolean canEnchant(ItemStack stack, EMagicElement element);

	public @Nonnull Result attemptEnchant(ItemStack stack, EMagicElement element);
	
}
