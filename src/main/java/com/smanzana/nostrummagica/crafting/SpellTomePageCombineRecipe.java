package com.smanzana.nostrummagica.crafting;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.SpellTomePage;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class SpellTomePageCombineRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	protected ResourceLocation registryName;
	
	public SpellTomePageCombineRecipe() {
		//RecipeSorter.register("SpellTomeUpgradeRecipe", SpellTomePageCombineRecipe.class, Category.SHAPELESS, "");
		
		this.setRegistryName(new ResourceLocation(NostrumMagica.MODID, "manual_recipe_spelltomepagecombine"));
	}
	
	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
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
	public ItemStack getCraftingResult(InventoryCrafting inv) {
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
		
		return SpellTomePage.getItemstack(enhancement, level);
	}

	@Override
	public ItemStack getRecipeOutput() {
		return SpellTomePage.getItemstack(SpellTomeEnhancement.EFFICIENCY, 1);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}
	
}