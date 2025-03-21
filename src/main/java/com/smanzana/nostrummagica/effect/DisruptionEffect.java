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
import com.smanzana.nostrummagica.spell.SpellDamage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class DisruptionEffect extends MobEffect {

	public static final String ID = "disruption";
	
	private static final Map<LivingEntity, Integer> lastDamage = new HashMap<>();
	
	public DisruptionEffect() {
		super(MobEffectCategory.HARMFUL, 0xFF916F82);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // Never ticks
	}

	@Override
	public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
		super.addAttributeModifiers(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeMap attributeMapIn, int amplifier) {
		super.removeAttributeModifiers(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	protected static boolean canDamage(LivingEntity entity) {
		Integer lastTicks = lastDamage.get(entity);
		return lastTicks == null || entity.tickCount - lastTicks >= 1;
	}
	
	protected static void markDamaged(LivingEntity entity) {
		lastDamage.put(entity, entity.tickCount);
	}
	
	@SubscribeEvent
	public static void onTeleport(EntityTeleportEvent event) {
		if (event.isCanceled()) {
			return;
		}
		if (!(event.getEntity() instanceof LivingEntity)) {
			return;
		}
		if (event.getEntity().level.isClientSide()) {
			return;
		}
		if (event.getEntity() instanceof Player && ((Player) event.getEntity()).isCreative()) {
			return;
		}
		final LivingEntity living = (LivingEntity) event.getEntity();
		
		final @Nullable LivingEntity cause;
		if (event instanceof NostrumTeleportEvent) {
			cause = ((NostrumTeleportEvent) event).getCausingEntity();
		} else {
			cause = null;
		}
		
		MobEffectInstance effect = living.getEffect(NostrumEffects.disruption);
		if (effect != null && effect.getDuration() > 0) {
			NostrumMagicaSounds.CAST_FAIL.play(living.level, living.getX(), living.getY(), living.getZ());
			boolean damaged = false;
			
			@Nullable INostrumMagic causeAttr = cause == null ? null : NostrumMagica.getMagicWrapper(cause);
			if (causeAttr != null && causeAttr.hasSkill(NostrumSkills.Ender_Corrupt)) {
				// Do additional damage and don't cancel
				living.invulnerableTime = 0;
				SpellDamage.DamageEntity(living, EMagicElement.ENDER, 4f + effect.getAmplifier(), cause);
				damaged = true;
			} else {
				event.setCanceled(true);
			}
			
			if (effect.getAmplifier() > 0 && canDamage(living)) {
				// Damage, too
				living.hurt(DamageSource.DROWN, effect.getAmplifier());
				damaged = true;
			}
			
			if (damaged) {
				markDamaged(living);
			}
		}
	}
	
}
