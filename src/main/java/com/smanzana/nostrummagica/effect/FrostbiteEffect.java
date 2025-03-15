package com.smanzana.nostrummagica.effect;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FrostbiteEffect extends Effect {

	public static final String ID = "frostbite";
	protected static IParticleData SnowParticle = null; 
	
	public FrostbiteEffect() {
		super(EffectType.HARMFUL, 0xFF93E0FF);
		
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
			SnowParticle = new BlockParticleData(ParticleTypes.FALLING_DUST, Blocks.SNOW.defaultBlockState());
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
				if (entity instanceof PlayerEntity) {
					NostrumMagica.instance.proxy.sendMana((PlayerEntity) entity);
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
				cloud.setFixedColor(Integer.valueOf(PotionUtils.getColor(Lists.newArrayList(new EffectInstance(NostrumEffects.frostbite, 20 * 3, 2)))));
				cloud.setWaiting(true);
				cloud.setCustomParticle(SnowParticle);
				cloud.setCustomParticleYOffset(2f);
				cloud.setCustomParticleFrequency(.4f);
				//cloud.addEffect();
				final boolean hasHeal = attr != null && attr.hasSkill(NostrumSkills.Ice_Weapon);
				final boolean hasHealBoost = attr != null && attr.hasSkill(NostrumSkills.Ice_Master);
				final boolean hasHealShield = attr != null && attr.hasSkill(NostrumSkills.Ice_Adept);
				cloud.addEffect((World world, Entity ent) -> {
					if (ent != entity) {
						if (hasHeal && ent instanceof LivingEntity && NostrumMagica.IsSameTeam(entity, (LivingEntity) ent)) {
							((LivingEntity) ent).heal(hasHealBoost ? 2f : 1f);
							((LivingEntity) ent).removeEffectNoUpdate(NostrumEffects.frostbite);
							
							if (hasHealShield && NostrumMagica.rand.nextInt(8) == 0) {
								((LivingEntity) ent).addEffect(new EffectInstance(NostrumEffects.magicShield, (int)((20 * 15) * 1f), 0));
							}
							NostrumParticles.FILLED_ORB.spawn(ent.level, new SpawnParams(
									10, ent.getX(), ent.getY() + ent.getBbHeight()/2, ent.getZ(), 4,
									30, 10,
									ent.getId()
									).color(this.getColor()).dieOnTarget(true));
						} else if (ent instanceof LivingEntity) {
							((LivingEntity) ent).addEffect(new EffectInstance(NostrumEffects.frostbite, 20 * 3, 2));
						}
					}
				}
				);
				cloud.addEffect(new IAreaLocationEffect() {
					@Override
					public void apply(World world, BlockPos pos) {
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
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack matrixStackIn, int x, int y, float z) {
		PotionIcon.FROSTBITE.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.FROSTBITE.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
}
