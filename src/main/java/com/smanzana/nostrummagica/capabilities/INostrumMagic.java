package com.smanzana.nostrummagica.capabilities;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.smanzana.nostrummagica.lore.ILoreTagged;
import com.smanzana.nostrummagica.lore.Lore;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.EntityLivingBase;

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
//	public List<IFamiliar> getFamiliars(); // TODO add interface
//	public void addFamiliar(IFamiliar familiar); // TODO here, too
	
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
			int mana
			);
	
	public Map<String, Integer> serializeLoreLevels();
	public Set<String> serializeSpellHistory();
	public Map<EMagicElement, Boolean> serializeElements();
	public Map<EAlteration, Boolean> serializeAlterations();
	public void deserializeLore(String key, Integer level);
	public void deserializeSpells(String crc);
	// Copy fields out of
	public void copy(INostrumMagic cap);
	public void provideEntity(EntityLivingBase entity);
}
