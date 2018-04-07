package com.smanzana.nostrummagica.capabilities;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.loretag.LoreCache;
import com.smanzana.nostrummagica.quests.objectives.IObjectiveState;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

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
			return (float) (xpBase * Math.pow(xpGrowth, level));
		}
		
		public static int maxMana(int level) {
			return (int) (manaBase * Math.pow(manaGrowth, level - 1));
		}
	}
	
	private boolean unlocked;
	private int level;
	private float xp;
	private float maxxp;
	private int skillPoints;
	private int control;
	private int tech;
	private int finesse;
	private int mana;
	private int maxMana;
	private float modMana;
	private float modManaCost;
	private float modManaRegen;
	
	private int baseMaxMana; // Max mana without mana bonuses
	
	//private List<IFamiliar> familiars;
	private boolean binding; // TODO binding interface
	
	private Map<String, Integer> loreLevels;
	private Set<String> spellCRCs; // spells we've done's CRCs
	private Map<EMagicElement, Boolean> knownElements;
	private Map<EMagicElement, Boolean> masteredElements;
	private List<SpellShape> shapes; // list of shape keys
	private List<SpellTrigger> triggers; // list of trigger keys
	private Map<EAlteration, Boolean> alterations;
	private List<String> completedQuests;
	private List<String> currentQuests;
	private Map<String, IObjectiveState> questData;
	private BlockPos markLocation;
	private int markDimension;
	
	private EntityLivingBase entity;
	
	public NostrumMagic() {
		unlocked = false;
		//familiars = new LinkedList<>();
		loreLevels = new HashMap<>();
		spellCRCs = new HashSet<>();
		knownElements = new EnumMap<>(EMagicElement.class);
		masteredElements = new EnumMap<>(EMagicElement.class);
		shapes = new LinkedList<>();
		triggers = new LinkedList<>();
		alterations = new EnumMap<>(EAlteration.class);
		currentQuests = new LinkedList<>();
		completedQuests = new LinkedList<>();
		questData = new HashMap<>();
		
		binding = false;
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
			mana = maxMana = getMaxMana();
			maxxp = LevelCurves.maxXP(1);
			skillPoints = control = tech = finesse = 0;
			binding = false;
			
			NostrumMagicaSounds.LEVELUP.play(entity);
		}
	}
	
	private void levelup() {

		level++;
		this.addSkillPoint();
		setLevel(level);
		
		if (entity != null)
			NostrumMagicaSounds.LEVELUP.play(entity);
		// TODO cool effects bruh
	}
	
	@Override
	public void setLevel(int level) {
		this.level = level;
		baseMaxMana = LevelCurves.maxMana(level);
		mana = maxMana = getMaxMana(); 
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
		return Math.round(baseMaxMana * (1f + this.getManaModifier()));
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
	public boolean isBinding() {
		return binding;
	}

	@Override
	public float bindingSecondsLeft() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void startBinding(float duration) {
		// TODO Auto-generated method stub
		
	}

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

	@Override
	public void giveBasicLore(ILoreTagged tagged) {
		if (getLore(tagged) != null)
			return; // Already has some lore
		
		String key = tagged.getLoreKey();
		Integer val = loreLevels.get(key);
		
		if (val != null && val == 2)
			return; // Already have full
		
		loreLevels.put(key, 1);
	}

	@Override
	public void giveFullLore(ILoreTagged tagged) {
		if (getLore(tagged) != null)
			return; // Already has some lore
		
		String key = tagged.getLoreKey();
		Integer val = loreLevels.get(key);
		
		if (val != null && val == 2)
			return; // Already has full
		
		loreLevels.put(key, 2);
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
	public Map<EMagicElement, Boolean> getMasteredElements() {
		return masteredElements;
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
	public void masterElement(EMagicElement element) {
		Boolean known = knownElements.get(element);
		if (known == null || !known)
			learnElement(element);
		
		masteredElements.put(element, Boolean.TRUE);
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
	public void deserialize(boolean unlocked, int level, float xp, int skillpoints, int control, int tech, int finesse,
			int mana, float modMana, float modManaCost, float modManaRegen) {
		this.unlocked = unlocked;
		this.level = level;
		this.xp = xp;
		this.maxxp = LevelCurves.maxXP(this.level);
		this.skillPoints = skillpoints;
		this.control = control;
		this.tech = tech;
		this.finesse = finesse;
		this.mana = mana;
		this.baseMaxMana = LevelCurves.maxMana(this.level);
		this.maxMana = getMaxMana();
		this.modMana = modMana;
		this.modManaCost = modManaCost;
		this.modManaRegen = modManaRegen;
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
	public Map<EMagicElement, Boolean> serializeMasteredElements() {
		return this.masteredElements;
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
				cap.getSkillPoints(), cap.getControl(), cap.getTech(),
				cap.getFinesse(), cap.getMana(),
				cap.getManaModifier(), cap.getManaCostModifier(), cap.getManaRegenModifier());
		
		this.loreLevels = cap.serializeLoreLevels();
		this.spellCRCs = cap.serializeSpellHistory();
		this.knownElements = cap.serializeKnownElements();
		this.masteredElements = cap.serializeMasteredElements();
		this.alterations = cap.serializeAlterations();
		this.shapes = cap.getShapes();
		this.triggers = cap.getTriggers();
		this.markLocation = cap.getMarkLocation();
		this.markDimension = cap.getMarkDimension();
		this.currentQuests = cap.getCurrentQuests();
		this.completedQuests = cap.getCompletedQuests();
		this.questData = cap.getQuestDataMap();
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
}
