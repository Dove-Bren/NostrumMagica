package com.smanzana.nostrummagica.crafting;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.smanzana.nostrummagica.crafting.AetherCloakModificationRecipe.TransformFuncs;
import com.smanzana.nostrummagica.integration.baubles.items.ItemAetherCloak.ToggleUpgrades;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class RecipeFactoryAetherCloakToggle implements IRecipeFactory {

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
		
		ToggleUpgrades upgrade = null;
		try {
			upgrade = ToggleUpgrades.valueOf(toggleKey.toUpperCase());
		} catch (Exception e) {
			throw new JsonParseException("Could not find Toggleable upgrade with key [" + toggleKey + "]");
		}
		
		ItemStack displayStack = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "display"), context);
		if (displayStack == null || displayStack.isEmpty()) {
			throw new JsonParseException("\"display\" section is required and must be a valid itemstack (not ingredient)");
		}
		
		final ToggleUpgrades upgradeFinal = upgrade;
		return new AetherCloakToggleRecipe(group.isEmpty() ? null : new ResourceLocation(group), displayStack, ingredients, new TransformFuncs() {
			@Override
			public boolean isAlreadySet(ItemStack cloak, NonNullList<ItemStack> extras) {
				return upgradeFinal.getFunc().isSet(cloak);
			}

			@Override
			public ItemStack transform(ItemStack cloak, NonNullList<ItemStack> extras) {
				// Don't have to care about extras and can just toggle.
				cloak = cloak.copy();
				upgradeFinal.getFunc().toggle(cloak);
				return cloak;
			}
			
		});
	}

}
