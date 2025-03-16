package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class LootLuckEffect extends MobEffect {

	public static final String ID = "loot_luck";
	
	public LootLuckEffect() {
		super(MobEffectCategory.HARMFUL, 0xFF9F6B76); 
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick effects
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onDropsEvent(LootingLevelEvent event) {
		if (!event.isCanceled()) {
			LivingEntity ent = event.getEntityLiving();
			MobEffectInstance effect = ent.getEffect(NostrumEffects.lootLuck);
			
			if (effect != null && effect.getDuration() > 0) {
				final int bonus = effect.getAmplifier() + 1;
				event.setLootingLevel(event.getLootingLevel() + bonus);
				
				LivingEntity target = event.getEntityLiving();
				((ServerLevel) target.level).sendParticles(ParticleTypes.HAPPY_VILLAGER,
						target.getX(),
						target.getY(),	
						target.getZ(),
						10,
						.2,
						.25,
						.2,
						.1
						);
			}
		}
	}
}
