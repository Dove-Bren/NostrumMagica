package com.smanzana.nostrummagica.spells.components;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicReduction;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.NostrumTameLightning;
import com.smanzana.nostrummagica.entity.dragon.EntityDragon;
import com.smanzana.nostrummagica.entity.dragon.EntityShadowDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.golem.EntityGolem;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEnder;
import com.smanzana.nostrummagica.entity.golem.EntityGolemFire;
import com.smanzana.nostrummagica.entity.golem.EntityGolemIce;
import com.smanzana.nostrummagica.entity.golem.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.golem.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.golem.EntityGolemWind;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble.ItemType;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.NostrumItemTags;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.SpellActionSummary;
import com.smanzana.nostrummagica.utils.ItemStacks;
import com.smanzana.nostrummagica.world.dimension.NostrumDimensions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.NetherBiome;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolType;

public class SpellAction {
	
	private static interface SpellEffect {
		public boolean apply(LivingEntity caster, LivingEntity entity, float eff);
		public boolean apply(LivingEntity caster, World world, BlockPos block, float eff);
	}
	
	private static abstract class NegativeSpellEffect implements SpellEffect {
		
		public boolean isHarmful() {
			return true;
		}
		
		protected abstract boolean applyEffect(LivingEntity caster, LivingEntity entity, float eff);
		
		public final boolean apply(LivingEntity caster, LivingEntity entity, float eff) {
			if (entity != null && isHarmful() && caster != entity) {
				if (caster.equals(NostrumMagica.getOwner(entity))) {
					return false; // we own the target entity
				}
				
				if (entity.equals(NostrumMagica.getOwner(caster))) {
					return false; // they own us
				}
			}
			
			return applyEffect(caster, entity, eff);
		}
	}
	
	private class DamageEffect extends NegativeSpellEffect {
		private float amount;
		private EMagicElement element;
		
		public DamageEffect(EMagicElement element, float amount) {
			this.amount = amount;
			this.element = element;
		}
		
