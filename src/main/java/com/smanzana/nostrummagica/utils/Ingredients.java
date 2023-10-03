package com.smanzana.nostrummagica.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IngredientNBT;

public class Ingredients {

	protected static class NBTIngredientWrapper extends IngredientNBT {
		public NBTIngredientWrapper(ItemStack stack) {
			super(stack);
		}
	}
	
	public static Ingredient MatchNBT(ItemStack stack) {
		return new NBTIngredientWrapper(stack);
	}
	
}
