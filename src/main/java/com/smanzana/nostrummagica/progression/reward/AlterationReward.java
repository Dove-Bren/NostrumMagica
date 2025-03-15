package com.smanzana.nostrummagica.progression.reward;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EAlteration;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;

public class AlterationReward implements IReward {

	private EAlteration alteration;
	
	public AlterationReward(EAlteration alteration) {
		this.alteration = alteration;
	}
	
	@Override
	public void award(PlayerEntity player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr != null)
			attr.unlockAlteration(alteration);
	}
	
	@Override
	public String getDescription() {
		return I18n.get("reward.alteration." + alteration.name().toLowerCase(), new Object[0]);
	}
	
	public EAlteration getAlteration() {
		return this.alteration;
	}
	
}
