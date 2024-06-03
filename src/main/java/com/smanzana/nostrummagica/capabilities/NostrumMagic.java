package com.smanzana.nostrummagica.capabilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.loretag.LoreCache;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.LoreMessage;
import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.stat.PlayerStat;
import com.smanzana.nostrummagica.stat.PlayerStatTracker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

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
	
	private boolean unlocked;
	private int level;
	private float xp;
	private float maxxp;
	private int skillPoints;
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
	private Map<EMagicElement, ElementalMastery> elementalMastery;
	private Map<EMagicElement, Boolean> elementTrials;
	private List<SpellShape> shapes; // list of shape keys
	private Map<EAlteration, Boolean> alterations;
	private List<String> completedQuests;
	private List<String> currentQuests;
	private List<String> completedResearch;
	private Set<Skill> skills;
	private BlockPos markLocation;
	private RegistryKey<World> markDimension;
	private boolean enhancedTeleport;
	private Map<EMagicElement, Map<EAlteration, Boolean>> spellKnowledge;
	private RegistryKey<World> sorceryPortalDim;
	private BlockPos sorceryPortalPos;
	private @Nullable VanillaRespawnInfo savedRespawnInfo;
	private Map<TransmuteKnowledge, Boolean> transmuteKnowledge;
	
	private LivingEntity entity;
	
	public NostrumMagic() {
		unlocked = false;
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
		sorceryPortalDim = World.OVERWORLD;
		sorceryPortalPos = null;
		savedRespawnInfo = null;
		enhancedTeleport = false;
		transmuteKnowledge = new HashMap<>();
		skills = new HashSet<>();
		
		modMana = new HashMap<>();
		modManaFlat = new HashMap<>();
		modManaCost = new HashMap<>();
		modManaRegen = new HashMap<>();
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
			baseMaxMana = LevelCurves.maxMana(1);
			mana = getMaxMana();
			maxxp = LevelCurves.maxXP(1);
			skillPoints = 0;
			
			this.setElementalMastery(EMagicElement.PHYSICAL, ElementalMastery.NOVICE);
			this.completeResearch("origin");
			this.addResearchPoint();
			//this.completeResearch("spellcraft");
			this.giveBasicLore(NostrumItems.GetRune(new SpellComponentWrapper(EMagicElement.FIRE)));
			this.giveBasicLore(NostrumItems.blankScroll);
			this.giveBasicLore(NostrumItems.spellScroll);
			this.giveBasicLore(NostrumItems.reagentMandrakeRoot);
			
			NostrumMagicaSounds.LEVELUP.play(entity);
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
			if (entity != null && entity instanceof PlayerEntity) {
				PlayerStatTracker.Update((PlayerEntity) entity, (stats) -> stats.addStat(PlayerStat.ManaSpentTotal, startingMana - this.mana));
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
		if (mana < 0 && entity != null && entity instanceof PlayerEntity) {
			PlayerStatTracker.Update((PlayerEntity) entity, (stats) -> stats.takeMax(PlayerStat.MaxReservedMana, this.reservedMana));
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
		return (getLore(tagged) != null);
	}
	
	@Override
	public boolean hasFullLore(ILoreTagged tagged) {
		String key = tagged.getLoreKey();
		Integer level = loreLevels.get(key);
		
		return (level != null && level >= 2);
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
		if (getLore(tagged) != null)
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
		
		if (entity != null && entity instanceof PlayerEntity && !entity.world.isRemote) {
			NetworkHandler.sendTo(
					new LoreMessage(tagged, this),
					(ServerPlayerEntity) entity);
			
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
		
		if (entity != null && entity instanceof PlayerEntity && !entity.world.isRemote) {
			NetworkHandler.sendTo(
					new LoreMessage(tagged, this),
					(ServerPlayerEntity) entity);
			
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
			map.put(element, getElementalMastery(element) != ElementalMastery.UNKNOWN);
		}
		return map;
	}
	
	public boolean setElementalMastery(EMagicElement element, ElementalMastery mastery) {
		elementalMastery.put(element, mastery);
		
		if (mastery != ElementalMastery.UNKNOWN) {
			if (this.entity != null && !this.entity.world.isRemote
					&& this.entity instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) this.entity;
				player.sendMessage(new TranslationTextComponent("info.element_mastery." + mastery.getTranslationKey(), element.getName()), Util.DUMMY_UUID);
			}
			
			// Old and unneeded?
			doUnlockCheck();
		}
		
		return true;
	}
	
	public ElementalMastery getElementalMastery(EMagicElement element) {
		ElementalMastery mastery = elementalMastery.get(element);
		return mastery == null ? ElementalMastery.UNKNOWN : mastery;
	}

	@Override
	public Map<EAlteration, Boolean> getAlterations() {
		return alterations;
	}

	@Override
	public void addShape(SpellShape shape) {
		shapes.add(shape);
		
		doUnlockCheck();
	}

	@Override
	public void unlockAlteration(EAlteration alteration) {
		alterations.put(alteration, Boolean.TRUE);
		doUnlockCheck();
	}
	
	private void doUnlockCheck() {
		if (this.unlocked)
			return;
		
		// Unlock (ritual of discovery) if at least one shape and trigger
		// and an element have been 'discovered'.
		
		int unused; // Revisit this process now that there are no triggers
		if (shapes.isEmpty())
			return;
		
		boolean found = false;
		for (EMagicElement e : EMagicElement.values()) {
			if (this.getElementalMastery(e) != null && this.getElementalMastery(e).isGreaterOrEqual(ElementalMastery.NOVICE)) {
				found = true;
				break;
			}
		}
		
		if (!found)
			return;
		
		unlock();
		// TODO effects
		if (this.entity != null && !this.entity.world.isRemote
				&& this.entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) this.entity;
			player.sendMessage(new StringTextComponent("Magic Unlocked"), Util.DUMMY_UUID);
		}
		
	}

	@Override
	public void deserialize(boolean unlocked, int level, float xp, int skillpoints, int researchpoints,
			int mana, int reservedMana) {
		this.unlocked = unlocked;
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
	public Map<EMagicElement, ElementalMastery> serializeElementMastery() {
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
		this.deserialize(cap.isUnlocked(), cap.getLevel(), cap.getXP(),
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
	}
	
	@Override
	public void provideEntity(LivingEntity entity) {
		this.entity = entity;
	}

	@Override
	public BlockPos getMarkLocation() {
		return markLocation;
	}
	
	@Override
	public RegistryKey<World> getMarkDimension() {
		return markDimension;
	}
	
	@Override
	public void setMarkLocation(RegistryKey<World> dimension, BlockPos pos) {
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
			entity.remove();
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
			spellKnowledge = new EnumMap<>(EMagicElement.class);
		Map<EAlteration, Boolean> map = spellKnowledge.get(element);
		if (map == null) {
			map = new HashMap<>();
			spellKnowledge.put(element, map);
		}
		
		Boolean old = map.put(alteration, true);
		if ((old == null || !old) && entity != null && entity instanceof PlayerEntity) {
			NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) entity);
		}
	}
	
	@Override
	public Map<EMagicElement, Map<EAlteration, Boolean>> getSpellKnowledge() {
		return this.spellKnowledge;
	}
	
	@Override
	public RegistryKey<World> getSorceryPortalDimension() {
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
	public void setSorceryPortalLocation(RegistryKey<World> dimension, BlockPos pos) {
		this.sorceryPortalDim = dimension;
		this.sorceryPortalPos = pos;
	}

	@Override
	public List<String> getCompletedResearches() {
		return this.completedResearch;
	}

	@Override
	public void completeResearch(String research) {
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
	public void refresh(ServerPlayerEntity player) {
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
	}
}