		@Override
		public boolean applyEffect(LivingEntity caster, LivingEntity entity, float efficiency) {
			float fin = calcDamage(caster, entity, amount * efficiency, element);
			source.setLastAttackedEntity(entity);
			entity.setRevengeTarget(caster);
			//entity.setHealth(Math.max(0f, entity.getHealth() - fin));
			entity.hurtResistantTime = 0;
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
			return true;
		}
		
		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos pos, float efficiency) {
			return false; // Do nothing
		}
	}
	
	private class HealEffect implements SpellEffect {
		private float amount;
		
		public HealEffect(float amount) {
			this.amount = amount;
		}
		
		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
			if (entity.isEntityUndead()) {
				source.setLastAttackedEntity(entity);
				entity.setRevengeTarget(caster);
				//entity.setHealth(Math.max(0f, entity.getHealth() - fin));
				entity.hurtResistantTime = 0;
				entity.attackEntityFrom(new MagicDamageSource(source, EMagicElement.ICE), amount * efficiency);
			} else {
				entity.heal(amount * efficiency);
				if (entity instanceof EntityTameDragonRed) {
					EntityTameDragonRed dragon = (EntityTameDragonRed) entity;
					if (dragon.isTamed() && dragon.getOwner() == caster) {
						dragon.addBond(1f);
					}
				}
			}
			
			NostrumMagicaSounds.STATUS_BUFF2.play(entity);
			return true;
		}
		
		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos pos, float efficiency) {
			return false; // Do nothing
		}
	}
	
	private class HealFoodEffect implements SpellEffect {
		private int amount;
		
		public HealFoodEffect(int amount) {
			this.amount = amount;
		}
		
		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
			if (entity instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) entity;
				player.getFoodStats().addStats((int) (amount * efficiency), 2);
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				return true;
			} else if (entity instanceof AnimalEntity && caster != null && 
					caster instanceof PlayerEntity) {
				((AnimalEntity) entity)
					.setInLove((PlayerEntity) caster);
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				return true;
			} else {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
				return false;
			}
			
		}
		
		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos pos, float efficiency) {
			return false; // Do nothing
		}
	}
	
	private class HealManaEffect implements SpellEffect {
		private int amount;
		
		public HealManaEffect(int amount) {
			this.amount = amount;
		}
		
		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
			INostrumMagic magic = NostrumMagica.getMagicWrapper(entity);
			if (magic == null) {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
				return false;
			} else {
				magic.addMana((int) (amount * efficiency));
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				return true;
			}
				
		}
		
		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos pos, float efficiency) {
			return false; // Do nothing
		}
	}
	
	private class StatusEffect extends NegativeSpellEffect {
		private Effect effect;
		private int duration;
		private int amp;
		
		public StatusEffect(Effect effect, int duration, int amp) {
			this.effect = effect;
			this.duration = duration;
			this.amp = amp; 
		}
		
		@Override
		public boolean isHarmful() {
			return this.effect.getEffectType() == EffectType.HARMFUL;
		}
		
		@Override
		public boolean applyEffect(LivingEntity caster, LivingEntity entity, float efficiency) {
			entity.addPotionEffect(new EffectInstance(effect, (int) (duration * efficiency), amp));
			
			if (effect.getEffectType() == EffectType.HARMFUL) {
				caster.setLastAttackedEntity(entity);
				entity.setRevengeTarget(caster);
				entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
				NostrumMagicaSounds.STATUS_DEBUFF2.play(entity);
			} else {
				NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			}
			return true;
		}
		
		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos pos, float efficiency) {
			return false; // Do nothing
		}
	}
	
	private class DispelEffect implements SpellEffect {
		private int number; // -1 to clear all
		
		public DispelEffect(int number) {
			this.number = number; 
		}
		
		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
			NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			
			if (number == -1 || entity.getActivePotionEffects().size() < number) {
				entity.clearActivePotions();
			} else {
				// Remove #number effects. We do this by getting another list of effects and shuffling, and then
				// just walking that list to remove from the real one
				List<EffectInstance> effectList = Lists.newArrayList(entity.getActivePotionEffects());
				Collections.shuffle(effectList);
				for (int i = 0; i < number; i++) {
					entity.removePotionEffect(effectList.get(i).getPotion());
				}
			}
			return true;
		}
		
		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos pos, float efficiency) {
			return false; // Do nothing
		}
	}
	
	private class BlinkEffect implements SpellEffect {
		private float dist;
		
		public BlinkEffect(float dist) {
			this.dist = dist;
		}
		
		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
			NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			
			if (caster != null && caster instanceof PlayerEntity) {
				// Look for lightning belt
				IInventory baubles = NostrumMagica.instance.curios.getCurios((PlayerEntity) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getSizeInventory(); i++) {
						ItemStack stack = baubles.getStackInSlot(i);
						if (stack.isEmpty() || !(stack.getItem() instanceof ItemMagicBauble)) {
							continue;
						}
						
						ItemType type = ((ItemMagicBauble) stack.getItem()).getType();
						if (type == ItemType.BELT_ENDER) {
							efficiency *= 2;
							break;
						}
					}
				}
			}
			
			if (EnchantedArmor.GetSetCount(entity, EMagicElement.ENDER, EnchantedArmor.Type.TRUE) == 4) {
				// has full ender set
				efficiency *= 2;
			}
			
			// Apply efficiency bonus
			float dist = this.dist * efficiency;
			
			Vec3d dest;
			Vec3d direction = entity.getLookVec().normalize();
			Vec3d source = entity.getPositionVector();
			source = source.add(0, entity.getEyeHeight(), 0);
			BlockPos bpos;
			Vec3d translation = new Vec3d(direction.x * dist,
					direction.y * dist,
					direction.z * dist);
			
			// Find ideal dest (vect addition). Can we go there? Then go there.
			// Else step backwards and raycast forward in 1/5 increments.
			// See if place we hit is same spot as raycast. If so, fail and do again
			
			dest = source.add(translation);
			bpos = new BlockPos(dest.x, dest.y, dest.z);
			if (isPassable(entity.world, bpos)
				&& isPassable(entity.world, bpos.add(0, 1, 0))) {
					// Whoo! Looks like we can teleport there!
			} else {
				int i = 4; // Attempt raytrace from (20% * i * pathlength)
				Vec3d endpoint = dest;
				dest = null;
				Vec3d from;
				double curDist;
				while (i >= 0) {
					if (i == 0) {
						// optimization
						from = source;
					} else {
						curDist = (.2 * i);
						from = new Vec3d(translation.x * curDist,
								translation.y * curDist,
								translation.z * curDist);
						from = source.add(from);
					}
					
					RayTraceResult mop = entity.world.rayTraceBlocks(new RayTraceContext(from, endpoint, BlockMode.COLLIDER, FluidMode.NONE, entity));
					if (mop != null && mop.getHitVec().distanceTo(from) > 0.5) {
						// We got one
						BlockPos pos = new BlockPos(mop.getHitVec());
						if (isPassable(entity.world, pos) && isPassable(entity.world, pos.add(0, 1, 0))) {
							dest = mop.getHitVec();
							break;
						}
					}
					
					i--;
				}
			}
			
			if (dest != null) {
				entity.setPositionAndUpdate(.5 + Math.floor(dest.x), Math.floor(dest.y), .5 + Math.floor(dest.z));
				entity.fallDistance = 0;
				NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			}
			
			return dest != null;
		}
		
		private boolean isPassable(World world, BlockPos pos) {
			if (world.isAirBlock(pos))
				return true;
			
			BlockState state = world.getBlockState(pos);
			
			if (state == null)
				return true;
			if (state.getMaterial().isLiquid())
				return true;
			if (!state.getMaterial().blocksMovement())
				return true;
			
			return false;
		}
		
		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos pos, float efficiency) {
			return false; // Do nothing
		}
	}
	
	private class PushEffect extends NegativeSpellEffect {
		private float range;
		private int amp; // - is pull
		
		public PushEffect(float range, int amp) {
			this.range = range;
			this.amp = amp; 
		}
		
		@Override
		public boolean applyEffect(LivingEntity caster, LivingEntity entity, float efficiency) {
			return apply(caster, entity.world, entity.getPosition(), efficiency);
		}
		
		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos pos, float efficiency) {

			// We abs the amp here, but check it belwo for pull and negate vector
			float magnitude = .35f * (Math.abs(amp) + 1.0f) * (float) Math.min(2.0f, Math.max(0.0f, 1.0f + Math.log(efficiency)));
			Vec3d center = new Vec3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
			NostrumMagicaSounds.DAMAGE_WIND.play(world, center.x, center.y, center.z);
			
			boolean any = false;
			for (Entity e : world.getEntitiesWithinAABBExcludingEntity(null, 
					new AxisAlignedBB(center.x - range, center.y - range, center.z - range, center.x + range, center.y + range, center.z + range)
					)) {
				double dist = e.getPositionVector().distanceTo(center); 
				if (dist <= range) {
					
					// If push, straight magnitude
					// If pull, cap magnitude so that it doesn't fly past player
					
					Vec3d force;
					Vec3d direction = e.getPositionVector().add(0, e.getEyeHeight(), 0).subtract(center).normalize();
					force = new Vec3d(
							direction.x * magnitude,
							direction.y * magnitude,
							direction.z * magnitude
							);
					if (amp < 0) {
						// pull
						// Cap force's magnitude at .2 dist
						double mod = force.length();
						if (mod > dist * .2) {
							mod = (dist * .4) / mod;
							force = new Vec3d(
									force.x * mod,
									force.y * mod,
									force.z * mod
									);
						}

						force = new Vec3d(
								force.x * -1.0,
								force.y * -1.0,
								force.z * -1.0);
					}
					
					e.addVelocity(force.x, force.y, force.z);
					any = true;
				}
			}
			
			return any;
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
			items.add(Items.SUGAR_CANE);
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
			blocks.add(Blocks.NOTE_BLOCK);
			blocks.add(Blocks.NETHERRACK);
			blocks.add(Blocks.SAND);
			blocks.add(Blocks.IRON_BARS);
			blocks.add(Blocks.DROPPER);
			blocks.add(Blocks.MOSSY_COBBLESTONE);
			blocks.add(Blocks.STONE);
			blocks.add(Blocks.NETHERRACK);
			blocks.add(Blocks.OAK_LOG);
			blocks.add(Blocks.PUMPKIN);
			blocks.add(Blocks.NETHER_QUARTZ_ORE);
			blocks.add(Blocks.OAK_PLANKS);
			blocks.add(Blocks.QUARTZ_STAIRS);
			blocks.add(Blocks.OAK_FENCE);
			blocks.add(Blocks.ACACIA_FENCE);
			blocks.add(Blocks.REDSTONE_ORE);
			blocks.add(Blocks.LAPIS_ORE);
			blocks.add(Blocks.CRAFTING_TABLE);
			blocks.add(Blocks.GOLD_ORE);
			blocks.add(Blocks.GRAVEL);
			blocks.add(Blocks.TERRACOTTA);
			blocks.add(Blocks.IRON_ORE);
		}
		
		private int level;
		
		public TransmuteEffect(int level) {
			this.level = level;
			
			TransmuteEffect.init();
		}
		
		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
			ItemStack inhand = entity.getHeldItemMainhand();
			boolean offhand = false;
			if (inhand.isEmpty()) {
				inhand = entity.getHeldItemOffhand();
				offhand = true;
			}
			
			if (inhand.isEmpty())
				return false;
			
			Item item = inhand.getItem();
			ItemStack stack = ItemStack.EMPTY;
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
				while (Item.getItemFromBlock(next) != item) { // TODO this only works for vanilla? That ok?
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
			
			if (stack.isEmpty()) {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
				return false;
			} else {
				NostrumMagicaSounds.CAST_CONTINUE.play(entity);
			}
			
			if (entity instanceof PlayerEntity) {
				PlayerEntity p = (PlayerEntity) entity;
				if (inhand.getCount() == 1) {
					if (offhand) {
						p.inventory.removeStackFromSlot(40);
					} else {
						p.inventory.removeStackFromSlot(p.inventory.currentItem);
					}
					((PlayerEntity) entity).inventory.addItemStackToInventory(stack);
				} else {
					inhand.split(1);
					((PlayerEntity) entity).inventory.addItemStackToInventory(stack);
				}
				
				
			} else {
				// MobEntity has held item in slot 0
				entity.setHeldItem(Hand.MAIN_HAND, stack);
			}
			
			return true;
		}
		
		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos pos, float efficiency) {
			Block block = world.getBlockState(pos).getBlock();
			if (!blocks.contains(block)) {
				NostrumMagicaSounds.CAST_FAIL.play(world, pos.getX() + .5, pos.getY(), pos.getZ() + .5);
				return false;
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
			return true;
		}
	}
	
	private static class BurnEffect extends NegativeSpellEffect {

		private int duration;
		
		public BurnEffect(int duration) {
			this.duration = duration;
		}
		
		@Override
		public boolean applyEffect(LivingEntity caster, LivingEntity entity, float efficiency) {
			int duration = (int) (this.duration * efficiency);
			if (duration == 0)
				return false; // Nope
			
			NostrumMagicaSounds.DAMAGE_FIRE.play(entity);
			
			caster.setLastAttackedEntity(entity);
			entity.setRevengeTarget(caster);
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
			
			entity.setFire((int) Math.ceil((float) duration / 20.0f));
			return true;
		}

		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos block, float efficiency) {
			BlockState state = world.getBlockState(block);
			if (state != null && state.getBlock() instanceof Candle) {
				Candle.light(world, block, state);
				NostrumMagicaSounds.DAMAGE_FIRE.play(world,
						block.getX() + .5, block.getY(), block.getZ() + .5);
				return true;
			}
			
			if (world.getDimension().getType() == NostrumDimensions.EmptyDimension) {
				return false;
			}
			
			if (!world.isAirBlock(block))
				block.add(0, 1, 0);
			if (world.isAirBlock(block)) {
				world.setBlockState(block, Blocks.FIRE.getDefaultState());
				NostrumMagicaSounds.DAMAGE_FIRE.play(world,
						block.getX() + .5, block.getY(), block.getZ() + .5);
				return true;
			}
			return false;
		}
		
	}
	
	private static class LightningEffect extends NegativeSpellEffect {
		
		public LightningEffect() {
			
		}
		
		@Override
		public boolean applyEffect(LivingEntity caster, LivingEntity entity, float efficiency) {
			entity.setRevengeTarget(caster);
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
			return apply(caster, entity.world, entity.getPosition(), efficiency);
		}

		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos block, float efficiency) {
			
			int count = 1;
			
			if (caster != null && caster instanceof PlayerEntity) {
				// Look for lightning belt
				IInventory baubles = NostrumMagica.instance.curios.getCurios((PlayerEntity) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getSizeInventory(); i++) {
						ItemStack stack = baubles.getStackInSlot(i);
						if (stack.isEmpty() || !(stack.getItem() instanceof ItemMagicBauble)) {
							continue;
						}
						
						ItemType type = ((ItemMagicBauble) stack.getItem()).getType();
						if (type == ItemType.BELT_LIGHTNING) {
							count = caster.getRNG().nextInt(3) + 3;
							break;
						}
					}
				}
			}
			
			MutableBlockPos cursor = new MutableBlockPos(block);
			Random rand = (caster == null ? new Random() : caster.getRNG());
			for (int i = 0; i < count; i++) {
				
				if (i == 0) {
					; // Don't adjust pos
				} else {
					// Apply random x/z offsets. Then step up to 4 to find surface
					cursor.setPos(
							block.getX() + rand.nextInt(6) - 3,
							block.getY() - 2,
							block.getZ() + rand.nextInt(6) - 3);
					
					// Find surface
					int dist = 0;
					while (dist++ < 4 && !world.isAirBlock(cursor)) {
						cursor.setY(cursor.getY() + 1);
					}
				}
				
				((ServerWorld) world).addLightningBolt(
					(new NostrumTameLightning(NostrumEntityTypes.tameLightning, world, cursor.getX() + 0.5, cursor.getY(), cursor.getZ() + 0.5))
					.setEntityToIgnore(caster)
					);
			}

			return true;
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
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
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
			
			return apply(caster, world, pos, efficiency);
		}

		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos block, float efficiency) {
			
			// For non-player entities, just spawn some new golems.
			// Else spawn player-bound golems
			if (caster instanceof PlayerEntity) {
				if (NostrumMagica.getMagicWrapper(caster) == null) {
					return false;
				}
				
				NostrumMagica.getMagicWrapper(caster).clearFamiliars();
				caster.removeActivePotionEffect(NostrumEffects.familiar);
				for (int i = 0; i < power; i++) {
					EntityGolem golem = spawnGolem(world);
					golem.setPosition(block.getX() + .5, block.getY(), block.getZ() + .5);
					world.addEntity(golem);
					golem.setOwnerId(caster.getUniqueID());
					NostrumMagica.getMagicWrapper(caster).addFamiliar(golem);
				}
				int time = (int) (20 * 60 * 2.5 * Math.pow(2, Math.max(0, power - 1)) * efficiency);
				caster.addPotionEffect(new EffectInstance(NostrumEffects.familiar, time, 0) {
					@Override
					public boolean tick(LivingEntity entityIn) {
						// heh snekky
						boolean ret = super.tick(entityIn);
						if (ret) {
							// we're not being removed. Check familiars
							if (entityIn.world.isRemote) {
								return true;
							}
							
							INostrumMagic attr = NostrumMagica.getMagicWrapper(entityIn);
							if (attr != null) {
								boolean active = false;
								if (!attr.getFamiliars().isEmpty()) {
									for (LivingEntity fam : attr.getFamiliars()) {
										if (fam.isAlive()) {
											active = true;
											break;
										}
									}
								}
								if (!active) {
									ret = false;
								}
							}
						}
						
						return ret;
					}
				});
			} else {
				// Just summon some new golems
				final int time = (int) (20f * (15f * efficiency));
				for (int i = 0; i < power; i++) {
					EntityGolem golem = spawnGolem(world);
					golem.setPosition(block.getX() + .5, block.getY(), block.getZ() + .5);
					golem.setExpiresAfterTicks(time);
					world.addEntity(golem);
				}
			}
			
			NostrumMagicaSounds.CAST_CONTINUE.play(world,
					block.getX() + .5, block.getY(), block.getZ() + .5);
			return true;
			
		}
		
		private EntityGolem spawnGolem(World world) {
			EntityGolem golem;
			
			switch (element) {
			case EARTH:
				golem = new EntityGolemEarth(NostrumEntityTypes.golemEarth, world);
				break;
			case ENDER:
				golem = new EntityGolemEnder(NostrumEntityTypes.golemEnder, world);
				break;
			case FIRE:
				golem = new EntityGolemFire(NostrumEntityTypes.golemFire, world);
				break;
			case ICE:
				golem = new EntityGolemIce(NostrumEntityTypes.golemIce, world);
				break;
			case LIGHTNING:
				golem = new EntityGolemLightning(NostrumEntityTypes.golemLightning, world);
				break;
			case WIND:
				golem = new EntityGolemWind(NostrumEntityTypes.golemWind, world);
				break;
			default:
			case PHYSICAL:
				golem = new EntityGolemPhysical(NostrumEntityTypes.golemPhysical, world);
				break;
			}
			
			return golem;
		}
	}
	
	private static class SwapEffect extends NegativeSpellEffect {
		
		public SwapEffect() {
			
		}
		
		@Override
		public boolean applyEffect(LivingEntity caster, LivingEntity entity, float efficiency) {
			if (caster == null || entity == null)
				return false;
			
			Vec3d pos = caster.getPositionVector();
			float pitch = caster.rotationPitch;
			float yaw = caster.rotationYawHead;
			
			if (caster instanceof PlayerEntity) {
				caster.setPositionAndRotation(caster.posX, caster.posY, caster.posZ, entity.rotationYawHead, entity.rotationPitch);
				caster.setPositionAndUpdate(
						entity.posX, entity.posY, entity.posZ);
			} else {
				caster.setPositionAndRotation(
						entity.posX, entity.posY, entity.posZ,
						entity.rotationPitch, entity.rotationYawHead
						);
			}
			
			if (entity instanceof PlayerEntity) {
				entity.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, yaw, pitch);
				entity.setPositionAndUpdate(pos.x, pos.y, pos.z);
			} else {
				entity.setPositionAndRotation(pos.x, pos.y, pos.z, yaw, pitch);
			}
			
			entity.fallDistance = 0;
			caster.fallDistance = 0;
			return true;			
		}
		
		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos pos, float efficiency) {
			caster.setPositionAndUpdate(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
			caster.fallDistance = 0;
			return true;
		}
	}
	
	private static class PropelEffect implements SpellEffect {
		
		int level;
		
		public PropelEffect(int level) {
			this.level = level;
		}
		
		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {

			Vec3d force = entity.getLookVec().add(0, 0.15, 0).normalize();
			float scale = 1f * (.5f * (level + 1)) * (float) (Math.max(0.0, Math.min(2.0, 1.0 - Math.log(efficiency))));
			
			force = new Vec3d(force.x * scale, force.y * scale, force.z * scale);
			
			entity.setMotion(entity.getMotion().add(force.x, force.y, force.z));
			entity.velocityChanged = true;
			
			NostrumMagicaSounds.DAMAGE_WIND.play(entity);
			return true;
		}
		
		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos pos, float efficiency) {
			return false; // Doesn't mean anything
		}
	}
	
	private static class PhaseEffect extends NegativeSpellEffect {
		
		private int level;
		
		public PhaseEffect(int level) {
			this.level = level;
		}
		
		@Override
		public boolean applyEffect(LivingEntity caster, LivingEntity entity, float efficiency) {
			if (caster != entity && entity instanceof MobEntity) {
				// Make sure they want to attack you if you do it
				entity.setRevengeTarget(caster);
				entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
				entity.hurtResistantTime = 0;
			}
			
			double radius = (16 + (32.0 * level)) * efficiency;
			
			if (caster != null && caster instanceof PlayerEntity) {
				// Look for ender belt
				IInventory baubles = NostrumMagica.instance.curios.getCurios((PlayerEntity) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getSizeInventory(); i++) {
						ItemStack stack = baubles.getStackInSlot(i);
						if (stack.isEmpty() || !(stack.getItem() instanceof ItemMagicBauble)) {
							continue;
						}
						
						ItemType type = ((ItemMagicBauble) stack.getItem()).getType();
						if (type == ItemType.BELT_ENDER) {
							radius *= 2.0;
							break;
						}
					}
				}
			}
			
			if (EnchantedArmor.GetSetCount(entity, EMagicElement.ENDER, EnchantedArmor.Type.TRUE) == 4) {
				// has full ender set
				radius *= 2.0;
			}
			
			NostrumMagicaSounds.DAMAGE_ENDER.play(entity);
			
			for (int i = 0; i < 20; i++) {
			
				// Find a random place to teleport
		        double x = entity.posX + (NostrumMagica.rand.nextDouble() - 0.5D) * radius;
		        double y = entity.posY + (double)(NostrumMagica.rand.nextInt((int) radius) - (int) radius / 2.0);
		        double z = entity.posZ + (NostrumMagica.rand.nextDouble() - 0.5D) * radius;
	
			    // Try to teleport
		        if (entity.attemptTeleport(x, y, z, false))
		        	break;
			}
			return true;
		}

		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos block, float efficiency) {
			// Summon an entity
			if (!world.isAirBlock(block))
				block.add(0, 1, 0);
			
			int count = 1;
			for (int i = 1; i < level; i++)
				if (NostrumMagica.rand.nextBoolean())
					count++;
			
			double x = block.getX() + .5,
					y = block.getY() + .1,
					z = block.getZ() + .5;
			
			for (int i = 0; i < count; i++) {
				Entity entity;
				if (NostrumMagica.rand.nextFloat() <= .1f) {
					entity = new ItemEntity(world,
							x, y, z,
							new ItemStack(Items.ENDER_PEARL));
				} else if (NostrumMagica.rand.nextFloat() <= .3) {
					entity = EntityType.ENDERMAN.create(world);
				} else {
					entity = EntityType.ENDERMITE.create(world);
				}
				
				entity.setPosition(x + (NostrumMagica.rand.nextFloat() - .5),
									y,
									z + (NostrumMagica.rand.nextFloat() - .5));
				
				world.addEntity(entity);
			}
			
			NostrumMagicaSounds.DAMAGE_ENDER.play(world, block.getX(), block.getY(), block.getZ());
			return true;
		}
		
	}
	
	private static class EnchantEffect implements SpellEffect {
		
		private int level;
		private EMagicElement element;
		
		public EnchantEffect(EMagicElement element, int level) {
			this.level = level;
			this.element = element;
		}
		
		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
			// Different effect if non-player casts: just give magic buff
			if (!(caster instanceof PlayerEntity)) {
				int count = level + 1;
				double amt = 2 + level;
				caster.removeActivePotionEffect(NostrumEffects.magicBuff);
				NostrumMagica.magicEffectProxy.applyMagicBuff(entity, element, amt, count);
				entity.addPotionEffect(new EffectInstance(NostrumEffects.magicBuff, 60 * 20, 0));
				return true;
			}
			
			ItemStack inhand = entity.getHeldItemMainhand();
			boolean offhand = false;
			if (!isEnchantable(inhand)) {
				inhand = entity.getHeldItemOffhand();
				offhand = true;
			}
			
			if (inhand.isEmpty())
				return false;

			ItemStack addedItem = ItemStack.EMPTY;
			boolean didEmpower = false;
			
			// Main hand attempt
			if (inhand != null) {
				Item item = inhand.getItem();
				if (NostrumItemTags.Items.InfusedGemVoid.contains(item)) {
					int count = (int) Math.pow(2, level - 1);
					addedItem = InfusedGemItem.getGem(element, count);
				} else if (item instanceof EssenceItem) {
					int count = level + 1;
					double amt = 2 + level;
					didEmpower = true;
					caster.removeActivePotionEffect(NostrumEffects.magicBuff);
					NostrumMagica.magicEffectProxy.applyMagicBuff(entity, element, amt, count);
					entity.addPotionEffect(new EffectInstance(NostrumEffects.magicBuff, 60 * 20, 0));
				}
			}
			
			if (addedItem.isEmpty() && !didEmpower) {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
			} else {
				if (entity instanceof PlayerEntity) {
					PlayerEntity p = (PlayerEntity) entity;
					if (inhand.getCount() == 1) {
						if (offhand) {
							p.inventory.removeStackFromSlot(40);
						} else {
							p.inventory.removeStackFromSlot(p.inventory.currentItem);
						}
					} else {
						inhand.split(1);
					}
					if (!addedItem.isEmpty()) {
						((PlayerEntity) entity).inventory.addItemStackToInventory(addedItem);
					}
					
					
				} else {
					// MobEntity has held item in slot 0
					if (!addedItem.isEmpty()) {
						entity.setHeldItem(Hand.MAIN_HAND, addedItem);
					}
				}
				NostrumMagicaSounds.CAST_CONTINUE.play(entity);
			}
			return true;
			
		}

		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos block, float efficiency) {
			return false;
		}
		
	}
	
	private static class GrowEffect implements SpellEffect {
		
		private int count;
		
		public GrowEffect(int count) {
			this.count = count;
		}

		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
			if (entity instanceof AnimalEntity) {
				AnimalEntity animal = (AnimalEntity) entity;
				animal.addGrowth((int) (count * 500 * efficiency));
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				return true;
			}
			
			return false;
		}

		@SuppressWarnings("deprecation")
		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos block, float efficiency) {
			if (world.isAirBlock(block)) {
				block = block.add(0, -1, 0);
			}
			
			if (world.isAirBlock(block)) {
				return false;
			}
			
			ItemStack junk = new ItemStack(Items.BONE_MEAL, 10); // each apply call may reduce count by 1
			boolean worked = false;
			for (int i = 0; i < count; i++) {
				if (caster instanceof PlayerEntity) {
					if (!BoneMealItem.applyBonemeal(junk, world, block, (PlayerEntity)caster)) {
						break;
					}
				} else {
					if (!BoneMealItem.applyBonemeal(junk, world, block)) {
						break;
					}
				}
				worked = true;
			}
			
			if (worked) {
				NostrumMagicaSounds.STATUS_BUFF2.play(world, block.getX(), block.getY(), block.getZ()); // TODO this and fire both movep osition up which is bad
			}
			
			return worked;
		}
	}
	
	private static class BurnArmorEffect extends NegativeSpellEffect {
		
		private int level;
		
		public BurnArmorEffect(int level) {
			this.level = level;
		}
		
		@Override
		public boolean applyEffect(LivingEntity caster, LivingEntity entity, float efficiency) {
			int amount = (int) (20 * level * efficiency);
			if (level > 2)
				amount *= 2;
			
			int count = 0;
			for (ItemStack equip : entity.getArmorInventoryList()) {
				if (equip.isEmpty())
					continue;
				
				count++;
			}
			if (count != 0) {
				for (ItemStack equip : entity.getArmorInventoryList()) {
					if (equip.isEmpty())
						continue;
					ItemStacks.damageEquippedArmor(equip, entity, ((ArmorItem) equip.getItem()).getEquipmentSlot(), amount/count);
				}
			}
			
			NostrumMagicaSounds.MELT_METAL.play(entity);
			caster.setLastAttackedEntity(entity);
			entity.setRevengeTarget(caster);
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
			return true;
		}

		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos block, float efficiency) {
			return false;
		}
		
	}
	
	private static class WallEffect implements SpellEffect {
		
		private int level;
		
		public WallEffect(int level) {
			this.level = level;
		}

		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
			return apply(caster, entity.world, entity.getPosition().add(0, 1, 0), efficiency);
		}

		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos block, float efficiency) {
			if (!world.isAirBlock(block))
				block = block.add(0, 1, 0); // TODO don't move up one cause it messes up AoE effects and double-affects locations!
			
			if (!world.isAirBlock(block)) {
				NostrumMagicaSounds.CAST_FAIL.play(world, block.getX(), block.getY(), block.getZ());
				return false;
			} else {
				NostrumMagicaSounds.DAMAGE_WIND.play(world, block.getX(), block.getY(), block.getZ());
				world.setBlockState(block, NostrumBlocks.magicWall.getState(level));
				return true;
			}
				
		}
		
	}

	private static class CursedIce implements SpellEffect {
		
		private int level;
		
		public CursedIce(int level) {
			this.level = level;
		}

		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
			return apply(caster, entity.world, entity.getPosition().add(0, 1, 0), efficiency);
		}

		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos block, float efficiency) {
			
			world.setBlockState(block, NostrumBlocks.cursedIce.getState(level));
			NostrumMagicaSounds.DAMAGE_ICE.play(world, block.getX(), block.getY(), block.getZ());
			return true;
			
		}
	}
	
	private static class GeoBlock implements SpellEffect {
		
		private int level;
		
		public GeoBlock(int level) {
			this.level = level;
		}

		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
			return apply(caster, entity.world, entity.getPosition().add(0, -1, 0), efficiency);
		}

		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos block, float efficiency) {
			
			Block result;
			float temp = world.getBiome(block).getTemperature(block);
			// < 0 exists for icy places
			// .1 to .2 has somethign to do with snow
			// desert is 2.0
			// Plains are just below 1
			if (world.getBiome(block) instanceof NetherBiome) {
				if (level == 1)
					result = Blocks.NETHERRACK;
				else if (level == 2)
					result = Blocks.LAVA;
				else
					if (NostrumMagica.rand.nextFloat() < 0.3f)
						result = Blocks.NETHER_QUARTZ_ORE;
					else
						result = Blocks.GLOWSTONE;
			} else if (temp < 0f) {
				if (level == 1)
					result = Blocks.SNOW;
				else if (level == 2)
					result = Blocks.ICE;
				else {
					if (NostrumMagica.rand.nextFloat() < 0.3f)
						result = Blocks.LAPIS_ORE;
					else
						result = Blocks.PACKED_ICE;
				}
			} else if (temp < 0.5f) {
				if (level == 1)
					result = Blocks.GRAVEL;
				else if (level == 2)
					result = Blocks.CLAY;
				else
					if (NostrumMagica.rand.nextFloat() < 0.3f)
						result = Blocks.GOLD_ORE;
					else
						result = Blocks.PRISMARINE;
			} else if (temp < 1.5f) {
				if (level == 1)
					result = Blocks.STONE;
				else if (level == 2)
					result = Blocks.MOSSY_COBBLESTONE;
				else
					if (NostrumMagica.rand.nextFloat() < 0.3f)
						result = Blocks.IRON_ORE;
					else
						result = Blocks.COAL_ORE;
			} else if (temp < 2.5f) {
				if (level == 1)
					result = Blocks.DIRT;
				else if (level == 2)
					if (NostrumMagica.rand.nextFloat() < 0.3f)
						result = Blocks.REDSTONE_ORE;
					else
						result = Blocks.OBSIDIAN;
				else
					if (NostrumMagica.rand.nextFloat() < 0.3f)
						result = Blocks.IRON_ORE;
					else
						result = Blocks.COAL_ORE;
			} else {
				if (level == 1)
					result = Blocks.NETHERRACK;
				else if (level == 2)
					result = Blocks.LAVA;
				else
					if (NostrumMagica.rand.nextFloat() < 0.3f)
						result = Blocks.NETHER_QUARTZ_ORE;
					else
						result = Blocks.GLOWSTONE;
			}
			
			world.setBlockState(block, result.getDefaultState());
			NostrumMagicaSounds.DAMAGE_FIRE.play(world, block.getX(), block.getY(), block.getZ());
			return true;
		}
	}
	
	private static class BreakEffect implements SpellEffect {

		private int level;
		
		public BreakEffect(int level) {
			this.level = level;
		}
		
		@Override
		public boolean apply(LivingEntity caster, LivingEntity entity, float eff) {
			return apply(caster, entity.world, entity.getPosition().add(0, -1, 0), eff);
		}
		
		protected boolean isTool(@Nullable PlayerEntity player, ItemStack stack) {
			if (stack.isEmpty()) {
				return false;
			}
			
			Set<ToolType> classes = stack.getItem().getToolTypes(stack);
			for (ToolType cla : classes) {
				// Required harvest level >= iron so throw-away levels like wood and stone don't count
				if (stack.getItem().getHarvestLevel(stack, cla, player, null) >= 2) {
					return true;
				}
			}
			
			return false;
		}

		@Override
		public boolean apply(LivingEntity caster, World world, BlockPos block, float eff) {
			if (world.isAirBlock(block))
				return false;
			
			if (world.getDimension().getType() == NostrumDimensions.EmptyDimension) {
				return false;
			}
			
			BlockState state = world.getBlockState(block);
			if (state == null || state.getMaterial().isLiquid())
				return false;
			
			boolean onlyStone = (level <= 1);
			if (onlyStone && caster instanceof PlayerEntity) {
				if (!Tags.Blocks.STONE.contains(state.getBlock())) {
					return false;
				}
			}
			
			boolean usePickaxe = (level >= 3);
			float hardness = state.getBlockHardness(world, block);
			
			if (hardness >= 100f || hardness < 0f)
				return false;
			
			if (caster instanceof ServerPlayerEntity) {
				// This checks item harvest level >:(
//				if (!state.getBlock().canHarvestBlock(world, block, (PlayerEntity) caster)) {
//					return false;
//				}
				
				if (usePickaxe) {
					// Check if they have a pickaxe
					ItemStack inHand = caster.getHeldItemMainhand();
					if (!isTool((PlayerEntity) caster, inHand)) {
						inHand = caster.getHeldItemOffhand();
					}
					if (!isTool((PlayerEntity) caster, inHand)) {
						usePickaxe = false;
					}
				}
				
				if (usePickaxe) {
					((ServerPlayerEntity) caster).interactionManager.tryHarvestBlock(block);
				} else {
					world.destroyBlock(block, true);
				}
			} else {
				world.destroyBlock(block, true);
			}
			
			return true;
		}
		
	}
	
