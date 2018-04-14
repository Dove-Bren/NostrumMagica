package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EAlteration;

import net.minecraft.entity.player.EntityPlayer;

public class RRequirementAlterationMastery implements IRitualRequirement{

	private EAlteration alteration;
	
	public RRequirementAlterationMastery(EAlteration alteration) {
		this.alteration = alteration;
	}

	@Override
	public boolean matches(EntityPlayer player, INostrumMagic attr) {
		Boolean bool = attr.getAlterations().get(alteration);
		return (bool != null && bool);
	}
}
