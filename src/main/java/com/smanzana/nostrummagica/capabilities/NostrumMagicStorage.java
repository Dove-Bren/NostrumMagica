package com.smanzana.nostrummagica.capabilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.quests.objectives.IObjectiveState;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.Constants.NBT;

public class NostrumMagicStorage implements IStorage<INostrumMagic> {

	private static final String NBT_UNLOCKED = "unlocked";
	private static final String NBT_LEVEL = "level";
	private static final String NBT_XP = "xp";
	private static final String NBT_SKILLPOINTS = "skillpoints";
	private static final String NBT_RESEARCHPOINTS = "researchpoints";
	private static final String NBT_CONTROL = "control";
	private static final String NBT_TECH = "tech";
	private static final String NBT_FINESSE = "finesse";
	private static final String NBT_MANA = "mana";
	private static final String NBT_RESERVED_MANA = "reserved_mana";
	
	private static final String NBT_MOD_MANA = "mod_mana";
	private static final String NBT_MOD_MANA_COST = "mod_mana_cost";
	private static final String NBT_MOD_MANA_REGEN = "mod_mana_regen";
	private static final String NBT_MOD_MANA_BONUS = "mod_mana_bonus";
	
	//private static final String NBT_FAMILIARS = "familiars";
	
	private static final String NBT_BINDING_COMPONENT = "binding_component";
	private static final String NBT_BINDING_SPELL = "binding_spell";
	private static final String NBT_BINDING_TOME_ID = "binding_tomeid";
	//private static final String NBT_BINDING_COMPONENT = "binding";
	
	private static final String NBT_LORELEVELS = "lore";
	private static final String NBT_SPELLCRCS = "spellcrcs"; // spells we've done's CRCs
	private static final String NBT_KNOWN_ELEMENTS = "known_elements";
	private static final String NBT_MASTERED_ELEMENTS = "mastered_elements";
	private static final String NBT_ELEMENT_TRIALS = "element_trials";
	private static final String NBT_SHAPES = "shapes"; // list of shape keys
	private static final String NBT_TRIGGERS = "triggers"; // list of trigger keys
	private static final String NBT_ALTERATIONS = "alterations";
	
	private static final String NBT_MARK_DIMENSION = "mark_dim";
	private static final String NBT_MARK_POS = "mark_pos";
	private static final String NBT_ENHANCED_TELEPORT = "enhanced_teleport";
	
	private static final String NBT_QUESTS_COMPLETED = "quests_completed";
	private static final String NBT_QUESTS_CURRENT = "quests_current";
	private static final String NBT_QUESTS_DATA = "quest_data";
	
	private static final String NBT_RESEARCHES = "research_completed";
	
	private static final String NBT_SPELLKNOWLEDGE = "spell_knowledge";
	
	private static final String NBT_SORCERYPORTAL_DIM = "sorcery_portal_dim";
	private static final String NBT_SORCERYPORTAL_POS = "sorcery_portal_pos";
	
