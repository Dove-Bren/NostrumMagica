package com.smanzana.nostrummagica.effect;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.AreaEffectEntity;
import com.smanzana.nostrummagica.entity.AreaEffectEntity.IAreaLocationEffect;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellDamage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FrostbiteEffect extends MobEffect {

	public static final String ID = "frostbite";
	protected static ParticleOptions SnowParticle = null; 
	
	public FrostbiteEffect() {
		super(MobEffectCategory.HARMFUL, 0xFF93E0FF);
		
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
				"60A6EF27-8A11-2213-A734-30A4B0CC4E90", -0.1D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		final int interval = Math.max(1, (int) (20.0 * (2.0 / Math.pow(2, amp))));
		return (duration % interval == 0); // 2 seconds, 1 second, .5 seconds, ...
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
		if (SnowParticle == null) {
			SnowParticle = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SNOW.defaultBlockState());
		}
		
		// If entity has blizzard set, heal instead of harm
		final int blizzardCount = ElementalArmor.GetSetCount(entity, EMagicElement.ICE, ElementalArmor.Type.MASTER);
		if (blizzardCount == 4) {
			entity.heal(1);
			if (!entity.level.isClientSide) {
				final int interval = Math.max(1, (int) (20.0 * (2.0 / Math.pow(2, amp))));
				final INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
				final int manaPerSecond = 10;
				final int manaCost = interval / (20 / manaPerSecond);
				if (attr == null || attr.getMana() < (manaCost)) {
					return;
				}
				attr.addMana(-manaCost);
				if (entity instanceof Player) {
					NostrumMagica.instance.proxy.sendMana((Player) entity);
				}
				
				AreaEffectEntity cloud = new AreaEffectEntity(NostrumEntityTypes.areaEffect, entity.level, entity.getX(), entity.getY(), entity.getZ());
				cloud.setOwner(entity);
				cloud.setIgnoreOwner(true);
				cloud.setRadius(10f);
				cloud.setHeight(3f);
				cloud.setGravity(true, 1.0);
				cloud.setVerticleStepping(false);
				cloud.setDuration(0);
				cloud.setWaitTime(interval); // Turn off vanilla effects completely by putting all time in 'wait'
				cloud.setParticle(ParticleTypes.ENTITY_EFFECT);
				cloud.setFixedColor(Integer.valueOf(PotionUtils.getColor(Lists.newArrayList(new MobEffectInstance(NostrumEffects.frostbite, 20 * 3, 2)))));
				cloud.setWaiting(true);
				cloud.setCustomParticle(SnowParticle);
				cloud.setCustomParticleYOffset(2f);
				cloud.setCustomParticleFrequency(.4f);
				//cloud.addEffect();
				final boolean hasHeal = attr != null && attr.hasSkill(NostrumSkills.Ice_Weapon);
				final boolean hasHealBoost = attr != null && attr.hasSkill(NostrumSkills.Ice_Master);
				final boolean hasHealShield = attr != null && attr.hasSkill(NostrumSkills.Ice_Adept);
				cloud.addEffect((Level world, Entity ent) -> {
					if (ent != entity) {
						if (hasHeal && ent instanceof LivingEntity && NostrumMagica.IsSameTeam(entity, (LivingEntity) ent)) {
							((LivingEntity) ent).heal(hasHealBoost ? 2f : 1f);
							((LivingEntity) ent).removeEffectNoUpdate(NostrumEffects.frostbite);
							
							if (hasHealShield && NostrumMagica.rand.nextInt(8) == 0) {
								((LivingEntity) ent).addEffect(new MobEffectInstance(NostrumEffects.magicShield, (int)((20 * 15) * 1f), 0));
							}
							NostrumParticles.FILLED_ORB.spawn(ent.level, new SpawnParams(
									10, ent.getX(), ent.getY() + ent.getBbHeight()/2, ent.getZ(), 4,
									30, 10,
									ent.getId()
									).color(this.getColor()).dieWithTarget(true));
						} else if (ent instanceof LivingEntity) {
							((LivingEntity) ent).addEffect(new MobEffectInstance(NostrumEffects.frostbite, 20 * 3, 2));
						}
					}
				}
				);
				cloud.addEffect(new IAreaLocationEffect() {
					@Override
					public void apply(Level world, BlockPos pos) {
						if (world.isEmptyBlock(pos)) {
							BlockState belowState = world.getBlockState(pos.below());
							if (belowState.getMaterial().blocksMotion()) {
								world.setBlockAndUpdate(pos, NostrumBlocks.mysticSnowLayer.defaultBlockState());
							}
						}
					}
				});
				entity.level.addFreshEntity(cloud);
			}
		} else {
			float damage = 1.0f;
			SpellDamage.DamageEntity(entity, EMagicElement.ICE, damage, null);
		}
    }
}
