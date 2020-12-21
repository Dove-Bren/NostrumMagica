package com.smanzana.nostrummagica.transforms;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.items.IElytraProvider;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class NostrumTransforms {

	public static boolean isElytraFlying(EntityLivingBase ent) {
		for (@Nullable ItemStack stack : ent.getEquipmentAndArmor()) {
			if (stack != null && stack.getItem() instanceof IElytraProvider) {
				if (((IElytraProvider) stack.getItem()).isElytraFlying(ent, stack)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
}
