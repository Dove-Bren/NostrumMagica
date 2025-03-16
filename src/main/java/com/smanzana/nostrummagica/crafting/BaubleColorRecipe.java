package com.smanzana.nostrummagica.crafting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.smanzana.nostrummagica.integration.curios.items.IColorableCurio;
import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class BaubleColorRecipe extends ShapelessRecipe {
	
	private final NonNullList<Ingredient> ingredients;
	private final @Nonnull ItemStack displayStack;
	private final @Nullable String group;

	public BaubleColorRecipe(ResourceLocation id, String group, @Nonnull ItemStack displayStack, NonNullList<Ingredient> ingredients) {
		super(id, group, displayStack /*TODO this okay? ItemStack.EMPTY*/, ingredients);
		
		if (ingredients == null || ingredients.isEmpty()) {
			throw new JsonParseException("ingredients items must be provided and contain a magic bauble");
		}
		
//		boolean found = false;
//		for (Ingredient ing : ingredients) {
//			for (ItemMagicBauble.ItemType type : ItemType.values()) {
//				final ItemStack bauble = ItemMagicBauble.getItem(type, 1);
//				if (ing.test(bauble)) {
//					found = true;
//					break;
//				}
//			}
//		}
//		
//		if (!found) {
//			throw new JsonParseException("At least one ingredient must allow a blank magic bauble");
//		}
		
		if (displayStack == null || displayStack.isEmpty() || !(displayStack.getItem() instanceof IColorableCurio)) {
			throw new JsonParseException("Display item must be a magic bauble");
		}
		
		this.ingredients = ingredients;
		this.group = group;
		this.displayStack = displayStack;
	}
	
	/**
	 * Will match one and only one cloak. If there are multiple, will return an .isEmpty itemstack.
	 * @param inv
	 * @return
	 */
	protected @Nonnull ItemStack findBauble(CraftingContainer inv) {
		@Nonnull ItemStack found = ItemStack.EMPTY;
		for (int i = 0; i < inv.getContainerSize(); i++) {
			@Nonnull ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty() && stack.getItem() instanceof IColorableCurio) {
				if (found.isEmpty()) {
					found = stack;
				} else {
					found = ItemStack.EMPTY; // Found a second! Fail out!
					break;
				}
			}
		}
		
		return found;
	}
	
	@Override
	public ItemStack assemble(CraftingContainer inv) {
		@Nonnull ItemStack result = ItemStack.EMPTY;
		@Nonnull ItemStack bauble = findBauble(inv);
		
		if (!bauble.isEmpty()) {
			NonNullList<ItemStack> extras = NonNullList.create();
			
			for (int i = 0; i < inv.getContainerSize(); i++) {
				@Nonnull ItemStack stack = inv.getItem(i);
				if (!stack.isEmpty() && stack != bauble) {
					extras.add(stack);
				}
			}
			
			EMagicElement current = ((IColorableCurio) bauble.getItem()).getEmbeddedElement(bauble);
			EMagicElement fromIng = findElement(extras);
			
			if (fromIng != null && current != fromIng) {
				result = bauble.copy();
				((IColorableCurio) result.getItem()).setEmbeddedElement(result, fromIng);
			}
		}
		
		return result;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= ingredients.size();
	}

	@Override
	public ItemStack getResultItem() {
		return displayStack;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}
	
	@Override
	public String getGroup() {
		return group == null ? "" : group.toString();
	}
	
	protected static @Nullable EMagicElement findElement(@Nonnull ItemStack stack) {
		@Nullable EMagicElement element = null;
		
		if (!stack.isEmpty()) {
			if (stack.getItem() instanceof EssenceItem) {
				element = EssenceItem.findType(stack);
			} // else if...
		}
		
		return element;
	}
	
	/**
	 * Searches the passed in stacks for a consistent color.
	 * If multiple items with color are present, returns null.
	 * If no items with color are present, also returns null.
	 * @param items
	 * @return
	 */
	protected static @Nullable EMagicElement findElement(NonNullList<ItemStack> items) {
		@Nullable EMagicElement element = null;
		
		for (ItemStack item : items) {
			EMagicElement indElement = findElement(item);
			if (indElement != null) {
				if (element == null) {
					element = indElement;
				} else if (element != indElement) {
					element = null;
					break;
				}
			}
		}
		
		return element;
	}
	
	@Override
	public RecipeSerializer<BaubleColorRecipe> getSerializer() {
		return NostrumCrafting.baubleColorSerializer;
	}
	
	public static final class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>  implements RecipeSerializer<BaubleColorRecipe> {

		public static final String ID = "bauble_color";
		
		@Override
		public BaubleColorRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			if (GsonHelper.isValidNode(json, "result")) {
				throw new JsonParseException("BaubleColor recipe cannot specify a result");
			}
			
			String group = GsonHelper.getAsString(json, "group", "");

			NonNullList<Ingredient> ingredients = NonNullList.create();
			for (JsonElement ele : GsonHelper.getAsJsonArray(json, "ingredients")) {
				ingredients.add(Ingredient.fromJson(ele));
			}

			if (ingredients.isEmpty()) {
				throw new JsonParseException("No ingredients for BaubleColor recipe");
			}
			
			ItemStack displayStack = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "display"), true);
			if (displayStack == null || displayStack.isEmpty()) {
				throw new JsonParseException("\"display\" section is required and must be a valid itemstack (not ingredient)");
			}
			
			return new BaubleColorRecipe(recipeId, group, displayStack, ingredients);
		}

		@Override
		public BaubleColorRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			// I think I can just piggy back off of shapeless recipe serializer
			ResourceLocation fakeRecipeId = new ResourceLocation(recipeId.getNamespace(), recipeId.getPath() + ".fake");
			ShapelessRecipe base = RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(fakeRecipeId, buffer);
			
			return new BaubleColorRecipe(recipeId, base.getGroup(), base.getResultItem(), base.getIngredients());
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, BaubleColorRecipe recipe) {
			RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
		}
		
	}
}


