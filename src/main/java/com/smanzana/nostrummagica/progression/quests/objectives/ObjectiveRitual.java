package com.smanzana.nostrummagica.progression.quests.objectives;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.progression.quests.NostrumQuest;
import com.smanzana.nostrummagica.rituals.IRitualListener;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ObjectiveRitual implements IObjective, IRitualListener {
	
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
	private String ritualKey;

	public ObjectiveRitual(String ritualKey) {
		this.ritualKey = ritualKey;
		RitualRegistry.instance().addRitualListener(this);
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
	public void onRitualPerformed(RitualRecipe ritual, World world, PlayerEntity player, BlockPos center) {
		if (ritual.getTitleKey().equalsIgnoreCase(ritualKey)) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			if (attr.getCurrentQuests().contains(quest.getKey())) {
				Object o = attr.getQuestData(quest.getKey());
				State s;
				if (o == null || !(o instanceof State))
					s = new State();
				else
					s = (State) o;
				s.state = true;
				attr.setQuestData(quest.getKey(), s);
					
			}
		}
	}
	
	@Override
	public String getDescription() {
		return I18n.format("objective.ritual." + ritualKey, new Object[0]);
	}

	@Override
	public boolean isComplete(INostrumMagic attr) {
		Object o = attr.getQuestData(quest.getKey());
		if (o == null || !(o instanceof State))
			return false;
		State s = (State) o;
		return s.state;
	}
	
	
	
}
