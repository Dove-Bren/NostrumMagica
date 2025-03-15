package com.smanzana.nostrummagica.integration.curios.items;

import com.smanzana.nostrummagica.effect.NostrumEffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;

public class FloatGuardItem extends NostrumCurio {

	public static final String ID = "float_guard";
	
	public FloatGuardItem() {
		super(NostrumCurios.PropCurio(), ID);
	}
	
	@Override
	public void onWornTick(ItemStack stack, LivingEntity player) {
		player.removeEffect(Effects.LEVITATION);
		player.removeEffect(NostrumEffects.rooted);
	}
	
}
