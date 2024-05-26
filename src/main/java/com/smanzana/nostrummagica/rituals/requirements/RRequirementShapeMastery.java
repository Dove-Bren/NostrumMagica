package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.components.LegacySpellShape;

import net.minecraft.entity.player.PlayerEntity;

public class RRequirementShapeMastery implements IRitualRequirement{

	private LegacySpellShape shape;
	
	public RRequirementShapeMastery(LegacySpellShape shape) {
		this.shape = shape;
	}

	@Override
	public boolean matches(PlayerEntity player, INostrumMagic attr) {
		return attr.getShapes().contains(shape);
	}
}
