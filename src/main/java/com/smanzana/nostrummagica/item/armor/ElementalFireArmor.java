package com.smanzana.nostrummagica.item.armor;

import java.lang.reflect.Field;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ElementalFireArmor extends ElementalArmor {

	public static final String ID_PREFIX = "armor_fire_";
	public static final String ID_HELM_NOVICE = ID_PREFIX + "helm_novice";
	public static final String ID_HELM_ADEPT = ID_PREFIX + "helm_adept";
	public static final String ID_HELM_MASTER = ID_PREFIX + "helm_master";
		
	public static final String ID_CHEST_NOVICE = ID_PREFIX + "chest_novice";
	public static final String ID_CHEST_ADEPT = ID_PREFIX + "chest_adept";
	public static final String ID_CHEST_MASTER = ID_PREFIX + "chest_master";
		
	public static final String ID_LEGS_NOVICE = ID_PREFIX + "legs_novice";
	public static final String ID_LEGS_ADEPT = ID_PREFIX + "legs_adept";
	public static final String ID_LEGS_MASTER = ID_PREFIX + "legs_master";
		
	public static final String ID_FEET_NOVICE = ID_PREFIX + "feet_novice";
	public static final String ID_FEET_ADEPT = ID_PREFIX + "feet_adept";
	public static final String ID_FEET_MASTER = ID_PREFIX + "feet_master";
		
	public ElementalFireArmor(EquipmentSlot slot, Type type, Item.Properties properties) {
		super(EMagicElement.FIRE, slot, type, properties);
	}
	
	public static final void onFullSetTick(LivingEntity entity, ElementalArmor.Type type) {
		// Fire prevents fire.
		// Level 1(0) reduces fire time (25% reduction by 50% of the time reducing by
		// another tick)
		// Level 2(1) halves fire time
		// Level 3 prevents fire all-together
		if (type == Type.MASTER) {
			if (entity.isOnFire()) {
				entity.clearFire();
			}
		} else {
			if (type == Type.ADEPT || NostrumMagica.rand.nextBoolean()) {
				try {
					Field fireField = ObfuscationReflectionHelper.findField(Entity.class,
							"remainingFireTicks");
					fireField.setAccessible(true);

					int val = fireField.getInt(entity);

					if (val > 0) {
						// On fire so decrease

						// Decrease every other 20 so damage ticks aren't doubled.
						// Do this by checking if divisible by 40 (true every 2 %20).
						// (We skip odds to get to evens to simplify logic)
						if (val % 2 == 0) {
							if (val % 20 != 0 || val % 40 == 0) {
								fireField.setInt(entity, val - 1);
							}
						} else {
							; // Skip so that next tick is even
						}
					}

					fireField.setAccessible(false);
				} catch (Exception e) {
					; // This will happen every tick, so don't log
				}
			}
		}
	}
	
}
