package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.spells.components.SpellAction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface EnchantedEquipment {

	public static final String NBT_ITEM = "nested_item";
	public static final String NBT_USES = "enchanted_uses";
	
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
	public SpellAction getTriggerAction(EntityLivingBase user, boolean offense, ItemStack stack);
	
	/**
	 * Should we trigger?
	 * @param stack TODO
	 * @param offsense Are we attacking?
	 * @return
	 */
	public boolean shouldTrigger(boolean offense, ItemStack stack);
	
}
