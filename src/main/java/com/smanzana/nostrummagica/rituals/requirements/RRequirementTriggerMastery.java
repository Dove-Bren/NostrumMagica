package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.player.EntityPlayer;

public class RRequirementTriggerMastery implements IRitualRequirement{

	private SpellTrigger trigger;
	
	public RRequirementTriggerMastery(SpellTrigger trigger) {
		this.trigger = trigger;
	}

	@Override
	public boolean matches(EntityPlayer player, INostrumMagic attr) {
		return attr.getTriggers().contains(trigger);
	}
}
