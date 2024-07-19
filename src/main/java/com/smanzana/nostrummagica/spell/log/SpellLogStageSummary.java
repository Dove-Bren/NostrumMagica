package com.smanzana.nostrummagica.spell.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.smanzana.nostrummagica.spell.SpellLocation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;

/**
 * Each shape in a spell can trigger multiple times, each time creating a spell log 'stage' with the same idx.
 * This summary class is one-to-one with spell shapes/effects (real stages) and holds all the sub-stages as
 * well as summary data
 * @author Skyler
 *
 */
public class SpellLogStageSummary {

	private final List<SpellLogStage> stages;
	private final Map<LivingEntity, Integer> affectedEntCounts;
	private final Map<SpellLocation, Integer> affectedLocCounts;
	private final ITextComponent label;
	
	private float totalDamage;
	private float totalHeal;
	private boolean hasEffects;
	
	public SpellLogStageSummary(ITextComponent label) {
		this.stages = new ArrayList<>();
		this.affectedEntCounts = new HashMap<>();
		this.affectedLocCounts = new HashMap<>();
		this.label = label;
		
		totalDamage = 0;
		totalHeal = 0;
		hasEffects = false;
	}
	
	public void addStage(SpellLogStage stage) {
		stages.add(stage);
		
		for (Entry<LivingEntity, SpellLogEffectSummary> entry : stage.getAffectedEnts().entrySet()) {
			this.affectedEntCounts.merge(entry.getKey(), 1, Integer::sum);
			if (entry.getValue() != null) {
				this.totalDamage += entry.getValue().getTotalDamage();
				this.totalHeal += entry.getValue().getTotalHeal();
				this.hasEffects = true;
			}
		}
		for (Entry<SpellLocation, SpellLogEffectSummary> entry : stage.getAffectedLocs().entrySet()) {
			this.affectedLocCounts.merge(entry.getKey(), 1, Integer::sum);
			if (entry.getValue() != null) {
				this.totalDamage += entry.getValue().getTotalDamage();
				this.totalHeal += entry.getValue().getTotalHeal();
				this.hasEffects = true;
			}
		}
	}

	public List<SpellLogStage> getStages() {
		return stages;
	}

	public Map<LivingEntity, Integer> getAffectedEntCounts() {
		return affectedEntCounts;
	}

	public Map<SpellLocation, Integer> getAffectedLocCounts() {
		return affectedLocCounts;
	}

	public ITextComponent getLabel() {
		return label;
	}

	public float getTotalDamage() {
		return totalDamage;
	}

	public float getTotalHeal() {
		return totalHeal;
	}
	
	public boolean hasEffects() {
		return this.hasEffects;
	}
	
}
