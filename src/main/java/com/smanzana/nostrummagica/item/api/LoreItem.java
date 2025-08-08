package com.smanzana.nostrummagica.item.api;

import com.smanzana.nostrummagica.loretag.IItemLoreTagged;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class LoreItem extends Item implements IItemLoreTagged {

	public LoreItem(Properties props) {
		super(props);
	}

	@Override
	public Item getItem() {
		return this;
	}

	@Override
	public ResourceLocation getItemRegistryName() {
		return this.getRegistryName();
	}
	
	

}
