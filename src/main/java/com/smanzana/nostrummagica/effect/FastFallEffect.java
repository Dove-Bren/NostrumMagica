package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class FastFallEffect extends Effect {

	public static final String ID = "fast_fall";
	private static final double MAX_VEL = 3.0;
	
	public FastFallEffect() {
		super(EffectType.HARMFUL, 0xFFAAB855);
	}
	
	public boolean isReady(int duration, int amp) {
		return duration > 0; // Every tick
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
//		if (entity.world.isRemote()) {
//			return;
//		}
		
		final Vector3d motion = entity.getMotion();
		if (motion.y < 0 && motion.y > -MAX_VEL) {
			final double y = Math.max(-MAX_VEL, motion.y * 1.4);
			entity.setMotion(motion.x, y, motion.z);
			//entity.velocityChanged = true;
		}
	}
	
	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entity, AttributeModifierManager attributeMap, int amplifier) {
		super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier) {
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onFall(LivingFallEvent event) {
		final LivingEntity ent = event.getEntityLiving();
		
		if (ent.world.isRemote()) {
			return;
		}
		
		EffectInstance effect = ent.getActivePotionEffect(NostrumEffects.fastFall);
		if (effect != null && effect.getDuration() > 0) {
			event.setDistance(event.getDistance() + 1 * (1 + effect.getAmplifier()));
		}
	}
	
}
