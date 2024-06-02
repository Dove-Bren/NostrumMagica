package com.smanzana.nostrummagica.crafting;

import com.smanzana.nostrummagica.item.SpellTomePage;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class SpellTomePageCombineRecipe extends SpecialRecipe {

	public static final String SERIALIZER_ID = "manual_recipe_spelltomepagecombine";
	
	public SpellTomePageCombineRecipe(ResourceLocation ID) {
		super(ID);
	}
	
	@Override
	public SpecialRecipeSerializer<SpellTomePageCombineRecipe> getSerializer() {
		return NostrumCrafting.spellTomePageCombineSerializer;
	}
	
	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		SpellTomeEnhancement enhancement = null;
		int count = 0;
		
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			
			if (!(stack.getItem() instanceof SpellTomePage))
				return false;
			
			SpellTomeEnhancement cur = SpellTomePage.getEnhancement(stack);
			if (cur == null)
				return false;
			
			if (enhancement != null && enhancement != cur)
				return false;
			
			int level = SpellTomePage.getLevel(stack);
			if (0 == level)
				return false;
			
			if (level >= cur.getMaxLevel())
				return false;
			
			enhancement = cur;
			count++;
		}
		
		return count == 2;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		SpellTomeEnhancement enhancement = null;
		int sum = 0;
		int count = 0;
		
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			
			if (!(stack.getItem() instanceof SpellTomePage))
				return ItemStack.EMPTY;
			
			SpellTomeEnhancement cur = SpellTomePage.getEnhancement(stack);
			if (cur == null)
				return ItemStack.EMPTY;
			
			if (enhancement != null && enhancement != cur)
				return ItemStack.EMPTY;
			
			int level = SpellTomePage.getLevel(stack);
			if (0 == level)
				return ItemStack.EMPTY;
			
			// Optimization to disallow adding when you are already max level
			if (level >= cur.getMaxLevel())
				return ItemStack.EMPTY;
			
			enhancement = cur;
			sum += level;
			count++;
		}
		
		if (count != 2 || enhancement == null)
			return ItemStack.EMPTY;
		
		// Check level
		int level = 1 + ((int) sum / 2); // f(x) = 1 + floor(x/2)
		if (level > enhancement.getMaxLevel())
			return ItemStack.EMPTY;
		
		return SpellTomePage.Create(enhancement, level);
	}

	@Override
	public ItemStack getRecipeOutput() {
		return SpellTomePage.Create(SpellTomeEnhancement.EFFICIENCY, 1);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
		return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}
	
}