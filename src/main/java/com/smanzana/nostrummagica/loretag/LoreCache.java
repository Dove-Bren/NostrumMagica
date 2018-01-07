package com.smanzana.nostrummagica.loretag;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches lore. :)
 * @author Skyler
 *
 */
public class LoreCache {

	private Map<String, Lore> basicLore;
	private Map<String, Lore> deepLore;
	
	private static LoreCache instance = null;
	public static LoreCache instance() {
		if (instance == null)
			instance = new LoreCache();
		
		return instance;
	}
	
	private LoreCache() {
		basicLore = new HashMap<>();
		deepLore = new HashMap<>();
	}
	
	public Lore getBasicLore(ILoreTagged tag) {
		String key = tag.getLoreKey();
		Lore lore = basicLore.get(key);
		
		if (lore == null) {
			lore = tag.getBasicLore();
			basicLore.put(key, lore);
		}
		
		return lore;
	}
	
	public Lore getDeepLore(ILoreTagged tag) {
		String key = tag.getLoreKey();
		Lore lore = deepLore.get(key);
		
		if (lore == null) {
			lore = tag.getDeepLore();
			deepLore.put(key, lore);
		}
		
		return lore;
	}
}
