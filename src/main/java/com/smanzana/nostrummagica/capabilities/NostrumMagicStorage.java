package com.smanzana.nostrummagica.capabilities;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.smanzana.nostrummagica.capabilities.INostrumMagic.TransmuteKnowledge;
import com.smanzana.nostrummagica.capabilities.INostrumMagic.VanillaRespawnInfo;
import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.NetUtils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.Constants.NBT;

public class NostrumMagicStorage implements IStorage<INostrumMagic> {

	private static final String NBT_TIER = "tier";
	private static final String NBT_LEVEL = "level";
	private static final String NBT_XP = "xp";
	private static final String NBT_SKILLPOINTS = "skillpoints";
	private static final String NBT_ELEMENTAL_SKILLPOINTS = "elemental_skillpoints";
	private static final String NBT_ELEMENTAL_XP = "elemental_xp";
	private static final String NBT_RESEARCHPOINTS = "researchpoints";
	private static final String NBT_MANA = "mana";
	private static final String NBT_RESERVED_MANA = "reserved_mana";
	
	private static final String NBT_MOD_MANA = "mod_mana";
	private static final String NBT_MOD_MANA_COST = "mod_mana_cost";
	private static final String NBT_MOD_MANA_REGEN = "mod_mana_regen";
	private static final String NBT_MOD_MANA_BONUS = "mod_mana_bonus";
	private static final String NBT_MOD_INTERNAL_ID = "id";
	private static final String NBT_MOD_INTERNAL_VALUE = "value";
	
	private static final String NBT_SKILLS = "skills";
	
	//private static final String NBT_FAMILIARS = "familiars";
	
	private static final String NBT_LORELEVELS = "lore";
	private static final String NBT_SPELLCRCS = "spellcrcs"; // spells we've done's CRCs
	private static final String NBT_MASTERED_ELEMENTS = "mastered_elements";
	private static final String NBT_ELEMENT_TRIALS = "element_trials";
	private static final String NBT_SHAPES = "shapes"; // list of shape keys
	private static final String NBT_ALTERATIONS = "alterations";
	
	private static final String NBT_MARK_DIMENSION = "mark_dim";
	private static final String NBT_MARK_POS = "mark_pos";
	private static final String NBT_ENHANCED_TELEPORT = "enhanced_teleport";
	
	private static final String NBT_QUESTS_COMPLETED = "quests_completed";
	private static final String NBT_QUESTS_CURRENT = "quests_current";
	
	private static final String NBT_RESEARCHES = "research_completed";
	
	private static final String NBT_SPELLKNOWLEDGE = "spell_knowledge";
	
	private static final String NBT_SORCERYPORTAL_DIM = "sorcery_portal_dim";
	private static final String NBT_SORCERYPORTAL_POS = "sorcery_portal_pos";
	
	private static final String NBT_SAVEDRESPAWN_DIM = "saved_respawn_dim";
	private static final String NBT_SAVEDRESPAWN_POS = "saved_respawn_pos";
	private static final String NBT_SAVEDRESPAWN_YAW = "saved_respawn_yaw";
	private static final String NBT_SAVEDRESPAWN_FORCE = "saved_respawn_force";
	
	private static final String NBT_TRANSMUTE_KNOWLEDGE = "transmute_knowledge";
	
	@Override
	public INBT writeNBT(Capability<INostrumMagic> capability, INostrumMagic instance, Direction side) {
		CompoundNBT nbt = new CompoundNBT();
		
		nbt.putString(NBT_TIER, instance.getTier().name().toLowerCase());
		nbt.putInt(NBT_LEVEL, instance.getLevel());
		nbt.putFloat(NBT_XP, instance.getXP());
		nbt.putInt(NBT_SKILLPOINTS, instance.getSkillPoints());
		nbt.putInt(NBT_RESEARCHPOINTS, instance.getResearchPoints());
		nbt.putInt(NBT_MANA, instance.getMana());
		nbt.putInt(NBT_RESERVED_MANA, instance.getReservedMana());
		
		nbt.put(NBT_ELEMENTAL_SKILLPOINTS, NetUtils.ToNBT(instance.getElementalSkillPointsMap(), IntNBT::valueOf));
		nbt.put(NBT_ELEMENTAL_XP, NetUtils.ToNBT(instance.getElementalXPMap(), IntNBT::valueOf));
		
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
			list.add(StringNBT.valueOf(crc));
		}
		nbt.put(NBT_SPELLCRCS, list);
		
