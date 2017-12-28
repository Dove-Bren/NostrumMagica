package com.smanzana.nostrummagica.spells.components;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;
import com.smanzana.nostrummagica.entity.EntityGolem;
import com.smanzana.nostrummagica.entity.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.EntityGolemFire;
import com.smanzana.nostrummagica.entity.EntityGolemIce;
import com.smanzana.nostrummagica.entity.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.EntityGolemWind;
import com.smanzana.nostrummagica.potions.MagicResistPotion;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.effect.EntityLightningBolt;
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
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class SpellAction {
	
	public static class MagicDamageSource extends EntityDamageSource {
		
		private EMagicElement element;
		
		public MagicDamageSource(Entity source, EMagicElement element) {
			super("mob", source);
			this.element = element;
		}
		
		@Override
		public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
			
	        String untranslated = "death.attack.magic." + element.name();
	        return new TextComponentTranslation(untranslated, new Object[] {entityLivingBaseIn.getDisplayName(), this.damageSourceEntity.getDisplayName()});
	    }

		public EMagicElement getElement() {
			return element;
		}
	};

	private static interface SpellEffect {
		public void apply(EntityLivingBase caster, EntityLivingBase entity);
		public void apply(EntityLivingBase caster, World world, BlockPos block);
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
			entity.attackEntityFrom(new MagicDamageSource(source, element), fin);
			
			NostrumMagicaSounds sound;
			switch (element) {
			case EARTH:
				sound = NostrumMagicaSounds.DAMAGE_EARTH;
				break;
			case ENDER:
				sound = NostrumMagicaSounds.DAMAGE_ENDER;
				break;
			case FIRE:
				sound = NostrumMagicaSounds.DAMAGE_FIRE;
				break;
			case ICE:
				sound = NostrumMagicaSounds.DAMAGE_ICE;
				break;
			case LIGHTNING:
				sound = NostrumMagicaSounds.DAMAGE_LIGHTNING;
				break;
			case PHYSICAL:
			default:
				sound = NostrumMagicaSounds.DAMAGE_PHYSICAL;
				break;
			case WIND:
				sound = NostrumMagicaSounds.DAMAGE_WIND;
				break;
			
			}
			
			sound.play(entity);
			
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos) {
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
			
			NostrumMagicaSounds.STATUS_BUFF2.play(entity);
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos) {
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
			
			if (effect.isBadEffect()) {
				entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
				NostrumMagicaSounds.STATUS_DEBUFF2.play(entity);
			} else {
				NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			}
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos) {
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
			NostrumMagicaSounds.STATUS_BUFF1.play(entity);
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
		public void apply(EntityLivingBase caster, World world, BlockPos pos) {
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
			NostrumMagicaSounds.STATUS_BUFF1.play(entity);
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
		public void apply(EntityLivingBase caster, World world, BlockPos pos) {
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
			NostrumMagicaSounds.DAMAGE_WIND.play(entity);
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
					Vec3d direction = e.getPositionVector().subtract(center).normalize();
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
		public void apply(EntityLivingBase caster, World world, BlockPos pos) {
			; // Do nothing // TODO could push from the cell!
		}
	}
	
	private static class TransmuteEffect implements SpellEffect {
		
		private static Set<Item> items;
		
		private static Set<Block> blocks;
		
		private static boolean initted = false;
		private static final void init() {
			if (initted)
				return;
			
			initted = true;
			
			items = Sets.newLinkedHashSet();
			items.add(Items.BEEF);
			items.add(Items.APPLE);
			items.add(Items.POTATO);
			items.add(Items.IRON_HELMET);
			items.add(Items.ENDER_PEARL);
			items.add(Items.CARROT);
			items.add(Items.BREAD);
			items.add(Items.COMPASS);
			items.add(Items.BRICK);
			items.add(Items.BONE);
			items.add(Items.EMERALD);
			items.add(Items.COAL);
			items.add(Items.EGG);
			items.add(Items.GOLD_INGOT);
			items.add(Items.REDSTONE);
			items.add(Items.BOOK);
			items.add(Items.QUARTZ);
			items.add(Items.CHAINMAIL_CHESTPLATE);
			items.add(Items.NETHER_WART);
			items.add(Items.IRON_INGOT);
			items.add(Items.DIAMOND_AXE);
			items.add(Items.DIAMOND_PICKAXE);
			items.add(Items.MELON_SEEDS);
			items.add(Items.REEDS);
			items.add(Items.PRISMARINE_CRYSTALS);
			items.add(Items.BREWING_STAND);
			items.add(Items.ENDER_EYE);
			items.add(Items.DIAMOND);
			items.add(Items.CHAINMAIL_BOOTS);
			items.add(Items.WOODEN_SWORD);
			items.add(Items.GLOWSTONE_DUST);
			items.add(Items.CLAY_BALL);
			items.add(Items.CLOCK);
			items.add(Items.COMPARATOR);
			items.add(Items.COOKIE);
			items.add(Items.EXPERIENCE_BOTTLE);
			items.add(Items.FEATHER);
			items.add(Items.SPIDER_EYE);
			items.add(Items.STRING);
			
			blocks = Sets.newLinkedHashSet();
					
			blocks.add(Blocks.BOOKSHELF);
			blocks.add(Blocks.CACTUS);
			blocks.add(Blocks.COAL_ORE);
			blocks.add(Blocks.END_STONE);
			blocks.add(Blocks.DIRT);
			blocks.add(Blocks.ICE);
			blocks.add(Blocks.NOTEBLOCK);
			blocks.add(Blocks.NETHERRACK);
			blocks.add(Blocks.SAND);
			blocks.add(Blocks.IRON_BARS);
			blocks.add(Blocks.DROPPER);
			blocks.add(Blocks.MOSSY_COBBLESTONE);
			blocks.add(Blocks.STONE);
			blocks.add(Blocks.NETHERRACK);
			blocks.add(Blocks.LOG);
			blocks.add(Blocks.PUMPKIN);
			blocks.add(Blocks.QUARTZ_ORE);
			blocks.add(Blocks.PLANKS);
			blocks.add(Blocks.QUARTZ_STAIRS);
			blocks.add(Blocks.OAK_FENCE);
			blocks.add(Blocks.ACACIA_FENCE);
			blocks.add(Blocks.REDSTONE_ORE);
			blocks.add(Blocks.LAPIS_ORE);
			blocks.add(Blocks.CRAFTING_TABLE);
			blocks.add(Blocks.GOLD_ORE);
			blocks.add(Blocks.GRAVEL);
			blocks.add(Blocks.HARDENED_CLAY);
			blocks.add(Blocks.IRON_ORE);
		}
		
		private int level;
		
		public TransmuteEffect(int level) {
			this.level = level;
			
			TransmuteEffect.init();
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			ItemStack inhand = entity.getHeldItemMainhand();
			boolean offhand = false;
			if (inhand == null) {
				inhand = entity.getHeldItemOffhand();
				offhand = true;
			}
			
			if (inhand == null)
				return;
			
			Item item = inhand.getItem();
			ItemStack stack = null;
			if (items.contains(item)) {
				Iterator<Item> it = items.iterator();
				Item next = it.next();
				while (next != item)
					next = it.next();
				
				// Now calculate offset
				int hop = 4 - (level > 3 ? 3 : level);
				for (int i = 0; i < hop; i++) {
					if (!it.hasNext())
						it = items.iterator();
					next = it.next();
				}
				stack = new ItemStack(next, 1);
			} else {
				// Try to go through blocks and see if it's in there
				Iterator<Block> it = blocks.iterator();
				Block next = it.next();
				while (Item.getItemFromBlock(next) != item) {
					if (!it.hasNext()) {
						next = null;
						break;
					}
					next = it.next();
				}
				
				if (next != null) {
					// Now calculate offset
					int hop = 4 - (level > 3 ? 3 : level);
					for (int i = 0; i < hop; i++) {
						if (!it.hasNext())
							it = blocks.iterator();
						next = it.next();
					}
					stack = new ItemStack(Item.getItemFromBlock(next), 1);
				}
			}
			
			if (stack == null) {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
				return;
			} else {
				NostrumMagicaSounds.CAST_CONTINUE.play(entity);
			}
			
			if (entity instanceof EntityPlayer) {
				EntityPlayer p = (EntityPlayer) entity;
				if (inhand.stackSize == 1) {
					if (offhand) {
						p.inventory.removeStackFromSlot(40);
					} else {
						p.inventory.removeStackFromSlot(p.inventory.currentItem);
					}
					((EntityPlayer) entity).inventory.addItemStackToInventory(stack);
				} else {
					inhand.splitStack(1);
					((EntityPlayer) entity).inventory.addItemStackToInventory(stack);
				}
				
				
			} else {
				// EntityLiving has held item in slot 0
				entity.setHeldItem(EnumHand.MAIN_HAND, stack);
			}
			
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos) {
			Block block = world.getBlockState(pos).getBlock();
			if (!blocks.contains(block)) {
				NostrumMagicaSounds.CAST_FAIL.play(world, pos.getX() + .5, pos.getY(), pos.getZ() + .5);
				return;
			} else {
				NostrumMagicaSounds.CAST_CONTINUE.play(world, pos.getX() + .5, pos.getY(), pos.getZ() + .5);
			}
			
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
			if (duration == 0)
				return; // Nope
			
			NostrumMagicaSounds.DAMAGE_FIRE.play(entity);
			
			entity.setFire((int) Math.ceil((float) duration / 20.0f));
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block) {
			if (!world.isAirBlock(block))
				block.add(0, 1, 0);
			if (world.isAirBlock(block)) {
				world.setBlockState(block, Blocks.FIRE.getDefaultState());
				NostrumMagicaSounds.DAMAGE_FIRE.play(world,
						block.getX() + .5, block.getY(), block.getZ() + .5);
			}
		}
		
	}
	
	private static class LightningEffect implements SpellEffect {
		
		public LightningEffect() {
			
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
			apply(caster, entity.worldObj, entity.getPosition());
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block) {
			world.addWeatherEffect(
					new EntityLightningBolt(world, block.getX() + 0.5, block.getY(), block.getZ() + 0.5, false)
					);
		}
		
	}
	
	private static class SummonEffect implements SpellEffect {
		private EMagicElement element;
		private int power;
		
		public SummonEffect(EMagicElement element, int power) {
			this.element = element;
			this.power = power;
			
			if (this.element == null)
				this.element = EMagicElement.PHYSICAL;
		}

		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			// Pick a place to spawn it and then defer to location one
			World world = caster.getEntityWorld();
			BlockPos center = caster.getPosition();
			BlockPos pos;
			do {
				pos = center.add(1, 1, 0);
				if (world.isAirBlock(pos))
					break;
				
				pos = center.add(-1, 1, 0);
				if (world.isAirBlock(pos))
					break;
				
				pos = center.add(0, 1, -1);
				if (world.isAirBlock(pos))
					break;
				
				pos = center.add(0, 1, 1);
				if (world.isAirBlock(pos))
					break;
				
				pos = center;
			} while (false);
			
			apply(caster, world, pos);
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block) {
			for (int i = 0; i < power; i++) {
				EntityGolem golem = spawnGolem(world);
				golem.setPosition(block.getX() + .5, block.getY(), block.getZ() + .5);
				world.spawnEntityInWorld(golem);
				golem.setOwnerId(caster.getPersistentID());
			}
			
			NostrumMagicaSounds.CAST_CONTINUE.play(world,
					block.getX() + .5, block.getY(), block.getZ() + .5);
			
		}
		
		private EntityGolem spawnGolem(World world) {
			EntityGolem golem;
			
			switch (element) {
			case EARTH:
				golem = new EntityGolemEarth(world);
				break;
			case ENDER:
			case FIRE:
				golem = new EntityGolemFire(world);
				break;
			case ICE:
				golem = new EntityGolemIce(world);
				break;
			case LIGHTNING:
				golem = new EntityGolemLightning(world);
				break;
			case WIND:
				golem = new EntityGolemWind(world);
				break;
			default:
			case PHYSICAL:
				golem = new EntityGolemPhysical(world);
				break;
			}
			
			return golem;
		}
	}
	
	private static class BurnArmorEffect implements SpellEffect {
		
		private int level;
		
		public BurnArmorEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity) {
			int amount = 20 * level;
			if (level > 2)
				amount *= 2;
			
			int count = 0;
			for (ItemStack equip : entity.getArmorInventoryList()) {
				if (equip == null)
					continue;
				
				count++;
			}
			if (count != 0) {
				for (ItemStack equip : entity.getArmorInventoryList()) {
					if (equip == null)
						continue;
					equip.damageItem(amount/count, entity);
				}
			}
			
			NostrumMagicaSounds.MELT_METAL.play(entity);
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block) {
			;
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
			e.apply(source, world, pos);
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
		
		PotionEffect resEffect = target.getActivePotionEffect(MagicResistPotion.instance());
		if (resEffect != null) {
			base *= Math.pow(.3, resEffect.getAmplifier() + 1);
		}
			
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
		effects.add(new PushEffect(radius, -level));
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
	
	public SpellAction summon(EMagicElement element, int power) {
		effects.add(new SummonEffect(element, power));
		return this;
	}
	
	public SpellAction lightning() {
		effects.add(new LightningEffect());
		return this;
	}
	
	public SpellAction burnArmor(int power) {
		effects.add(new BurnArmorEffect(power));
		return this;
	}
}
