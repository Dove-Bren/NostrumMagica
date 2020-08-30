package com.smanzana.nostrummagica.research;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

/**
 * Represents a piece of research a player can complete.
 * These are similar to NostrumQuest objects but are purchased with research points and are on the research tab.
 * Quests are for personal improvement, while research items are for crafting/ritual progression.
 * @author Skyler
 *
 */
public class NostrumResearch {

	public static enum Size {
		NORMAL,
		LARGE,
		GIANT,
	}
	
	protected final String key;
	
	/**
	 * Size to display the research task as
	 */
	protected final Size size;
	
	/**
	 * List of parent research items. This is a sanitized array and expects each to exist.
	 */
	protected final String[] parentKeys;
	
	/**
	 * List of lore keys that are required before this research can be seen/purchased
	 */
	protected final String[] loreKeys;
	
	/**
	 * List of quest keys. Each quest must be completed before this research item can be seen/purchased.
	 */
	protected final String[] questKeys;
	
	/**
	 * Positional offsets for display.
	 */
	protected final int x;
	protected final int y;
	
	/**
	 * Icon to display
	 */
	protected final @Nullable ItemStack iconItem;
	
	/**
	 * Things to link to when the player is viewing the details of their completed research.
	 * I'm thinking these will be keys that the infoscreen system can open up to via a click or something.
	 */
	protected final String[] references;
	
	private NostrumResearch(String key, Size size, int x, int y, ItemStack icon, String[] parents, String[] lore, String[] quests, String[] references) {
		this.key = key;
		this.size = size;
		this.x = x;
		this.y = y;
		this.iconItem = icon;
		this.parentKeys = parents;
		this.loreKeys = lore;
		this.questKeys = quests;
		this.references = references;
	}
	
	public String getKey() {
		return key;
	}

	public Size getSize() {
		return size;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public @Nullable ItemStack getIconItem() {
		return iconItem;
	}
	
	public String[] getReferences() {
		return references;
	}

	public String getNameKey() {
		return "research." + key + ".name";
	}
	
	public String getDescKey() {
		return "research." + key + ".desc";
	}


	/***********************************************
	 *             Builder methods
	 ***********************************************/
	public static final class Builder {
		protected List<String> parentKeys;
		protected List<String> loreKeys;
		protected List<String> questKeys;
		protected List<String> references;
		
		protected Builder() {
			parentKeys = new LinkedList<>();
			loreKeys = new LinkedList<>();
			questKeys = new LinkedList<>();
		}
		
		public Builder parent(String parent) {
			parentKeys.add(parent);
			return this;
		}
		
		public Builder lore(String lore) {
			loreKeys.add(lore);
			return this;
		}
		
		public Builder quest(String quest) {
			questKeys.add(quest);
			return this;
		}
		
		public Builder reference(String reference) {
			references.add(reference);
			return this;
		}
		
		public NostrumResearch build(String key, Size size, int x, int y, ItemStack icon) {
			return new NostrumResearch(key, size, x, y, icon,
					parentKeys.isEmpty() ? null : parentKeys.toArray(new String[parentKeys.size()]),
					loreKeys.isEmpty() ? null : loreKeys.toArray(new String[loreKeys.size()]),
					questKeys.isEmpty() ? null : questKeys.toArray(new String[questKeys.size()]),
					references.isEmpty() ? null : references.toArray(new String[references.size()])
					);
		}
		
		public NostrumResearch build(String key, Size size, int x, int y) {
			return build(key, size, x, y, null);
		}
	}
	
	public static Builder startBuilding() {
		return new Builder();
	}
	
}
