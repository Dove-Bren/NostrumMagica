package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.entity.player.PlayerEntity;

public class RRequirementResearch implements IRitualRequirement{

	private String researchKey;
	
	public RRequirementResearch(String key) {
		this.researchKey = key;
	}

	@Override
	public boolean matches(PlayerEntity player, INostrumMagic attr) {
		return attr.getCompletedResearches().contains(researchKey);
	}
}
