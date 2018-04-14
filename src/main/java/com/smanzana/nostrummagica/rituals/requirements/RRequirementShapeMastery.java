package com.smanzana.nostrummagica.rituals.requirements;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.components.SpellShape;

import net.minecraft.entity.player.EntityPlayer;

public class RRequirementShapeMastery implements IRitualRequirement{

	private SpellShape shape;
	
	public RRequirementShapeMastery(SpellShape shape) {
		this.shape = shape;
	}

	@Override
	public boolean matches(EntityPlayer player, INostrumMagic attr) {
		return attr.getShapes().contains(shape);
	}
}
