package com.smanzana.nostrummagica.effects;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.MagicArmor;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.MagicDamageSource;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class SublimationEffect extends Effect {

	public static final String ID = "sublimation";
	
	public SublimationEffect() {
		super(EffectType.HARMFUL, 0xFFEC6D8E);
	}
	
	@Override
	public boolean isReady(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		// 2, 1, ...
		final int interval = Math.max(1, (int) (20.0 * (2.0 / Math.pow(2, amp))));
		return (duration % interval == 0); // 2 second, 1 seconds, ...
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
		if (!entity.world.isRemote() && entity.isInWaterRainOrBubbleColumn()) {
			handleRainTick(entity, amp);
		}
		
		// Maybe if fire resistance is present, make sure we're doing some damage?
	}
	
	private static boolean recurseCheck = false;
	protected static void handleFireAttack(LivingEntity entity, DamageSource source, float amt, int amp) {
		//NostrumMagica.logger.debug(amt + " - got fire damage");
		if (!recurseCheck) {
			recurseCheck = true;

			final float perc = .25f * (float) Math.pow(2, amp); // .25%, 50%, 100%, 200%
			final float addDmg = Math.max(.25f, amt * perc);
			//NostrumMagica.logger.debug(addDmg + " - doing fire bonus");
			entity.hurtResistantTime = 0;
			entity.attackEntityFrom(DamageSource.MAGIC, addDmg);
			
			recurseCheck = false;
		}
	}
	
	protected static void handleRainTick(LivingEntity entity, int amp) {
		//NostrumMagica.logger.debug("1.0 - doing rain tick");
		entity.attackEntityFrom(DamageSource.DROWN, 1f);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onEntityAttacked(LivingAttackEvent event) {
		final boolean isFire;
		final DamageSource source = event.getSource();
		final LivingEntity entity = event.getEntityLiving();
		
		if (entity == null || entity.world.isRemote()) {
			return;
		}
		
		if (source.isFireDamage()) {
			// To avoid bypassing hurt cooldowns from fire damage, only care if the regular fire
			// damage would apply
			isFire = entity.hurtResistantTime <= 10;
		} else if (source instanceof MagicDamageSource) {
			isFire = ((MagicDamageSource) source).getElement() == EMagicElement.FIRE;
		} else {
			isFire = false;
		}
		
		if (isFire) {
			EffectInstance effect = entity.getActivePotionEffect(NostrumEffects.sublimation);
			if (effect != null) {
				int amp = effect.getAmplifier();
				handleFireAttack(entity, source, event.getAmount(), amp);
				
				// If we suspect regular fire damage is happening next, set resistant time to 0 so it still happens.
				// Otherwise, don't reset
				final boolean lavaSet = MagicArmor.GetSetCount(entity, EMagicElement.FIRE, MagicArmor.Type.MASTER) == 4;
				final boolean trueSet = MagicArmor.GetSetCount(entity, EMagicElement.FIRE, MagicArmor.Type.TRUE) == 4;
				if (!entity.isImmuneToFire()
						&& entity.getActivePotionEffect(Effects.FIRE_RESISTANCE) == null
						&& !lavaSet && !trueSet) {
					entity.hurtResistantTime = 0;
				}
			}
		}
	}
	
}
