package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class FastFallEffect extends MobEffect {

	public static final String ID = "fast_fall";
	private static final double MAX_VEL = 3.0;
	
	public FastFallEffect() {
		super(MobEffectCategory.HARMFUL, 0xFFAAB855);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return duration > 0; // Every tick
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
//		if (entity.world.isRemote()) {
//			return;
//		}
		
		final Vec3 motion = entity.getDeltaMovement();
		if (motion.y < 0 && motion.y > -MAX_VEL) {
			final double y = Math.max(-MAX_VEL, motion.y * 1.4);
			entity.setDeltaMovement(motion.x, y, motion.z);
			//entity.velocityChanged = true;
		}
	}
	
	@Override
	public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
		super.addAttributeModifiers(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeMap attributeMapIn, int amplifier) {
		super.removeAttributeModifiers(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onFall(LivingFallEvent event) {
		final LivingEntity ent = event.getEntityLiving();
		
		if (ent.level.isClientSide()) {
			return;
		}
		
		MobEffectInstance effect = ent.getEffect(NostrumEffects.fastFall);
		if (effect != null && effect.getDuration() > 0) {
			event.setDistance(event.getDistance() + 1 * (1 + effect.getAmplifier()));
		}
	}
	
}
