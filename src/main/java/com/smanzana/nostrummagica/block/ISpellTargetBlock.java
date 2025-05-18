package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellAction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This block supports processing spell effects directly in itself
 */
public interface ISpellTargetBlock {

	public boolean processSpellEffect(Level level, BlockState state, BlockPos pos, LivingEntity caster, SpellLocation hitLocation, Spell spell, SpellAction action);
	
}
