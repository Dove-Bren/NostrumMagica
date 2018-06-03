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

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.Constants.NBT;

public class NostrumMagicStorage implements IStorage<INostrumMagic> {

	private static final String NBT_UNLOCKED = "unlocked";
	private static final String NBT_LEVEL = "level";
	private static final String NBT_XP = "xp";
	private static final String NBT_SKILLPOINTS = "skillpoints";
	private static final String NBT_CONTROL = "control";
	private static final String NBT_TECH = "tech";
	private static final String NBT_FINESSE = "finesse";
	private static final String NBT_MANA = "mana";
	
	private static final String NBT_MOD_MANA = "mod_mana";
	private static final String NBT_MOD_MANA_COST = "mod_mana_cost";
	private static final String NBT_MOD_MANA_REGEN = "mod_mana_regen";
	
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
	
	private static final String NBT_QUESTS_COMPLETED = "quests_completed";
	private static final String NBT_QUESTS_CURRENT = "quests_current";
	private static final String NBT_QUESTS_DATA = "quest_data";
	
	private static final String NBT_SPELLKNOWLEDGE = "spell_knowledge";
	
	@Override
	public NBTBase writeNBT(Capability<INostrumMagic> capability, INostrumMagic instance, EnumFacing side) {
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setBoolean(NBT_UNLOCKED, instance.isUnlocked());
		nbt.setInteger(NBT_LEVEL, instance.getLevel());
		nbt.setFloat(NBT_XP, instance.getXP());
		nbt.setInteger(NBT_SKILLPOINTS, instance.getSkillPoints());
		nbt.setInteger(NBT_CONTROL, instance.getControl());
		nbt.setInteger(NBT_TECH, instance.getTech());
		nbt.setInteger(NBT_FINESSE, instance.getFinesse());
		nbt.setInteger(NBT_MANA, instance.getMana());
		nbt.setFloat(NBT_MOD_MANA, instance.getManaModifier());
		nbt.setFloat(NBT_MOD_MANA_COST, instance.getManaCostModifier());
		nbt.setFloat(NBT_MOD_MANA_REGEN, instance.getManaRegenModifier());
		
		NBTTagCompound compound = new NBTTagCompound();
		{
			Map<String, Integer> map = instance.serializeLoreLevels();
			for (String key : map.keySet()) {
				compound.setInteger(key, map.get(key));
			}
		}
		nbt.setTag(NBT_LORELEVELS, compound);
		
		NBTTagList list = new NBTTagList();
		for (String crc : instance.serializeSpellHistory()) {
			list.appendTag(new NBTTagString(crc));
		}
		nbt.setTag(NBT_SPELLCRCS, list);
		
		compound = new NBTTagCompound();
		{
			Map<EMagicElement, Boolean> map = instance.serializeKnownElements();
			for (EMagicElement key : map.keySet()) {
				compound.setBoolean(key.name(), map.get(key));
			}
		}
		nbt.setTag(NBT_KNOWN_ELEMENTS, compound);
		
		compound = new NBTTagCompound();
		{
			Map<EMagicElement, Integer> map = instance.serializeElementMastery();
			for (EMagicElement key : map.keySet()) {
				compound.setInteger(key.name(), map.get(key));
			}
		}
		nbt.setTag(NBT_MASTERED_ELEMENTS, compound);
		
		compound = new NBTTagCompound();
		{
			Map<EMagicElement, Boolean> map = instance.serializeElementTrials();
			for (EMagicElement key : map.keySet()) {
				compound.setBoolean(key.name(), map.get(key));
			}
		}
		nbt.setTag(NBT_ELEMENT_TRIALS, compound);
		
		list = new NBTTagList();
		for (SpellShape shape : instance.getShapes()) {
			String key = shape.getShapeKey();
			list.appendTag(new NBTTagString(key));
		}
		nbt.setTag(NBT_SHAPES, list);
		
		list = new NBTTagList();
		for (SpellTrigger trigger : instance.getTriggers()) {
			String key = trigger.getTriggerKey();
			list.appendTag(new NBTTagString(key));
		}
		nbt.setTag(NBT_TRIGGERS, list);
		
		compound = new NBTTagCompound();
		{
			Map<EAlteration, Boolean> map = instance.serializeAlterations();
			for (EAlteration key : map.keySet()) {
				compound.setBoolean(key.name(), map.get(key));
			}
		}
		nbt.setTag(NBT_ALTERATIONS, compound);
		
		BlockPos markPos = instance.getMarkLocation();
		if (markPos != null) {
			NBTTagCompound posTag = new NBTTagCompound();
			posTag.setInteger("x", markPos.getX());
			posTag.setInteger("y", markPos.getY());
			posTag.setInteger("z", markPos.getZ());
			nbt.setInteger(NBT_MARK_DIMENSION, instance.getMarkDimension());
			nbt.setTag(NBT_MARK_POS, posTag);
		}
		
		List<String> stringList = instance.getCurrentQuests();
		if (stringList != null && !stringList.isEmpty()) {
			NBTTagList tagList = new NBTTagList();
			for (String quest : stringList) {
				tagList.appendTag(new NBTTagString(quest));
			}
			nbt.setTag(NBT_QUESTS_CURRENT, tagList);
		}
		
		stringList = instance.getCompletedQuests();
		if (stringList != null && !stringList.isEmpty()) {
			NBTTagList tagList = new NBTTagList();
			for (String quest : stringList) {
				tagList.appendTag(new NBTTagString(quest));
			}
			nbt.setTag(NBT_QUESTS_COMPLETED, tagList);
		}
		
		{
			Map<String, IObjectiveState> data = instance.getQuestDataMap();
			if (data != null && !data.isEmpty()) {
				compound = new NBTTagCompound();
				
				for (String quest : data.keySet()) {
					compound.setTag(quest, data.get(quest).toNBT());
				}
				
				nbt.setTag(NBT_QUESTS_DATA, compound);
			}
		
		}
		
		if (instance.isBinding()) {
			nbt.setString(NBT_BINDING_COMPONENT, instance.getBindingComponent().getKeyString());
			nbt.setInteger(NBT_BINDING_TOME_ID, instance.getBindingID());
			nbt.setInteger(NBT_BINDING_SPELL, instance.getBindingSpell().getRegistryID());
		}
		
		compound = new NBTTagCompound();;
		Map<EMagicElement, Map<EAlteration, Boolean>> knowledge = instance.getSpellKnowledge();
		if (knowledge != null && !knowledge.isEmpty())
		for (EMagicElement elem : knowledge.keySet()) {
			NBTTagCompound subtag = new NBTTagCompound();
			Map<EAlteration, Boolean> map = knowledge.get(elem);
			if (map == null || map.isEmpty())
				continue;
			for (EAlteration alt : map.keySet()) {
				Boolean bool = map.get(alt);
				if (bool != null && bool) {
					subtag.setBoolean(alt == null ? "none" : alt.name(), true);
				}
			}
			compound.setTag(elem.name(), subtag);
		}
		nbt.setTag(NBT_SPELLKNOWLEDGE, compound);
		
		return nbt;
	}

