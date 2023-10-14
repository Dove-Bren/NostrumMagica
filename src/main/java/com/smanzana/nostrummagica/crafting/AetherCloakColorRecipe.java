package com.smanzana.nostrummagica.crafting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.curios.items.AetherCloakItem.ColorUpgrades;

import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
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

public class AetherCloakColorRecipe extends AetherCloakModificationRecipe {
	
	private final ColorUpgrades upgradeType;
	
	public AetherCloakColorRecipe(ResourceLocation id, String group, @Nonnull ItemStack display, NonNullList<Ingredient> ingredients, ColorUpgrades upgradeType) {
		super(id, group, display, ingredients, makeTransformFunc(upgradeType));
		this.upgradeType = upgradeType;
	}
	
	public ColorUpgrades getUpgradeType() {
		return this.upgradeType;
	}
	
	protected static TransformFuncs makeTransformFunc(ColorUpgrades upgradeType) {
		return new TransformFuncs() {
			@Override
			public boolean isAlreadySet(ItemStack cloak, NonNullList<ItemStack> extras) {
				DyeColor color = findColor(extras);
				return color == null || upgradeType.getFunc().isSet(cloak, color);
			}

			@Override
			public ItemStack transform(ItemStack cloak, NonNullList<ItemStack> extras) {
				DyeColor color = findColor(extras);
				
				if (color == null) {
					NostrumMagica.logger.error("Asked to change color on Aether Cloak with no consistent color ingredients!");
					color = DyeColor.RED;
				}
				
				cloak = cloak.copy();
				upgradeType.getFunc().set(cloak, color);
				return cloak;
			}
			
		};
	}
	
	/**
	 * Searches the passed in stacks for a consistent color.
	 * If multiple items with color are present, returns null.
	 * If no items with color are present, also returns null.
	 * @param items
	 * @return
	 */
	protected static @Nullable DyeColor findColor(NonNullList<ItemStack> items) {
		@Nullable DyeColor color = null;
		
		for (ItemStack item : items) {
			DyeColor indColor = findColor(item);
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
	
	protected static @Nullable DyeColor findColor(@Nonnull ItemStack stack) {
		@Nullable DyeColor color = null;
		
		if (!stack.isEmpty()) {
			if (stack.getItem() instanceof DyeItem) {
				color = ((DyeItem) stack.getItem()).getDyeColor();
			} // else if...
		}
		
		return color;
	}
	
	@Override
	public IRecipeSerializer<?> getSerializer() {
		return NostrumCrafting.aetherCloakColorSerializer;
	}
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>>  implements IRecipeSerializer<AetherCloakColorRecipe> {
		
		public static final String ID = "aether_cloak_color";
		
		@Override
		public AetherCloakColorRecipe read(ResourceLocation recipeId, JsonObject json) {
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
			
			ColorUpgrades upgrade = null;
			try {
				upgrade = ColorUpgrades.valueOf(toggleKey.toUpperCase());
			} catch (Exception e) {
				throw new JsonParseException("Could not find Color upgrade with key [" + toggleKey + "]");
			}
			
			ItemStack displayStack = CraftingHelper.getItemStack(JSONUtils.getJsonObject(json, "display"), true);
			if (displayStack == null || displayStack.isEmpty()) {
				throw new JsonParseException("\"display\" section is required and must be a valid itemstack (not ingredient)");
			}
			
			return new AetherCloakColorRecipe(recipeId, group, displayStack, ingredients, upgrade);
		}

		@Override
		public AetherCloakColorRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			// I think I can just piggy back off of shaped recipe serializer
			ResourceLocation fakeRecipeId = new ResourceLocation(recipeId.getNamespace(), recipeId.getPath() + "#FAKE");
			ShapelessRecipe base = IRecipeSerializer.CRAFTING_SHAPELESS.read(fakeRecipeId, buffer);
			
			// Also read toggle func key
			String upgradeKey = buffer.readString();
			ColorUpgrades upgrade = null;
			try {
				upgrade = ColorUpgrades.valueOf(upgradeKey.toUpperCase());
			} catch (Exception e) {
				throw new JsonParseException("Could not find Color upgrade with key [" + upgradeKey + "]");
			}
			
			return new AetherCloakColorRecipe(recipeId, base.getGroup(), base.getRecipeOutput(), base.getIngredients(), upgrade);
		}

		@Override
		public void write(PacketBuffer buffer, AetherCloakColorRecipe recipe) {
			IRecipeSerializer.CRAFTING_SHAPELESS.write(buffer, recipe);
			buffer.writeString(recipe.getUpgradeType().name());
		}
	}
}


