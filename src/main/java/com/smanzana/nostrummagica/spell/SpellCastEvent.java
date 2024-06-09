package com.smanzana.nostrummagica.spell;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.spell.SpellCasting.SpellCastResult;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public abstract class SpellCastEvent extends Event {

	protected Spell spell;
	protected final @Nonnull LivingEntity caster;
	
	public SpellCastEvent(Spell spell, @Nonnull LivingEntity caster) {
		this.spell = spell;
		this.caster = caster;
	}

	public Spell getSpell() {
		return spell;
	}

	public LivingEntity getCaster() {
		return caster;
	}

	@Cancelable
	public static class Pre extends SpellCastEvent {
		
		public Pre(Spell spell, @Nonnull LivingEntity caster) {
			super(spell, caster);
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
		
		public Post(Spell spell, LivingEntity caster, SpellCastResult result) {
			super(spell, caster);
			this.result = result;
		}
		
		public SpellCastResult getCastResult() {
			return this.result;
		}
		
	}
}
