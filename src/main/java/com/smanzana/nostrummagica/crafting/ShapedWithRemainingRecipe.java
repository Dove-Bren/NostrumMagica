package com.smanzana.nostrummagica.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class ShapedWithRemainingRecipe extends ShapedRecipe {
	
	private final ShapedRecipe inner;
	private final NonNullList<Ingredient> remaining;
	
	public ShapedWithRemainingRecipe(ShapedRecipe original, NonNullList<Ingredient> remaining) {
		// ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn
		super(new ResourceLocation(original.getId().getNamespace(), original.getId().getPath()), original.getGroup(),
				original.getRecipeWidth(), original.getRecipeHeight(), original.getIngredients(),
				original.getRecipeOutput());
		inner = original;
		this.remaining = remaining;
		
		if (remaining == null || remaining.isEmpty()) {
			throw new JsonParseException("Remaining items must be provided");
		}
	}
	
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
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
				if (ingredientToLeave.test(inv.getStackInSlot(i))) {
					list.set(i, inv.getStackInSlot(i).copy()); // Stamp it back in there
					break;
				}
			}
		}
		
		return list;
	}
	
	@Override
	public IRecipeSerializer<ShapedWithRemainingRecipe> getSerializer() {
		return NostrumCrafting.shapedWithRemainingSerializer;
	}
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>>  implements IRecipeSerializer<ShapedWithRemainingRecipe> {

		public static final String ID = "shaped_with_remaining";
		
		@Override
		public ShapedWithRemainingRecipe read(ResourceLocation recipeId, JsonObject json) {
			ShapedRecipe original = IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, json);
			
			NonNullList<Ingredient> remaining = NonNullList.create();
			for (JsonElement ele : JSONUtils.getJsonArray(json, "remaining")) {
				remaining.add(Ingredient.deserialize(ele));
			}
			
			return new ShapedWithRemainingRecipe(original, remaining);
		}

		@Override
		public ShapedWithRemainingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			ShapedRecipe original = IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, buffer);
			
			int remainingSize = buffer.readInt();
			NonNullList<Ingredient> remaining = NonNullList.withSize(remainingSize, Ingredient.EMPTY);
			for (int i = 0; i < remainingSize; i++) {
				remaining.set(i, Ingredient.read(buffer));
			}
			
			return new ShapedWithRemainingRecipe(original, remaining);
		}

		@Override
		public void write(PacketBuffer buffer, ShapedWithRemainingRecipe recipe) {
			IRecipeSerializer.CRAFTING_SHAPED.write(buffer, recipe);
			
			// Write size and then list of remaining items
			buffer.writeInt(recipe.remaining.size());
			for (Ingredient remaining : recipe.remaining) {
				remaining.write(buffer);
			}
		}

	}
	
}
