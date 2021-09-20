package com.smanzana.nostrummagica.crafting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonParseException;
import com.smanzana.nostrummagica.integration.baubles.items.ItemAetherCloak;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public abstract class AetherCloakModificationRecipe extends ShapelessOreRecipe {
	
	protected static interface TransformFuncs {
		public boolean isAlreadySet(@Nonnull ItemStack cloak, NonNullList<ItemStack> extras);
		public @Nonnull ItemStack transform(@Nonnull ItemStack cloak, NonNullList<ItemStack> extras);
	}

	private final NonNullList<Ingredient> ingredients;
	private final TransformFuncs func;
	private @Nullable final ResourceLocation group;
	
	public AetherCloakModificationRecipe(ResourceLocation group, NonNullList<Ingredient> ingredients, TransformFuncs func) {
		super(group, ingredients, ItemStack.EMPTY);
		
		if (ingredients == null || ingredients.isEmpty()) {
			throw new JsonParseException("ingredients items must be provided and contain at least an Aether Cloak");
		}
		
		final ItemStack cloak = new ItemStack(ItemAetherCloak.instance());
		boolean found = false;
		for (Ingredient ing : ingredients) {
			if (ing.apply(cloak)) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			throw new JsonParseException("At least one ingredient must allow a blank Aether Cloak");
		}
		
		this.ingredients = ingredients;
		this.func = func;
		this.group = group;
	}
	
	/**
	 * Will match one and only one cloak. If there are multiple, will return an .isEmpty itemstack.
	 * @param inv
	 * @return
	 */
	protected @Nonnull ItemStack findAetherCloak(InventoryCrafting inv) {
		@Nonnull ItemStack found = ItemStack.EMPTY;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			@Nonnull ItemStack stack = inv.getStackInSlot(i);
			if (!stack.isEmpty() && stack.getItem() instanceof ItemAetherCloak) {
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
		@Nonnull ItemStack cloak = findAetherCloak(inv);
		
		if (!cloak.isEmpty()) {
			NonNullList<ItemStack> extras = NonNullList.create();
			
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				@Nonnull ItemStack stack = inv.getStackInSlot(i);
				if (!stack.isEmpty() && stack != cloak) {
					extras.add(stack);
				}
			}
			
			if (!func.isAlreadySet(cloak, extras)) {
				result = func.transform(cloak, extras);
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
		return new ItemStack(ItemAetherCloak.instance());
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