	@Override
	public INBT writeNBT(Capability<INostrumMagic> capability, INostrumMagic instance, Direction side) {
		CompoundNBT nbt = new CompoundNBT();
		
		nbt.putBoolean(NBT_UNLOCKED, instance.isUnlocked());
		nbt.putInt(NBT_LEVEL, instance.getLevel());
		nbt.putFloat(NBT_XP, instance.getXP());
		nbt.putInt(NBT_SKILLPOINTS, instance.getSkillPoints());
		nbt.putInt(NBT_RESEARCHPOINTS, instance.getResearchPoints());
		nbt.putInt(NBT_CONTROL, instance.getControl());
		nbt.putInt(NBT_TECH, instance.getTech());
		nbt.putInt(NBT_FINESSE, instance.getFinesse());
		nbt.putInt(NBT_MANA, instance.getMana());
		nbt.putInt(NBT_RESERVED_MANA, instance.getReservedMana());
		nbt.putFloat(NBT_MOD_MANA, instance.getManaModifier());
		nbt.putFloat(NBT_MOD_MANA_COST, instance.getManaCostModifier());
		nbt.putFloat(NBT_MOD_MANA_REGEN, instance.getManaRegenModifier());
		nbt.putInt(NBT_MOD_MANA_BONUS, instance.getManaBonus());
		
		CompoundNBT compound = new CompoundNBT();
		{
			Map<String, Integer> map = instance.serializeLoreLevels();
			for (String key : map.keySet()) {
				compound.putInt(key, map.get(key));
			}
		}
		nbt.put(NBT_LORELEVELS, compound);
		
		ListNBT list = new ListNBT();
		for (String crc : instance.serializeSpellHistory()) {
			list.add(new StringNBT(crc));
		}
		nbt.put(NBT_SPELLCRCS, list);
		
		compound = new CompoundNBT();
		{
			Map<EMagicElement, Boolean> map = instance.serializeKnownElements();
			for (EMagicElement key : map.keySet()) {
				compound.putBoolean(key.name(), map.get(key));
			}
		}
		nbt.put(NBT_KNOWN_ELEMENTS, compound);
		
		compound = new CompoundNBT();
		{
			Map<EMagicElement, Integer> map = instance.serializeElementMastery();
			for (EMagicElement key : map.keySet()) {
				compound.putInt(key.name(), map.get(key));
			}
		}
		nbt.put(NBT_MASTERED_ELEMENTS, compound);
		
		compound = new CompoundNBT();
		{
			Map<EMagicElement, Boolean> map = instance.serializeElementTrials();
			for (EMagicElement key : map.keySet()) {
				compound.putBoolean(key.name(), map.get(key));
			}
		}
		nbt.put(NBT_ELEMENT_TRIALS, compound);
		
		list = new ListNBT();
		for (SpellShape shape : instance.getShapes()) {
			String key = shape.getShapeKey();
			list.add(new StringNBT(key));
		}
		nbt.put(NBT_SHAPES, list);
		
		list = new ListNBT();
		for (SpellTrigger trigger : instance.getTriggers()) {
			String key = trigger.getTriggerKey();
			list.add(new StringNBT(key));
		}
		nbt.put(NBT_TRIGGERS, list);
		
		compound = new CompoundNBT();
		{
			Map<EAlteration, Boolean> map = instance.serializeAlterations();
			for (EAlteration key : map.keySet()) {
				compound.putBoolean(key.name(), map.get(key));
			}
		}
		nbt.put(NBT_ALTERATIONS, compound);
		
		BlockPos markPos = instance.getMarkLocation();
		if (markPos != null) {
			CompoundNBT posTag = new CompoundNBT();
			posTag.putInt("x", markPos.getX());
			posTag.putInt("y", markPos.getY());
			posTag.putInt("z", markPos.getZ());
			nbt.putInt(NBT_MARK_DIMENSION, instance.getMarkDimension());
			nbt.put(NBT_MARK_POS, posTag);
		}
		if (instance.hasEnhancedTeleport()) {
			nbt.putBoolean(NBT_ENHANCED_TELEPORT, true);
		}
		
		List<String> stringList = instance.getCurrentQuests();
		if (stringList != null && !stringList.isEmpty()) {
			ListNBT tagList = new ListNBT();
			for (String quest : stringList) {
				tagList.add(new StringNBT(quest));
			}
			nbt.put(NBT_QUESTS_CURRENT, tagList);
		}
		
		stringList = instance.getCompletedQuests();
		if (stringList != null && !stringList.isEmpty()) {
			ListNBT tagList = new ListNBT();
			for (String quest : stringList) {
				tagList.add(new StringNBT(quest));
			}
			nbt.put(NBT_QUESTS_COMPLETED, tagList);
		}
		
		{
			Map<String, IObjectiveState> data = instance.getQuestDataMap();
			if (data != null && !data.isEmpty()) {
				compound = new CompoundNBT();
				
				for (String quest : data.keySet()) {
					compound.put(quest, data.get(quest).toNBT());
				}
				
				nbt.put(NBT_QUESTS_DATA, compound);
			}
		
		}
		
		stringList = instance.getCompletedResearches();
		if (stringList != null && !stringList.isEmpty()) {
			ListNBT tagList = new ListNBT();
			for (String research : stringList) {
				tagList.add(new StringNBT(research));
			}
			nbt.put(NBT_RESEARCHES, tagList);
		}
		
		if (instance.isBinding()) {
			nbt.putString(NBT_BINDING_COMPONENT, instance.getBindingComponent().getKeyString());
			nbt.putInt(NBT_BINDING_TOME_ID, instance.getBindingID());
			nbt.putInt(NBT_BINDING_SPELL, instance.getBindingSpell().getRegistryID());
		}
		
		compound = new CompoundNBT();;
		Map<EMagicElement, Map<EAlteration, Boolean>> knowledge = instance.getSpellKnowledge();
		if (knowledge != null && !knowledge.isEmpty())
		for (EMagicElement elem : knowledge.keySet()) {
			CompoundNBT subtag = new CompoundNBT();
			Map<EAlteration, Boolean> map = knowledge.get(elem);
			if (map == null || map.isEmpty())
				continue;
			for (EAlteration alt : map.keySet()) {
				Boolean bool = map.get(alt);
				if (bool != null && bool) {
					subtag.putBoolean(alt == null ? "none" : alt.name(), true);
				}
			}
			compound.put(elem.name(), subtag);
		}
		nbt.put(NBT_SPELLKNOWLEDGE, compound);
		
		if (instance.getSorceryPortalPos() != null) {
			nbt.putInt(NBT_SORCERYPORTAL_DIM, instance.getSorceryPortalDimension());
			nbt.putLong(NBT_SORCERYPORTAL_POS, instance.getSorceryPortalPos().toLong());
		}
		
		return nbt;
	}

