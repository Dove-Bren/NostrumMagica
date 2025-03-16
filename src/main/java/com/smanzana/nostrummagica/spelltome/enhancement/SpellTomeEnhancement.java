package com.smanzana.nostrummagica.spelltome.enhancement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.world.entity.LivingEntity;

public abstract class SpellTomeEnhancement {
	
	public static EnhancementLowerReagentCost LOWER_REAGENT_COST;
	public static EnhancementLowerManaCost LOWER_MANA_COST;
	public static EnhancementEfficiency EFFICIENCY;
	public static EnhancementBonusXP BONUS_XP;
	

	private static Map<String, SpellTomeEnhancement> registry = new HashMap<>();
	
	public static void registerEnhancement(String key, SpellTomeEnhancement enhancement) {
		if (registry.containsKey(key)) {
			NostrumMagica.logger.error("Duplicate spelltome enhancement with key " + key);
			return;
		}
		registry.put(key, enhancement);
	}
	
	public static SpellTomeEnhancement lookupEnhancement(String key) {
		return registry.get(key);
	}
	
	public static Collection<SpellTomeEnhancement> getEnhancements() {
		return registry.values();
	}
	
	private String titleKey;
	
	/**
	 * 
	 * @param key Key for translation and serialization.
	 * This expects the translation keys:
	 * enhancement.[KEY].name
	 * enhancement.[KEY].desc
	 */
	public SpellTomeEnhancement(String key) {
		this.titleKey = key;
		registerEnhancement(key, this);
	}
	
	public String getTitleKey() {
		return titleKey;
	}
	
	public String getNameFormat() {
		return "enhancement." + this.titleKey + ".name";
	}
	
	public String getDescFormat() {
		return "enhancement." + this.titleKey + ".desc";
	}
	
	/**
	 * Return the maximum number of levels this enhancement can have.
	 * At least 1 is required. Most go up to 3
	 * @return
	 */
	public abstract int getMaxLevel();
	
	/**
	 * For the given level, how much weight does this enhancement carry?
	 * This is a relative value. 1 is no big deal (like efficiency enchantment).
	 * 5 is like infinity -- a big deal.
	 * Spell tomes cannot exceed the number of enhancement points they have.
	 * They will normally have between 1 and 10
	 * @param level
	 * @return
	 */
	public abstract int getWeight(int level);

	/**
	 * Apply the effects of this enhancement.
	 * This method is called when a spell is being cast. As such,
	 * most of the alterations here apply to casting
	 * @param level
	 * @param summaryIn
	 * @param target
	 * @param source
	 * @param attributes
	 */
	public abstract void onCast(int level, SpellCastSummary summaryIn, LivingEntity source, INostrumMagic attributes);
	
	public static void initDefaultEnhancements() {
		LOWER_REAGENT_COST = new EnhancementLowerReagentCost();
		LOWER_MANA_COST = new EnhancementLowerManaCost();
		BONUS_XP = new EnhancementBonusXP();
		EFFICIENCY = new EnhancementEfficiency();
	}
	
}
