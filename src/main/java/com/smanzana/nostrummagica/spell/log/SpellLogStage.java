package com.smanzana.nostrummagica.spell.log;

import java.util.Map;

import com.smanzana.nostrummagica.spell.SpellLocation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;

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
	private final ITextComponent label;

	public SpellLogStage(ITextComponent label, Map<LivingEntity, SpellLogEffectSummary> affectedEnts, Map<SpellLocation, SpellLogEffectSummary> affectedLocs, int elapsedTicks) {
		this.label = label;
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
	
	public ITextComponent getLabel() {
		return label;
	}
}
