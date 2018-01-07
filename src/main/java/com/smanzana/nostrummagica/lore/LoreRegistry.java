package com.smanzana.nostrummagica.lore;

import java.util.HashMap;
import java.util.Map;

import com.smanzana.nostrummagica.entity.EntityGolemPhysical;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.MagicArmorBase;
import com.smanzana.nostrummagica.items.MagicSwordBase;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTableItem;
import com.smanzana.nostrummagica.items.SpellTome;

/**
 * Provides lookup from key to ILoreTagged to support offlining and onlining
 * of earned lore
 * @author Skyler
 *
 */
public class LoreRegistry {

	private static LoreRegistry instance = null;
	public static LoreRegistry instance() {
		if (instance == null)
			instance = new LoreRegistry();
		
		return instance;
	}
	
	private Map<String, ILoreTagged> lore;
	
	private LoreRegistry() {
		lore = new HashMap<>();
		
		init();
	}
	
	public void register(ILoreTagged tagged) {
		lore.put(tagged.getLoreKey(), tagged);
	}
	
	public ILoreTagged lookup(String key) {
		return lore.get(key);
	}
	
	private void init() {
		// All of the compile-time known lore elements are here.
		register(ReagentItem.instance());
		register(SpellTome.instance());
		register(SpellScroll.instance());
		register(SpellRune.instance());
		register(ReagentBag.instance());
		register(MagicSwordBase.instance());
		register(MagicArmorBase.helm);
		register(BlankScroll.instance());
		register(SpellTableItem.instance());
		register(InfusedGemItem.instance());
		register(new EntityGolemPhysical(null));
		
		for (ILoreTagged.Preset preset : ILoreTagged.Preset.values()) {
			register(preset);
		}
	}
}
