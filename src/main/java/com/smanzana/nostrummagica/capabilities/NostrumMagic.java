package com.smanzana.nostrummagica.capabilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.criteria.TierCriteriaTrigger;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.LoreMessage;
import com.smanzana.nostrummagica.network.message.TutorialMessage;
import com.smanzana.nostrummagica.progression.research.NostrumResearches;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.progression.tutorial.NostrumTutorial;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.stat.PlayerStat;
import com.smanzana.nostrummagica.stat.PlayerStatTracker;
import com.smanzana.nostrummagica.util.NetUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Default implementation of the INostrumMagic interface
 * @author Skyler
 *
 */
public class NostrumMagic implements INostrumMagic {
	
	public static class LevelCurves {
		
		private static final float xpGrowth = 1.65f;
		private static final float xpBase = 100.0f;
		private static final float manaGrowth = 1.2f;
		private static final float manaBase = 100.0f;
		
		public static float maxXP(int level) {
			return (float) (xpBase * Math.pow(xpGrowth, level - 1));
		}
		
		public static int maxMana(int level) {
			return (int) (manaBase * Math.pow(manaGrowth, level - 1));
		}
	}
	
	public static class KnowledgeCurves {
	
		private static final int knowledgeGrowth = 2;
		private static final int knowledgeBase = 2;
		
		public static int maxKnowledge(int level) {
			return knowledgeBase + (knowledgeGrowth * (level - 1));
		}
		
		public static int knowledgeLevel(int knowledge) {
			if (knowledge < knowledgeBase) {
				return 1;
			}
			
			knowledge -= knowledgeBase;
			return 2 + knowledge / knowledgeGrowth;
		}
	}
	
	public static class ElementCurves {
	
		private static final int growth = 100;
		private static final int base = 50;
		
		public static int maxXP(int level) {
			return base + (growth * (level - 1));
		}
		
		public static int elementalSkillLevel(int xp) {
			if (xp < base) {
				return 1;
			}
			
			xp -= base;
			return 2 + xp / growth;
		}
	}
	
	private EMagicTier tier;
	private int level;
	private float xp;
	private float maxxp;
	private int skillPoints;
	private Map<EMagicElement, Integer> elementalSkillPoints;
	private Map<EMagicElement, Integer> elementalXP;
	private int researchPoints;
	private int mana;
	//private int maxMana; // We calculate max instead of storing it
	private int reservedMana; // Mana deducted from max cause it's being actively used for an ongoing effect
	private Map<UUID, Float> modMana; // Additional % mana
	private Map<UUID, Integer> modManaFlat; // Additiona mana (flat int value)
	private Map<UUID, Float> modManaCost;
	private Map<UUID, Float> modManaRegen;
	
	private int baseMaxMana; // Max mana without mana bonuses
	
	private List<LivingEntity> familiars;
	
	private Map<String, Integer> loreLevels;
	private Set<String> spellCRCs; // spells we've done's CRCs
	private Map<EMagicElement, EElementalMastery> elementalMastery;
	private Map<EMagicElement, Boolean> elementTrials;
	private List<SpellShape> shapes; // list of shape keys
	private Map<EAlteration, Boolean> alterations;
	private List<String> completedQuests;
	private List<String> currentQuests;
	private List<ResourceLocation> completedResearch;
	private Set<Skill> skills;
	private BlockPos markLocation;
	private ResourceKey<Level> markDimension;
	private boolean enhancedTeleport;
	private Map<EMagicElement, Map<EAlteration, Boolean>> spellKnowledge;
	private ResourceKey<Level> sorceryPortalDim;
	private BlockPos sorceryPortalPos;
	private @Nullable VanillaRespawnInfo savedRespawnInfo;
	private Map<TransmuteKnowledge, Boolean> transmuteKnowledge;
	
	private final LivingEntity entity;
	
	public NostrumMagic(LivingEntity entity) {
		tier = EMagicTier.LOCKED;
		//familiars = new ArrayList<>();
		loreLevels = new HashMap<>();
		spellCRCs = new HashSet<>();
		elementalMastery = new EnumMap<>(EMagicElement.class);
		elementTrials = new EnumMap<>(EMagicElement.class);
		shapes = new ArrayList<>();
		alterations = new EnumMap<>(EAlteration.class);
		currentQuests = new ArrayList<>();
		completedQuests = new ArrayList<>();
		completedResearch = new ArrayList<>();
		familiars = new ArrayList<>();
		sorceryPortalDim = Level.OVERWORLD;
		sorceryPortalPos = null;
		savedRespawnInfo = null;
		enhancedTeleport = false;
		elementalSkillPoints = new EnumMap<>(EMagicElement.class);
		transmuteKnowledge = new HashMap<>();
		skills = new HashSet<>();
		elementalXP = new EnumMap<>(EMagicElement.class);
		
		modMana = new HashMap<>();
		modManaFlat = new HashMap<>();
		modManaCost = new HashMap<>();
		modManaRegen = new HashMap<>();
		
		this.entity = entity;
	}

	@Override
	public boolean isUnlocked() {
		return this.getTier() != EMagicTier.LOCKED;
	}

	@Override
	public void unlock() {
		if (!isUnlocked()) {
			tier = EMagicTier.MANI;
			level = 1;
			xp = 0;
			baseMaxMana = LevelCurves.maxMana(1);
			mana = getMaxMana();
			maxxp = LevelCurves.maxXP(1);
			skillPoints = 0;
			
			this.setElementalMastery(EMagicElement.NEUTRAL, EElementalMastery.NOVICE);
			this.completeResearch(NostrumResearches.Origin);
			this.addResearchPoint();
			//this.completeResearch("spellcraft");
//			this.giveBasicLore(NostrumItems.GetRune(new SpellComponentWrapper(EMagicElement.FIRE)));
//			this.giveBasicLore(NostrumItems.blankScroll);
//			this.giveBasicLore(NostrumItems.spellScroll);
//			this.giveBasicLore(NostrumItems.reagentMandrakeRoot);
			
			if (this.entity != null && this.entity instanceof ServerPlayer player) {
				NetworkHandler.sendTo(new TutorialMessage(NostrumTutorial.CAST_SPELL), player);
			}
		}
	}