		compound = new CompoundNBT();
		{
			Map<EMagicElement, EElementalMastery> map = instance.serializeElementMastery();
			for (EMagicElement key : map.keySet()) {
				compound.put(key.name(), map.get(key).toNBT());
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
		{
			Map<UUID, Float> map = instance.getManaModifiers();
			for (UUID id : map.keySet()) {
				if (id == null) continue;
				
				compound = new CompoundNBT();
				compound.putUniqueId(NBT_MOD_INTERNAL_ID, id);
				compound.putFloat(NBT_MOD_INTERNAL_VALUE, map.get(id));
				list.add(compound);
			}
		}
		nbt.put(NBT_MOD_MANA, list);
		
		list = new ListNBT();
		{
			Map<UUID, Float> map = instance.getManaCostModifiers();
			for (UUID id : map.keySet()) {
				if (id == null) continue;
				
				compound = new CompoundNBT();
				compound.putUniqueId(NBT_MOD_INTERNAL_ID, id);
				compound.putFloat(NBT_MOD_INTERNAL_VALUE, map.get(id));
				list.add(compound);
			}
		}
		nbt.put(NBT_MOD_MANA_COST, list);
		
		list = new ListNBT();
		{
			Map<UUID, Float> map = instance.getManaRegenModifiers();
			for (UUID id : map.keySet()) {
				if (id == null) continue;
				
				compound = new CompoundNBT();
				compound.putUniqueId(NBT_MOD_INTERNAL_ID, id);
				compound.putFloat(NBT_MOD_INTERNAL_VALUE, map.get(id));
				list.add(compound);
			}
		}
		nbt.put(NBT_MOD_MANA_REGEN, list);
		
		list = new ListNBT();
		{
			Map<UUID, Integer> map = instance.getManaBonusModifiers();
			for (UUID id : map.keySet()) {
				if (id == null) continue;
				
				compound = new CompoundNBT();
				compound.putUniqueId(NBT_MOD_INTERNAL_ID, id);
				compound.putInt(NBT_MOD_INTERNAL_VALUE, map.get(id));
				list.add(compound);
			}
		}
		nbt.put(NBT_MOD_MANA_BONUS, list);
		
		list = new ListNBT();
		for (SpellShape shape : instance.getShapes()) {
			String key = shape.getShapeKey();
			list.add(StringNBT.valueOf(key));
		}
		nbt.put(NBT_SHAPES, list);
		
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
			nbt.putString(NBT_MARK_DIMENSION, instance.getMarkDimension().getLocation().toString());
			nbt.put(NBT_MARK_POS, posTag);
		}
		if (instance.hasEnhancedTeleport()) {
			nbt.putBoolean(NBT_ENHANCED_TELEPORT, true);
		}
		
		List<String> stringList = instance.getCurrentQuests();
		if (stringList != null && !stringList.isEmpty()) {
			ListNBT tagList = new ListNBT();
			for (String quest : stringList) {
				tagList.add(StringNBT.valueOf(quest));
			}
			nbt.put(NBT_QUESTS_CURRENT, tagList);
		}
		
		stringList = instance.getCompletedQuests();
		if (stringList != null && !stringList.isEmpty()) {
			ListNBT tagList = new ListNBT();
			for (String quest : stringList) {
				tagList.add(StringNBT.valueOf(quest));
			}
			nbt.put(NBT_QUESTS_COMPLETED, tagList);
		}
		
		stringList = instance.getCompletedResearches();
		if (stringList != null && !stringList.isEmpty()) {
			ListNBT tagList = new ListNBT();
			for (String research : stringList) {
				tagList.add(StringNBT.valueOf(research));
			}
			nbt.put(NBT_RESEARCHES, tagList);
		}
		
		Collection<Skill> skills = instance.getSkills();
		if (skills != null && !skills.isEmpty()) {
			ListNBT tagList = new ListNBT();
			for (Skill skill : skills) {
				tagList.add(StringNBT.valueOf(skill.getKey().toString()));
			}
			nbt.put(NBT_SKILLS, tagList);
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
			nbt.putString(NBT_SORCERYPORTAL_DIM, instance.getSorceryPortalDimension().getRegistryName().toString());
			nbt.put(NBT_SORCERYPORTAL_POS, NBTUtil.writeBlockPos(instance.getSorceryPortalPos()));
		}
		
		final VanillaRespawnInfo respawnInfo = instance.getSavedRespawnInfo();
		if (respawnInfo != null) {
			nbt.putString(NBT_SAVEDRESPAWN_DIM, respawnInfo.dimension.getLocation().toString());
			nbt.put(NBT_SAVEDRESPAWN_POS, NBTUtil.writeBlockPos(respawnInfo.pos));
			nbt.putFloat(NBT_SAVEDRESPAWN_YAW, respawnInfo.yaw);
			nbt.putBoolean(NBT_SAVEDRESPAWN_FORCE, respawnInfo.forced);
		}
		
		list = new ListNBT();
		for (Entry<TransmuteKnowledge, Boolean> entry : instance.getTransmuteKnowledge().entrySet()) {
			if (entry.getValue() == null || !entry.getValue()) {
				continue;
			}
			CompoundNBT subtag = entry.getKey().toNBT();
			list.add(subtag);
		}
		nbt.put(NBT_TRANSMUTE_KNOWLEDGE, list);
		
		return nbt;
	}

