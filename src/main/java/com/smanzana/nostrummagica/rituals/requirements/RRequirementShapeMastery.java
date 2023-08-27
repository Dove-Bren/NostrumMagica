package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.components.SpellShape;

import net.minecraft.entity.player.PlayerEntity;

public class RRequirementShapeMastery implements IRitualRequirement{

	private SpellShape shape;
	
	public RRequirementShapeMastery(SpellShape shape) {
		this.shape = shape;
	}

	@Override
	public boolean matches(PlayerEntity player, INostrumMagic attr) {
		return attr.getShapes().contains(shape);
	}
}
