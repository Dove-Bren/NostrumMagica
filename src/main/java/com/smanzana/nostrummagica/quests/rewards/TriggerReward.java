package com.smanzana.nostrummagica.quests.rewards;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

public class TriggerReward implements IReward {

	private SpellTrigger trigger;
	
	public TriggerReward(SpellTrigger trigger) {
		this.trigger = trigger;
	}
	
	@Override
	public void award(EntityPlayer player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr != null)
			attr.addTrigger(trigger);
	}
	
	@Override
	public String getDescription() {
		return I18n.format("reward.trigger." + trigger.getTriggerKey(), new Object[0]);
	}
	
	public SpellTrigger getTrigger() {
		return this.trigger;
	}
	
}