//	private static class InfuseEffect implements SpellEffect {
//		
//		private int level;
//		private EMagicElement element;
//		
//		public InfuseEffect(EMagicElement element, int level) {
//			this.level = level;
//			this.element = element;
//		}
//
//		@Override
//		public boolean apply(LivingEntity caster, LivingEntity entity, float efficiency) {
//			ItemStack inhand = entity.getHeldItemMainhand();
//			boolean offhand = false;
//			if (inhand.isEmpty()) {
//				inhand = entity.getHeldItemOffhand();
//				offhand = true;
//			}
//			
//			if (inhand.isEmpty())
//				return;
//			
//			Item item = inhand.getItem();
//			 else {
//				NostrumMagicaSounds.CAST_FAIL.play(entity);
//			}
//		}
//
//		@Override
//		public boolean apply(LivingEntity caster, World world, BlockPos block, float efficiency) {
//			; // No effect
//		}
//		
//	}
	
	private LivingEntity source;
	private List<SpellEffect> effects;
	private String nameKey;
	
	public SpellAction(LivingEntity source) {
		this.source = source;
		effects = new LinkedList<>();
	}
	
	/**
	 * Applies the contained effect(s) on the provided entity at the given efficiency.
	 * Returns whether the entity was affected. 
	 * @param entity
	 * @param efficiency
	 * @return
	 */
	public boolean apply(LivingEntity entity, float efficiency) {
		if (entity.world.isRemote)
			return false;
		
		final LivingEntity ent = entity;
		boolean affected = false;
		
		SpellActionSummary summary = new SpellActionSummary(this, efficiency);
		NostrumMagica.playerListener.onMagicEffect(entity, source, summary);
		if (!summary.wasCancelled()) {
			for (SpellEffect e : effects) {
				final SpellEffect effect = e;
				
				if (!entity.getServer().isOnExecutionThread()) { // TODO I think?
					throw new RuntimeException("Wrong thread for spell effects!");
				}
				//entity.getServer().runAsync(() -> {
				affected = effect.apply(source, ent, summary.getEfficiency()) || affected;
				//});
			}
		}
		
		return affected;
	}
	
	/**
	 * Apply the contained effect(s) at the provided location.
	 * Returns whether the location was affected
	 * @param world
	 * @param pos
	 * @param efficiency
	 * @return
	 */
	public boolean apply(World world, BlockPos pos, float efficiency) {
		if (world.isRemote)
			return false;
		
		final World w = world;
		final BlockPos b = pos;
		
		boolean affected = false;
		for (SpellEffect e : effects) {
			final SpellEffect effect = e;
			
			if (!world.getServer().isOnExecutionThread()) {
				throw new RuntimeException("Wrong thread for spell effects!");
			}
			
			//world.getMinecraftServer().runAsync(() -> {
				affected = effect.apply(source, w, b, efficiency) || affected;
			//});
		}
		
		return affected;
	}
	
	/**
	 * Check and return whether any obviously harmful effects are in this action
	 * @return
	 */
	public boolean isHarmful() {
		for (SpellEffect e : effects) {
			if (e instanceof NegativeSpellEffect && ((NegativeSpellEffect) e).isHarmful()) {
				return true;
			}
		}
		
		return false;
	}
	
	public static final float calcDamage(LivingEntity caster, LivingEntity target, float base, EMagicElement element) {
		float amt = 0f;
		
		if (target == null)
			return amt;
		
		// Really, I should just make an attribute for magic potency (which could be the same that everyhting else has, too!)
		// Attribute made. Should rework
		EffectInstance boostEffect = caster.getActivePotionEffect(NostrumEffects.magicBoost);
		if (boostEffect != null) {
			base *= Math.pow(1.5, boostEffect.getAmplifier() + 1);
		}
		boostEffect = caster.getActivePotionEffect(NostrumEffects.lightningCharge);
		if (boostEffect != null) {
			base *= 2.0;
		}
		
		if (element == EMagicElement.PHYSICAL) {
			base = applyArmor(target, base);
			// Physical is reduced by real armor but not affected by magic resist effects and attributes.
			// It still gains power from magic boost (above) AND is still reduces with magic reduction (below).
		} else {
		
			final int armor = target.getTotalArmorValue();
			final boolean undead = target.isEntityUndead();
			final boolean ender;
			final boolean light;
			final boolean flamy;
			
			if (target instanceof EndermanEntity || target instanceof EndermiteEntity
					|| target instanceof EntityDragon) {
				ender = true;
			} else {
				ender = false;
			}
			
			if (target.getHeight() < 1.5f || target instanceof EndermanEntity || target instanceof EntityShadowDragonRed) {
				light = true;
			} else {
				light = false;
			}
			
			if (target.isImmuneToFire()) {
				flamy = true;
			} else {
				flamy = false;
			}
			
			EffectInstance resEffect = target.getActivePotionEffect(NostrumEffects.magicResist);
			if (resEffect != null) {
				base *= Math.pow(.75, resEffect.getAmplifier() + 1);
			}
			
			IAttributeInstance attr = target.getAttribute(AttributeMagicResist.instance());
			if (attr != null && attr.getValue() != 0.0D) {
				base *= Math.max(0.0D, Math.min(2.0D, 1.0D - (attr.getValue() / 100.0D)));
			}
				
			switch (element) {
			case ENDER:
				if (ender) return 0.0f; // does not affect ender
				base *= 1.2f; // return raw damage (+20%) not affected by armor
				break;
			case LIGHTNING:
				base *= (.75f + ((float) armor / 20f)); // double in power for every 20 armor
				break;
			case FIRE:
				base *= (undead ? 1.5f : (flamy ? .5f : 1f)); // 1.5x damage against undead. Regular otherwise
				break;
			case EARTH:
				//base; // raw damage. Not affected by armor
				break;
			case ICE:
				base *= (undead ? .6f : 1.3f); // More affective against everything except undead
				if (target.isBurning()) {
					base *= 2;
				}
				break;
			case WIND:
				base *= (light ? 1.8f : .8f); // 180% against light (endermen included) enemies
				break;
			default:
				//base;
				break;
			}
		}
		
		// Apply armor reductions
		IAttributeInstance attr = target.getAttribute(AttributeMagicReduction.instance(element));
		if (attr != null && attr.getValue() != 0.0D) {
			base -= attr.getValue();
		}
		
		return base;
	}
	
	public static final float applyArmor(LivingEntity target, float damage) {
            int i = 25 - target.getTotalArmorValue();
            float f = damage * (float)i;
            return f / 25.0F;
	}
	
	public static final boolean isEnchantable(ItemStack stack) {
		Item item = stack.getItem();
		if (NostrumItemTags.Items.InfusedGemVoid.contains(stack.getItem())) {
			return true;
		} else if (item instanceof EssenceItem && ((EssenceItem) stack.getItem()).getElement() != EMagicElement.PHYSICAL) {
			return true;
		}
		
		return false;
	}
	
	public SpellAction damage(EMagicElement element, float amount) {
		effects.add(new DamageEffect(element, amount));
		return this;
	}
	
	public SpellAction heal(float amount) {
		effects.add(new HealEffect(amount));
		return this;
	}
	
	public SpellAction status(Effect effect, int duration, int amplitude) {
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
	
	public SpellAction healFood(int level) {
		effects.add(new HealFoodEffect(4 * level));
		return this;
	}
	
	public SpellAction healMana(int level) {
		effects.add(new HealManaEffect(20 * level));
		return this;
	}
	
	public SpellAction swap() {
		effects.add(new SwapEffect());
		return this;
	}
	
	public SpellAction propel(int level) {
		effects.add(new PropelEffect(level));
		return this;
	}
	
	public SpellAction phase(int level) {
		effects.add(new PhaseEffect(level));
		return this;
	}
	
	public SpellAction enchant(EMagicElement element, int level) {
		effects.add(new EnchantEffect(element, level));
		return this;
	}
	
	public SpellAction grow(int level) {
		effects.add(new GrowEffect((int) Math.pow(2, level - 1)));
		return this;
	}
	
	public SpellAction wall(int level) {
//		Do different things in the block based on level.
//		Spawn three different types of it.
//		1) just regular block
//		2) Only players can go through it
//		3) Only the caster can go through it
		effects.add(new WallEffect(level));
		return this;
	}
	
	public SpellAction geoblock(int level) {
		effects.add(new GeoBlock(level));
		return this;
	}
	
	public SpellAction cursedIce(int level) {
		effects.add(new CursedIce(level));
		return this;
	}
	
//	public SpellAction infuse(EMagicElement element, int level) {
//		effects.add(new InfuseEffect(element, level));
//		return this;
//	}
	
	public SpellAction blockBreak(int level) {
		effects.add(new BreakEffect(level));
		return this;
	}
	
	public SpellAction name(String key) {
		this.nameKey = key;
		return this;
	}
	
	public String getName() {
		return this.nameKey;
	}
}
