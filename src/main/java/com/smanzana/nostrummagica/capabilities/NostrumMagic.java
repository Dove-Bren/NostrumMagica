package com.smanzana.nostrummagica.capabilities;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.smanzana.nostrummagica.Lore.ILoreTagged;
import com.smanzana.nostrummagica.Lore.Lore;
import com.smanzana.nostrummagica.Lore.LoreCache;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

/**
 * Default implementation of the INostrumMagic interface
 * @author Skyler
 *
 */
public class NostrumMagic implements INostrumMagic {
	
	public static class LevelCurves {
		
		private static final float xpGrowth = 2.5f;
		private static final float xpBase = 100.0f;
		private static final float manaGrowth = 1.2f;
		private static final float manaBase = 100.0f;
		
		public static float maxXP(int level) {
			return (float) (xpBase * Math.pow(xpGrowth, level));
		}
		
		public static int maxMana(int level) {
			return (int) (manaBase * Math.pow(manaGrowth, level));
		}
	}
	
	private boolean unlocked;
	private int level;
	private float xp;
	private float maxxp;
	private int skillPoints;
	private int control;
	private int tech;
	private int finesse;
	private int mana;
	private int maxMana;
	
	//private List<IFamiliar> familiars;
	private boolean binding; // TODO binding interface
	
	private Map<String, Integer> loreLevels;
	private Set<String> spellCRCs; // spells we've done's CRCs
	private Map<EMagicElement, Boolean> elements;
	private List<String> shapes; // list of shape keys
	private List<String> triggers; // list of trigger keys
	private Map<EAlteration, Boolean> alterations;
	
	public NostrumMagic() {
		unlocked = false;
		//familiars = new LinkedList<>();
		loreLevels = new HashMap<>();
		spellCRCs = new HashSet<>();
		elements = new EnumMap<>(EMagicElement.class);
		shapes = new LinkedList<>();
		triggers = new LinkedList<>();
		alterations = new EnumMap<>(EAlteration.class);
		
		binding = false;
	}

	@Override
	public boolean isUnlocked() {
		return unlocked;
	}

	@Override
	public void unlock() {
		if (!unlocked) {
			unlocked = true;
			level = 1;
			xp = 0;
			mana = maxMana = LevelCurves.maxMana(1);
			maxxp = LevelCurves.maxXP(1);
			skillPoints = control = tech = finesse = 0;
			binding = false;
		}
	}
	
	private void levelup() {
		mana = maxMana = LevelCurves.maxMana(1);
		maxxp = LevelCurves.maxXP(1);
		
		this.addSkillPoint();
		level++;
		
		// TODO cool effects bruh
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public float getXP() {
		return xp;
	}

	@Override
	public float getMaxXP() {
		return maxxp;
	}

	@Override
	public void addXP(float xp) {
		this.xp += xp;
		if (this.xp > this.maxxp) {
			this.xp -= this.maxxp;
			levelup();
		}
	}

	@Override
	public int getSkillPoints() {
		return skillPoints;
	}

	@Override
	public void addSkillPoint() {
		this.skillPoints++;
	}

	@Override
	public void takeSkillPoint() {
		if (this.skillPoints > 0)
			this.skillPoints--;
	}

	@Override
	public int getControl() {
		return control;
	}

	@Override
	public void addControl() {
		control++;
	}

	@Override
	public int getTech() {
		return tech;
	}

	@Override
	public void addTech() {
		tech++;
	}

	@Override
	public int getFinesse() {
		return finesse;
	}

	@Override
	public void addFinesse() {
		finesse++;
	}

	@Override
	public int getMana() {
		return mana;
	}

	@Override
	public int getMaxMana() {
		return maxMana;
	}

	@Override
	public void setMana(int mana) {
		this.mana = Math.min(mana, this.maxMana);
	}

	@Override
	public void addMana(int mana) {
		this.mana = Math.min(this.mana + mana, this.maxMana);
	}

	@Override
	public void setMaxMana(int max) {
		this.maxMana = max;
		if (this.mana > maxMana)
			this.mana = maxMana;
	}

//	@Override
//	public List<IFamiliar> getFamiliars() {
//		return familiars;
//	}
//
//	@Override
//	public void addFamiliar(IFamiliar familiar) {
//		familiars.add(familiar);
//	}

	@Override
	public boolean isBinding() {
		return binding;
	}

	@Override
	public float bindingSecondsLeft() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void startBinding(float duration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasLore(ILoreTagged tagged) {
		return (getLore(tagged) != null);
	}

	@Override
	public Lore getLore(ILoreTagged tagged) {
		String key = tagged.getLoreKey();
		Integer level = loreLevels.get(key);
		
		if (level == null || level == 0)
			return null;
		
		Lore lore = null;
		if (level == 1)
			lore = LoreCache.instance().getBasicLore(tagged);
		else if (level == 2)
			lore = LoreCache.instance().getDeepLore(tagged);
		
		return lore;
	}

	@Override
	public void giveBasicLore(ILoreTagged tagged) {
		if (getLore(tagged) != null)
			return; // Already has some lore
		
		String key = tagged.getLoreKey();
		loreLevels.put(key, 1);
	}

	@Override
	public void giveFullLore(ILoreTagged tagged) {
		if (getLore(tagged) != null)
			return; // Already has some lore
		
		String key = tagged.getLoreKey();
		Integer val = loreLevels.get(key);
		
		if (val == 2)
			return; // Already has full
		
		loreLevels.put(key, 2);
	}

	@Override
	public boolean wasSpellDone(Spell spell) {
		String CRC = spell.crc();
		
		return !spellCRCs.add(CRC);
	}

	@Override
	public List<SpellShape> getShapes() {
		List<SpellShape> ret = new LinkedList<>();
		
		for (String name : shapes) {
			// TODO lookup
		}
		
		return ret;
	}

	@Override
	public List<SpellTrigger> getTriggers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<EMagicElement, Boolean> getElements() {
		return elements;
	}

	@Override
	public Map<EAlteration, Boolean> getAlterations() {
		return alterations;
	}

	@Override
	public void addShape(SpellShape shape) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTrigger(SpellTrigger trigger) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unlockElement(EMagicElement element) {
		elements.put(element, Boolean.TRUE);
	}

	@Override
	public void unlockAlteration(EAlteration alteration) {
		alterations.put(alteration, Boolean.TRUE);
	}

	@Override
	public void deserialize(boolean unlocked, int level, float xp, int skillpoints, int control, int tech, int finesse,
			int mana, int maxmana) {
		this.unlocked = unlocked;
		this.level = level;
		this.xp = xp;
		this.maxxp = LevelCurves.maxXP(this.level);
		this.skillPoints = skillpoints;
		this.control = control;
		this.tech = tech;
		this.finesse = finesse;
		this.mana = mana;
		this.maxMana = maxmana;
	}

	@Override
	public Map<String, Integer> serializeLoreLevels() {
		return this.loreLevels;
	}

	@Override
	public Set<String> serializeSpellHistory() {
		return this.spellCRCs;
	}

	@Override
	public Map<EMagicElement, Boolean> serializeElements() {
		return this.elements;
	}

	@Override
	public Map<EAlteration, Boolean> serializeAlterations() {
		return this.alterations;
	}

	@Override
	public void deserializeLore(String key, Integer level) {
		this.loreLevels.put(key, level);
	}

	@Override
	public void deserializeSpells(String crc) {
		this.spellCRCs.add(crc);
	}
	
}
