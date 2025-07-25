package com.smanzana.nostrummagica.item.api;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.spell.component.SpellAction;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IReactiveEquipment {

	/**
	 * Return a spell action to apply appropriately when this enchantment
	 * is triggered.
	 * <p>
	 * User is the entity that's holdind/wearing the equipment.
	 * Target is either the entity being attacked (if offense) or
	 * the entity that's doing the damaging. (Target is suppied to the spell action)
	 * </p>
	 * @param offense Are WE the ones attacking? (this is a weapon)
	 * @return null to issue no action, or the action to issue
	 */
	public SpellAction getTriggerAction(LivingEntity user, boolean offense, @Nonnull ItemStack stack);
	
	/**
	 * Should we trigger?
	 * @param stack TODO
	 * @param offsense Are we attacking?
	 * @return
	 */
	public boolean shouldTrigger(boolean offense, @Nonnull ItemStack stack);
	
}
