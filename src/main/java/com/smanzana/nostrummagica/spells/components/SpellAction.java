package com.smanzana.nostrummagica.spells.components;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SpellAction {

	private static interface SpellEffect {
		public void apply(EntityLivingBase caster, EntityLivingBase entity);
		public void apply(World world, BlockPos block);
	}
	
	private class DamageEffect implements SpellEffect {
		private float amount;
		private EMagicElement element;
		
		public DamageEffect(EMagicElement element, float amount) {
			this.amount = amount;
			this.element = element;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			float fin = calcDamage(entity, amount, element);
			entity.attackEntityFrom(DamageSource.causeMobDamage(source), fin);
		}
		
		@Override
		public void apply(World world, BlockPos pos) {
			; // Do nothing
		}
	}
	
	private class HealEffect implements SpellEffect {
		private float amount;
		
		public HealEffect(float amount) {
			this.amount = amount;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			entity.heal(amount);
		}
		
		@Override
		public void apply(World world, BlockPos pos) {
			; // Do nothing
		}
	}
	
	private class StatusEffect implements SpellEffect {
		private Potion effect;
		private int duration;
		private int amp;
		
		public StatusEffect(Potion effect, int duration, int amp) {
			this.effect = effect;
			this.duration = duration;
			this.amp = amp; 
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			entity.addPotionEffect(new PotionEffect(effect.getId(), duration, amp));
		}
		
		@Override
		public void apply(World world, BlockPos pos) {
			; // Do nothing
		}
	}
	
	private class DispelEffect implements SpellEffect {
		private int number; // -1 to clear all
		
		public DispelEffect(int number) {
			this.number = number; 
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			Collection<PotionEffect> effects = entity.getActivePotionEffects();
			Random rand = new Random();
			if (number == -1 || effects.size() < number)
				entity.clearActivePotions();
			else {
				int ids[] = new int[number];
				int cur = 0;
				while (cur < number) {
					int id = rand.nextInt(number);
					if (cur == 0) {
						ids[cur++] = id;
					} else {
						// Make sure we don't already have this ID
						boolean found = false;
						for (int i = 0; i < cur; i++) {
							if (ids[i] == id) {
								found = true;
								break;
							}
						}
						if (found)
							continue;
						
						ids[cur++] = id;
					}
						
				}
				
				Iterator<PotionEffect> it = effects.iterator();
				PotionEffect effect;
				int index = 0;
				int len = effects.size();
				while (index < len) {
					effect = it.next();
					for (int i = 0; i < number; i++) {
						// is ids[i] == index? If so, remove
						if (ids[i] == index) {
							entity.removePotionEffect(effect.getPotionID());
							break;
						}
					}
					
					index++;
				}
			}
		}
		
		@Override
		public void apply(World world, BlockPos pos) {
			; // Do nothing
		}
	}
	
	private class BlinkEffect implements SpellEffect {
		private float dist;
		
		public BlinkEffect(float dist) {
			this.dist = dist;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			Vec3 dest;
			Vec3 direction = entity.getLookVec().normalize();
			Vec3 source = entity.getPositionVector();
			BlockPos bpos;
			Vec3 translation = new Vec3(direction.xCoord * dist,
					direction.yCoord * dist,
					direction.zCoord * dist);
			
			// Find ideal dest (vect addition). Can we go there? Then go there.
			// Else step backwards and raycast forward in 1/5 increments.
			// See if place we hit is same spot as raycast. If so, fail and do again
			
			dest = source.add(translation);
			bpos = new BlockPos(dest.xCoord, dest.yCoord, dest.zCoord);
			if (entity.worldObj.isAirBlock(bpos)
					|| entity.worldObj.getBlockState(bpos).getBlock().getMaterial().isLiquid()) {
				// Whoo! Looks like we can teleport there!
			} else {
				int i = 4; // Attempt raytrace from (20% * i * pathlength)
				dest = null;
				Vec3 from;
				double curDist;
				while (i >= 0) {
					if (i == 0) {
						// optimization
						from = source;
					} else {
						curDist = dist * (.2 * i);
						from = new Vec3(translation.xCoord * curDist,
								translation.yCoord * curDist,
								translation.zCoord * curDist);
						from = source.add(from);
					}
					
					MovingObjectPosition mop = entity.worldObj.rayTraceBlocks(from, translation, false);
					if (mop != null && mop.hitVec.distanceTo(from) > 0.5) {
						// We got one
						dest = mop.hitVec;
						break;
					}
					
					i--;
				}
			}
			
			if (dest != null)
				entity.setPosition(dest.xCoord, dest.yCoord, dest.zCoord);
		}
		
		@Override
		public void apply(World world, BlockPos pos) {
			; // Do nothing
		}
	}
	
	private class PushEffect implements SpellEffect {
		private float range;
		private int amp; // - is pull
		
		public PushEffect(float range, int amp) {
			this.range = range;
			this.amp = amp; 
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			float magnitude = 2f * (float) amp;
			
			Vec3 center = entity.getPositionVector();
			for (Entity e : entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, 
					AxisAlignedBB.fromBounds(entity.posX - range, entity.posY - range, entity.posZ - range, entity.posX + range, entity.posY + range, entity.posZ + range)
					)) {
				double dist = e.getPositionVector().distanceTo(center); 
				if (dist <= range) {
					
					// If push, straight magnitude
					// If pull, cap magnitude so that it doesn't fly past player
					
					Vec3 force;
					Vec3 direction = center.subtract(e.getPositionVector()).normalize();
					force = new Vec3(
							direction.xCoord * magnitude,
							direction.yCoord * magnitude,
							direction.zCoord * magnitude
							);
					if (amp < 0) {
						// pull
						// Cap force's magnitude at .2 dist
						double mod = force.lengthVector();
						if (mod > dist * .2) {
							mod = mod / (dist * .2);
							force = new Vec3(
									force.xCoord * mod,
									force.yCoord * mod,
									force.zCoord * mod
									);
						}
					}
					
					e.addVelocity(force.xCoord, force.yCoord, force.zCoord);
				}
			}
			
		}
		
		@Override
		public void apply(World world, BlockPos pos) {
			; // Do nothing
		}
	}
	
	private static class TransmuteEffect implements SpellEffect {
		
		private static final Set<Item> items = Sets.newHashSet(
				Items.beef,
				Items.apple,
				Items.potato,
				Items.iron_helmet,
				Items.ender_pearl,
				Items.carrot,
				Items.bread,
				Items.compass,
				Items.brick,
				Items.bone,
				Items.emerald,
				Items.coal,
				Items.egg,
				Items.gold_ingot,
				Items.redstone,
				Items.book,
				Items.quartz,
				Items.chainmail_chestplate,
				Items.nether_wart,
				Items.iron_ingot,
				Items.diamond_axe,
				Items.diamond_pickaxe,
				Items.melon_seeds,
				Items.reeds,
				Items.prismarine_crystals,
				Items.brewing_stand,
				Items.ender_eye,
				Items.diamond,
				Items.chainmail_boots,
				Items.wooden_sword,
				Items.glowstone_dust,
				Items.clay_ball,
				Items.clock,
				Items.comparator,
				Items.cookie,
				Items.experience_bottle,
				Items.feather,
				Items.spider_eye,
				Items.string);
		
		private static final Set<Block> blocks = Sets.newHashSet(
				Blocks.bookshelf,
				Blocks.cactus,
				Blocks.coal_ore,
				Blocks.end_stone,
				Blocks.dirt,
				Blocks.ice,
				Blocks.noteblock,
				Blocks.netherrack,
				Blocks.sand,
				Blocks.iron_bars,
				Blocks.dropper,
				Blocks.mossy_cobblestone,
				Blocks.stone,
				Blocks.netherrack,
				Blocks.log,
				Blocks.pumpkin,
				Blocks.quartz_ore,
				Blocks.planks,
				Blocks.quartz_stairs,
				Blocks.oak_fence,
				Blocks.redstone_ore,
				Blocks.lapis_ore,
				Blocks.acacia_fence,
				Blocks.crafting_table,
				Blocks.gold_ore,
				Blocks.gravel,
				Blocks.hardened_clay,
				Blocks.iron_ore
				);
		
		private int level;
		
		public TransmuteEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			Item item = entity.getHeldItem().getItem();
			
			if (item == null)
				return;
			
			if (!items.contains(item))
				return;
			
			Iterator<Item> it = items.iterator();
			Item next = it.next();
			while (next != item)
				next = it.next();
			
			// Now calculate offset
			int hop = 4 - (level > 3 ? 3 : level);
			for (int i = 0; i < hop; i++) {
				next = it.next();
				if (!it.hasNext())
					it = items.iterator();
			}
			
			ItemStack stack = new ItemStack(next, 1);
			if (entity instanceof EntityPlayer) {
				entity.getHeldItem().splitStack(1);
				((EntityPlayer) entity).inventory.addItemStackToInventory(stack);
			} else {
				// EntityLiving has held item in slot 0
				entity.setCurrentItemOrArmor(0, stack);
			}
		}
		
		@Override
		public void apply(World world, BlockPos pos) {
			Block block = world.getBlockState(pos).getBlock();
			if (!blocks.contains(block))
				return;
			
			Iterator<Block> it = blocks.iterator();
			Block next = it.next();
			while (next != block)
				next = it.next();
			
			// Now calculate offset
			int hop = 4 - (level > 3 ? 3 : level);
			for (int i = 0; i < hop; i++) {
				next = it.next();
				if (!it.hasNext())
					it = blocks.iterator();
			}
			
			world.setBlockState(pos, next.getDefaultState());
		}
	}
	
	private static class BurnEffect implements SpellEffect {

		private int duration;
		
		public BurnEffect(int duration) {
			this.duration = duration;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			entity.setFire((int) Math.ceil((float) duration / 20.0f));
		}

		@Override
		public void apply(World world, BlockPos block) {
			block.add(0, 1, 0);
			if (world.isAirBlock(block)) {
				world.setBlockState(block, Blocks.fire.getDefaultState());
			}
		}
		
	}
	
	private EntityLivingBase source;
	private List<SpellEffect> effects;
	
	public SpellAction(EntityLivingBase source) {
		this.source = source;
		effects = new LinkedList<>();
	}
	
	public void apply(EntityLivingBase entity) {
		for (SpellEffect e : effects) {
			e.apply(source, entity);
		}
	}
	
	public void apply(World world, BlockPos pos) {
		for (SpellEffect e : effects) {
			e.apply(world, pos);
		}
	}
	
	public static final float calcDamage(EntityLivingBase target, float base, EMagicElement element) {
		float amt = 0f;
		
		if (target == null)
			return amt;
		
		if (element == EMagicElement.PHYSICAL)
			return applyArmor(target, base);
		
		int armor = target.getTotalArmorValue();
		boolean undead = target.isEntityUndead();
		boolean ender = false;
		if (target instanceof EntityEnderman || target instanceof EntityEndermite
				|| target instanceof EntityDragon)
			ender = true;
		boolean light = false;
		if (target.height < 1.5f || target instanceof EntityEnderman)
			light = true;
			
		switch (element) {
		case ENDER:
			if (ender) return 0.0f;
			return base;
		case LIGHTNING:
			return base * (1f + (float)(armor / 20)); // double in power for every 20 armor
		case FIRE:
			return base * (undead ? 1.5f : 1f);
		case EARTH:
			return base;
		case ICE:
			return base * (undead ? 1.75f : .8f);
		case WIND:
			return base * (light ? 1.8f : .8f);
		default:
			return base;
		}
	}
	
	public static final float applyArmor(EntityLivingBase target, float damage) {
            int i = 25 - target.getTotalArmorValue();
            float f = damage * (float)i;
            return f / 25.0F;
	}
	
	public SpellAction damage(EMagicElement element, float amount) {
		effects.add(new DamageEffect(element, amount));
		return this;
	}
	
	public SpellAction heal(float amount) {
		effects.add(new HealEffect(amount));
		return this;
	}
	
	public SpellAction status(Potion effect, int duration, int amplitude) {
		effects.add(new StatusEffect(effect, duration, amplitude));
		return this;
	}
	
	public SpellAction dispel(int number) {
		effects.add(new DispelEffect(number));
		return this;
	}
	
	public SpellAction blink(float maxDistance) {
		effects.add(new BlinkEffect(maxDistance));
		return this;
	}
	
	public SpellAction push(float radius, int level) {
		effects.add(new PushEffect(radius, level));
		return this;
	}
	
	public SpellAction pull(float radius, int level) {
		effects.add(new PushEffect(radius, level));
		return this;
	}
	
	public SpellAction transmute(int level) {
		effects.add(new TransmuteEffect(level));
		return this;
	}
	
	public SpellAction burn(int durationTicks) {
		effects.add(new BurnEffect(durationTicks));
		return this;
	}
}
