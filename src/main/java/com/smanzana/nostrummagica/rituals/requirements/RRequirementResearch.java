package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.entity.player.EntityPlayer;

public class RRequirementResearch implements IRitualRequirement{

	private String researchKey;
	
	public RRequirementResearch(String key) {
		this.researchKey = key;
	}

	@Override
	public boolean matches(EntityPlayer player, INostrumMagic attr) {
		return attr.getCompletedResearches().contains(researchKey);
	}
}
