package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.entity.player.PlayerEntity;

public class RRequirementQuest implements IRitualRequirement{

	private String questKey;
	
	public RRequirementQuest(String key) {
		this.questKey = key;
	}

	@Override
	public boolean matches(PlayerEntity player, INostrumMagic attr) {
		return attr.getCompletedQuests().contains(questKey);
	}
}
