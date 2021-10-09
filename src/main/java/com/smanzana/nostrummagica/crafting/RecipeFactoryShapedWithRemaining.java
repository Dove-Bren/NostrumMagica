package com.smanzana.nostrummagica.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeFactoryShapedWithRemaining implements IRecipeFactory {

	private static final class ShapedOreRecipeWrapper extends ShapedOreRecipe {

		private final ShapedOreRecipe inner;
		private final NonNullList<Ingredient> remaining;
		
		public ShapedOreRecipeWrapper(ShapedOreRecipe original, NonNullList<Ingredient> remaining) {
			super(new ResourceLocation(original.getGroup()), original.getRecipeOutput(), makeFakePrimer(original));
			inner = original;
			this.remaining = remaining;
			
			if (remaining == null || remaining.isEmpty()) {
				throw new JsonParseException("Remaining items must be provided");
			}
		}
		
		protected static ShapedPrimer makeFakePrimer(ShapedOreRecipe original) {
			ShapedPrimer primer = new ShapedPrimer();
			primer.width = original.getRecipeWidth();
			primer.height = original.getRecipeHeight();
			primer.mirrored = false;
			primer.input = original.getIngredients();
			return primer;
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
						list.set(i, inv.getStackInSlot(i).copy()); // Stamp it back in there
						break;
					}
				}
			}
			
			return list;
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
