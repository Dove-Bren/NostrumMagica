package com.smanzana.nostrummagica.utils;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.equipment.MageBlade;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.NBTIngredient;

public class Ingredients {

	protected static class NBTIngredientWrapper extends NBTIngredient {
		public NBTIngredientWrapper(ItemStack stack) {
			super(stack);
		}
	}
	
	public static Ingredient MatchNBT(ItemStack stack) {
		return new NBTIngredientWrapper(stack);
	}
	
	protected static final class MageBladeIngredientWrapper extends Ingredient {
		
		private final @Nullable EMagicElement element;
		
		private static final ItemStack makeMatchStack(EMagicElement element) {
			ItemStack stack = new ItemStack(NostrumItems.mageBlade);
			NostrumItems.mageBlade.setElement(stack, element);
			return stack;
		}

		public MageBladeIngredientWrapper(EMagicElement element) {
			super(Stream.of(new Ingredient.SingleItemList(makeMatchStack(element))));
			this.element = element;
		}
		
		@Override
		public boolean test(ItemStack stack) {
			return stack != null
					&& !stack.isEmpty()
					&& stack.getItem() instanceof MageBlade
					&& ((MageBlade) stack.getItem()).getElement(stack) == this.element;
		}
		
	}
	
	public static Ingredient MatchMageBlade(EMagicElement element) {
		return new MageBladeIngredientWrapper(element);
	}
	
}
