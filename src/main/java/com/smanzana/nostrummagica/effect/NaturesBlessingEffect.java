package com.smanzana.nostrummagica.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

public class NaturesBlessingEffect extends MobEffect {

	public static final String ID = "naturesblessing";
	
	public NaturesBlessingEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF38810D);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		// 10, 5, 2.5, ...
		final int interval = Math.max(1, (int) (20.0 * (10.0 / Math.pow(2, amp))));
		return (duration % interval == 0); // 10 seconds, 5 second, 2.5 seconds, ...
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
		if (!entity.level.isClientSide) {
			final float amt = 1; // Doesn't depend on amp
			
			if (entity.getHealth() < entity.getMaxHealth() && entity.getRandom().nextBoolean()) {
				// Health
				entity.heal(amt);
			} else {
				// Food
				if (entity instanceof Player) {
					Player player = (Player) entity;
					player.getFoodData().eat((int) amt, 0);
				} else if (entity instanceof Animal) {
					((Animal) entity).setInLove(null);
				}
			}
		}
    }
}
