package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public interface ILoreBlock extends ILoreTagged {
	
	private static String unwrap(ResourceLocation key) {
		return key.toString().replace(':', '.');
	}
	
	private static String getUnlocName(String key) {
		return "lore.block.%s.name".formatted(key);
	}
	
	private static String getUnlocName(ResourceLocation key) {
		return getUnlocName(unwrap(key));
	}
	
	private static String getUnlocDesc(String key, boolean deep) {
		return "lore.block.%s.%s".formatted(key, deep ? "deep" : "basic");
	}
	
	private static String getUnlocDesc(ResourceLocation key, boolean deep) {
		return getUnlocDesc(unwrap(key), deep);
	}
	
	public ResourceLocation getRegistryName();
	
	public default ResourceLocation getLoreName() {
		return getRegistryName();
	}

	@Override
	public default String getLoreKey() {
		return unwrap(this.getLoreName());
	}

	@Override
	public default String getLoreDisplayName() {
		return I18n.get(getUnlocName(getLoreName()));
	}

	@Override
	public default Lore getBasicLore() {
		return new Lore(getUnlocDesc(getLoreName(), false));
	}
	
	@Override
	public default Lore getDeepLore() {
		return new Lore(getUnlocDesc(getLoreName(), true));
	}

	@Override
	public default ELoreCategory getCategory() {
		return ELoreCategory.BLOCK;
	}
	
}
