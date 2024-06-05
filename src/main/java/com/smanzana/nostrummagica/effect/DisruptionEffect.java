package com.smanzana.nostrummagica.effect;

import java.util.HashMap;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class DisruptionEffect extends Effect {

	public static final String ID = "disruption";
	
	private static final Map<LivingEntity, Integer> lastDamage = new HashMap<>();
	
	public DisruptionEffect() {
		super(EffectType.HARMFUL, 0xFF916F82);
	}
	
	public boolean isReady(int duration, int amp) {
		return false; // Never ticks
	}

	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entity, AttributeModifierManager attributeMap, int amplifier) {
		super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier) {
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	protected static boolean canDamage(LivingEntity entity) {
		Integer lastTicks = lastDamage.get(entity);
		return lastTicks == null || entity.ticksExisted - lastTicks >= 1;
	}
	
	protected static void markDamaged(LivingEntity entity) {
		lastDamage.put(entity, entity.ticksExisted);
	}
	
	@SubscribeEvent
	public static void onTeleport(EntityTeleportEvent event) {
		if (event.isCanceled()) {
			return;
		}
		if (!(event.getEntity() instanceof LivingEntity)) {
			return;
		}
		if (event.getEntity().world.isRemote()) {
			return;
		}
		if (event.getEntity() instanceof PlayerEntity && ((PlayerEntity) event.getEntity()).isCreative()) {
			return;
		}
		final LivingEntity ent = (LivingEntity) event.getEntity();
		
		EffectInstance effect = ent.getActivePotionEffect(NostrumEffects.disruption);
		if (effect != null && effect.getDuration() > 0) {
			NostrumMagicaSounds.CAST_FAIL.play(ent.world, ent.getPosX(), ent.getPosY(), ent.getPosZ());
			event.setCanceled(true);
			
			if (effect.getAmplifier() > 0 && canDamage(ent)) {
				// Damage, too
				ent.attackEntityFrom(DamageSource.DROWN, effect.getAmplifier());
				markDamaged(ent);
			}
		}
	}
	
}