	@Override
	public void readNBT(Capability<INostrumMagic> capability, INostrumMagic instance, Direction side, INBT nbt) {
		CompoundNBT tag = (CompoundNBT) nbt;
		EMagicTier tier = EMagicTier.LOCKED;
		try {
			tier = EMagicTier.valueOf(tag.getString(NBT_TIER).toUpperCase());
		} catch (Exception e) {
			tier = EMagicTier.LOCKED;
		}
		instance.deserialize(
			tier,
			tag.getInt(NBT_LEVEL),
			tag.getFloat(NBT_XP),
			tag.getInt(NBT_SKILLPOINTS),
			tag.getInt(NBT_RESEARCHPOINTS),
			tag.getInt(NBT_MANA),
			tag.getInt(NBT_RESERVED_MANA)
			);
		
		Map<EMagicElement, Integer> elementalSkillPoints = new EnumMap<>(EMagicElement.class);
		NetUtils.FromNBT(elementalSkillPoints, EMagicElement.class, tag.getCompound(NBT_ELEMENTAL_SKILLPOINTS), (p) -> ((IntNBT) p).getInt());
		instance.setElementalSkillPointMap(elementalSkillPoints);
		
		Map<EMagicElement, Integer> elementalXP = new EnumMap<>(EMagicElement.class);
		NetUtils.FromNBT(elementalXP, EMagicElement.class, tag.getCompound(NBT_ELEMENTAL_XP), (p) -> ((IntNBT) p).getInt());
		instance.setElementalXPMap(elementalXP);
			
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
		
		// ELEMENTS
		compound = tag.getCompound(NBT_MASTERED_ELEMENTS);
		for (String key : compound.keySet()) {
			EMagicElement elem = EMagicElement.valueOf(key);
			instance.setElementalMastery(elem, EElementalMastery.fromNBT(compound.get(key)));
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
			String dimension = tag.getString(NBT_MARK_DIMENSION);
			RegistryKey<World> dimKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, ResourceLocation.tryCreate(dimension));
			
			instance.setMarkLocation(dimKey, location);
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
		
		if (tag.contains(NBT_RESEARCHES, NBT.TAG_LIST)) {
			ListNBT tagList = tag.getList(NBT_RESEARCHES, NBT.TAG_STRING);
			for (int i = 0; i < tagList.size(); i++) {
				String research = tagList.getString(i);
				instance.completeResearch(research);
			}
		}
		
		if (tag.contains(NBT_SKILLS, NBT.TAG_LIST)) {
			ListNBT tagList = tag.getList(NBT_SKILLS, NBT.TAG_STRING);
			for (int i = 0; i < tagList.size(); i++) {
				String raw = tagList.getString(i);
				ResourceLocation loc = new ResourceLocation(raw);
				if (Skill.lookup(loc) != null) {
					instance.addSkill(Skill.lookup(loc));
				}
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
			String dimName = tag.getString(NBT_SORCERYPORTAL_DIM);
			RegistryKey<World> dim = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, ResourceLocation.tryCreate(dimName));
			instance.setSorceryPortalLocation(
					dim,
					NBTUtil.readBlockPos(tag.getCompound(NBT_SORCERYPORTAL_POS))); // Warning: can break if save used across game versions
		}
		
		if (tag.contains(NBT_SAVEDRESPAWN_DIM)) {
			final RegistryKey<World> dim = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(tag.getString(NBT_SAVEDRESPAWN_DIM)));
			final BlockPos pos = NBTUtil.readBlockPos(tag.getCompound(NBT_SAVEDRESPAWN_POS));
			final float yaw = tag.getFloat(NBT_SAVEDRESPAWN_YAW);
			final boolean forced = tag.getBoolean(NBT_SAVEDRESPAWN_FORCE);
			instance.setSavedRespawnInfo(new VanillaRespawnInfo(dim, pos, yaw, forced));
		} else {
			instance.setSavedRespawnInfo(null);
		}
		
