package com.smanzana.nostrummagica.quests.rewards;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EAlteration;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

public class AlterationReward implements IReward {

	private EAlteration alteration;
	
	public AlterationReward(EAlteration alteration) {
		this.alteration = alteration;
	}
	
	@Override
	public void award(EntityPlayer player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr != null)
			attr.unlockAlteration(alteration);
	}
	
	@Override
	public String getDescription() {
		return I18n.format("reward.alteration." + alteration.name().toLowerCase(), new Object[0]);
	}
	
}
