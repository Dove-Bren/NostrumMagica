package com.smanzana.nostrummagica.item;

import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * Like ISpellEquipment except {@link #onCastFromTool(LivingEntity, SpellCastSummary, ItemStack)} is only called when
 * it's the originating tool that's casting the spell, whereas all ISpellEquipment that a player has equipped are applied.
 * This allows things like tomes to cap the total bonus from the tool before all the player's equipment gets involved, as well
 * as makes it easier to limit affecting spells if you just happen to be in a hand.
 * @author Skyler
 *
 */
public interface ISpellCastingTool {

	public void onStartCastFromTool(LivingEntity caster, SpellCastSummary summary, ItemStack stack);
	
	public void onFinishCastFromTool(LivingEntity caster, final SpellCastSummary summary, ItemStack stack);
	
}
