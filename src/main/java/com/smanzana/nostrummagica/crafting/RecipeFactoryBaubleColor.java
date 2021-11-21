package com.smanzana.nostrummagica.crafting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble.ItemType;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RecipeFactoryBaubleColor implements IRecipeFactory {

	private static final class BaubleColorRecipe extends ShapelessOreRecipe {
		
		private final NonNullList<Ingredient> ingredients;
		private final @Nonnull ItemStack displayStack;

		public BaubleColorRecipe(ResourceLocation group, @Nonnull ItemStack displayStack, NonNullList<Ingredient> ingredients) {
			super(group, ingredients, ItemStack.EMPTY);
			
			if (ingredients == null || ingredients.isEmpty()) {
				throw new JsonParseException("ingredients items must be provided and contain a magic bauble");
			}
			
			boolean found = false;
			for (Ingredient ing : ingredients) {
				for (ItemMagicBauble.ItemType type : ItemType.values()) {
					final ItemStack bauble = ItemMagicBauble.getItem(type, 1);
					if (ing.apply(bauble)) {
						found = true;
						break;
					}
				}
			}
			
			if (!found) {
				throw new JsonParseException("At least one ingredient must allow a blank magic bauble");
			}
			
			if (displayStack == null || displayStack.isEmpty() || !(displayStack.getItem() instanceof ItemMagicBauble)) {
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
		protected @Nonnull ItemStack findBauble(InventoryCrafting inv) {
			@Nonnull ItemStack found = ItemStack.EMPTY;
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				@Nonnull ItemStack stack = inv.getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() instanceof ItemMagicBauble) {
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
		public ItemStack getCraftingResult(InventoryCrafting inv) {
			@Nonnull ItemStack result = ItemStack.EMPTY;
			@Nonnull ItemStack bauble = findBauble(inv);
			
			if (!bauble.isEmpty()) {
				NonNullList<ItemStack> extras = NonNullList.create();
				
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					@Nonnull ItemStack stack = inv.getStackInSlot(i);
					if (!stack.isEmpty() && stack != bauble) {
						extras.add(stack);
					}
				}
				
				EMagicElement current = ((ItemMagicBauble) bauble.getItem()).getEmbeddedElement(bauble);
				EMagicElement fromIng = findElement(extras);
				
				if (fromIng != null && current != fromIng) {
					result = bauble.copy();
					((ItemMagicBauble) result.getItem()).setEmbeddedElement(result, fromIng);
				}
			}
			
			return result;
		}

		@Override
		public boolean canFit(int width, int height) {
			return width * height >= ingredients.size();
		}

		@Override
		public ItemStack getRecipeOutput() {
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
	}
	
	@Override
	public IRecipe parse(JsonContext context, JsonObject json) {
		if (JsonUtils.hasField(json, "result")) {
			throw new JsonParseException("BaubleColor recipe cannot specify a result");
		}
		
		String group = JsonUtils.getString(json, "group", "");

		NonNullList<Ingredient> ingredients = NonNullList.create();
		for (JsonElement ele : JsonUtils.getJsonArray(json, "ingredients")) {
			ingredients.add(CraftingHelper.getIngredient(ele, context));
		}

		if (ingredients.isEmpty()) {
			throw new JsonParseException("No ingredients for BaubleColor recipe");
		}
		
		ItemStack displayStack = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "display"), context);
		if (displayStack == null || displayStack.isEmpty()) {
			throw new JsonParseException("\"display\" section is required and must be a valid itemstack (not ingredient)");
		}
		
		return new BaubleColorRecipe(group.isEmpty() ? null : new ResourceLocation(group), displayStack, ingredients);
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

}
