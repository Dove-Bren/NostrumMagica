package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams.TargetBehavior;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class MysticAirEffect extends Effect {

	public static final String ID = "mystic_air";
	
	public MysticAirEffect() {
		super(EffectType.BENEFICIAL, 0xFFD5CFDF);
	}
	
	public boolean isReady(int duration, int amp) {
		return duration % 20 == 0;
	}
	
	protected void reduceAmp(LivingEntity entity, int newAmplifier) {
		EffectInstance instance = entity.getActivePotionEffect(this);
		entity.removePotionEffect(this);
		
		if (newAmplifier >= 0) {
			entity.addPotionEffect(new EffectInstance(this, instance.getDuration(), newAmplifier));
		}
	}
	
	@Override
	public void performEffect(LivingEntity entity, int amplifier) {
		if (entity.world.isRemote()) {
			return;
		}
		
		// Amplifier is how many time we can apply. We only apply when air is at 25% and restore up to 50%.
		if (entity.getAir() <= entity.getMaxAir()/4) {
			entity.setAir(entity.getMaxAir() / 2);
			reduceAmp(entity, (amplifier) - 1);
			NostrumParticles.FILLED_ORB.spawn(entity.world, new SpawnParams(
					5, entity.getPosX(), entity.getPosY() + .75, entity.getPosZ(), 1,
					40, 0,
					entity.getEntityId()
					).color(EMagicElement.WIND.getColor()).setTargetBehavior(TargetBehavior.ORBIT));
			entity.world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), SoundEvents.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS, 1f, 1f);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityFall(LivingFallEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		if (event.getEntity().world.isRemote()) {
			return;
		}
		
		if (event.getDistance() > 3) { // 3 is magic number pulled from LivingEntity which lives in a protected method
			EffectInstance effect = event.getEntityLiving().getActivePotionEffect(NostrumEffects.mysticAir);
			if (effect != null && effect.getDuration() > 0) {
				final LivingEntity living = event.getEntityLiving();
				// Instantly extinguish and cancel in exchange for some effect
				event.setCanceled(true);
				((MysticAirEffect) effect.getPotion()).reduceAmp(living, effect.getAmplifier() - 1);
				//event.getEntityLiving().world.playSound(null, living.getPosX(), living.getPosY(), living.getPosZ(), SoundEvents.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS, 1f, 1f);
				NostrumMagicaSounds.WING_FLAP.play(living);
				((ServerWorld) living.world).spawnParticle(ParticleTypes.POOF, living.getPosX(), living.getPosY(), living.getPosZ(), 10, 0, 0, 0, .05);
				
			}
		}
		
	}
}
