package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;

import net.minecraft.world.entity.LivingEntity;

/**
 * This entity supports processing spell effects directly in itself
 */
public interface ISpellHandlingEntity {

	public boolean processSpellEffect(LivingEntity caster, SpellEffectPart effect, SpellAction action);
	
}
