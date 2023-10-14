package com.smanzana.nostrummagica.crafting;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.smanzana.nostrummagica.integration.curios.items.AetherCloakItem.ToggleUpgrades;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class AetherCloakToggleRecipe extends AetherCloakModificationRecipe {
	
	private final ToggleUpgrades upgradeType;

	public AetherCloakToggleRecipe(ResourceLocation id, String group, @Nonnull ItemStack display, NonNullList<Ingredient> ingredients, ToggleUpgrades upgradeType) {
		super(id, group, display, ingredients, makeTransformFunc(upgradeType));
		this.upgradeType = upgradeType;
	}
	
	protected static TransformFuncs makeTransformFunc(ToggleUpgrades upgrade) {
		return new TransformFuncs() {
			@Override
			public boolean isAlreadySet(ItemStack cloak, NonNullList<ItemStack> extras) {
				return upgrade.getFunc().isSet(cloak);
			}

			@Override
			public ItemStack transform(ItemStack cloak, NonNullList<ItemStack> extras) {
				// Don't have to care about extras and can just toggle.
				cloak = cloak.copy();
				upgrade.getFunc().toggle(cloak);
				return cloak;
			}
		};
	}
	
	public ToggleUpgrades getUpgradeType() {
		return this.upgradeType;
	}
	
	@Override
	public IRecipeSerializer<AetherCloakToggleRecipe> getSerializer() {
		return NostrumCrafting.aetherCloakToggleSerializer;
	}
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>>  implements IRecipeSerializer<AetherCloakToggleRecipe> {
		
		public static final String ID = "aether_cloak_toggle";
		
		@Override
		public AetherCloakToggleRecipe read(ResourceLocation recipeId, JsonObject json) {
			if (JSONUtils.hasField(json, "result")) {
				throw new JsonParseException("AetherCloak recipe cannot specify a result");
			}
			
			String group = JSONUtils.getString(json, "group", "");

			NonNullList<Ingredient> ingredients = NonNullList.create();
			for (JsonElement ele : JSONUtils.getJsonArray(json, "ingredients")) {
				ingredients.add(Ingredient.deserialize(ele));
			}

			if (ingredients.isEmpty()) {
				throw new JsonParseException("No ingredients for aether cloak recipe");
			}
			
			String toggleKey = JSONUtils.getString(json, "key", "");
			if (toggleKey.isEmpty()) {
				throw new JsonParseException("Must provide a 'key' field");
			}
			
			ToggleUpgrades upgrade = null;
			try {
				upgrade = ToggleUpgrades.valueOf(toggleKey.toUpperCase());
			} catch (Exception e) {
				throw new JsonParseException("Could not find Toggleable upgrade with key [" + toggleKey + "]");
			}
			
			ItemStack displayStack = CraftingHelper.getItemStack(JSONUtils.getJsonObject(json, "display"), true);
			if (displayStack == null || displayStack.isEmpty()) {
				throw new JsonParseException("\"display\" section is required and must be a valid itemstack (not ingredient)");
			}
			
			return new AetherCloakToggleRecipe(recipeId, group, displayStack, ingredients, upgrade);
		}

		@Override
		public AetherCloakToggleRecipe read(ResourceLocation recipeId,
				PacketBuffer buffer) {
			// I think I can just piggy back off of shaped recipe serializer
			ResourceLocation fakeRecipeId = new ResourceLocation(recipeId.getNamespace(), recipeId.getPath() + "#FAKE");
			ShapelessRecipe base = IRecipeSerializer.CRAFTING_SHAPELESS.read(fakeRecipeId, buffer);
			
			// Also read toggle func key
			String toggleKey = buffer.readString();
			ToggleUpgrades upgrade = null;
			try {
				upgrade = ToggleUpgrades.valueOf(toggleKey.toUpperCase());
			} catch (Exception e) {
				throw new JsonParseException("Could not find Toggleable upgrade with key [" + toggleKey + "]");
			}
			
			return new AetherCloakToggleRecipe(recipeId, base.getGroup(), base.getRecipeOutput(), base.getIngredients(), upgrade);
		}

		@Override
		public void write(PacketBuffer buffer, AetherCloakToggleRecipe recipe) {
			IRecipeSerializer.CRAFTING_SHAPELESS.write(buffer, recipe);
			buffer.writeString(recipe.getUpgradeType().name());
		}
	}
}
	
