package com.smanzana.nostrummagica.integration.curios.items;

import com.smanzana.nostrummagica.effect.NostrumEffects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffects;

public class FloatGuardItem extends NostrumCurio {

	public static final String ID = "float_guard";
	
	public FloatGuardItem() {
		super(NostrumCurios.PropCurio(), ID);
	}
	
	@Override
	public void onWornTick(ItemStack stack, LivingEntity player) {
		player.removeEffect(MobEffects.LEVITATION);
		player.removeEffect(NostrumEffects.rooted);
	}
	
}