		// Modifiers
		Map<UUID, Float> modMana = new HashMap<>();
		Map<UUID, Integer> modManaFlat = new HashMap<>();
		Map<UUID, Float> modManaCost = new HashMap<>();
		Map<UUID, Float> modManaRegen = new HashMap<>();
		
		if (tag.contains(NBT_MOD_MANA, NBT.TAG_LIST)) {
			ListNBT tagList = tag.getList(NBT_MOD_MANA, NBT.TAG_COMPOUND);
			
			for (int i = 0; i < tagList.size(); i++) {
				CompoundNBT subtag = tagList.getCompound(i);
				UUID id = subtag.getUniqueId(NBT_MOD_INTERNAL_ID);
				float val = subtag.getFloat(NBT_MOD_INTERNAL_VALUE);
				if (id != null) {
					modMana.put(id, val);
				}
			}
		}
		
		if (tag.contains(NBT_MOD_MANA_BONUS, NBT.TAG_LIST)) {
			ListNBT tagList = tag.getList(NBT_MOD_MANA_BONUS, NBT.TAG_COMPOUND);
			
			for (int i = 0; i < tagList.size(); i++) {
				CompoundNBT subtag = tagList.getCompound(i);
				UUID id = subtag.getUniqueId(NBT_MOD_INTERNAL_ID);
				int val = subtag.getInt(NBT_MOD_INTERNAL_VALUE);
				if (id != null) {
					modManaFlat.put(id, val);
				}
			}
		}
		
		if (tag.contains(NBT_MOD_MANA_COST, NBT.TAG_LIST)) {
			ListNBT tagList = tag.getList(NBT_MOD_MANA_COST, NBT.TAG_COMPOUND);
			
			for (int i = 0; i < tagList.size(); i++) {
				CompoundNBT subtag = tagList.getCompound(i);
				UUID id = subtag.getUniqueId(NBT_MOD_INTERNAL_ID);
				float val = subtag.getFloat(NBT_MOD_INTERNAL_VALUE);
				if (id != null) {
					modManaCost.put(id, val);
				}
			}
		}
		
		if (tag.contains(NBT_MOD_MANA_REGEN, NBT.TAG_LIST)) {
			ListNBT tagList = tag.getList(NBT_MOD_MANA_REGEN, NBT.TAG_COMPOUND);
			
			for (int i = 0; i < tagList.size(); i++) {
				CompoundNBT subtag = tagList.getCompound(i);
				UUID id = subtag.getUniqueId(NBT_MOD_INTERNAL_ID);
				float val = subtag.getFloat(NBT_MOD_INTERNAL_VALUE);
				if (id != null) {
					modManaRegen.put(id, val);
				}
			}
		}
		
		instance.setModifierMaps(modMana, modManaFlat, modManaCost, modManaRegen);
		
		if (tag.contains(NBT_TRANSMUTE_KNOWLEDGE, NBT.TAG_LIST)) {
			ListNBT tagList = tag.getList(NBT_TRANSMUTE_KNOWLEDGE, NBT.TAG_COMPOUND);
			
			for (int i = 0; i < tagList.size(); i++) {
				CompoundNBT subtag = tagList.getCompound(i);
				TransmuteKnowledge knowledge = TransmuteKnowledge.fromNBT(subtag);
				instance.giveTransmuteKnowledge(knowledge.key, knowledge.level);
			}
		}
	}

}
