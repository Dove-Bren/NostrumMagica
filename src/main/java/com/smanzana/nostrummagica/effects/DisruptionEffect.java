package com.smanzana.nostrummagica.effects;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class DisruptionEffect extends Effect {

	public static final String ID = "disruption";
	
	public DisruptionEffect() {
		super(EffectType.HARMFUL, 0xFF916F82);
	}
	
	public boolean isReady(int duration, int amp) {
		return false; // Never ticks
	}

	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entity, AbstractAttributeMap attributeMap, int amplifier) {
		super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@SubscribeEvent
	public static void onTeleport(EnderTeleportEvent event) {
		final LivingEntity ent = event.getEntityLiving();
		
		if (ent.world.isRemote()) {
			return;
		}
		
		EffectInstance effect = ent.getActivePotionEffect(NostrumEffects.disruption);
		if (effect != null && effect.getDuration() > 0) {
			NostrumMagicaSounds.CAST_FAIL.play(ent.world, ent.posX, ent.posY, ent.posZ);
			event.setCanceled(true);
			
			if (effect.getAmplifier() > 0) {
				// Damage, too
				ent.attackEntityFrom(DamageSource.DROWN, effect.getAmplifier());
			}
		}
	}
	
}
