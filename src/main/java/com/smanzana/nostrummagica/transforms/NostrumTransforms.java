package com.smanzana.nostrummagica.transforms;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.items.IElytraProvider;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class NostrumTransforms {

	public static boolean isElytraFlying(LivingEntity ent) {
		for (@Nonnull ItemStack stack : ent.getEquipmentAndArmor()) {
			if (!stack.isEmpty() && stack.getItem() instanceof IElytraProvider) {
				if (((IElytraProvider) stack.getItem()).isElytraFlying(ent, stack)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
}
