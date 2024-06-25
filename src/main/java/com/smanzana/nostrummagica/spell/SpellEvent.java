package com.smanzana.nostrummagica.spell;

import javax.annotation.Nonnull;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public abstract class SpellEvent extends Event {

	protected Spell spell;
	protected final @Nonnull LivingEntity caster;
	
	public SpellEvent(Spell spell, @Nonnull LivingEntity caster) {
		this.spell = spell;
		this.caster = caster;
	}

	public Spell getSpell() {
		return spell;
	}

	public LivingEntity getCaster() {
		return caster;
	}
}
