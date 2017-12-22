package com.smanzana.nostrummagica.capabilities;

import java.util.List;
import java.util.Map;

import com.smanzana.nostrummagica.Lore.ILoreTagged;
import com.smanzana.nostrummagica.Lore.Lore;

public interface INostrumMagic {

	// Unlock
	public boolean isUnlocked();
	public void unlock();
	
	// Progression
	public int getLevel();
	public float getXP();
	public float getMaxXP();
	public void addXP(float xp);
	
	public int getSkillPoints();
	public void addSkillPoint();
	public void takeSkillPoint();
	
	// Spell crafting attributes
	public int getControl();
	public void addControl();
	public int getTech();
	public void addTech();
	public int getFinesse();
	public void addFinesse();
	
	// Mana
	public int getMana();
	public int getMaxMana();
	public void setMana(int mana);
	public void addMana(int mana);
	public void setMaxMana(int max);
	
	// Familiars
	public IFamiliar getFamiliars(); // TODO add interface
	public void addFamiliar(IFamiliar familiar); // TODO here, too
	
	// Binding
	public boolean isBinding();
	public float bindingSecondsLeft();
	public void startBinding(float duration); // TODO interface for spell and tome we're binding
	
	// Lore
	public boolean hasLore(ILoreTagged tagged);
	public Lore getLore(ILoreTagged tagged);
	public void giveBasicLore(ILoreTagged tagged);
	public void giveFullLore(ILoreTagged tagged);
	
	// Spell Familiarity (xp mechanic)
	public boolean wasSpellDone(Spell spell); // returns true if spell was done before
	
	// Spell components
	public List<SpellShape> getShapes();
	public void addShape(SpellShape shape);
	public List<SpellTrigger> getTriggers();
	public void addTrigger(SpellTrigger trigger);
	public Map<EMagicElement, Boolean> getElements();
	public void unlockElement(EMagicElement element);
	public Map<EAlteration, Boolean> getAlterations();
	public void unlockAlteration(EAlteration alteration);
	
	// Serialization/Deserialization. Do not call.
	public void deserialize(
			boolean unlocked,
			int level,
			float xp,
			int skillpoints,
			int control,
			int tech,
			int finesse,
			int mana,
			int maxmana
			);
	
	public Map<String, Integer> serializeLoreLevels();
	public List<String> serializeSpellHistory();
	public Map<EMagicElement, Boolean> serializeElements();
	public Map<EAlteration, Boolean> serializeAlterations();
	public void deserializeLore(String key, Integer level);
	public void deserializeSpells(String crc);
}
