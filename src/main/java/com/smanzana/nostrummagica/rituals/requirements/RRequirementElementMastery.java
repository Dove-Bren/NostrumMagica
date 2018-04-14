package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.player.EntityPlayer;

public class RRequirementElementMastery implements IRitualRequirement{

	private EMagicElement element;
	
	public RRequirementElementMastery(EMagicElement element) {
		this.element = element;
	}

	@Override
	public boolean matches(EntityPlayer player, INostrumMagic attr) {
		Boolean bool = attr.getMasteredElements().get(element);
		return (bool != null && bool);
	}
}
