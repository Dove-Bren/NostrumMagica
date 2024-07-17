package com.smanzana.nostrummagica.spell.log;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.LivingEntity;

/**
 * Top level entry for a spell log.
 * Encoding that spells start with shapes first by letting the stages be a flat list at this level instead of letting stages contain stages+effects...
 * @author Skyler
 *
 */
public class SpellLogEntry {

	private final LivingEntity caster;
	private final List<SpellLogStage> stages;
	
	public SpellLogEntry(LivingEntity caster) {
		this.caster = caster;
		this.stages = new ArrayList<>();
	}
	
	public void addStage(SpellLogStage stage) {
		stages.add(stage);
	}
	
	public List<SpellLogStage> getStages() {
		return stages;
	}
	
	public LivingEntity getCaster() {
		return caster;
	}
	
}
