package com.smanzana.nostrummagica.effects;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class LootLuckEffect extends Effect {

	public static final String ID = "loot_luck";
	
	public LootLuckEffect() {
		super(EffectType.HARMFUL, 0xFF9F6B76); 
	}
	
	public boolean isReady(int duration, int amp) {
		return false; // No tick effects
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onDropsEvent(LootingLevelEvent event) {
		if (!event.isCanceled()) {
			LivingEntity ent = event.getEntityLiving();
			EffectInstance effect = ent.getActivePotionEffect(NostrumEffects.lootLuck);
			
			if (effect != null && effect.getDuration() > 0) {
				final int bonus = effect.getAmplifier() + 1;
				event.setLootingLevel(event.getLootingLevel() + bonus);
				
				LivingEntity target = event.getEntityLiving();
				((ServerWorld) target.world).spawnParticle(ParticleTypes.HAPPY_VILLAGER,
						target.posX,
						target.posY,	
						target.posZ,
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
