package com.smanzana.nostrummagica.integration.aetheria.items;

import com.smanzana.nostrumaetheria.api.item.IAetherBurnable;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Misc. resource items for aether-related progression
 * @author Skyler
 *
 */
public class NostrumAetherResourceItem extends Item implements ILoreTagged, IAetherBurnable {

	private static final String ID_PREFIX = "aether_resource_";
	public static final String ID_GINSENG_FLOWER = ID_PREFIX + "ginseng_flower";
	public static final String ID_MANDRAKE_FLOWER = ID_PREFIX + "mandrake_flower";
	
	private final int burnTicks;
	private final int aetherYield;
	
	public NostrumAetherResourceItem(int burnTicks, int aetherYield, Item.Properties builder) {
		super(builder);
		this.burnTicks = burnTicks;
		this.aetherYield = aetherYield;
	}
	
    @Override
	public String getLoreKey() {
		return "nostrum_aether_resource";
	}

	@Override
	public String getLoreDisplayName() {
		return "Aether Flowers";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Flowers of mandrake and ginseng that can't be used as reagents... and yet, you can tell there's something magical about them.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Flowers of mandrake and ginseng with high levels of aether.", "These flowers cannot be used as reagents by themselves but produce more aether than regular reagents when burned.");
	}
	
	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public int getBurnTicks(ItemStack stack) {
		return this.burnTicks;
	}

	@Override
	public float getAetherYield(ItemStack stack) {
		return this.aetherYield;
	}
}
