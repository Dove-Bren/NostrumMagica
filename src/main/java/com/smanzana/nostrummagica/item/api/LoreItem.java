package com.smanzana.nostrummagica.item.api;

import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class LoreItem extends Item implements ILoreTagged {
	
	protected static final String unwrap(ResourceLocation key) {
		return key.toString().replace(':', '.');
	}
	
	protected static final String getUnlocName(String key) {
		return "lore.item.%s.name".formatted(key);
	}
	
	protected static final String getUnlocName(ResourceLocation key) {
		return getUnlocName(unwrap(key));
	}
	
	protected static final String getUnlocDesc(String key, boolean deep) {
		return "lore.item.%s.%s".formatted(key, deep ? "deep" : "basic");
	}
	
	protected static final String getUnlocDesc(ResourceLocation key, boolean deep) {
		return getUnlocDesc(unwrap(key), deep);
	}

	public LoreItem(Properties props) {
		super(props);
		
	}

	@Override
	public String getLoreKey() {
		return unwrap(this.getRegistryName());
	}

	@Override
	public String getLoreDisplayName() {
		return I18n.get(getUnlocName(getRegistryName()));
	}

	@Override
	public Lore getBasicLore() {
		return new Lore(getUnlocDesc(getRegistryName(), false));
	}

	@Override
	public Lore getDeepLore() {
		return new Lore(getUnlocDesc(getRegistryName(), true));
	}

	@Override
	public ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
	}

}
