package com.smanzana.nostrummagica.crafting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.crafting.AetherCloakModificationRecipe.TransformFuncs;
import com.smanzana.nostrummagica.integration.baubles.items.ItemAetherCloak.ColorUpgrades;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class RecipeFactoryAetherCloakColor implements IRecipeFactory {

	private static final class AetherCloakToggleRecipe extends AetherCloakModificationRecipe {

		public AetherCloakToggleRecipe(ResourceLocation group, @Nonnull ItemStack display, NonNullList<Ingredient> ingredients, TransformFuncs func) {
			super(group, display, ingredients, func);
		}
	}
	
	@Override
	public IRecipe parse(JsonContext context, JsonObject json) {
		if (JsonUtils.hasField(json, "result")) {
			throw new JsonParseException("AetherCloak recipe cannot specify a result");
		}
		
		String group = JsonUtils.getString(json, "group", "");

		NonNullList<Ingredient> ingredients = NonNullList.create();
		for (JsonElement ele : JsonUtils.getJsonArray(json, "ingredients")) {
			ingredients.add(CraftingHelper.getIngredient(ele, context));
		}

		if (ingredients.isEmpty()) {
			throw new JsonParseException("No ingredients for aether cloak recipe");
		}
		
		String toggleKey = JsonUtils.getString(json, "key", "");
		if (toggleKey.isEmpty()) {
			throw new JsonParseException("Must provide a 'key' field");
		}
		
		ColorUpgrades upgrade = null;
		try {
			upgrade = ColorUpgrades.valueOf(toggleKey.toUpperCase());
		} catch (Exception e) {
			throw new JsonParseException("Could not find Color upgrade with key [" + toggleKey + "]");
		}
		
		ItemStack displayStack = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "display"), context);
		if (displayStack == null || displayStack.isEmpty()) {
			throw new JsonParseException("\"display\" section is required and must be a valid itemstack (not ingredient)");
		}
		
		final ColorUpgrades upgradeFinal = upgrade;
		return new AetherCloakToggleRecipe(group.isEmpty() ? null : new ResourceLocation(group), displayStack, ingredients, new TransformFuncs() {
			@Override
			public boolean isAlreadySet(ItemStack cloak, NonNullList<ItemStack> extras) {
				EnumDyeColor color = findColor(extras);
				return color == null || upgradeFinal.getFunc().isSet(cloak, color);
			}

			@Override
			public ItemStack transform(ItemStack cloak, NonNullList<ItemStack> extras) {
				EnumDyeColor color = findColor(extras);
				
				if (color == null) {
					NostrumMagica.logger.error("Asked to change color on Aether Cloak with no consistent color ingredients!");
					color = EnumDyeColor.RED;
				}
				
				cloak = cloak.copy();
				upgradeFinal.getFunc().set(cloak, color);
				return cloak;
			}
			
		});
	}
	
	protected @Nullable EnumDyeColor findColor(@Nonnull ItemStack stack) {
		@Nullable EnumDyeColor color = null;
		
		if (!stack.isEmpty()) {
			if (stack.getItem() instanceof ItemDye) {
				color = EnumDyeColor.byDyeDamage(stack.getMetadata());
			} // else if...
		}
		
		return color;
	}
	
	/**
	 * Searches the passed in stacks for a consistent color.
	 * If multiple items with color are present, returns null.
	 * If no items with color are present, also returns null.
	 * @param items
	 * @return
	 */
	protected @Nullable EnumDyeColor findColor(NonNullList<ItemStack> items) {
		@Nullable EnumDyeColor color = null;
		
		for (ItemStack item : items) {
			EnumDyeColor indColor = findColor(item);
			if (indColor != null) {
				if (color == null) {
					color = indColor;
				} else if (color != indColor) {
					color = null;
					break;
				}
			}
		}
		
		return color;
	}

}
