package com.smanzana.nostrummagica.effects;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.entity.EntityAreaEffect;
import com.smanzana.nostrummagica.entity.EntityAreaEffect.IAreaEntityEffect;
import com.smanzana.nostrummagica.entity.EntityAreaEffect.IAreaLocationEffect;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FrostbiteEffect extends Effect {

	public static final String ID = "potions-frostbite";
	protected static IParticleData SnowParticle = null; 
	
	public FrostbiteEffect() {
		super(EffectType.HARMFUL, 0xFF93E0FF);
		
		this.addAttributesModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
				"60A6EF27-8A11-2213-A734-30A4B0CC4E90", -0.1D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
	
	public boolean isReady(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		final int interval = Math.max(1, (int) (20.0 * (2.0 / Math.pow(2, amp))));
		return (duration % interval == 0); // 2 seconds, 1 second, .5 seconds, ...
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
		if (SnowParticle == null) {
			SnowParticle = new BlockParticleData(ParticleTypes.FALLING_DUST, Blocks.SNOW.getDefaultState());
		}
		
		// If entity has blizzard set, heal instead of harm
		final int blizzardCount = EnchantedArmor.GetSetCount(entity, EMagicElement.ICE, EnchantedArmor.Type.TRUE);
		if (blizzardCount == 4) {
			entity.heal(1);
			if (!entity.world.isRemote) {
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
				
				EntityAreaEffect cloud = new EntityAreaEffect(NostrumEntityTypes.areaEffect, entity.world, entity.posX, entity.posY, entity.posZ);
				cloud.setOwner(entity);
				cloud.setIgnoreOwner(true);
				cloud.setRadius(10f);
				cloud.setGravity(true, 1.0);
				cloud.setVerticleStepping(false);
				cloud.setDuration(0);
				cloud.setWaitTime(interval); // Turn off vanilla effects completely by putting all time in 'wait'
				final EffectInstance effect = new EffectInstance(NostrumEffects.frostbite, 20 * 3, 2);
				cloud.setParticleData(ParticleTypes.ENTITY_EFFECT);
				cloud.setColor(Integer.valueOf(PotionUtils.getPotionColorFromEffectList(Lists.newArrayList(new EffectInstance(NostrumEffects.frostbite, 20 * 3, 2)))));
				cloud.setIgnoreRadius(true);
				cloud.setCustomParticle(SnowParticle);
				cloud.setCustomParticleYOffset(2f);
				cloud.setCustomParticleFrequency(.4f);
				//cloud.addEffect();
				cloud.addEffect(new IAreaEntityEffect() {
					@Override
					public void apply(World world, Entity ent) {
						if (effect.doesShowParticles()) {
							effect.getEffectName();
						}
						if (ent instanceof LivingEntity) {
							((LivingEntity) ent).addPotionEffect(new EffectInstance(NostrumEffects.frostbite, 20 * 3, 2));
						}
					}
				});
				cloud.addEffect(new IAreaLocationEffect() {
					@Override
					public void apply(World world, BlockPos pos) {
						if (world.isAirBlock(pos)) {
							BlockState belowState = world.getBlockState(pos.down());
							if (belowState.getMaterial().blocksMovement()) {
								world.setBlockState(pos, Blocks.SNOW.getDefaultState());
							}
						}
					}
				});
				entity.world.addEntity(cloud);
			}
		} else {
			float damage = 1.0f;
	        entity.attackEntityFrom(DamageSource.MAGIC, damage);
		}
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, int x, int y, float z) {
		PotionIcon.FROSTBITE.draw(gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, int x, int y, float z, float alpha) {
		PotionIcon.FROSTBITE.draw(Minecraft.getInstance(), x + 3, y + 3);
	}
}
