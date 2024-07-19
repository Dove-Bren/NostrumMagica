package com.smanzana.nostrummagica.spell.log;

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.Spell;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;

/**
 * Top level entry for a spell log.
 * Encoding that spells start with shapes first by letting the stages be a flat list at this level instead of letting stages contain stages+effects...
 * @author Skyler
 *
 */
public class SpellLogEntry {

	public static SpellLogEntry LAST = null;
	
	private final Spell spell;
	private final LivingEntity caster;
	private final Map<Integer, SpellLogStageSummary> stages;
	
	public SpellLogEntry(Spell spell, LivingEntity caster) {
		this.spell = spell;
		this.caster = caster;
		this.stages = new TreeMap<>();
		
		LAST = this; int unused;
	}
	
	public void addStage(int stageIdx, ITextComponent label, SpellLogStage stage) {
		stages.computeIfAbsent(stageIdx, idx -> new SpellLogStageSummary(label)).addStage(stage);
	}
	
	public @Nullable SpellLogStageSummary getStages(int idx) {
		return stages.get(idx); // not computeIfAbsent so size still reflects real size
	}
	
	public Map<Integer, SpellLogStageSummary> getAllStages() {
		return stages;
	}
	
	public int getStageIndexCount() {
		return stages.size();
	}
	
	public LivingEntity getCaster() {
		return caster;
	}
	
	public Spell getSpell() {
		return this.spell;
	}
	
}