	@Override
	public void readNBT(Capability<INostrumMagic> capability, INostrumMagic instance, EnumFacing side, NBTBase nbt) {
		NBTTagCompound tag = (NBTTagCompound) nbt;
		instance.deserialize(tag.getBoolean(NBT_UNLOCKED),
			tag.getInteger(NBT_LEVEL),
			tag.getFloat(NBT_XP),
			tag.getInteger(NBT_SKILLPOINTS),
			tag.getInteger(NBT_CONTROL),
			tag.getInteger(NBT_TECH),
			tag.getInteger(NBT_FINESSE),
			tag.getInteger(NBT_MANA),
			tag.getFloat(NBT_MOD_MANA),
			tag.getFloat(NBT_MOD_MANA_COST),
			tag.getFloat(NBT_MOD_MANA_REGEN));
			
		// LORE
		NBTTagCompound compound = tag.getCompoundTag(NBT_LORELEVELS);
		for (String key : compound.getKeySet()) {
			Integer level = compound.getInteger(key);
			instance.deserializeLore(key, level);
		}
		
		// SPELLS
		NBTTagList list = tag.getTagList(NBT_SPELLCRCS, NBT.TAG_STRING);
		for (int i = 0; i < list.tagCount(); i++) {
			instance.deserializeSpells(list.getStringTagAt(i));
		}
		
		// KNOWNELEMENTS
		compound = tag.getCompoundTag(NBT_KNOWN_ELEMENTS);
		for (String key : compound.getKeySet()) {
			Boolean val = compound.getBoolean(key);
			if (val != null && val) {
				EMagicElement elem = EMagicElement.valueOf(key);
				instance.learnElement(elem);
			}
		}
		
		// ELEMENTS
		compound = tag.getCompoundTag(NBT_MASTERED_ELEMENTS);
		for (String key : compound.getKeySet()) {
			int val = compound.getInteger(key);
			if (val != 0) {
				EMagicElement elem = EMagicElement.valueOf(key);
				instance.setElementMastery(elem, val);
			}
		}
		
		compound = tag.getCompoundTag(NBT_ELEMENT_TRIALS);
		for (String key : compound.getKeySet()) {
			Boolean val = compound.getBoolean(key);
			if (val != null && val) {
				EMagicElement elem = EMagicElement.valueOf(key);
				instance.startTrial(elem);
			}
		}
		
		// SHAPES
		list = tag.getTagList(NBT_SHAPES, NBT.TAG_STRING);
		for (int i = 0; i < list.tagCount(); i++) {
			SpellShape shape = SpellShape.get(list.getStringTagAt(i));
			instance.addShape(shape);
		}
		
		// TRIGGERS
		list = tag.getTagList(NBT_TRIGGERS, NBT.TAG_STRING);
		for (int i = 0; i < list.tagCount(); i++) {
			SpellTrigger trigger = SpellTrigger.get(list.getStringTagAt(i));
			instance.addTrigger(trigger);
		}
		
		// ALTERATIONS

		compound = tag.getCompoundTag(NBT_ALTERATIONS);
		for (String key : compound.getKeySet()) {
			Boolean val = compound.getBoolean(key);
			if (val != null && val) {
				EAlteration elem = EAlteration.valueOf(key);
				instance.unlockAlteration(elem);
			}
		}
		
		// Mark Location
		if (tag.hasKey(NBT_MARK_POS, NBT.TAG_COMPOUND)) {
			NBTTagCompound posTag = tag.getCompoundTag(NBT_MARK_POS);
			BlockPos location = new BlockPos(
					posTag.getInteger("x"),
					posTag.getInteger("y"),
					posTag.getInteger("z")
					);
			int dimension = tag.getInteger(NBT_MARK_DIMENSION);
			
			instance.setMarkLocation(dimension, location);
		}
		
		// Quests
		if (tag.hasKey(NBT_QUESTS_CURRENT, NBT.TAG_LIST)) {
			NBTTagList tagList = tag.getTagList(NBT_QUESTS_CURRENT, NBT.TAG_STRING);
			for (int i = 0; i < tagList.tagCount(); i++) {
				String quest = tagList.getStringTagAt(i);
				instance.addQuest(quest);
			}
		}
		if (tag.hasKey(NBT_QUESTS_COMPLETED, NBT.TAG_LIST)) {
			NBTTagList tagList = tag.getTagList(NBT_QUESTS_COMPLETED, NBT.TAG_STRING);
			for (int i = 0; i < tagList.tagCount(); i++) {
				String quest = tagList.getStringTagAt(i);
				instance.addQuest(quest);
				instance.completeQuest(quest);
			}
		}
		Map<String, IObjectiveState> data = new HashMap<>();
		if (tag.hasKey(NBT_QUESTS_DATA, NBT.TAG_COMPOUND)) {
			NBTTagCompound dataTag = tag.getCompoundTag(NBT_QUESTS_DATA);
			for (String key : dataTag.getKeySet()) {
				NostrumQuest quest = NostrumQuest.lookup(key);
				if (quest == null)
					continue;
				
				if (quest.getObjective() != null) {
					IObjectiveState state = quest.getObjective().getBaseState();
					state.fromNBT(dataTag.getCompoundTag(key));
					data.put(key, state);
				}
			}
		}
		instance.setQuestDataMap(data);
		
		if (tag.hasKey(NBT_BINDING_COMPONENT, NBT.TAG_STRING)
				&& tag.hasKey(NBT_BINDING_SPELL, NBT.TAG_INT)
				&& tag.hasKey(NBT_BINDING_TOME_ID, NBT.TAG_INT)) {
			Spell spell = NostrumMagica.spellRegistry.lookup(tag.getInteger(NBT_BINDING_SPELL));
			if (spell != null) {
				SpellComponentWrapper comp = SpellComponentWrapper.fromKeyString(tag.getString(NBT_BINDING_COMPONENT));
				int tomeID = tag.getInteger(NBT_BINDING_TOME_ID);
				instance.startBinding(spell, comp, tomeID);
			} else {
				NostrumMagica.logger.warn("Found illegal spell ID. Player will lose binding information");
			}
		}
		
		if (tag.hasKey(NBT_SPELLKNOWLEDGE, NBT.TAG_COMPOUND)) {
			compound = tag.getCompoundTag(NBT_SPELLKNOWLEDGE);
			for (String key : compound.getKeySet()) {
				try {
					EMagicElement elem = EMagicElement.valueOf(key);
					NBTTagCompound subtag = compound.getCompoundTag(key);
					for (String altKey : subtag.getKeySet()) {
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
	}

}
