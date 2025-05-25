package com.smanzana.nostrummagica.spell;

import java.util.Map;
import java.util.Set;

import net.minecraft.world.entity.LivingEntity;

public class SpellResult {
	public final boolean anySuccess;
	public final float damageTotal;
	public final float healingTotal;
	public final Map<LivingEntity, Map<EMagicElement, Float>> affectedEntities;
	public final Set<SpellLocation> affectedLocations;
	
	
	public SpellResult(boolean anySuccess, float damageTotal, float healingTotal,
			Map<LivingEntity, Map<EMagicElement, Float>> affectedEntities, Set<SpellLocation> affectedLocations) {
		super();
		this.anySuccess = anySuccess;
		this.damageTotal = damageTotal;
		this.healingTotal = healingTotal;
		this.affectedLocations = affectedLocations;
		this.affectedEntities = affectedEntities;
	}
}