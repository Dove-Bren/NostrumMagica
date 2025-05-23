package com.smanzana.nostrummagica.effect;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.MagicDamageSource;
import com.smanzana.nostrummagica.spell.SpellDamage;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class SublimationEffect extends MobEffect {

	public static final String ID = "sublimation";
	
	public SublimationEffect() {
		super(MobEffectCategory.HARMFUL, 0xFFEC6D8E);
	}
	
	@Override
	public boolean isDurationEffectTick(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		// 2, 1, ...
		final int interval = Math.max(1, (int) (20.0 * (2.0 / Math.pow(2, amp))));
		return (duration % interval == 0); // 2 second, 1 seconds, ...
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
		if (!entity.level.isClientSide() && entity.isInWaterRainOrBubble()) {
			handleRainTick(entity, amp);
		}
		
		// Maybe if fire resistance is present, make sure we're doing some damage?
	}
	
	protected static void onSublimationDamage(LivingEntity target) {
		// Look for nearby spread-enablers
		boolean spread = false;
		Entity spreadingEnt = null;
		List<Entity> nearbyEnts = target.getCommandSenderWorld().getEntities(target, target.getBoundingBox().inflate(10), e -> true);
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
			final MobEffectInstance effect = target.getEffect(NostrumEffects.sublimation);
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
				
				((LivingEntity) ent).addEffect(new MobEffectInstance(NostrumEffects.sublimation, effect.getDuration(), effect.getAmplifier()));
				
				NostrumParticles.FILLED_ORB.spawn(target.level, new SpawnParams(
						10, target.getX(), target.getY() + target.getBbHeight()/2, target.getZ(), 0,
						40, 10,
						new TargetLocation(ent, true)
						).color(0xFFEC6D8E).setTargetBehavior(new ParticleTargetBehavior().orbitMode(true).dieWithTarget()));
			}
			
			NostrumParticles.FILLED_ORB.spawn(target.level, new SpawnParams(
					50, target.getX(), target.getY() + target.getBbHeight()/2, target.getZ(), 0,
					30, 10,
					new Vec3(0, .1, 0), new Vec3(.2, .05, .2)
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
			entity.invulnerableTime = 0;
			SpellDamage.DamageEntity(entity, EMagicElement.FIRE, addDmg, null);
			onSublimationDamage(entity);
			
			recurseCheck = false;
		}
	}
	
	protected static void handleRainTick(LivingEntity entity, int amp) {
		//NostrumMagica.logger.debug("1.0 - doing rain tick");
		entity.hurt(DamageSource.DROWN, 1f);
		onSublimationDamage(entity);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onEntityAttacked(LivingAttackEvent event) {
		final boolean isFire;
		final DamageSource source = event.getSource();
		final LivingEntity entity = event.getEntityLiving();
		
		if (entity == null || entity.level.isClientSide()) {
			return;
		}
		
		if (source.isFire()) {
			// To avoid bypassing hurt cooldowns from fire damage, only care if the regular fire
			// damage would apply
			isFire = entity.invulnerableTime <= 10;
		} else if (source instanceof MagicDamageSource) {
			isFire = ((MagicDamageSource) source).getElement() == EMagicElement.FIRE;
		} else {
			isFire = false;
		}
		
		if (isFire) {
			MobEffectInstance effect = entity.getEffect(NostrumEffects.sublimation);
			if (effect != null) {
				int amp = effect.getAmplifier();
				handleFireAttack(entity, source, event.getAmount(), amp);
				
				// If we suspect regular fire damage is happening next, set resistant time to 0 so it still happens.
				// Otherwise, don't reset
				final boolean lavaSet = ElementalArmor.GetSetCount(entity, EMagicElement.FIRE, ElementalArmor.Type.MASTER) == 4;
				if (!entity.fireImmune()
						&& entity.getEffect(MobEffects.FIRE_RESISTANCE) == null
						&& !lavaSet) {
					entity.invulnerableTime = 0;
				}
			}
		}
	}
	
}
