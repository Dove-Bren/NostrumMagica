package com.smanzana.nostrummagica.quests.objectives;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpell;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpellShape;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpell.ICastListener;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class ObjectiveSpellCast implements IObjective, ICastListener {
	
	private static class State implements IObjectiveState {

		private static final String KEY = "state";
		private boolean state;
		
		public State() {
			this.state = false;
		}
		
		@Override
		public CompoundNBT toNBT() {
			CompoundNBT tag = new CompoundNBT();
			tag.putBoolean(KEY, state);
			return tag;
		}

		@Override
		public void fromNBT(CompoundNBT nbt) {
			this.state = nbt.getBoolean(KEY);
		}
	}
	
	private NostrumQuest quest;
	private int numElems;
	private int numComps;
	private int numTriggers;
	private Map<EMagicElement, Integer> elements;
	private Map<EAlteration, Integer> alterations;
	private Map<SpellTrigger, Integer> triggers;
	private Map<LegacySpellShape, Integer> shapes;

	public ObjectiveSpellCast() {
		LegacySpell.registerCastListener(this);
		this.elements = new EnumMap<>(EMagicElement.class);
		this.alterations = new EnumMap<>(EAlteration.class);
		this.triggers = new HashMap<>();
		this.shapes = new HashMap<>();
	}
	
	public ObjectiveSpellCast numElems(int count) {
		this.numElems = count;
		return this;
	}
	
	public ObjectiveSpellCast numComps(int count) {
		this.numComps = count;
		return this;
	}
	
	public ObjectiveSpellCast numTriggers(int count) {
		this.numTriggers = count;
		return this;
	}
	
	public ObjectiveSpellCast requiredElement(EMagicElement element) {
		int count = 0;
		if (this.elements.get(element) != null)
			count = elements.get(element);
		this.elements.put(element, ++count);
		return this;
	}
	
	public ObjectiveSpellCast requiredAlteration(EAlteration alteration) {
		int count = 0;
		if (this.alterations.get(alteration) != null)
			count = alterations.get(alteration);
		this.alterations.put(alteration, ++count);
		return this;
	}
	
	public ObjectiveSpellCast requiredShape(LegacySpellShape shape) {
		int count = 0;
		if (this.shapes.get(shape) != null)
			count = shapes.get(shape);
		this.shapes.put(shape, ++count);
		return this;
	}
	
	public ObjectiveSpellCast requiredTrigger(SpellTrigger trigger) {
		int count = 0;
		if (this.triggers.get(trigger) != null)
			count = triggers.get(trigger);
		this.triggers.put(trigger, ++count);
		return this;
	}
	
	@Override
	public void setParentQuest(NostrumQuest quest) {
		this.quest = quest;
	}

	@Override
	public IObjectiveState getBaseState() {
		return new State();
	}

	@Override
	public String getDescription() {
		return I18n.format("objective.spell", new Object[0]);
	}

	@Override
	public boolean isComplete(INostrumMagic attr) {
		Object o = attr.getQuestData(quest.getKey());
		if (o == null || !(o instanceof State))
			return false;
		State s = (State) o;
		return s.state;
	}

	@Override
	public void onCast(LivingEntity entity, LegacySpell spell) {
		
		if (entity instanceof PlayerEntity) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr == null)
				return;
			
			if (attr.getCurrentQuests().contains(quest.getKey())) {
				// Check spell criteria
				
				Object o = attr.getQuestData(quest.getKey());
				State s;
				boolean changed = false;
				if (o == null || !(o instanceof State))
					s = new State();
				else
					s = (State) o;
				if (s.state)
					return; // Already activated
				
				if (spellMatches(spell)) {
					s.state = true;
					changed = true;
				}
				attr.setQuestData(quest.getKey(), s);
				if (changed && !entity.world.isRemote) {
					// Spells are cast on the server, so sync to client quest state
					NetworkHandler.sendTo(
							new StatSyncMessage(attr), (ServerPlayerEntity) entity);
				}
					
			}
		}
		
		return;
	}
	
	private boolean spellMatches(LegacySpell spell) {
		if (numElems > 0)
			if (numElems > spell.getElementCount())
				return false;
		if (numTriggers > 0)
			if (numTriggers > spell.getTriggerCount())
				return false;
		if (numComps > 0)
			if (numComps > spell.getComponentCount())
				return false;
		
		if (!elements.isEmpty()) {
			Map<EMagicElement, Integer> spellMap = spell.getElements();
			for (EMagicElement element : elements.keySet()) {
				Integer count = elements.get(element);
				if (count == null || count == 0)
					continue;
				Integer spellCount = spellMap.get(element);
				if (spellCount == null || spellCount == 0)
					return false;
				if (spellCount < count)
					return false;
			}
		}
		
		if (!alterations.isEmpty()) {
			Map<EAlteration, Integer> spellMap = spell.getAlterations();
			for (EAlteration alteration : alterations.keySet()) {
				Integer count = alterations.get(alteration);
				if (count == null || count == 0)
					continue;
				Integer spellCount = spellMap.get(alteration);
				if (spellCount == null || spellCount == 0)
					return false;
				if (spellCount < count)
					return false;
			}
		}
		
		if (!shapes.isEmpty()) {
			Map<LegacySpellShape, Integer> spellMap = spell.getShapes();
			for (LegacySpellShape shape: shapes.keySet()) {
				Integer count = shapes.get(shape);
				if (count == null || count == 0)
					continue;
				Integer spellCount = spellMap.get(shape);
				if (spellCount == null || spellCount == 0)
					return false;
				if (spellCount < count)
					return false;
			}
		}
		
		if (!triggers.isEmpty()) {
			Map<SpellTrigger, Integer> spellMap = spell.getTriggers();
			for (SpellTrigger trigger: triggers.keySet()) {
				Integer count = triggers.get(trigger);
				if (count == null || count == 0)
					continue;
				Integer spellCount = spellMap.get(trigger);
				if (spellCount == null || spellCount == 0)
					return false;
				if (spellCount < count)
					return false;
			}
		}
		
		return true;
	}

}
