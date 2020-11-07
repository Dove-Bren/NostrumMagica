package com.smanzana.nostrummagica.capabilities;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.loretag.LoreCache;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.LoreMessage;
import com.smanzana.nostrummagica.quests.objectives.IObjectiveState;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

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
	private int control;
	private int tech;
	private int finesse;
	private int mana;
	//private int maxMana; // We calculate max instead of storing it
	private float modMana; // Additional % mana
	private int modManaFlat; // Additiona mana (flat int value)
	private float modManaCost;
	private float modManaRegen;
	
	private int baseMaxMana; // Max mana without mana bonuses
	
	private List<EntityLivingBase> familiars;
	private SpellComponentWrapper bindingComponent;
	private Spell bindingSpell;
	private int bindingTomeID;
	
	private Map<String, Integer> loreLevels;
	private Set<String> spellCRCs; // spells we've done's CRCs
	private Map<EMagicElement, Boolean> knownElements;
	private Map<EMagicElement, Integer> elementalMastery;
	private Map<EMagicElement, Boolean> elementTrials;
	private List<SpellShape> shapes; // list of shape keys
	private List<SpellTrigger> triggers; // list of trigger keys
	private Map<EAlteration, Boolean> alterations;
	private List<String> completedQuests;
	private List<String> currentQuests;
	private Map<String, IObjectiveState> questData;
	private List<String> completedResearch;
	private BlockPos markLocation;
	private int markDimension;
	private boolean enhancedTeleport;
	private Map<EMagicElement, Map<EAlteration, Boolean>> spellKnowledge;
	private int sorceryPortalDim;
	private BlockPos sorceryPortalPos;
	
	private EntityLivingBase entity;
	
	public NostrumMagic() {
		unlocked = false;
		//familiars = new LinkedList<>();
		loreLevels = new HashMap<>();
		spellCRCs = new HashSet<>();
		knownElements = new EnumMap<>(EMagicElement.class);
		elementalMastery = new EnumMap<>(EMagicElement.class);
		elementTrials = new EnumMap<>(EMagicElement.class);
		shapes = new LinkedList<>();
		triggers = new LinkedList<>();
		alterations = new EnumMap<>(EAlteration.class);
		currentQuests = new LinkedList<>();
		completedQuests = new LinkedList<>();
		questData = new HashMap<>();
		completedResearch = new LinkedList<>();
		bindingSpell = null;
		bindingComponent = null;
		familiars = new LinkedList<>();
		sorceryPortalDim = 0;
		sorceryPortalPos = null;
		enhancedTeleport = false;
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
			skillPoints = control = tech = finesse = 0;
			bindingSpell = null;
			bindingComponent = null;
			
			this.setElementMastery(EMagicElement.PHYSICAL, 1);
			this.completeResearch("origin");
			this.addResearchPoint();
			//this.completeResearch("spellcraft");
			this.giveBasicLore(SpellRune.instance());
			this.giveBasicLore(BlankScroll.instance());
			this.giveBasicLore(SpellScroll.instance());
			this.giveBasicLore(ReagentItem.instance());
			
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
		return this.getManaBonus() + Math.round(baseMaxMana * (1f + this.getManaModifier()));
	}

	@Override
	public void setMana(int mana) {
		this.mana = Math.min(mana, this.getMaxMana());
	}

	@Override
	public void addMana(int mana) {
		this.mana = Math.min(this.mana + mana, this.getMaxMana());
	}

	@Override
	public void setMaxMana(int max) {
		this.baseMaxMana = max;
		if (this.mana > getMaxMana())
			this.mana = getMaxMana();
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
		
//		if (NostrumMagica.proxy.getPlayer() != null) {
//			NostrumMagicaSounds.UI_TICK.play(NostrumMagica.proxy.getPlayer());
//		}
		
		if (entity != null && entity instanceof EntityPlayer && !entity.worldObj.isRemote) {
			NetworkHandler.getSyncChannel().sendTo(
					new LoreMessage(tagged, this),
					(EntityPlayerMP) entity);
			
		}
	}

	@Override
	public void giveFullLore(ILoreTagged tagged) {
		String key = tagged.getLoreKey();
		Integer val = loreLevels.get(key);
		
		if (val != null && val == 2)
			return; // Already has full
		
		loreLevels.put(key, 2);
//		if (NostrumMagica.proxy.getPlayer() != null || !NostrumMagica.proxy.getPlayer().worldObj.isRemote) {
//			NostrumMagicaSounds.UI_TICK.play(NostrumMagica.proxy.getPlayer());
//		}
		
		if (entity != null && entity instanceof EntityPlayer && !entity.worldObj.isRemote) {
			NetworkHandler.getSyncChannel().sendTo(
					new LoreMessage(tagged, this),
					(EntityPlayerMP) entity);
			
		}
		
		addLoreBonus(this, val, 2);
	}

	@Override
	public boolean wasSpellDone(Spell spell) {
		String CRC = spell.crc();
		
		return !spellCRCs.add(CRC);
	}

	@Override
	public List<SpellShape> getShapes() {
		return shapes;
	}

	@Override
	public List<SpellTrigger> getTriggers() {
		return triggers;
	}

	@Override
	public Map<EMagicElement, Boolean> getKnownElements() {
		return knownElements;
	}

	@Override
	public Map<EMagicElement, Integer> getElementMastery() {
		return elementalMastery;
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
	public void addTrigger(SpellTrigger trigger) {
		triggers.add(trigger);
		
		doUnlockCheck();
	}

	@Override
	public void learnElement(EMagicElement element) {
		Boolean old = knownElements.put(element, Boolean.TRUE);
		if (old == null || !old) {
			// Learned for the first time
			// TODO effect
			if (this.entity != null && !this.entity.worldObj.isRemote
					&& this.entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) this.entity;
				player.addChatComponentMessage(new TextComponentString("The forces of "
						+ element.getName() + " have been unlocked!"));
			}

			doUnlockCheck();
		}
	}

	@Override
	public void setElementMastery(EMagicElement element, int level) {
		Boolean known = knownElements.get(element);
		if (known == null || !known)
			learnElement(element);
		
		elementalMastery.put(element, level);
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
		
		if (shapes.isEmpty() || triggers.isEmpty())
			return;
		
		boolean found = false;
		for (EMagicElement e : EMagicElement.values()) {
			if (knownElements.get(e) != null
					&& knownElements.get(e)) {
				found = true;
				break;
			}
		}
		
		if (!found)
			return;
		
		unlock();
		// TODO effects
		if (this.entity != null && !this.entity.worldObj.isRemote
				&& this.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) this.entity;
			player.addChatComponentMessage(new TextComponentString(
					"Magic Unlocked"));
		}
		
	}

	@Override
	public void deserialize(boolean unlocked, int level, float xp, int skillpoints, int researchpoints, int control, int tech, int finesse,
			int mana, float modMana, int manaBonus, float modManaCost, float modManaRegen) {
		this.unlocked = unlocked;
		this.level = level;
		this.xp = xp;
		this.maxxp = LevelCurves.maxXP(this.level);
		this.skillPoints = skillpoints;
		this.researchPoints = researchpoints;
		this.control = control;
		this.tech = tech;
		this.finesse = finesse;
		this.mana = mana;
		this.baseMaxMana = LevelCurves.maxMana(this.level);
		this.modMana = modMana;
		this.modManaCost = modManaCost;
		this.modManaRegen = modManaRegen;
		this.modManaFlat = manaBonus;
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
	public Map<EMagicElement, Boolean> serializeKnownElements() {
		return this.knownElements;
	}

	@Override
	public Map<EMagicElement, Integer> serializeElementMastery() {
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
	public void copy(INostrumMagic cap) {
		System.out.println("Overriding stats from" + this.mana + " to " + cap.getMana() + " mana");
		this.deserialize(cap.isUnlocked(), cap.getLevel(), cap.getXP(),
				cap.getSkillPoints(), cap.getResearchPoints(),
				cap.getControl(), cap.getTech(), cap.getFinesse(), cap.getMana(),
				cap.getManaModifier(), cap.getManaBonus(), cap.getManaCostModifier(), cap.getManaRegenModifier());
		
		this.loreLevels = cap.serializeLoreLevels();
		this.spellCRCs = cap.serializeSpellHistory();
		this.knownElements = cap.serializeKnownElements();
		this.elementalMastery = cap.serializeElementMastery();
		this.elementTrials = cap.serializeElementTrials();
		this.alterations = cap.serializeAlterations();
		this.shapes = cap.getShapes();
		this.triggers = cap.getTriggers();
		this.markLocation = cap.getMarkLocation();
		this.markDimension = cap.getMarkDimension();
		this.currentQuests = cap.getCurrentQuests();
		this.completedQuests = cap.getCompletedQuests();
		this.questData = cap.getQuestDataMap();
		this.completedResearch = cap.getCompletedResearches();
		this.bindingTomeID = cap.getBindingID();
		this.bindingSpell = cap.getBindingSpell();
		this.bindingComponent = cap.getBindingComponent();
		this.spellKnowledge = cap.getSpellKnowledge();
		this.enhancedTeleport = cap.hasEnhancedTeleport();
	}
	
	@Override
	public void provideEntity(EntityLivingBase entity) {
		this.entity = entity;
	}

	@Override
	public BlockPos getMarkLocation() {
		return markLocation;
	}
	
	@Override
	public int getMarkDimension() {
		return markDimension;
	}
	
	@Override
	public void setMarkLocation(int dimension, BlockPos pos) {
		this.markDimension = dimension;
		this.markLocation = pos;
	}

	@Override
	public void addManaModifier(float modifier) {
		this.modMana += modifier;
	}
	
	@Override
	public void addManaBonus(int mana) {
		this.modManaFlat += mana;
	}

	@Override
	public void addManaRegenModifier(float modifier) {
		this.modManaRegen += modifier;
	}

	@Override
	public void addManaCostModifer(float modifier) {
		this.modManaCost += modifier;
	}

	@Override
	public float getManaModifier() {
		return this.modMana;
	}
	
	@Override
	public int getManaBonus() {
		return this.modManaFlat;
	}

	@Override
	public float getManaRegenModifier() {
		return this.modManaRegen;
	}

	@Override
	public float getManaCostModifier() {
		return this.modManaCost;
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
	public IObjectiveState getQuestData(String quest) {
		return questData.get(quest);
	}
	
	@Override
	public void setQuestData(String quest, IObjectiveState data) {
		if (data == null)
			questData.remove(quest);
		else
			questData.put(quest, data);
	}
	
	@Override
	public Map<String, IObjectiveState> getQuestDataMap() {
		return questData;
	}
	
	@Override
	public void setQuestDataMap(Map<String, IObjectiveState> map) {
		this.questData = map;
	}

	@Override
	public SpellComponentWrapper getBindingComponent() {
		return this.bindingComponent;
	}

	@Override
	public Spell getBindingSpell() {
		return this.bindingSpell;
	}

	@Override
	public void startBinding(Spell spell, SpellComponentWrapper comp, int tomeID) {
		this.bindingSpell = spell;
		this.bindingComponent = comp;
		this.bindingTomeID = tomeID;
	}

	@Override
	public boolean isBinding() {
		return this.bindingSpell != null;
	}
	
	@Override
	public int getBindingID() {
		return this.bindingTomeID;
	}

	@Override
	public void completeBinding(ItemStack tome) {
		
		if (this.entity != null && this.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			
			if (tome == null) {
				tome = NostrumMagica.findTome(player, bindingTomeID);
				if (tome == null) {
					player.addChatComponentMessage(new TextComponentTranslation(
							"info.tome.bind_missing", new Object[] {bindingSpell.getName()}));
					return;
				}
			}
			
			if (!this.entity.worldObj.isRemote) {
				NostrumMagicaSounds.LEVELUP.play(player);
				player.addChatComponentMessage(new TextComponentTranslation(
						"info.tome.bind_finish", new Object[] {bindingSpell.getName()}));
			}
			
			SpellTome.addSpell(tome, this.bindingSpell);
		}

		this.bindingSpell = null;
		this.bindingTomeID = 0;
		this.bindingComponent = null;
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
	public List<EntityLivingBase> getFamiliars() {
		return familiars;
	}

	@Override
	public void addFamiliar(EntityLivingBase familiar) {
		familiars.add(familiar);
	}

	@Override
	public void clearFamiliars() {
		if (familiars.isEmpty())
			return;
		
		for (EntityLivingBase entity : familiars) {
			entity.setDead();
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
		if ((old == null || !old) && entity != null && entity instanceof EntityPlayer) {
			NostrumMagica.proxy.syncPlayer((EntityPlayerMP) entity);
		}
	}
	
	@Override
	public Map<EMagicElement, Map<EAlteration, Boolean>> getSpellKnowledge() {
		return this.spellKnowledge;
	}
	
	@Override
	public int getSorceryPortalDimension() {
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
	public void setSorceryPortalLocation(int dimension, BlockPos pos) {
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
}
