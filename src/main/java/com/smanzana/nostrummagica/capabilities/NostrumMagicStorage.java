package com.smanzana.nostrummagica.capabilities;

import java.util.Map;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
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
	private static final String NBT_MAXMANA = "maxmana";
	
	//private static final String NBT_FAMILIARS = "familiars";
	private static final String NBT_BINDING = "binding"; // TODO binding interface
	
	private static final String NBT_LORELEVELS = "lore";
	private static final String NBT_SPELLCRCS = "spellcrcs"; // spells we've done's CRCs
	private static final String NBT_ELEMENTS = "elements";
	private static final String NBT_SHAPES = "shapes"; // list of shape keys
	private static final String NBT_TRIGGERS = "triggers"; // list of trigger keys
	private static final String NBT_ALTERATIONS = "alterations";
	
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
		nbt.setInteger(NBT_MAXMANA, instance.getMaxMana());
		
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
			Map<EMagicElement, Boolean> map = instance.serializeElements();
			for (EMagicElement key : map.keySet()) {
				compound.setBoolean(key.name(), map.get(key));
			}
		}
		nbt.setTag(NBT_ELEMENTS, compound);
		
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
			tag.getInteger(NBT_MAXMANA));
		
		// LORE
		NBTTagCompound compound = tag.getCompoundTag(NBT_LORELEVELS);
		for (String key : tag.getKeySet()) {
			Integer level = tag.getInteger(key);
			instance.deserializeLore(key, level);
		}
		
		// SPELLS
		NBTTagList list = tag.getTagList(NBT_SPELLCRCS, NBT.TAG_STRING);
		for (int i = 0; i < list.tagCount(); i++) {
			instance.deserializeSpells(list.getStringTagAt(i));
		}
		
		// ELEMENTS
		compound = tag.getCompoundTag(NBT_ELEMENTS);
		for (String key : tag.getKeySet()) {
			Boolean val = tag.getBoolean(key);
			if (val != null && val) {
				EMagicElement elem = EMagicElement.valueOf(key);
				instance.unlockElement(elem);
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
			instance.addShape(trigger);
		}
		
		// ALTERATIONS

		compound = tag.getCompoundTag(NBT_ALTERATIONS);
		for (String key : tag.getKeySet()) {
			Boolean val = tag.getBoolean(key);
			if (val != null && val) {
				EAlteration elem = EAlteration.valueOf(key);
				instance.unlockAlteration(elem);
			}
		}
	}

}
