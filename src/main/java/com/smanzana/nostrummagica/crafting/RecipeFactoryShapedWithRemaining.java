package com.smanzana.nostrummagica.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipeFactoryShapedWithRemaining implements IRecipeFactory {

	private static final class ShapedOreRecipeWrapper extends IForgeRegistryEntry.Impl<IRecipe> implements IShapedRecipe {

		private final ShapedOreRecipe inner;
		private final NonNullList<Ingredient> remaining;
		
		public ShapedOreRecipeWrapper(ShapedOreRecipe original, NonNullList<Ingredient> remaining) {
			inner = original;
			this.remaining = remaining;
			
			if (remaining == null || remaining.isEmpty()) {
				throw new JsonParseException("Remaining items must be provided");
			}
		}
		
		@Override
		public boolean matches(InventoryCrafting inv, World worldIn) {
			return inner.matches(inv, worldIn);
		}

		@Override
		public ItemStack getCraftingResult(InventoryCrafting inv) {
			return inner.getCraftingResult(inv);
		}

		@Override
		public boolean canFit(int width, int height) {
			return inner.canFit(width, height);
		}

		@Override
		public ItemStack getRecipeOutput() {
			return inner.getRecipeOutput();
		}

		@Override
		public int getRecipeWidth() {
			return inner.getRecipeWidth();
		}

		@Override
		public int getRecipeHeight() {
			return inner.getRecipeHeight();
		}
		
		@Override
		public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
			NonNullList<ItemStack> list = inner.getRemainingItems(inv);
			if (list == null) {
				list = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
			}
			
			// Fill with any we find, too.
			// Replace items that have overlap
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				if (inv.getStackInSlot(i).isEmpty()) {
					continue;
				}
				
				for (Ingredient ingredientToLeave : remaining) {
					if (ingredientToLeave.apply(inv.getStackInSlot(i))) {
						list.set(i, inv.getStackInSlot(i)); // Stamp it back in there
					}
				}
			}
			
			return list;
		}
		
		@Override
		public NonNullList<Ingredient> getIngredients() {
			return inner.getIngredients();
		}
		
		@Override
		public boolean isDynamic() {
			return inner.isDynamic();
		}
		
		@Override
		public String getGroup() {
			return inner.getGroup();
		}
		
	}
	
	@Override
	public IRecipe parse(JsonContext context, JsonObject json) {
		ShapedOreRecipe original = ShapedOreRecipe.factory(context, json);
		
		NonNullList<Ingredient> remaining = NonNullList.create();
		for (JsonElement ele : JsonUtils.getJsonArray(json, "remaining")) {
			remaining.add(CraftingHelper.getIngredient(ele, context));
		}
		
		return new ShapedOreRecipeWrapper(original, remaining);
	}

}
