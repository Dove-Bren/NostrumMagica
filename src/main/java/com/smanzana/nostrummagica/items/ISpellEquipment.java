package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * Equipment that affects spell casting.
 * 
 * Each piece of ISpellEquipment that is equipped by an entity when they cast
 * is called.
 * This happens after bonuses and discounts from the tome and player's natrual
 * abilities have been factored in.
 * @author Skyler
 *
 */
public interface ISpellEquipment {

	public void apply(LivingEntity caster, SpellCastSummary summary, ItemStack stack);
	
	public static void ApplyAll(LivingEntity entity, SpellCastSummary summary) {
		// Visit an equipped spell armor
		for (ItemStack equip : entity.getEquipmentAndArmor()) {
			if (equip.isEmpty())
				continue;
			if (equip.getItem() instanceof ISpellEquipment) {
				ISpellEquipment armor = (ISpellEquipment) equip.getItem();
				armor.apply(entity, summary, equip);
			}
		}
		
		// Possibly use baubles (for players)
		if (entity instanceof PlayerEntity) {
			final PlayerEntity playerCast = (PlayerEntity) entity;
			IInventory curios = NostrumMagica.instance.curios.getCurios(playerCast);
			if (curios != null) {
				for (int i = 0; i < curios.getSizeInventory(); i++) {
					ItemStack equip = curios.getStackInSlot(i);
					if (equip.isEmpty()) {
						continue;
					}
					
					if (equip.getItem() instanceof ISpellEquipment) {
						ISpellEquipment armor = (ISpellEquipment) equip.getItem();
						armor.apply(entity, summary, equip);
					}
				}
			}
		}
	}
	
}
