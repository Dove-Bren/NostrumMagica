package com.smanzana.nostrummagica.effect;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.armor.MagicArmor;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.MagicDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
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
	
	protected static void onSublimationDamage(LivingEntity target) {
		// Look for nearby spread-enablers
		boolean spread = false;
		Entity spreadingEnt = null;
		List<Entity> nearbyEnts = target.getEntityWorld().getEntitiesInAABBexcluding(target, target.getEntity().getBoundingBox().grow(10), e -> true);
		for (Entity ent : nearbyEnts) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(ent);
			if (attr != null && attr.hasSkill(NostrumSkills.Fire_Corrupt) && NostrumMagica.rand.nextBoolean()) {
				spread = true;
				spreadingEnt = ent;
				break;
			}
		}
		
		// Spread
		if (spread) {
			final EffectInstance effect = target.getActivePotionEffect(NostrumEffects.sublimation);
			for (Entity ent : nearbyEnts) {
				if (!(ent instanceof LivingEntity)) {
					continue;
				}
				
				if (ent == spreadingEnt || (spreadingEnt instanceof LivingEntity && NostrumMagica.IsSameTeam((LivingEntity) spreadingEnt, (LivingEntity) ent))) {
					continue;
				}
				
				if (ent == target) {
					continue;
				}
				
				((LivingEntity) ent).addPotionEffect(new EffectInstance(NostrumEffects.sublimation, effect.getDuration(), effect.getAmplifier()));
				
				NostrumParticles.FILLED_ORB.spawn(target.world, new SpawnParams(
						10, target.getPosX(), target.getPosY() + target.getHeight()/2, target.getPosZ(), 0,
						40, 10,
						ent.getEntityId()
						).color(0xFFEC6D8E).dieOnTarget(true));
			}
			
			NostrumParticles.FILLED_ORB.spawn(target.world, new SpawnParams(
					50, target.getPosX(), target.getPosY() + target.getHeight()/2, target.getPosZ(), 0,
					30, 10,
					new Vector3d(0, .1, 0), new Vector3d(.2, .05, .2)
					).color(0xFFEC6D8E).gravity(true));
			NostrumMagicaSounds.DAMAGE_FIRE.play(target);
		}
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
			onSublimationDamage(entity);
			
			recurseCheck = false;
		}
	}
	
	protected static void handleRainTick(LivingEntity entity, int amp) {
		//NostrumMagica.logger.debug("1.0 - doing rain tick");
		entity.attackEntityFrom(DamageSource.DROWN, 1f);
		onSublimationDamage(entity);
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
