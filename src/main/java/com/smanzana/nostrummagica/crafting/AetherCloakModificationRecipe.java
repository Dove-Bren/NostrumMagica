package com.smanzana.nostrummagica.crafting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonParseException;
import com.smanzana.nostrummagica.integration.baubles.items.ItemAetherCloak;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public abstract class AetherCloakModificationRecipe extends ShapelessRecipe {
	
	protected static interface TransformFuncs {
		public boolean isAlreadySet(@Nonnull ItemStack cloak, NonNullList<ItemStack> extras);
		public @Nonnull ItemStack transform(@Nonnull ItemStack cloak, NonNullList<ItemStack> extras);
	}

	private final NonNullList<Ingredient> ingredients;
	private final TransformFuncs func;
	private @Nullable final String group;
	private final @Nonnull ItemStack displayStack;
	
	public AetherCloakModificationRecipe(ResourceLocation id, String group, @Nonnull ItemStack displayStack, NonNullList<Ingredient> ingredients, TransformFuncs func) {
		super(id, group, displayStack /* TODO this ok? ItemStack.EMPTY*/, ingredients);
		
		if (ingredients == null || ingredients.isEmpty()) {
			throw new JsonParseException("ingredients items must be provided and contain at least an Aether Cloak");
		}
		
		final ItemStack cloak = new ItemStack(ItemAetherCloak.instance());
		boolean found = false;
		for (Ingredient ing : ingredients) {
			if (ing.test(cloak)) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			throw new JsonParseException("At least one ingredient must allow a blank Aether Cloak");
		}
		
		if (displayStack == null || displayStack.isEmpty() || !(displayStack.getItem() instanceof ItemAetherCloak)) {
			throw new JsonParseException("Display item must be an aether cloak");
		}
		
		this.ingredients = ingredients;
		this.func = func;
		this.group = group;
		this.displayStack = displayStack;
	}
	
	/**
	 * Will match one and only one cloak. If there are multiple, will return an .isEmpty itemstack.
	 * @param inv
	 * @return
	 */
	protected @Nonnull ItemStack findAetherCloak(CraftingInventory inv) {
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
	public ItemStack getCraftingResult(CraftingInventory inv) {
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
	
	@Override
	public abstract IRecipeSerializer<?> getSerializer();
	
}