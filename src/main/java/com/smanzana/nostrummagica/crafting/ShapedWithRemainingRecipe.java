package com.smanzana.nostrummagica.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class ShapedWithRemainingRecipe extends ShapedRecipe {
	
	private final ShapedRecipe inner;
	private final NonNullList<Ingredient> remaining;
	
	public ShapedWithRemainingRecipe(ShapedRecipe original, NonNullList<Ingredient> remaining) {
		// ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn
		super(new ResourceLocation(original.getId().getNamespace(), original.getId().getPath()), original.getGroup(),
				original.getRecipeWidth(), original.getRecipeHeight(), original.getIngredients(),
				original.getResultItem());
		inner = original;
		this.remaining = remaining;
		
		if (remaining == null || remaining.isEmpty()) {
			throw new JsonParseException("Remaining items must be provided");
		}
	}
	
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		NonNullList<ItemStack> list = inner.getRemainingItems(inv);
		if (list == null) {
			list = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
		}
		
		// Fill with any we find, too.
		// Replace items that have overlap
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (inv.getItem(i).isEmpty()) {
				continue;
			}
			
			for (Ingredient ingredientToLeave : remaining) {
				if (ingredientToLeave.test(inv.getItem(i))) {
					list.set(i, inv.getItem(i).copy()); // Stamp it back in there
					break;
				}
			}
		}
		
		return list;
	}
	
	@Override
	public RecipeSerializer<ShapedWithRemainingRecipe> getSerializer() {
		return NostrumCrafting.shapedWithRemainingSerializer;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>  implements RecipeSerializer<ShapedWithRemainingRecipe> {

		public static final String ID = "shaped_with_remaining";
		
		@Override
		public ShapedWithRemainingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			ShapedRecipe original = RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json);
			
			NonNullList<Ingredient> remaining = NonNullList.create();
			for (JsonElement ele : GsonHelper.getAsJsonArray(json, "remaining")) {
				remaining.add(Ingredient.fromJson(ele));
			}
			
			return new ShapedWithRemainingRecipe(original, remaining);
		}

		@Override
		public ShapedWithRemainingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			ShapedRecipe original = RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer);
			
			int remainingSize = buffer.readInt();
			NonNullList<Ingredient> remaining = NonNullList.withSize(remainingSize, Ingredient.EMPTY);
			for (int i = 0; i < remainingSize; i++) {
				remaining.set(i, Ingredient.fromNetwork(buffer));
			}
			
			return new ShapedWithRemainingRecipe(original, remaining);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, ShapedWithRemainingRecipe recipe) {
			RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
			
			// Write size and then list of remaining items
			buffer.writeInt(recipe.remaining.size());
			for (Ingredient remaining : recipe.remaining) {
				remaining.toNetwork(buffer);
			}
		}

	}
	
}
