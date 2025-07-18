package com.smanzana.nostrummagica.spell;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.spell.SpellCasting.SpellCastResult;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Events around the casting (evoking) of a spell, including information about what spell, who was casting
 * it, and summary information about how the cast went.
 * These events are at the very beginning of a spell's world lifetime and no relation to whether the spell
 * actually does anything.
 * @author Skyler
 *
 */
public abstract class SpellCastEvent extends SpellEvent {
	
	public final boolean isChecking;

	public SpellCastEvent(Spell spell, @Nonnull LivingEntity caster, boolean isChecking) {
		super(spell, caster);
		this.isChecking = isChecking;
	}

	@Cancelable
	public static class Pre extends SpellCastEvent {
		
		public Pre(Spell spell, @Nonnull LivingEntity caster, boolean isChecking) {
			super(spell, caster, isChecking);
		}

		/**
		 * Change what spell will actually be cast
		 * @param spell
		 */
		public void setSpell(Spell spell) {
			this.spell = spell;
		}
	}
	
	public static class Post extends SpellCastEvent {

		protected final SpellCastResult result;
		
		public Post(Spell spell, LivingEntity caster, SpellCastResult result, boolean isChecking) {
			super(spell, caster, isChecking);
			this.result = result;
		}
		
		public SpellCastResult getCastResult() {
			return this.result;
		}
		
	}
}
