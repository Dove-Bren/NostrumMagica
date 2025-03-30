package com.smanzana.nostrummagica.integration.curios.items;

import com.smanzana.nostrummagica.effect.NostrumEffects;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class FloatGuardItem extends NostrumCurio {

	public static final String ID = "float_guard";
	
	public FloatGuardItem() {
		super(NostrumCurios.PropCurio(), ID);
	}
	
	@Override
	public void onWornTick(ItemStack stack, SlotContext slot) {
		slot.entity().removeEffect(MobEffects.LEVITATION);
		slot.entity().removeEffect(NostrumEffects.rooted);
	}
	
}
