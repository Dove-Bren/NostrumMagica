package com.smanzana.nostrummagica.progression.research;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

/**
 * Tab key class that helps organize researches into different tabs.
 * All checks are done based on reference equality. Instantiate once and use that same instance in all calls.
 * @author Skyler
 *
 */
public class NostrumResearchTab {
	
	/**
	 * Unique key for this tab.
	 * Drives translations.
	 */
	private final String key;
	
	/**
	 * The icon to display on the tab. Required.
	 */
	private @Nonnull ItemStack icon;
	
	private final Supplier<ItemStack> iconSupplier;
	
	public NostrumResearchTab(String key, @Nonnull Supplier<ItemStack> iconSupplier) {
		this.key = key;
		this.iconSupplier = iconSupplier;
		
		AllTabs.add(this);
	}
	
	public String getRawKey() {
		return key;
	}
	
	public @Nonnull ItemStack getIcon() {
		if (icon == null) {
			icon = iconSupplier.get();
		}
		return icon;
	}
	
	public String getNameKey() {
		return "research.tab." + key + ".name";
	}
	
	private static List<NostrumResearchTab> AllTabs = new ArrayList<>();
	
	public static List<NostrumResearchTab> All() {
		return AllTabs;
	}
}