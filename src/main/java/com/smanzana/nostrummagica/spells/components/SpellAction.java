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
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
			entity.addPotionEffect(new PotionEffect(effect, duration, amp));
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
							entity.removePotionEffect(effect.getPotion());
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
			Vec3d dest;
			Vec3d direction = entity.getLookVec().normalize();
			Vec3d source = entity.getPositionVector();
			BlockPos bpos;
			Vec3d translation = new Vec3d(direction.xCoord * dist,
					direction.yCoord * dist,
					direction.zCoord * dist);
			
			// Find ideal dest (vect addition). Can we go there? Then go there.
			// Else step backwards and raycast forward in 1/5 increments.
			// See if place we hit is same spot as raycast. If so, fail and do again
			
			dest = source.add(translation);
			bpos = new BlockPos(dest.xCoord, dest.yCoord, dest.zCoord);
			if (entity.worldObj.isAirBlock(bpos)
					|| entity.worldObj.getBlockState(bpos).getMaterial().isLiquid()) {
				// Whoo! Looks like we can teleport there!
			} else {
				int i = 4; // Attempt raytrace from (20% * i * pathlength)
				dest = null;
				Vec3d from;
				double curDist;
				while (i >= 0) {
					if (i == 0) {
						// optimization
						from = source;
					} else {
						curDist = dist * (.2 * i);
						from = new Vec3d(translation.xCoord * curDist,
								translation.yCoord * curDist,
								translation.zCoord * curDist);
						from = source.add(from);
					}
					
					RayTraceResult mop = entity.worldObj.rayTraceBlocks(from, translation, false);
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
			
			Vec3d center = entity.getPositionVector();
			for (Entity e : entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, 
					new AxisAlignedBB(entity.posX - range, entity.posY - range, entity.posZ - range, entity.posX + range, entity.posY + range, entity.posZ + range)
					)) {
				double dist = e.getPositionVector().distanceTo(center); 
				if (dist <= range) {
					
					// If push, straight magnitude
					// If pull, cap magnitude so that it doesn't fly past player
					
					Vec3d force;
					Vec3d direction = center.subtract(e.getPositionVector()).normalize();
					force = new Vec3d(
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
							force = new Vec3d(
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
				Items.BEEF,
				Items.APPLE,
				Items.POTATO,
				Items.IRON_HELMET,
				Items.ENDER_PEARL,
				Items.CARROT,
				Items.BREAD,
				Items.COMPASS,
				Items.BRICK,
				Items.BONE,
				Items.EMERALD,
				Items.COAL,
				Items.EGG,
				Items.GOLD_INGOT,
				Items.REDSTONE,
				Items.BOOK,
				Items.QUARTZ,
				Items.CHAINMAIL_CHESTPLATE,
				Items.NETHER_WART,
				Items.IRON_INGOT,
				Items.DIAMOND_AXE,
				Items.DIAMOND_PICKAXE,
				Items.MELON_SEEDS,
				Items.REEDS,
				Items.PRISMARINE_CRYSTALS,
				Items.BREWING_STAND,
				Items.ENDER_EYE,
				Items.DIAMOND,
				Items.CHAINMAIL_BOOTS,
				Items.WOODEN_SWORD,
				Items.GLOWSTONE_DUST,
				Items.CLAY_BALL,
				Items.CLOCK,
				Items.COMPARATOR,
				Items.COOKIE,
				Items.EXPERIENCE_BOTTLE,
				Items.FEATHER,
				Items.SPIDER_EYE,
				Items.STRING);
		
		private static final Set<Block> blocks = Sets.newHashSet(
				Blocks.BOOKSHELF,
				Blocks.CACTUS,
				Blocks.COAL_ORE,
				Blocks.END_STONE,
				Blocks.DIRT,
				Blocks.ICE,
				Blocks.NOTEBLOCK,
				Blocks.NETHERRACK,
				Blocks.SAND,
				Blocks.IRON_BARS,
				Blocks.DROPPER,
				Blocks.MOSSY_COBBLESTONE,
				Blocks.STONE,
				Blocks.NETHERRACK,
				Blocks.LOG,
				Blocks.PUMPKIN,
				Blocks.QUARTZ_ORE,
				Blocks.PLANKS,
				Blocks.QUARTZ_STAIRS,
				Blocks.OAK_FENCE,
				Blocks.REDSTONE_ORE,
				Blocks.LAPIS_ORE,
				Blocks.ACACIA_FENCE,
				Blocks.CRAFTING_TABLE,
				Blocks.GOLD_ORE,
				Blocks.GRAVEL,
				Blocks.HARDENED_CLAY,
				Blocks.IRON_ORE
				);
		
		private int level;
		
		public TransmuteEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			ItemStack inhand = entity.getHeldItemMainhand();
			if (inhand == null)
				inhand = entity.getHeldItemOffhand();
			
			
			if (inhand == null)
				return;
			
			Item item = inhand.getItem();
			
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
				inhand.splitStack(1);
				((EntityPlayer) entity).inventory.addItemStackToInventory(stack);
			} else {
				// EntityLiving has held item in slot 0
				entity.setHeldItem(EnumHand.MAIN_HAND, stack);
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
				world.setBlockState(block, Blocks.FIRE.getDefaultState());
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
