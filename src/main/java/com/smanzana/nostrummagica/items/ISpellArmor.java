package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Equipment that affects spell casting.
 * 
 * Each piece of SpellArmor that is equiped by an entity when they cast
 * is called.
 * This happens after bonuses and discounts from the tome and player's natrual
 * abilities have been factored in.
 * @author Skyler
 *
 */
public interface ISpellArmor {

	public void apply(EntityLivingBase caster, SpellCastSummary summary, ItemStack stack);
	
}
