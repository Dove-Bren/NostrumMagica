package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This block supports processing spell effects directly in itself
 */
public interface ISpellTargetBlock {

	/**
	 * Optionally process the spell effect.
	 * Return true if the block 'consumes' the effect, and the effect should not play.
	 * @param level
	 * @param state
	 * @param pos
	 * @param caster
	 * @param hitLocation
	 * @param effect
	 * @param action
	 * @return
	 */
	public boolean processSpellEffect(Level level, BlockState state, BlockPos pos, LivingEntity caster, SpellLocation hitLocation, SpellEffectPart effect, SpellAction action);
	
}
