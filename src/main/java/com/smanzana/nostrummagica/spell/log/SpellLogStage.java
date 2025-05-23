package com.smanzana.nostrummagica.spell.log;

import java.util.Map;

import com.smanzana.nostrummagica.spell.SpellLocation;

import net.minecraft.world.entity.LivingEntity;

/**
 * One discrete 'stage' of the spell. Maps to 'shapes' in the actual spell, where each stage
 * ends up specifying some number of locations/entities to affect.
 * @author Skyler
 *
 */
public class SpellLogStage {
	
	private final Map<LivingEntity, SpellLogEffectSummary> affectedEnts;
	private final Map<SpellLocation, SpellLogEffectSummary> affectedLocs;
	private final int elapsedTicks;

	public SpellLogStage(Map<LivingEntity, SpellLogEffectSummary> affectedEnts, Map<SpellLocation, SpellLogEffectSummary> affectedLocs, int elapsedTicks) {
		this.affectedEnts = affectedEnts;
		this.affectedLocs = affectedLocs;
		this.elapsedTicks = elapsedTicks;
	}

	public Map<LivingEntity, SpellLogEffectSummary> getAffectedEnts() {
		return affectedEnts;
	}

	public Map<SpellLocation, SpellLogEffectSummary> getAffectedLocs() {
		return affectedLocs;
	}

	public int getElapsedTicks() {
		return elapsedTicks;
	}
}
