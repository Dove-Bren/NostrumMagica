package com.smanzana.nostrummagica.loretag;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Subclass of ILoreTagged for blocks that can't be actually set up as ILoreTagged.
 * @author Skyler
 *
 */
public interface IItemLoreTagged extends ILoreTagged {
	
	public Item getItem();
	
	public default ItemStack makeStack() {
		return new ItemStack(getItem());
	}
	
	public static String unwrap(ResourceLocation key) {
		return key.toString().replace(':', '.');
	}
	
	public static String getUnlocName(String key) {
		return "lore.item.%s.name".formatted(key);
	}
	
	public static String getUnlocName(ResourceLocation key) {
		return getUnlocName(unwrap(key));
	}
	
	public static String getUnlocDesc(String key, boolean deep) {
		return "lore.item.%s.%s".formatted(key, deep ? "deep" : "basic");
	}
	
	public static String getUnlocDesc(ResourceLocation key, boolean deep) {
		return getUnlocDesc(unwrap(key), deep);
	}
	
	public ResourceLocation getItemRegistryName();

	@Override
	public default String getLoreKey() {
		return unwrap(this.getItemRegistryName());
	}

	@Override
	public default String getLoreDisplayName() {
		return I18n.get(getUnlocName(getItemRegistryName()));
	}

	@Override
	public default Lore getBasicLore() {
		return new Lore(getUnlocDesc(getItemRegistryName(), false));
	}

	@Override
	public default Lore getDeepLore() {
		return new Lore(getUnlocDesc(getItemRegistryName(), true));
	}

	@Override
	public default ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
	}
}
