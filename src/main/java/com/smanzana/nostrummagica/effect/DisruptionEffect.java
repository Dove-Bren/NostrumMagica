package com.smanzana.nostrummagica.effect;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.NostrumMagica.NostrumTeleportEvent;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.MagicDamageSource;

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
		final LivingEntity living = (LivingEntity) event.getEntity();
		
		final @Nullable LivingEntity cause;
		if (event instanceof NostrumTeleportEvent) {
			cause = ((NostrumTeleportEvent) event).getCausingEntity();
		} else {
			cause = null;
		}
		
		EffectInstance effect = living.getActivePotionEffect(NostrumEffects.disruption);
		if (effect != null && effect.getDuration() > 0) {
			NostrumMagicaSounds.CAST_FAIL.play(living.world, living.getPosX(), living.getPosY(), living.getPosZ());
			boolean damaged = false;
			
			@Nullable INostrumMagic causeAttr = cause == null ? null : NostrumMagica.getMagicWrapper(cause);
			if (causeAttr != null && causeAttr.hasSkill(NostrumSkills.Ender_Corrupt)) {
				// Do additional damage and don't cancel
				living.attackEntityFrom(new MagicDamageSource(cause, EMagicElement.ENDER), 4f + effect.getAmplifier());
				damaged = true;
			} else {
				event.setCanceled(true);
			}
			
			if (effect.getAmplifier() > 0 && canDamage(living)) {
				// Damage, too
				living.attackEntityFrom(DamageSource.DROWN, effect.getAmplifier());
				damaged = true;
			}
			
			if (damaged) {
				markDamaged(living);
			}
		}
	}
	
}
