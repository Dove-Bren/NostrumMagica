package com.smanzana.nostrummagica.spell;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.Spell.SpellResult;
import com.smanzana.nostrummagica.spell.component.SpellAction.SpellActionResult;

import net.minecraft.entity.LivingEntity;

public abstract class SpellEffectEvent extends SpellEvent {
	
	public SpellEffectEvent(Spell spell, @Nullable LivingEntity caster) {
		super(spell, caster);
	}
	
	public static class SpellEffectEntityEvent extends SpellEffectEvent {

		protected final LivingEntity affectedEntity;
		protected final SpellActionResult result;
		
		public SpellEffectEntityEvent(Spell spell, @Nullable LivingEntity caster, LivingEntity affectedEntity, SpellActionResult result) {
			super(spell, caster);
			this.affectedEntity = affectedEntity;
			this.result = result;
		}
		
		public LivingEntity getAffectedEntity() {
			return this.affectedEntity;
		}

		public SpellActionResult getSpellResult() {
			return result;
		}
	}
	
	public static class SpellEffectBlockEvent extends SpellEffectEvent {
		
		protected final SpellLocation targetLocation;
		protected final SpellActionResult result;

		public SpellEffectBlockEvent(Spell spell, LivingEntity caster, SpellLocation targetLocation, SpellActionResult result) {
			super(spell, caster);
			this.targetLocation = targetLocation;
			this.result = result;
		}

		public SpellLocation getTargetLocation() {
			return targetLocation;
		}

		public SpellActionResult getSpellResult() {
			return result;
		}
	}
	
	public static class SpellEffectEndEvent extends SpellEffectEvent {

		protected final SpellResult result;
		
		public SpellEffectEndEvent(Spell spell, LivingEntity caster, SpellResult result) {
			super(spell, caster);
			this.result = result;
		}
		
		public SpellResult getSpellFinalResults() {
			return this.result;
		}
		
	}
}
