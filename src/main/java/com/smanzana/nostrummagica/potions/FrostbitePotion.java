package com.smanzana.nostrummagica.potions;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.entity.EntityAreaEffect;
import com.smanzana.nostrummagica.entity.EntityAreaEffect.IAreaEntityEffect;
import com.smanzana.nostrummagica.entity.EntityAreaEffect.IAreaLocationEffect;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FrostbitePotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-frostbite");
	
	private static FrostbitePotion instance;
	public static FrostbitePotion instance() {
		if (instance == null)
			instance = new FrostbitePotion();
		
		return instance;
	}
	
	private FrostbitePotion() {
		super(true, 0xFF93E0FF);
		
		this.setPotionName("potion.frostbite.name");
		
		this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
				"60A6EF27-8A11-2213-A734-30A4B0CC4E90", -0.1D, 2);
		
		NostrumMagica.registerPotion(this, Resource);
	}
	
	public boolean isReady(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		final int interval = Math.max(1, (int) (20.0 * (2.0 / Math.pow(2, amp))));
		return (duration % interval == 0); // 2 seconds, 1 second, .5 seconds, ...
	}

	@Override
	public void performEffect(EntityLivingBase entity, int amp) {
		// If entity has blizzard set, heal instead of harm
		final int blizzardCount = EnchantedArmor.GetSetCount(entity, EMagicElement.ICE, 3);
		if (blizzardCount == 4) {
			entity.heal(1);
			if (!entity.worldObj.isRemote) {
				final int interval = Math.max(1, (int) (20.0 * (2.0 / Math.pow(2, amp))));
				final INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
				final int manaPerSecond = 10;
				final int manaCost = interval / (20 / manaPerSecond);
				if (attr == null || attr.getMana() < (manaCost)) {
					return;
				}
				attr.addMana(-manaCost);
				if (entity instanceof EntityPlayer) {
					NostrumMagica.proxy.sendMana((EntityPlayer) entity);
				}
				
				EntityAreaEffect cloud = new EntityAreaEffect(entity.worldObj, entity.posX, entity.posY, entity.posZ);
				cloud.setOwner(entity);
				cloud.setIgnoreOwner(true);
				cloud.setRadius(10f);
				cloud.setGravity(true, 1.0);
				cloud.setVerticleStepping(false);
				cloud.setDuration(0);
				cloud.setWaitTime(interval); // Turn off vanilla effects completely by putting all time in 'wait'
				final PotionEffect effect = new PotionEffect(FrostbitePotion.instance(), 20 * 3, 2);
				cloud.setParticle(EnumParticleTypes.SPELL_MOB);
				cloud.setColor(Integer.valueOf(PotionUtils.getPotionColorFromEffectList(Lists.newArrayList(new PotionEffect(FrostbitePotion.instance(), 20 * 3, 2)))));
				cloud.setIgnoreRadius(true);
				cloud.setCustomParticle(EnumParticleTypes.FALLING_DUST);
				cloud.setCustomParticleParam1(Block.getStateId(Blocks.SNOW.getDefaultState()));
				cloud.setCustomParticleYOffset(2f);
				cloud.setCustomParticleFrequency(.4f);
				//cloud.addEffect();
				cloud.addEffect(new IAreaEntityEffect() {
					@Override
					public void apply(World world, Entity ent) {
						if (effect.doesShowParticles()) {
							effect.getEffectName();
						}
						if (ent instanceof EntityLivingBase) {
							((EntityLivingBase) ent).addPotionEffect(new PotionEffect(FrostbitePotion.instance(), 20 * 3, 2));
						}
					}
				});
				cloud.addEffect(new IAreaLocationEffect() {
					@Override
					public void apply(World world, BlockPos pos) {
						if (world.isAirBlock(pos)) {
							IBlockState belowState = world.getBlockState(pos.down());
							if (belowState.getMaterial().blocksMovement()) {
								world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState());
							}
						}
					}
				});
				entity.worldObj.spawnEntityInWorld(cloud);
			}
		} else {
			float damage = 1.0f;
	        entity.attackEntityFrom(DamageSource.magic, damage);
		}
    }
	
	@SideOnly(Side.CLIENT)
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		PotionIcon.FROSTBITE.draw(mc, x + 6, y + 7);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		PotionIcon.FROSTBITE.draw(mc, x + 3, y + 3);
	}
	
}