	@Override
	public void readNBT(Capability<INostrumMagic> capability, INostrumMagic instance, Direction side, INBT nbt) {
		CompoundNBT tag = (CompoundNBT) nbt;
		instance.deserialize(tag.getBoolean(NBT_UNLOCKED),
			tag.getInt(NBT_LEVEL),
			tag.getFloat(NBT_XP),
			tag.getInt(NBT_SKILLPOINTS),
			tag.getInt(NBT_RESEARCHPOINTS),
			tag.getInt(NBT_CONTROL),
			tag.getInt(NBT_TECH),
			tag.getInt(NBT_FINESSE),
			tag.getInt(NBT_MANA),
			tag.getInt(NBT_RESERVED_MANA),
			tag.getFloat(NBT_MOD_MANA),
			tag.getInt(NBT_MOD_MANA_BONUS),
			tag.getFloat(NBT_MOD_MANA_COST),
			tag.getFloat(NBT_MOD_MANA_REGEN));
			
		// LORE
		CompoundNBT compound = tag.getCompound(NBT_LORELEVELS);
		for (String key : compound.keySet()) {
			Integer level = compound.getInt(key);
			instance.deserializeLore(key, level);
		}
		
		// SPELLS
		ListNBT list = tag.getList(NBT_SPELLCRCS, NBT.TAG_STRING);
		for (int i = 0; i < list.size(); i++) {
			instance.deserializeSpells(list.getString(i));
		}
		
		// KNOWNELEMENTS
		compound = tag.getCompound(NBT_KNOWN_ELEMENTS);
		for (String key : compound.keySet()) {
			boolean val = compound.getBoolean(key);
			if (val) {
				EMagicElement elem = EMagicElement.valueOf(key);
				instance.learnElement(elem);
			}
		}
		
		// ELEMENTS
		compound = tag.getCompound(NBT_MASTERED_ELEMENTS);
		for (String key : compound.keySet()) {
			int val = compound.getInt(key);
			if (val != 0) {
				EMagicElement elem = EMagicElement.valueOf(key);
				instance.setElementMastery(elem, val);
			}
		}
		
		// PATCH #1: Known implies mastery 1
		for (EMagicElement elem : EMagicElement.values()) {
			Boolean known = instance.getKnownElements().get(elem);
			if (known != null && known) {
				Integer mastery = instance.getElementMastery().get(elem);
				if (mastery == null || mastery <= 0) {
					instance.setElementMastery(elem, 1);
				}
			}
		}
		
		compound = tag.getCompound(NBT_ELEMENT_TRIALS);
		for (String key : compound.keySet()) {
			boolean val = compound.getBoolean(key);
			if (val) {
				EMagicElement elem = EMagicElement.valueOf(key);
				instance.startTrial(elem);
			}
		}
		
		// SHAPES
		list = tag.getList(NBT_SHAPES, NBT.TAG_STRING);
		for (int i = 0; i < list.size(); i++) {
			SpellShape shape = SpellShape.get(list.getString(i));
			instance.addShape(shape);
		}
		
		// TRIGGERS
		list = tag.getList(NBT_TRIGGERS, NBT.TAG_STRING);
		for (int i = 0; i < list.size(); i++) {
			SpellTrigger trigger = SpellTrigger.get(list.getString(i));
			instance.addTrigger(trigger);
		}
		
		// ALTERATIONS

		compound = tag.getCompound(NBT_ALTERATIONS);
		for (String key : compound.keySet()) {
			boolean val = compound.getBoolean(key);
			if (val) {
				EAlteration elem = EAlteration.valueOf(key);
				instance.unlockAlteration(elem);
			}
		}
		
		// Mark Location
		if (tag.contains(NBT_MARK_POS, NBT.TAG_COMPOUND)) {
			CompoundNBT posTag = tag.getCompound(NBT_MARK_POS);
			BlockPos location = new BlockPos(
					posTag.getInt("x"),
					posTag.getInt("y"),
					posTag.getInt("z")
					);
			int dimension = tag.getInt(NBT_MARK_DIMENSION);
			
			instance.setMarkLocation(dimension, location);
		}
		
		if (tag.contains(NBT_ENHANCED_TELEPORT) && tag.getBoolean(NBT_ENHANCED_TELEPORT)) {
			instance.unlockEnhancedTeleport();
		}
		
		// Quests
		if (tag.contains(NBT_QUESTS_CURRENT, NBT.TAG_LIST)) {
			ListNBT tagList = tag.getList(NBT_QUESTS_CURRENT, NBT.TAG_STRING);
			for (int i = 0; i < tagList.size(); i++) {
				String quest = tagList.getString(i);
				instance.addQuest(quest);
			}
		}
		if (tag.contains(NBT_QUESTS_COMPLETED, NBT.TAG_LIST)) {
			ListNBT tagList = tag.getList(NBT_QUESTS_COMPLETED, NBT.TAG_STRING);
			for (int i = 0; i < tagList.size(); i++) {
				String quest = tagList.getString(i);
				instance.addQuest(quest);
				instance.completeQuest(quest);
			}
		}
		Map<String, IObjectiveState> data = new HashMap<>();
		if (tag.contains(NBT_QUESTS_DATA, NBT.TAG_COMPOUND)) {
			CompoundNBT dataTag = tag.getCompound(NBT_QUESTS_DATA);
			for (String key : dataTag.keySet()) {
				NostrumQuest quest = NostrumQuest.lookup(key);
				if (quest == null)
					continue;
				
				if (quest.getObjective() != null) {
					IObjectiveState state = quest.getObjective().getBaseState();
					state.fromNBT(dataTag.getCompound(key));
					data.put(key, state);
				}
			}
		}
		instance.setQuestDataMap(data);
		
		if (tag.contains(NBT_RESEARCHES, NBT.TAG_LIST)) {
			ListNBT tagList = tag.getList(NBT_RESEARCHES, NBT.TAG_STRING);
			for (int i = 0; i < tagList.size(); i++) {
				String research = tagList.getString(i);
				instance.completeResearch(research);
			}
		}
		
		if (tag.contains(NBT_BINDING_COMPONENT, NBT.TAG_STRING)
				&& tag.contains(NBT_BINDING_SPELL, NBT.TAG_INT)
				&& tag.contains(NBT_BINDING_TOME_ID, NBT.TAG_INT)) {
			Spell spell = NostrumMagica.instance.getSpellRegistry().lookup(tag.getInt(NBT_BINDING_SPELL));
			if (spell != null) {
				SpellComponentWrapper comp = SpellComponentWrapper.fromKeyString(tag.getString(NBT_BINDING_COMPONENT));
				int tomeID = tag.getInt(NBT_BINDING_TOME_ID);
				instance.startBinding(spell, comp, tomeID);
			} else {
				NostrumMagica.logger.warn("Found illegal spell ID. Player will lose binding information");
			}
		}
		
		if (tag.contains(NBT_SPELLKNOWLEDGE, NBT.TAG_COMPOUND)) {
			compound = tag.getCompound(NBT_SPELLKNOWLEDGE);
			for (String key : compound.keySet()) {
				try {
					EMagicElement elem = EMagicElement.valueOf(key);
					CompoundNBT subtag = compound.getCompound(key);
					for (String altKey : subtag.keySet()) {
						EAlteration alt;
						if (altKey.equalsIgnoreCase("none"))
							alt = null;
						else
							alt = EAlteration.valueOf(altKey);
						if (subtag.getBoolean(altKey)) {
							instance.setKnowledge(elem, alt);
						}
					}
				} catch (Exception e) {
					continue;
				}
			}
		}
		
		if (tag.contains(NBT_SORCERYPORTAL_POS)) {
			instance.setSorceryPortalLocation(
					tag.getInt(NBT_SORCERYPORTAL_DIM),
					BlockPos.fromLong(tag.getLong(NBT_SORCERYPORTAL_POS)));
		}
	}

}
