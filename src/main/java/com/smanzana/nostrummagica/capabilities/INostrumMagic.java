package com.smanzana.nostrummagica.capabilities;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.quests.objectives.IObjectiveState;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public interface INostrumMagic {
	
	public static class TransmuteKnowledge {
		public final String key;
		public final int level;
		
		public TransmuteKnowledge(String key, int level) {
			this.key = key;
			this.level = level;
		}
		
		public CompoundNBT toNBT() {
			CompoundNBT tag = new CompoundNBT();
			tag.putString("key", key);
			tag.putInt("level", level);
			return tag;
		}
		
		public static TransmuteKnowledge fromNBT(CompoundNBT tag) {
			return new TransmuteKnowledge(tag.getString("key"), tag.getInt("level"));
		}
		
		@Override
		public int hashCode() {
			return key.hashCode() * 67 + Integer.hashCode(level);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof TransmuteKnowledge) {
				return ((TransmuteKnowledge) o).key.equals(key) && ((TransmuteKnowledge) o).level == level;
			}
			
			return false;
		}
	}
	
	public static enum ElementalMastery {
		UNKNOWN,
		NOVICE,
		ADEPT,
		MASTER;
		
		private ElementalMastery() {
			
		}
		
		public INBT toNBT() {
			return new StringNBT(this.name());
		}
		
		public static ElementalMastery fromNBT(INBT nbt) {
			try {
				return ElementalMastery.valueOf(
						((StringNBT) nbt).getString().toUpperCase()
					);
			} catch (Exception e) {
				return ElementalMastery.NOVICE;
			}
		}
		
		public String getTranslationKey() {
			return this.name().toLowerCase();
		}
		
		public boolean isGreaterOrEqual(ElementalMastery other) {
			// Cheat and use ordinals
			return this.ordinal() >= other.ordinal();
		}
	}

	// Unlock
	public boolean isUnlocked();
	public void unlock();
	
	// Progression
	public int getLevel();
	public float getXP();
	public float getMaxXP();
	public void addXP(float xp);
	public void setLevel(int level);
	
	public int getSkillPoints();
	public void addSkillPoint();
	public void takeSkillPoint();
	
	public int getResearchPoints();
	public void addResearchPoint();
	public void takeResearchPoint();
	
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
	public int getMaxMana(boolean includeReserved);
	public void setMana(int mana);
	public void addMana(int mana);
	public void setMaxMana(int max);
	public int getReservedMana();
	public void setReservedMana(int reserved);
	public void addReservedMana(int reserved);
	
	// Modifiers
	public float getManaModifier(); // % bonus
	public int getManaBonus(); // flat int bonus
	public float getManaRegenModifier();
	public float getManaCostModifier();
	
	public void addManaModifier(UUID id, float modifier);
	public void addManaRegenModifier(UUID id, float modifier);
	public void addManaCostModifier(UUID id, float modifier);
	public void addManaBonus(UUID id, int bonus);
	public void removeManaModifier(UUID id);
	public void removeManaRegenModifier(UUID id);
	public void removeManaCostModifier(UUID id);
	public void removeManaBonus(UUID id);
	
	// Familiars
	public List<LivingEntity> getFamiliars();
	public void addFamiliar(LivingEntity familiar);
	public void clearFamiliars();
	
	// Binding
	public boolean isBinding();
	public SpellComponentWrapper getBindingComponent(); // Get current needed component
	public Spell getBindingSpell();
	public int getBindingID();
	public void startBinding(Spell spell, SpellComponentWrapper component, int tomeID);
	public void completeBinding(ItemStack tome); // if tome == null, will require it be in player inventory
	
	// Lore
	public boolean hasLore(ILoreTagged tagged);
	public boolean hasFullLore(ILoreTagged tagged);
	public List<ILoreTagged> getAllLore();
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
	public Map<EAlteration, Boolean> getAlterations();
	public void unlockAlteration(EAlteration alteration);
	
	// Elemental mastery
	public Map<EMagicElement, Boolean> getKnownElements();
	public boolean setElementalMastery(EMagicElement element, ElementalMastery mastery);
	public ElementalMastery getElementalMastery(EMagicElement element);
	
	// Element Trials
	public void startTrial(EMagicElement element);
	public void endTrial(EMagicElement element);
	public boolean hasTrial(EMagicElement element);
	
	// Mark/recall
	public void setMarkLocation(int dimension, BlockPos location);
	public BlockPos getMarkLocation();
	public int getMarkDimension();
	public void unlockEnhancedTeleport();
	public boolean hasEnhancedTeleport();
	
	// Transmute knowledge
	public void giveTransmuteKnowledge(String key, int level);
	public boolean hasTransmuteKnowledge(String key, int level);
	
	// Serialization/Deserialization. Do not call.
	public void deserialize(
			boolean unlocked,
			int level,
			float xp,
			int skillpoints,
			int researchpoints,
			int control,
			int tech,
			int finesse,
			int mana,
			int reserved_mana
			);
	
	public Map<String, Integer> serializeLoreLevels();
	public Set<String> serializeSpellHistory();
	public Map<EMagicElement, ElementalMastery> serializeElementMastery();
	public Map<EMagicElement, Boolean> serializeElementTrials();
	public Map<EAlteration, Boolean> serializeAlterations();
	public Map<UUID, Float> getManaModifiers();
	public Map<UUID, Integer> getManaBonusModifiers();
	public Map<UUID, Float> getManaCostModifiers();
	public Map<UUID, Float> getManaRegenModifiers();
	public Map<TransmuteKnowledge, Boolean> getTransmuteKnowledge();
	public void deserializeLore(String key, Integer level);
	public void deserializeSpells(String crc);
	public void setModifierMaps(Map<UUID, Float> modifiers_mana,
			Map<UUID, Integer> modifiers_bonus_mana,
			Map<UUID, Float> modifiers_cost,
			Map<UUID, Float> modifiers_regen);
	public void setTransmuteKnowledge(Map<TransmuteKnowledge, Boolean> map);
	
	// Copy fields out of
	public void copy(INostrumMagic cap);
	public void provideEntity(LivingEntity entity);
	
	// Quests
	public List<String> getCompletedQuests();
	public List<String> getCurrentQuests();
	public void addQuest(String quest);
	public void completeQuest(String quest);
	public IObjectiveState getQuestData(String quest);
	public void setQuestData(String quest, IObjectiveState data);
	public Map<String, IObjectiveState> getQuestDataMap();
	public void setQuestDataMap(Map<String, IObjectiveState> map);
	
	// Research
	public List<String> getCompletedResearches();
	public void completeResearch(String research);
	
	// Spell Knowledge
	public boolean hasKnowledge(EMagicElement element, EAlteration alteration);
	public void setKnowledge(EMagicElement element, EAlteration alteration);
	public Map<EMagicElement, Map<EAlteration, Boolean>> getSpellKnowledge();
	
	// Sorcery Portal
	public DimensionType getSorceryPortalDimension();
	public BlockPos getSorceryPortalPos();
	public void clearSorceryPortal();
	public void setSorceryPortalLocation(DimensionType dimension, BlockPos pos);
	
	// Refresh attributes and rescan for them
	public void refresh(ServerPlayerEntity player);
}