	@Override
	public EMagicTier getTier() {
		return this.tier;
	}

	@Override
	public void setTier(EMagicTier tier) {
		if (tier != EMagicTier.LOCKED) {
			this.unlock();
		}
		this.tier = tier;
		
		if (this.entity != null && this.entity instanceof ServerPlayer) {
			TierCriteriaTrigger.Instance.trigger((ServerPlayer) this.entity, tier);
			
			if (tier.isGreaterOrEqual(EMagicTier.KANI)) {
				this.giveFullLore(LoreRegistry.QuickCastLore);
			}
			if (tier.isGreaterOrEqual(EMagicTier.VANI)) {
				this.giveFullLore(LoreRegistry.SpellSavingLore);
			}
		}
	}
	
	private void levelup() {

		level++;
		this.addSkillPoint();
		this.addResearchPoint();
		this.addResearchPoint();
		this.addResearchPoint();
		setLevel(level);
		
		if (entity != null)
			NostrumMagicaSounds.LEVELUP.play(entity);
		// TODO cool effects bruh
	}
	
	@Override
	public void setLevel(int level) {
		this.level = level;
		baseMaxMana = LevelCurves.maxMana(level);
		mana = getMaxMana(); 
		maxxp = LevelCurves.maxXP(level);
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
	public int getElementalSkillPoints(EMagicElement element) {
		return this.elementalSkillPoints.getOrDefault(element, 0);
	}

	@Override
	public void addElementalSkillPoint(EMagicElement element) {
		this.elementalSkillPoints.merge(element, 1, Integer::sum);
	}

	@Override
	public void takeElementalSkillPoint(EMagicElement element) {
		this.elementalSkillPoints.put(element, Math.max(0, this.getElementalSkillPoints(element) - 1));
	}
	
	@Override
	public int getResearchPoints() {
		return researchPoints;
	}

	@Override
	public void addResearchPoint() {
		this.researchPoints++;
	}

	@Override
	public void takeResearchPoint() {
		if (this.researchPoints > 0)
			this.researchPoints--;
	}

	@Override
	public int getMana() {
		return mana;
	}

	@Override
	public int getMaxMana(boolean includeReserved) {
		int max = this.getManaBonus() + Math.round(baseMaxMana * (1f + this.getManaModifier()));
		if (!includeReserved) {
			max -= this.getReservedMana();
		}
		return max;
	}
	
	@Override
	public int getMaxMana() {
		return getMaxMana(false);
	}

	@Override
	public void setMana(int mana) {
		this.mana = Math.min(mana, this.getMaxMana());
	}

	@Override
	public void addMana(int mana) {
		final int startingMana = this.mana;
		this.mana = Math.max(0, Math.min(this.mana + mana, this.getMaxMana()));
		if (startingMana > this.mana) {
			if (entity != null && entity instanceof Player) {
				PlayerStatTracker.Update((Player) entity, (stats) -> stats.addStat(PlayerStat.ManaSpentTotal, startingMana - this.mana));
			}
		}
	}

	@Override
	public void setMaxMana(int max) {
		this.baseMaxMana = max;
		if (this.mana > getMaxMana())
			this.mana = getMaxMana();
		if (this.reservedMana > this.getMaxMana())
			this.reservedMana = this.getMaxMana();
	}
	
	@Override
	public int getReservedMana() {
		return this.reservedMana;
	}
	
	@Override
	public void setReservedMana(int reserved) {
		this.reservedMana = reserved;
	}
	
	@Override
	public void addReservedMana(int reserved) {
		// Bound between 0 and max mana
		this.reservedMana = Math.max(0, Math.min(this.getMaxMana(), this.reservedMana + reserved));
		if (mana < 0 && entity != null && entity instanceof Player) {
			PlayerStatTracker.Update((Player) entity, (stats) -> stats.takeMax(PlayerStat.MaxReservedMana, this.reservedMana));
		}
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
	public boolean hasLore(ILoreTagged tagged) {
		return (loreLevels.getOrDefault(tagged.getLoreKey(), 0) >= 1);
	}
	
	@Override
	public boolean hasFullLore(ILoreTagged tagged) {
		String key = tagged.getLoreKey();
		Integer level = loreLevels.get(key);
		
		return (level != null && level >= 2);
	}

	public static int getKnowledge(INostrumMagic attr) {
		int knowledge = 0;
		for (Integer val : attr.serializeLoreLevels().values()) {
			if (val == null || val == 0) {
				continue;
			}
			
			knowledge += val;
		}
		return knowledge;
	}
	
	protected void addLoreBonus(NostrumMagic attr, Integer oldVal, int newVal) {
		if (oldVal == null) {
			oldVal = 0;
		}
		int knowledge = getKnowledge(attr);
		int oldLvl = KnowledgeCurves.knowledgeLevel(knowledge - (newVal - oldVal));
		int newLvl = KnowledgeCurves.knowledgeLevel(knowledge);
		if (oldLvl < newLvl) {
			for (int i = 0; i < (newLvl - oldLvl); i++) {
				this.addResearchPoint();
			}
		}
	}

	@Override
	public void giveBasicLore(ILoreTagged tagged) {
		if (hasLore(tagged))
			return; // Already has some lore
		
		String key = tagged.getLoreKey();
		Integer val = loreLevels.get(key);
		
		if (val != null && val == 2)
			return; // Already have full
		
		loreLevels.put(key, 1);
		
		addLoreBonus(this, val, 1);
		
//		if (NostrumMagica.instance.proxy.getPlayer() != null) {
//			NostrumMagicaSounds.UI_TICK.play(NostrumMagica.instance.proxy.getPlayer());
//		}
		
		if (entity != null && entity instanceof Player && !entity.level.isClientSide) {
			NetworkHandler.sendTo(
					new LoreMessage(tagged, this),
					(ServerPlayer) entity);
			
		}
	}

	@Override
	public void giveFullLore(ILoreTagged tagged) {
		String key = tagged.getLoreKey();
		Integer val = loreLevels.get(key);
		
		if (val != null && val == 2)
			return; // Already has full
		
		loreLevels.put(key, 2);
//		if (NostrumMagica.instance.proxy.getPlayer() != null || !NostrumMagica.instance.proxy.getPlayer().world.isRemote) {
//			NostrumMagicaSounds.UI_TICK.play(NostrumMagica.instance.proxy.getPlayer());
//		}
		
		if (entity != null && entity instanceof Player && !entity.level.isClientSide) {
			NetworkHandler.sendTo(
					new LoreMessage(tagged, this),
					(ServerPlayer) entity);
			
		}
		
		addLoreBonus(this, val, 2);
	}

	@Override
	public boolean wasSpellDone(Spell spell) {
		String CRC = spell.crc();
		
		return !spellCRCs.add(CRC);
	}
	
	@Override
	public boolean hasTransmuteKnowledge(String key, int level) {
		Boolean val = transmuteKnowledge.get(new TransmuteKnowledge(key, level));
		return val != null && val;
	}
	
	@Override
	public void giveTransmuteKnowledge(String key, int level) {
		transmuteKnowledge.put(new TransmuteKnowledge(key, level), true);
	}

	@Override
	public List<SpellShape> getShapes() {
		return shapes;
	}

	public Map<EMagicElement, Boolean> getKnownElements() {
		Map<EMagicElement, Boolean> map = new EnumMap<>(EMagicElement.class);
		for (EMagicElement element : EMagicElement.values()) {
			map.put(element, getElementalMastery(element) != EElementalMastery.UNKNOWN);
		}
		return map;
	}
	
	public boolean setElementalMastery(EMagicElement element, EElementalMastery mastery) {
		elementalMastery.put(element, mastery);
		
		return true;
	}
	
	public EElementalMastery getElementalMastery(EMagicElement element) {
		EElementalMastery mastery = elementalMastery.get(element);
		return mastery == null ? EElementalMastery.UNKNOWN : mastery;
	}

	@Override
	public Map<EAlteration, Boolean> getAlterations() {
		return alterations;
	}

	@Override
	public void addShape(SpellShape shape) {
		shapes.add(shape);
	}

	@Override
	public void unlockAlteration(EAlteration alteration) {
		alterations.put(alteration, Boolean.TRUE);
	}
	
	@Override
	public void deserialize(EMagicTier tier, int level, float xp, int skillpoints, int researchpoints,
			int mana, int reservedMana) {
		this.tier = tier;
		this.level = level;
		this.xp = xp;
		this.maxxp = LevelCurves.maxXP(this.level);
		this.skillPoints = skillpoints;
		this.researchPoints = researchpoints;
		this.mana = mana;
		this.reservedMana = reservedMana;
		this.baseMaxMana = LevelCurves.maxMana(this.level);
		this.modMana = new HashMap<>();
		this.modManaCost = new HashMap<>();
		this.modManaRegen = new HashMap<>();
		this.modManaFlat = new HashMap<>();
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
	public Map<EMagicElement, EElementalMastery> serializeElementMastery() {
		return this.elementalMastery;
	}
	
	@Override
	public Map<EMagicElement, Boolean> serializeElementTrials() {
		return this.elementTrials;
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
	
	@Override
	public void setModifierMaps(Map<UUID, Float> modifiers_mana, Map<UUID, Integer> modifiers_bonus_mana,
			Map<UUID, Float> modifiers_cost, Map<UUID, Float> modifiers_regen) {
		this.modMana = modifiers_mana;
		this.modManaFlat = modifiers_bonus_mana;
		this.modManaCost = modifiers_cost;
		this.modManaRegen = modifiers_regen;
	}
	
	@Override
	public Map<EMagicElement, Integer> getElementalSkillPointsMap() {
		return this.elementalSkillPoints;
	}
	
	@Override
	public void setElementalSkillPointMap(Map<EMagicElement, Integer> map) {
		this.elementalSkillPoints = map;
	}
	
	@Override
	public void setTransmuteKnowledge(Map<TransmuteKnowledge, Boolean> map) {
		this.transmuteKnowledge = new HashMap<>(map);
	}
	
	@Override
	public Map<TransmuteKnowledge, Boolean> getTransmuteKnowledge() {
		return this.transmuteKnowledge;
	}

	@Override
	public void copy(INostrumMagic cap) {
		System.out.println("Overriding stats from" + this.mana + " to " + cap.getMana() + " mana");
		this.deserialize(cap.getTier(), cap.getLevel(), cap.getXP(),
				cap.getSkillPoints(), cap.getResearchPoints(),
				cap.getMana(), cap.getReservedMana()
				);
		
		this.loreLevels = cap.serializeLoreLevels();
		this.spellCRCs = cap.serializeSpellHistory();
		this.elementalMastery = cap.serializeElementMastery();
		this.elementTrials = cap.serializeElementTrials();
		this.alterations = cap.serializeAlterations();
		this.shapes = cap.getShapes();
		this.markLocation = cap.getMarkLocation();
		this.markDimension = cap.getMarkDimension();
		this.currentQuests = cap.getCurrentQuests();
		this.completedQuests = cap.getCompletedQuests();
		this.completedResearch = cap.getCompletedResearches();
		this.spellKnowledge = cap.getSpellKnowledge();
		this.enhancedTeleport = cap.hasEnhancedTeleport(); 
		this.transmuteKnowledge = cap.getTransmuteKnowledge();
		this.setModifierMaps(cap.getManaModifiers(), cap.getManaBonusModifiers(), cap.getManaCostModifiers(), cap.getManaRegenModifiers());
		this.skills.clear(); this.skills.addAll(cap.getSkills());
		this.elementalXP.clear(); this.elementalXP.putAll(cap.getElementalXPMap());
		this.elementalSkillPoints.clear(); this.elementalSkillPoints.putAll(cap.getElementalSkillPointsMap());
	}
	
	@Override
	public BlockPos getMarkLocation() {
		return markLocation;
	}
	
	@Override
	public ResourceKey<Level> getMarkDimension() {
		return markDimension;
	}
	
	@Override
	public void setMarkLocation(ResourceKey<Level> dimension, BlockPos pos) {
		this.markDimension = dimension;
		this.markLocation = pos;
	}

	@Override
	public float getManaModifier() {
		float sum = 0f;
		for (Float mod : this.modMana.values()) {
			if (mod == null) {
				continue;
			}
			sum += mod;
		}
		return sum;
	}
	
	@Override
	public int getManaBonus() {
		int sum = 0;
		for (Integer mod : this.modManaFlat.values()) {
			if (mod == null) {
				continue;
			}
			sum += mod;
		}
		return sum;
	}

	@Override
	public float getManaRegenModifier() {
		float sum = 0f;
		for (Float mod : this.modManaRegen.values()) {
			if (mod == null) {
				continue;
			}
			sum += mod;
		}
		return sum;
	}

	@Override
	public float getManaCostModifier() {
		float sum = 0f;
		for (Float mod : this.modManaCost.values()) {
			if (mod == null) {
				continue;
			}
			sum += mod;
		}
		return sum;
	}

	@Override
	public List<String> getCompletedQuests() {
		return this.completedQuests;
	}

	@Override
	public List<String> getCurrentQuests() {
		return this.currentQuests;
	}

	@Override
	public void addQuest(String quest) {
		if (!currentQuests.contains(quest) && !completedQuests.contains(quest))
			currentQuests.add(quest);
	}

	@Override
	public void completeQuest(String quest) {
		if (!completedQuests.contains(quest) && currentQuests.contains(quest)) {
			currentQuests.remove(quest);
			completedQuests.add(quest);
		}
			
	}

	@Override
	public void startTrial(EMagicElement element) {
		this.elementTrials.put(element, Boolean.TRUE);
	}

	@Override
	public void endTrial(EMagicElement element) {
		this.elementTrials.put(element, Boolean.FALSE);
	}

	@Override
	public boolean hasTrial(EMagicElement element) {
		Boolean bool = this.elementTrials.get(element);
		return (bool != null && bool);
	}

	@Override
	public List<LivingEntity> getFamiliars() {
		return familiars;
	}

	@Override
	public void addFamiliar(LivingEntity familiar) {
		familiars.add(familiar);
	}

	@Override
	public void clearFamiliars() {
		if (familiars.isEmpty())
			return;
		
		for (LivingEntity entity : familiars) {
			entity.discard();
		}
		
		familiars.clear();
	}

	@Override
	public List<ILoreTagged> getAllLore() {
		List<ILoreTagged> lore = new ArrayList<>();
		for (String key : this.loreLevels.keySet()) {
			ILoreTagged tag = LoreRegistry.instance().lookup(key);
			if (tag != null)
				lore.add(tag);
		}
		return lore;
	}
	
	@Override
	public boolean hasKnowledge(EMagicElement element, EAlteration alteration) {
		if (spellKnowledge == null)
			return false;
		Map<EAlteration, Boolean> map = spellKnowledge.get(element);
		if (map == null) {
			return false;
		}
		
		Boolean bool = map.get(alteration);
		return (bool != null && bool);
	}
	
	@Override
	public void setKnowledge(EMagicElement element, EAlteration alteration) {
		if (spellKnowledge == null)
			spellKnowledge = new HashMap<>(); // can't be enum map, since alteration can be null
		Map<EAlteration, Boolean> map = spellKnowledge.get(element);
		if (map == null) {
			map = new HashMap<>();
			spellKnowledge.put(element, map);
		}
		
		Boolean old = map.put(alteration, true);
		if ((old == null || !old) && entity != null && entity instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
			NostrumMagica.Proxy.syncPlayer((ServerPlayer) entity);
		}
	}
	
	@Override
	public Map<EMagicElement, Map<EAlteration, Boolean>> getSpellKnowledge() {
		return this.spellKnowledge;
	}
	
	@Override
	public ResourceKey<Level> getSorceryPortalDimension() {
		return this.sorceryPortalDim;
	}
	
	@Override
	public BlockPos getSorceryPortalPos() {
		return this.sorceryPortalPos;
	}
	
	@Override
	public void clearSorceryPortal() {
		this.sorceryPortalPos = null;
	}
	
	@Override
	public void setSorceryPortalLocation(ResourceKey<Level> dimension, BlockPos pos) {
		this.sorceryPortalDim = dimension;
		this.sorceryPortalPos = pos;
	}

	@Override
	public List<ResourceLocation> getCompletedResearches() {
		return this.completedResearch;
	}

	@Override
	public void completeResearch(ResourceLocation research) {
		this.completedResearch.add(research);
	}
	
	@Override
	public void unlockEnhancedTeleport() {
		this.enhancedTeleport = true;
	}
	
	@Override
	public boolean hasEnhancedTeleport() {
		return this.enhancedTeleport;
	}

	@Override
	public void refresh(ServerPlayer player) {
//		// Capture current mana to avoid having to always regen bonus
//		final int startingMana = this.getMana();
//		
//		
//		if (NostrumMagica.instance.curios.isEnabled()) {
//			NostrumMagica.instance.curios.forEachCurio(player, (stack) -> {
//				if (stack.getItem() instanceof INostrumCurio) {
//					((INostrumCurio) stack.getItem()).onUnequipped(stack, player);
//				}
//				return false;
//			});
//		}
//		
//		this.modMana = 0;
//		this.modManaCost = 0;
//		this.modManaFlat = 0;
//		this.modManaRegen = 0;
//		
//		// Reapply quests
//		for (String string : this.completedQuests) {
//			NostrumQuest quest = NostrumQuest.lookup(string);
//			quest.grantReward(player);
//		}
//		
//		if (NostrumMagica.instance.curios.isEnabled()) {
//			NostrumMagica.instance.curios.forEachCurio(player, (stack) -> {
//				if (stack.getItem() instanceof INostrumCurio) {
//					((INostrumCurio) stack.getItem()).onEquipped(stack, player);
//				}
//				return false;
//			});
//		}
//		
//		this.setMana(startingMana); // Will get clamped
	}

	@Override
	public void addManaModifier(UUID id, float modifier) {
		modMana.put(id, modifier);
	}

	@Override
	public void addManaRegenModifier(UUID id, float modifier) {
		modManaRegen.put(id, modifier);
	}

	@Override
	public void addManaCostModifier(UUID id, float modifier) {
		modManaCost.put(id, modifier);
	}

	@Override
	public void addManaBonus(UUID id, int bonus) {
		modManaFlat.put(id, bonus);
	}

	@Override
	public void removeManaModifier(UUID id) {
		modMana.remove(id);
	}

	@Override
	public void removeManaRegenModifier(UUID id) {
		modManaRegen.remove(id);
	}

	@Override
	public void removeManaCostModifier(UUID id) {
		modManaCost.remove(id);
	}

	@Override
	public void removeManaBonus(UUID id) {
		modManaFlat.remove(id);
	}

	@Override
	public Map<UUID, Float> getManaModifiers() {
		return this.modMana;
	}

	@Override
	public Map<UUID, Integer> getManaBonusModifiers() {
		return this.modManaFlat;
	}

	@Override
	public Map<UUID, Float> getManaCostModifiers() {
		return this.modManaCost;
	}

	@Override
	public Map<UUID, Float> getManaRegenModifiers() {
		return this.modManaRegen;
	}

	@Override
	public @Nullable VanillaRespawnInfo getSavedRespawnInfo() {
		return savedRespawnInfo;
	}

	@Override
	public void setSavedRespawnInfo(VanillaRespawnInfo info) {
		this.savedRespawnInfo = info;
	}
	
	@Override
	public Collection<Skill> getSkills() {
		return skills;
	}
	
	@Override
	public boolean hasSkill(Skill skill) {
		return skills.contains(skill);
	}
	
	@Override
	public void addSkill(Skill skill) {
		skills.add(skill);
		
		ServerPlayer player = this.entity != null && this.entity instanceof ServerPlayer serverPlayer ? serverPlayer : null;
		
		// Hacky place to do this...
		if (skill == NostrumSkills.Wind_Adept) {
			// skills don't have rewards tied to them but maybe they should?
			this.addShape(NostrumSpellShapes.Cutter);
		} else if (skill == NostrumSkills.Spellcasting_Overcharge && player != null && player.connection != null) {
			NetworkHandler.sendTo(new TutorialMessage(NostrumTutorial.OVERCHARGE), player);
			this.giveFullLore(LoreRegistry.SpellOverchargingLore);
		}
	}

	@Override
	public int getElementXP(EMagicElement element) {
		return this.elementalXP.getOrDefault(element, 0);
	}

	@Override
	public int getElementMaxXP(EMagicElement element) {
		final int effectiveLevel = ElementCurves.elementalSkillLevel(this.getElementXP(element));
		return ElementCurves.maxXP(effectiveLevel);
	}

	@Override
	public void addElementXP(EMagicElement element, int xp) {
		final int oldXP = this.getElementXP(element);
		final int newXP = xp + oldXP;
		this.elementalXP.put(element, newXP);
		int oldLvl = ElementCurves.elementalSkillLevel(newXP - xp);
		int newLvl = ElementCurves.elementalSkillLevel(newXP);
		if (oldLvl < newLvl) {
			for (int i = 0; i < (newLvl - oldLvl); i++) {
				this.addElementalSkillPoint(element);
			}
		}
	}

	@Override
	public Map<EMagicElement, Integer> getElementalXPMap() {
		return elementalXP;
	}

	@Override
	public void setElementalXPMap(Map<EMagicElement, Integer> map) {
		this.elementalXP = map;
	}
	
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
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		
		nbt.putString(NBT_TIER, getTier().name().toLowerCase());
		nbt.putInt(NBT_LEVEL, getLevel());
		nbt.putFloat(NBT_XP, getXP());
		nbt.putInt(NBT_SKILLPOINTS, getSkillPoints());
		nbt.putInt(NBT_RESEARCHPOINTS, getResearchPoints());
		nbt.putInt(NBT_MANA, getMana());
		nbt.putInt(NBT_RESERVED_MANA, getReservedMana());
		
		nbt.put(NBT_ELEMENTAL_SKILLPOINTS, NetUtils.ToNBT(getElementalSkillPointsMap(), IntTag::valueOf));
		nbt.put(NBT_ELEMENTAL_XP, NetUtils.ToNBT(getElementalXPMap(), IntTag::valueOf));
		
		CompoundTag compound = new CompoundTag();
		{
			Map<String, Integer> map = serializeLoreLevels();
			for (String key : map.keySet()) {
				compound.putInt(key, map.get(key));
			}
		}
		nbt.put(NBT_LORELEVELS, compound);
		
		ListTag list = new ListTag();
		for (String crc : serializeSpellHistory()) {
			list.add(StringTag.valueOf(crc));
		}
		nbt.put(NBT_SPELLCRCS, list);
		
		compound = new CompoundTag();
		{
			Map<EMagicElement, EElementalMastery> map = serializeElementMastery();
			for (EMagicElement key : map.keySet()) {
				compound.put(key.name(), map.get(key).toNBT());
			}
		}
		nbt.put(NBT_MASTERED_ELEMENTS, compound);
		
		compound = new CompoundTag();
		{
			Map<EMagicElement, Boolean> map = serializeElementTrials();
			for (EMagicElement key : map.keySet()) {
				compound.putBoolean(key.name(), map.get(key));
			}
		}
		nbt.put(NBT_ELEMENT_TRIALS, compound);
		
		list = new ListTag();
		{
			Map<UUID, Float> map = getManaModifiers();
			for (UUID id : map.keySet()) {
				if (id == null) continue;
				
				compound = new CompoundTag();
				compound.putUUID(NBT_MOD_INTERNAL_ID, id);
				compound.putFloat(NBT_MOD_INTERNAL_VALUE, map.get(id));
				list.add(compound);
			}
		}
		nbt.put(NBT_MOD_MANA, list);
		
		list = new ListTag();
		{
			Map<UUID, Float> map = getManaCostModifiers();
			for (UUID id : map.keySet()) {
				if (id == null) continue;
				
				compound = new CompoundTag();
				compound.putUUID(NBT_MOD_INTERNAL_ID, id);
				compound.putFloat(NBT_MOD_INTERNAL_VALUE, map.get(id));
				list.add(compound);
			}
		}
		nbt.put(NBT_MOD_MANA_COST, list);
		
		list = new ListTag();
		{
			Map<UUID, Float> map = getManaRegenModifiers();
			for (UUID id : map.keySet()) {
				if (id == null) continue;
				
				compound = new CompoundTag();
				compound.putUUID(NBT_MOD_INTERNAL_ID, id);
				compound.putFloat(NBT_MOD_INTERNAL_VALUE, map.get(id));
				list.add(compound);
			}
		}
		nbt.put(NBT_MOD_MANA_REGEN, list);
		
		list = new ListTag();
		{
			Map<UUID, Integer> map = getManaBonusModifiers();
			for (UUID id : map.keySet()) {
				if (id == null) continue;
				
				compound = new CompoundTag();
				compound.putUUID(NBT_MOD_INTERNAL_ID, id);
				compound.putInt(NBT_MOD_INTERNAL_VALUE, map.get(id));
				list.add(compound);
			}
		}
		nbt.put(NBT_MOD_MANA_BONUS, list);
		
		list = new ListTag();
		for (SpellShape shape : getShapes()) {
			String key = shape.getShapeKey();
			list.add(StringTag.valueOf(key));
		}
		nbt.put(NBT_SHAPES, list);
		
		compound = new CompoundTag();
		{
			Map<EAlteration, Boolean> map = serializeAlterations();
			for (EAlteration key : map.keySet()) {
				compound.putBoolean(key.name(), map.get(key));
			}
		}
		nbt.put(NBT_ALTERATIONS, compound);
		
		BlockPos markPos = getMarkLocation();
		if (markPos != null) {
			CompoundTag posTag = new CompoundTag();
			posTag.putInt("x", markPos.getX());
			posTag.putInt("y", markPos.getY());
			posTag.putInt("z", markPos.getZ());
			nbt.putString(NBT_MARK_DIMENSION, getMarkDimension().location().toString());
			nbt.put(NBT_MARK_POS, posTag);
		}
		if (hasEnhancedTeleport()) {
			nbt.putBoolean(NBT_ENHANCED_TELEPORT, true);
		}
		
		List<String> stringList = getCurrentQuests();
		if (stringList != null && !stringList.isEmpty()) {
			ListTag tagList = new ListTag();
			for (String quest : stringList) {
				tagList.add(StringTag.valueOf(quest));
			}
			nbt.put(NBT_QUESTS_CURRENT, tagList);
		}
		
		stringList = getCompletedQuests();
		if (stringList != null && !stringList.isEmpty()) {
			ListTag tagList = new ListTag();
			for (String quest : stringList) {
				tagList.add(StringTag.valueOf(quest));
			}
			nbt.put(NBT_QUESTS_COMPLETED, tagList);
		}
		
		List<ResourceLocation> keyList = getCompletedResearches();
		if (keyList != null && !keyList.isEmpty()) {
			ListTag tagList = new ListTag();
			for (ResourceLocation research : keyList) {
				tagList.add(StringTag.valueOf(research.toString()));
			}
			nbt.put(NBT_RESEARCHES, tagList);
		}
		
		Collection<Skill> skills = getSkills();
		if (skills != null && !skills.isEmpty()) {
			ListTag tagList = new ListTag();
			for (Skill skill : skills) {
				tagList.add(StringTag.valueOf(skill.getKey().toString()));
			}
			nbt.put(NBT_SKILLS, tagList);
		}
		
		compound = new CompoundTag();;
		Map<EMagicElement, Map<EAlteration, Boolean>> knowledge = getSpellKnowledge();
		if (knowledge != null && !knowledge.isEmpty())
		for (EMagicElement elem : knowledge.keySet()) {
			CompoundTag subtag = new CompoundTag();
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
		
		if (getSorceryPortalPos() != null) {
			nbt.putString(NBT_SORCERYPORTAL_DIM, getSorceryPortalDimension().getRegistryName().toString());
			nbt.put(NBT_SORCERYPORTAL_POS, NbtUtils.writeBlockPos(getSorceryPortalPos()));
		}
		
		final VanillaRespawnInfo respawnInfo = getSavedRespawnInfo();
		if (respawnInfo != null) {
			nbt.putString(NBT_SAVEDRESPAWN_DIM, respawnInfo.dimension.location().toString());
			nbt.put(NBT_SAVEDRESPAWN_POS, NbtUtils.writeBlockPos(respawnInfo.pos));
			nbt.putFloat(NBT_SAVEDRESPAWN_YAW, respawnInfo.yaw);
			nbt.putBoolean(NBT_SAVEDRESPAWN_FORCE, respawnInfo.forced);
		}
		
		list = new ListTag();
		for (Entry<TransmuteKnowledge, Boolean> entry : getTransmuteKnowledge().entrySet()) {
			if (entry.getValue() == null || !entry.getValue()) {
				continue;
			}
			CompoundTag subtag = entry.getKey().toNBT();
			list.add(subtag);
		}
		nbt.put(NBT_TRANSMUTE_KNOWLEDGE, list);
		
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag tag) {
		EMagicTier tier = EMagicTier.LOCKED;
		try {
			tier = EMagicTier.valueOf(tag.getString(NBT_TIER).toUpperCase());
		} catch (Exception e) {
			tier = EMagicTier.LOCKED;
		}
		deserialize(
			tier,
			tag.getInt(NBT_LEVEL),
			tag.getFloat(NBT_XP),
			tag.getInt(NBT_SKILLPOINTS),
			tag.getInt(NBT_RESEARCHPOINTS),
			tag.getInt(NBT_MANA),
			tag.getInt(NBT_RESERVED_MANA)
			);
		
		Map<EMagicElement, Integer> elementalSkillPoints = new EnumMap<>(EMagicElement.class);
		NetUtils.FromNBT(elementalSkillPoints, EMagicElement.class, tag.getCompound(NBT_ELEMENTAL_SKILLPOINTS), (p) -> ((IntTag) p).getAsInt());
		setElementalSkillPointMap(elementalSkillPoints);
		
		Map<EMagicElement, Integer> elementalXP = new EnumMap<>(EMagicElement.class);
		NetUtils.FromNBT(elementalXP, EMagicElement.class, tag.getCompound(NBT_ELEMENTAL_XP), (p) -> ((IntTag) p).getAsInt());
		setElementalXPMap(elementalXP);
			
		// LORE
		CompoundTag compound = tag.getCompound(NBT_LORELEVELS);
		for (String key : compound.getAllKeys()) {
			Integer level = compound.getInt(key);
			deserializeLore(key, level);
		}
		
		// SPELLS
		ListTag list = tag.getList(NBT_SPELLCRCS, Tag.TAG_STRING);
		for (int i = 0; i < list.size(); i++) {
			deserializeSpells(list.getString(i));
		}
		
		// ELEMENTS
		compound = tag.getCompound(NBT_MASTERED_ELEMENTS);
		for (String key : compound.getAllKeys()) {
			EMagicElement elem = EMagicElement.parse(key);
			setElementalMastery(elem, EElementalMastery.fromNBT(compound.get(key)));
		}
		
		compound = tag.getCompound(NBT_ELEMENT_TRIALS);
		for (String key : compound.getAllKeys()) {
			boolean val = compound.getBoolean(key);
			if (val) {
				EMagicElement elem = EMagicElement.parse(key);
				startTrial(elem);
			}
		}
		
		// SHAPES
		list = tag.getList(NBT_SHAPES, Tag.TAG_STRING);
		for (int i = 0; i < list.size(); i++) {
			SpellShape shape = SpellShape.get(list.getString(i));
			addShape(shape);
		}
		
		// ALTERATIONS

		compound = tag.getCompound(NBT_ALTERATIONS);
		for (String key : compound.getAllKeys()) {
			boolean val = compound.getBoolean(key);
			if (val) {
				try {
					EAlteration elem = EAlteration.valueOf(key);
					unlockAlteration(elem);
				} catch (IllegalArgumentException e) {
					NostrumMagica.logger.warn("Ignoring unknown alteration (%s) in player data".formatted(key));
				}
			}
		}
		
		// Mark Location
		if (tag.contains(NBT_MARK_POS, Tag.TAG_COMPOUND)) {
			CompoundTag posTag = tag.getCompound(NBT_MARK_POS);
			BlockPos location = new BlockPos(
					posTag.getInt("x"),
					posTag.getInt("y"),
					posTag.getInt("z")
					);
			String dimension = tag.getString(NBT_MARK_DIMENSION);
			ResourceKey<Level> dimKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, ResourceLocation.tryParse(dimension));
			
			setMarkLocation(dimKey, location);
		}
		
		if (tag.contains(NBT_ENHANCED_TELEPORT) && tag.getBoolean(NBT_ENHANCED_TELEPORT)) {
			unlockEnhancedTeleport();
		}
		
		// Quests
		if (tag.contains(NBT_QUESTS_CURRENT, Tag.TAG_LIST)) {
			ListTag tagList = tag.getList(NBT_QUESTS_CURRENT, Tag.TAG_STRING);
			for (int i = 0; i < tagList.size(); i++) {
				String quest = tagList.getString(i);
				addQuest(quest);
			}
		}
		if (tag.contains(NBT_QUESTS_COMPLETED, Tag.TAG_LIST)) {
			ListTag tagList = tag.getList(NBT_QUESTS_COMPLETED, Tag.TAG_STRING);
			for (int i = 0; i < tagList.size(); i++) {
				String quest = tagList.getString(i);
				addQuest(quest);
				completeQuest(quest);
			}
		}
		
		if (tag.contains(NBT_RESEARCHES, Tag.TAG_LIST)) {
			ListTag tagList = tag.getList(NBT_RESEARCHES, Tag.TAG_STRING);
			for (int i = 0; i < tagList.size(); i++) {
				ResourceLocation research = ResourceLocation.parse(tagList.getString(i));
				completeResearch(research);
			}
		}
		
		if (tag.contains(NBT_SKILLS, Tag.TAG_LIST)) {
			ListTag tagList = tag.getList(NBT_SKILLS, Tag.TAG_STRING);
			for (int i = 0; i < tagList.size(); i++) {
				String raw = tagList.getString(i);
				ResourceLocation loc = new ResourceLocation(raw);
				if (Skill.lookup(loc) != null) {
					addSkill(Skill.lookup(loc));
				}
			}
		}
		
		if (tag.contains(NBT_SPELLKNOWLEDGE, Tag.TAG_COMPOUND)) {
			compound = tag.getCompound(NBT_SPELLKNOWLEDGE);
			for (String key : compound.getAllKeys()) {
				try {
					EMagicElement elem = EMagicElement.parse(key);
					CompoundTag subtag = compound.getCompound(key);
					for (String altKey : subtag.getAllKeys()) {
						EAlteration alt = null;
						try {
							if (altKey.equalsIgnoreCase("none"))
								alt = null;
							else
								alt = EAlteration.valueOf(altKey);
							if (subtag.getBoolean(altKey)) {
								setKnowledge(elem, alt);
							}
						} catch (Exception e) {
							NostrumMagica.logger.error("Failed to parse sub alteration row for %s: %s", alt == null ? "No Alteration" : alt.toString(), e);
							continue;
						}
					}
				} catch (Exception e) {
					continue;
				}
			}
		}
		
		if (tag.contains(NBT_SORCERYPORTAL_POS)) {
			String dimName = tag.getString(NBT_SORCERYPORTAL_DIM);
			ResourceKey<Level> dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, ResourceLocation.tryParse(dimName));
			setSorceryPortalLocation(
					dim,
					NbtUtils.readBlockPos(tag.getCompound(NBT_SORCERYPORTAL_POS))); // Warning: can break if save used across game versions
		}
		
		if (tag.contains(NBT_SAVEDRESPAWN_DIM)) {
			final ResourceKey<Level> dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString(NBT_SAVEDRESPAWN_DIM)));
			final BlockPos pos = NbtUtils.readBlockPos(tag.getCompound(NBT_SAVEDRESPAWN_POS));
			final float yaw = tag.getFloat(NBT_SAVEDRESPAWN_YAW);
			final boolean forced = tag.getBoolean(NBT_SAVEDRESPAWN_FORCE);
			setSavedRespawnInfo(new VanillaRespawnInfo(dim, pos, yaw, forced));
		} else {
			setSavedRespawnInfo(null);
		}
		
		// Modifiers
		Map<UUID, Float> modMana = new HashMap<>();
		Map<UUID, Integer> modManaFlat = new HashMap<>();
		Map<UUID, Float> modManaCost = new HashMap<>();
		Map<UUID, Float> modManaRegen = new HashMap<>();
		
		if (tag.contains(NBT_MOD_MANA, Tag.TAG_LIST)) {
			ListTag tagList = tag.getList(NBT_MOD_MANA, Tag.TAG_COMPOUND);
			
			for (int i = 0; i < tagList.size(); i++) {
				CompoundTag subtag = tagList.getCompound(i);
				UUID id = subtag.getUUID(NBT_MOD_INTERNAL_ID);
				float val = subtag.getFloat(NBT_MOD_INTERNAL_VALUE);
				if (id != null) {
					modMana.put(id, val);
				}
			}
		}
		
		if (tag.contains(NBT_MOD_MANA_BONUS, Tag.TAG_LIST)) {
			ListTag tagList = tag.getList(NBT_MOD_MANA_BONUS, Tag.TAG_COMPOUND);
			
			for (int i = 0; i < tagList.size(); i++) {
				CompoundTag subtag = tagList.getCompound(i);
				UUID id = subtag.getUUID(NBT_MOD_INTERNAL_ID);
				int val = subtag.getInt(NBT_MOD_INTERNAL_VALUE);
				if (id != null) {
					modManaFlat.put(id, val);
				}
			}
		}
		
		if (tag.contains(NBT_MOD_MANA_COST, Tag.TAG_LIST)) {
			ListTag tagList = tag.getList(NBT_MOD_MANA_COST, Tag.TAG_COMPOUND);
			
			for (int i = 0; i < tagList.size(); i++) {
				CompoundTag subtag = tagList.getCompound(i);
				UUID id = subtag.getUUID(NBT_MOD_INTERNAL_ID);
				float val = subtag.getFloat(NBT_MOD_INTERNAL_VALUE);
				if (id != null) {
					modManaCost.put(id, val);
				}
			}
		}
		
		if (tag.contains(NBT_MOD_MANA_REGEN, Tag.TAG_LIST)) {
			ListTag tagList = tag.getList(NBT_MOD_MANA_REGEN, Tag.TAG_COMPOUND);
			
			for (int i = 0; i < tagList.size(); i++) {
				CompoundTag subtag = tagList.getCompound(i);
				UUID id = subtag.getUUID(NBT_MOD_INTERNAL_ID);
				float val = subtag.getFloat(NBT_MOD_INTERNAL_VALUE);
				if (id != null) {
					modManaRegen.put(id, val);
				}
			}
		}
		
		setModifierMaps(modMana, modManaFlat, modManaCost, modManaRegen);
		
		if (tag.contains(NBT_TRANSMUTE_KNOWLEDGE, Tag.TAG_LIST)) {
			ListTag tagList = tag.getList(NBT_TRANSMUTE_KNOWLEDGE, Tag.TAG_COMPOUND);
			
			for (int i = 0; i < tagList.size(); i++) {
				CompoundTag subtag = tagList.getCompound(i);
				TransmuteKnowledge knowledge = TransmuteKnowledge.fromNBT(subtag);
				giveTransmuteKnowledge(knowledge.key, knowledge.level);
			}
		}
	}
}
