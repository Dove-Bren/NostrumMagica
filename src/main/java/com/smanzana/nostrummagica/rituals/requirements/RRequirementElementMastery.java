package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.player.PlayerEntity;

public class RRequirementElementMastery implements IRitualRequirement{

	private EMagicElement element;
	
	public RRequirementElementMastery(EMagicElement element) {
		this.element = element;
	}

	@Override
	public boolean matches(PlayerEntity player, INostrumMagic attr) {
		Boolean known = attr.getKnownElements().get(element);
		return (known != null && known);
	}
}
