package com.smanzana.nostrummagica.spell.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.block.Candle;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.effect.ElementalEnchantEffect;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.IEnchantableEntity;
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
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.item.IEnchantableItem;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.item.armor.MagicArmor;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellActionSummary;
import com.smanzana.nostrummagica.spell.component.Transmutation.TransmuteResult;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.HarvestUtil;
import com.smanzana.nostrummagica.util.ItemStacks;
import com.smanzana.nostrummagica.util.HarvestUtil.ITreeWalker;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
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
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolType;

public class SpellAction {
	
	public static class SpellActionProperties {
		public final boolean isHarmful;
		public final boolean affectsEntity;
		public final boolean affectsBlock;
		
		protected SpellActionProperties(SpellAction action) {
			boolean isHarmful = false;
			boolean affectsEntity = false;
			boolean affectsBlock = false;
			
			for (SpellEffect effect : action.effects) {
				if (!isHarmful && effect.isHarmful()) {
					isHarmful = true;
				}
				
				if (!affectsEntity && effect.affectsEntities()) {
					affectsEntity = true;
				}
				
				if (!affectsBlock && effect.affectsBlocks()) {
					affectsBlock = true;
				}
			}
			
			
			this.isHarmful = isHarmful;
			this.affectsEntity = affectsEntity;
			this.affectsBlock = affectsBlock;
		}
	}
	
	public static class SpellActionResult {
		private static final SpellActionResult FAIL = new SpellActionResult();
		
		public boolean applied;
		public float damage;
		public float heals;
		
		private SpellActionResult() {
			this.applied = false;
			this.damage = 0f;
			this.heals = 0f;
		}
	}
	
	private static interface SpellEffect {
		public void apply(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder);
		public void apply(LivingEntity caster, World world, BlockPos block, float eff, SpellActionResult resultBuilder);
		
		public default boolean isHarmful() {
			return false;
		}
		
		public boolean affectsEntities();
		public boolean affectsBlocks();
	}
	
	private static abstract class NegativeSpellEffect implements SpellEffect {
		
		@Override
		public boolean isHarmful() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
		
		protected abstract void applyEffect(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder);
		
		public final void apply(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder) {
			if (entity != null && isHarmful() && caster != entity) {
				if (PetFuncs.GetOwner(entity) != null && caster.equals(PetFuncs.GetOwner(entity))) {
					return; // we own the target entity
				}
				
				if (PetFuncs.GetOwner(caster) != null && entity.equals(PetFuncs.GetOwner(caster))) {
					return; // they own us
				}
			}
			
			applyEffect(caster, entity, eff, resultBuilder);
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
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			float fin = calcDamage(caster, entity, amount * efficiency, element);
			caster.setLastAttackedEntity(entity);
			entity.setRevengeTarget(caster);
			//entity.setHealth(Math.max(0f, entity.getHealth() - fin));
			entity.hurtResistantTime = 0;
			entity.attackEntityFrom(new MagicDamageSource(caster, element), fin);
			
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
			resultBuilder.applied |= true;
			resultBuilder.damage += fin;
		}
		
		@Override
		public void apply(LivingEntity caster, World world, BlockPos pos, float efficiency, SpellActionResult resultBuilder) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
	}
	
	private class HealEffect implements SpellEffect {
		private float amount;
		
		public HealEffect(float amount) {
			this.amount = amount;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			float base = this.amount;
			if (NostrumMagica.getMagicWrapper(caster).hasSkill(NostrumSkills.Ice_Master)) {
				base *= 2;
			}
			
			if (entity.isEntityUndead()) {
				caster.setLastAttackedEntity(entity);
				entity.setRevengeTarget(caster);
				//entity.setHealth(Math.max(0f, entity.getHealth() - fin));
				entity.hurtResistantTime = 0;
				entity.attackEntityFrom(new MagicDamageSource(caster, EMagicElement.ICE), base * efficiency);
				resultBuilder.damage += base * efficiency;
			} else {
				entity.heal(base * efficiency);
				if (entity instanceof EntityTameDragonRed) {
					EntityTameDragonRed dragon = (EntityTameDragonRed) entity;
					if (dragon.isTamed() && dragon.getOwner() == caster) {
						dragon.addBond(1f);
					}
				} else if (entity instanceof EntityArcaneWolf) {
					EntityArcaneWolf wolf = (EntityArcaneWolf) entity;
					if (wolf.isTamed() && wolf.getOwner() == caster) {
						wolf.addBond(1f);
					}
				}
				resultBuilder.heals += base * efficiency;
				
				if (NostrumMagica.getMagicWrapper(caster).hasSkill(NostrumSkills.Ice_Adept)) {
					if (NostrumMagica.rand.nextBoolean()) {
						entity.addPotionEffect(new EffectInstance(NostrumEffects.magicShield, (int)((20 * 15) * efficiency), 0));
					}
				}
			}
			
			NostrumMagicaSounds.STATUS_BUFF2.play(entity);
			resultBuilder.applied |= true;
		}
		
		@Override
		public void apply(LivingEntity caster, World world, BlockPos pos, float efficiency, SpellActionResult resultBuilder) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private class HealFoodEffect implements SpellEffect {
		private int amount;
		
		public HealFoodEffect(int amount) {
			this.amount = amount;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			if (entity instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) entity;
				player.getFoodStats().addStats((int) (amount * efficiency), 2);
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				resultBuilder.applied |= true;
				return;
			} else if (entity instanceof AnimalEntity && caster != null && 
					caster instanceof PlayerEntity) {
				((AnimalEntity) entity)
					.setInLove((PlayerEntity) caster);
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				resultBuilder.applied |= true;
				return;
			} else {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
				return;
			}
			
		}
		
		@Override
		public void apply(LivingEntity caster, World world, BlockPos pos, float efficiency, SpellActionResult resultBuilder) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private class HealManaEffect implements SpellEffect {
		private int amount;
		
		public HealManaEffect(int amount) {
			this.amount = amount;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			INostrumMagic magic = NostrumMagica.getMagicWrapper(entity);
			if (magic == null) {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
				return;
			} else {
				magic.addMana((int) (amount * efficiency));
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				resultBuilder.applied |= true;
				return;
			}
				
		}
		
		@Override
		public void apply(LivingEntity caster, World world, BlockPos pos, float efficiency, SpellActionResult resultBuilder) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
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
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			entity.addPotionEffect(new EffectInstance(effect, (int) (duration * efficiency), amp));
			
			if (effect.getEffectType() == EffectType.HARMFUL) {
				caster.setLastAttackedEntity(entity);
				entity.setRevengeTarget(caster);
				entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
				NostrumMagicaSounds.STATUS_DEBUFF2.play(entity);
			} else {
				NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			}
			resultBuilder.applied |= true;
			return;
		}
		
		@Override
		public void apply(LivingEntity caster, World world, BlockPos pos, float efficiency, SpellActionResult resultBuilder) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private class DispelEffect implements SpellEffect {
		private int number; // -1 to clear all
		
		public DispelEffect(int number) {
			this.number = number; 
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
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
			resultBuilder.applied |= true;
			return;
		}
		
		@Override
		public void apply(LivingEntity caster, World world, BlockPos pos, float efficiency, SpellActionResult resultBuilder) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private class BlinkEffect implements SpellEffect {
		private float dist;
		
		public BlinkEffect(float dist) {
			this.dist = dist;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			
			// Can be disabled via disruption effect
			EffectInstance effect = entity.getActivePotionEffect(NostrumEffects.disruption);
			if (effect != null && effect.getDuration() > 0) {
				if (effect.getAmplifier() > 0) {
					// Damage, too
					entity.attackEntityFrom(new MagicDamageSource(caster, EMagicElement.ENDER), effect.getAmplifier());
				}
				
				return;
			}
			
			if (caster != null && caster instanceof PlayerEntity) {
				// Look for lightning belt
				IInventory baubles = NostrumMagica.instance.curios.getCurios((PlayerEntity) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getSizeInventory(); i++) {
						ItemStack stack = baubles.getStackInSlot(i);
						if (stack.isEmpty() || stack.getItem() != NostrumCurios.enderBelt) {
							continue;
						}
						
						efficiency *= 2;
						break;
					}
				}
			}
			
			if (MagicArmor.GetSetCount(entity, EMagicElement.ENDER, MagicArmor.Type.TRUE) == 4) {
				// has full ender set
				efficiency *= 2;
			}
			
			// Apply efficiency bonus
			float dist = this.dist * efficiency;
			
			Vector3d dest;
			Vector3d direction = entity.getLookVec().normalize();
			Vector3d source = entity.getPositionVec();
			source = source.add(0, entity.getEyeHeight(), 0);
			BlockPos bpos;
			Vector3d translation = new Vector3d(direction.x * dist,
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
				Vector3d endpoint = dest;
				dest = null;
				Vector3d from;
				double curDist;
				while (i >= 0) {
					if (i == 0) {
						// optimization
						from = source;
					} else {
						curDist = (.2 * i);
						from = new Vector3d(translation.x * curDist,
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
			

			resultBuilder.applied |= dest != null;
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
		public void apply(LivingEntity caster, World world, BlockPos pos, float efficiency, SpellActionResult resultBuilder) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
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
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			apply(caster, entity.world, entity.getPosition(), efficiency, resultBuilder);
		}
		
		@Override
		public void apply(LivingEntity caster, World world, BlockPos pos, float efficiency, SpellActionResult resultBuilder) {

			// We abs the amp here, but check it belwo for pull and negate vector
			float magnitude = .35f * (Math.abs(amp) + 1.0f) * (float) Math.min(2.0f, Math.max(0.0f, 1.0f + Math.log(efficiency)));
			Vector3d center = new Vector3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
			NostrumMagicaSounds.DAMAGE_WIND.play(world, center.x, center.y, center.z);
			
			boolean any = false;
			for (Entity e : world.getEntitiesWithinAABBExcludingEntity(null, 
					new AxisAlignedBB(center.x - range, center.y - range, center.z - range, center.x + range, center.y + range, center.z + range)
					)) {
				double dist = e.getPositionVec().distanceTo(center); 
				if (dist <= range) {
					
					// If push, straight magnitude
					// If pull, cap magnitude so that it doesn't fly past player
					
					Vector3d force;
					Vector3d direction = e.getPositionVec().add(0, e.getEyeHeight(), 0).subtract(center).normalize();
					force = new Vector3d(
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
							force = new Vector3d(
									force.x * mod,
									force.y * mod,
									force.z * mod
									);
						}

						force = new Vector3d(
								force.x * -1.0,
								force.y * -1.0,
								force.z * -1.0);
					}
					
					e.addVelocity(force.x, force.y, force.z);
					any = true;
				}
			}
			
			resultBuilder.applied |= any;
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
		}
	}
	
	private static class TransmuteEffect implements SpellEffect {
		
		private int level;
		
		public TransmuteEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			ItemStack inhand = entity.getHeldItemMainhand();
			boolean offhand = false;
			if (inhand.isEmpty() || inhand.getItem() instanceof SpellScroll) {
				inhand = entity.getHeldItemOffhand();
				offhand = true;
			}
			
			if (inhand.isEmpty())
				return;
			
			Item item = inhand.getItem();
			TransmuteResult<Item> result = Transmutation.GetTransmutationResult(item, level);
			
			if (!result.valid) {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
				return;
			}
			
			ItemStack stack = new ItemStack(result.output);
			NostrumMagicaSounds.CAST_CONTINUE.play(entity);
			
			// Award knowledge
			INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			attr.giveTransmuteKnowledge(result.source.getName(), level);
			if (caster instanceof ServerPlayerEntity) {
				NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) caster);
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

			resultBuilder.applied |= true;
		}
		
		@Override
		public void apply(LivingEntity caster, World world, BlockPos pos, float efficiency, SpellActionResult resultBuilder) {
			Block block = world.getBlockState(pos).getBlock();
			TransmuteResult<Block> result = Transmutation.GetTransmutationResult(block, level);
			if (!result.valid) {
				NostrumMagicaSounds.CAST_FAIL.play(world, pos.getX() + .5, pos.getY(), pos.getZ() + .5);
				return;
			}
			
			NostrumMagicaSounds.CAST_CONTINUE.play(world, pos.getX() + .5, pos.getY(), pos.getZ() + .5);

			// Award knowledge
			INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			attr.giveTransmuteKnowledge(result.source.getName(), level);
			
			if (caster instanceof ServerPlayerEntity) {
				NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) caster);
			}
			
			world.setBlockState(pos, result.output.getDefaultState());
			resultBuilder.applied |= true;
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class BurnEffect extends NegativeSpellEffect {

		private int duration;
		
		public BurnEffect(int duration) {
			this.duration = duration;
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			int duration = (int) (this.duration * efficiency);
			if (duration == 0)
				return; // Nope
			
			NostrumMagicaSounds.DAMAGE_FIRE.play(entity);
			
			caster.setLastAttackedEntity(entity);
			entity.setRevengeTarget(caster);
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
			
			entity.setFire((int) Math.ceil((float) duration / 20.0f));
			resultBuilder.applied |= true;
		}

		@Override
		public void apply(LivingEntity caster, World world, BlockPos block, float efficiency, SpellActionResult resultBuilder) {
			BlockState state = world.getBlockState(block);
			if (state != null && state.getBlock() instanceof Candle) {
				Candle.light(world, block, state);
				NostrumMagicaSounds.DAMAGE_FIRE.play(world,
						block.getX() + .5, block.getY(), block.getZ() + .5);
				resultBuilder.applied |= true;
			}
			
			if (DimensionUtils.IsSorceryDim(world)) {
				return;
			}
			
			if (!world.isAirBlock(block))
				block.add(0, 1, 0);
			if (world.isAirBlock(block)) {
				world.setBlockState(block, Blocks.FIRE.getDefaultState());
				NostrumMagicaSounds.DAMAGE_FIRE.play(world,
						block.getX() + .5, block.getY(), block.getZ() + .5);
				resultBuilder.applied |= true;
			}
			return;
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class LightningEffect extends NegativeSpellEffect {
		
		public LightningEffect() {
			
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			entity.setRevengeTarget(caster);
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
			apply(caster, entity.world, entity.getPosition(), efficiency, resultBuilder);
		}

		@Override
		public void apply(LivingEntity caster, World world, BlockPos block, float efficiency, SpellActionResult resultBuilder) {
			
			int count = 1;
			
			if (caster != null && caster instanceof PlayerEntity) {
				// Look for lightning belt
				IInventory baubles = NostrumMagica.instance.curios.getCurios((PlayerEntity) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getSizeInventory(); i++) {
						ItemStack stack = baubles.getStackInSlot(i);
						if (stack.isEmpty() || stack.getItem() != NostrumCurios.lightningBelt) {
							continue;
						}
						
						count = caster.getRNG().nextInt(3) + 3;
						break;
					}
				}
			}
			
			BlockPos.Mutable cursor = new BlockPos.Mutable().setPos(block);
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
				
				((ServerWorld) world).addEntity(
					(new NostrumTameLightning(NostrumEntityTypes.tameLightning, world, cursor.getX() + 0.5, cursor.getY(), cursor.getZ() + 0.5))
					.setEntityToIgnore(caster)
					);
			}

			resultBuilder.applied |= true;
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
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
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
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
			
			apply(caster, world, pos, efficiency, resultBuilder);
		}

		@Override
		public void apply(LivingEntity caster, World world, BlockPos block, float efficiency, SpellActionResult resultBuilder) {
			
			// For non-player entities, just spawn some new golems.
			// Else spawn player-bound golems
			if (caster instanceof PlayerEntity) {
				if (NostrumMagica.getMagicWrapper(caster) == null) {
					return;
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
					public boolean tick(LivingEntity entityIn, Runnable onComplete) {
						// heh snekky
						boolean ret = super.tick(entityIn, onComplete);
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
			resultBuilder.applied |= true;
			
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
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
		}
	}
	
	private static class SwapEffect extends NegativeSpellEffect {
		
		public SwapEffect() {
			
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			if (caster == null || entity == null)
				return;
			
			// Can be disabled via disruption effect
			EffectInstance effect = entity.getActivePotionEffect(NostrumEffects.disruption);
			if (effect != null && effect.getDuration() > 0) {
				if (effect.getAmplifier() > 0) {
					// Damage, too
					entity.attackEntityFrom(new MagicDamageSource(caster, EMagicElement.ENDER), effect.getAmplifier());
				}
				return;
			}
			
			effect = caster.getActivePotionEffect(NostrumEffects.disruption);
			if (effect != null && effect.getDuration() > 0) {
				if (effect.getAmplifier() > 0) {
					// Damage, too
					caster.attackEntityFrom(new MagicDamageSource(caster, EMagicElement.ENDER), effect.getAmplifier());
				}
				return;
			}
			
			Vector3d pos = caster.getPositionVec();
			float pitch = caster.rotationPitch;
			float yaw = caster.rotationYawHead;
			
			if (caster instanceof PlayerEntity) {
				caster.setPositionAndRotation(caster.getPosX(), caster.getPosY(), caster.getPosZ(), entity.rotationYawHead, entity.rotationPitch);
				caster.setPositionAndUpdate(
						entity.getPosX(), entity.getPosY(), entity.getPosZ());
			} else {
				caster.setPositionAndRotation(
						entity.getPosX(), entity.getPosY(), entity.getPosZ(),
						entity.rotationPitch, entity.rotationYawHead
						);
			}
			
			if (entity instanceof PlayerEntity) {
				entity.setPositionAndRotation(entity.getPosX(), entity.getPosY(), entity.getPosZ(), yaw, pitch);
				entity.setPositionAndUpdate(pos.x, pos.y, pos.z);
			} else {
				entity.setPositionAndRotation(pos.x, pos.y, pos.z, yaw, pitch);
			}
			
			entity.fallDistance = 0;
			caster.fallDistance = 0;
			resultBuilder.applied |= true;			
		}
		
		protected BlockPos adjustPosition(World world, BlockPos pos) {
			// Try to avoid putting people in walls or in the flow
			if (world.isAirBlock(pos)) {
				return pos;
			}
			
			for (BlockPos attempt : new BlockPos[] {
				pos.up(), pos.north(), pos.east(), pos.south(), pos.west(), pos.down() 
			}) {
				if (world.isAirBlock(attempt)) {
					return attempt;
				}
			}
			
			// Is air block and all a round is, too. Go with original?
			return pos;
		}
		
		@Override
		public void apply(LivingEntity caster, World world, BlockPos pos, float efficiency, SpellActionResult resultBuilder) {
			// Can be disabled via disruption effect
			EffectInstance effect = caster.getActivePotionEffect(NostrumEffects.disruption);
			if (effect != null && effect.getDuration() > 0) {
				if (effect.getAmplifier() > 0) {
					// Damage, too
					caster.attackEntityFrom(new MagicDamageSource(caster, EMagicElement.ENDER), effect.getAmplifier());
				}
				return;
			}
			
			pos = adjustPosition(world, pos);
			caster.setPositionAndUpdate(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
			caster.fallDistance = 0;
			resultBuilder.applied |= true;
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class PropelEffect implements SpellEffect {
		
		int level;
		
		public PropelEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {

			Vector3d force = entity.getLookVec().add(0, 0.15, 0).normalize();
			float scale = 1f * (.5f * (level + 1)) * (float) (Math.max(0.0, Math.min(2.0, 1.0 - Math.log(efficiency))));
			
			force = new Vector3d(force.x * scale, force.y * scale, force.z * scale);
			
			entity.setMotion(entity.getMotion().add(force.x, force.y, force.z));
			entity.velocityChanged = true;
			
			NostrumMagicaSounds.DAMAGE_WIND.play(entity);
			resultBuilder.applied |= true;
		}
		
		@Override
		public void apply(LivingEntity caster, World world, BlockPos pos, float efficiency, SpellActionResult resultBuilder) {
			return; // Doesn't mean anything
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class PhaseEffect extends NegativeSpellEffect {
		
		private int level;
		
		public PhaseEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			// Can be disabled via disruption effect
			EffectInstance effect = entity.getActivePotionEffect(NostrumEffects.disruption);
			if (effect != null && effect.getDuration() > 0) {
				if (effect.getAmplifier() > 0) {
					// Damage, too
					entity.attackEntityFrom(new MagicDamageSource(caster, EMagicElement.ENDER), effect.getAmplifier());
				}
				return;
			}
			
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
						if (stack.isEmpty() || stack.getItem() != NostrumCurios.enderBelt) {
							continue;
						}
						
						radius *= 2.0;
						break;
					}
				}
			}
			
			if (MagicArmor.GetSetCount(entity, EMagicElement.ENDER, MagicArmor.Type.TRUE) == 4) {
				// has full ender set
				radius *= 2.0;
			}
			
			NostrumMagicaSounds.DAMAGE_ENDER.play(entity);
			
			for (int i = 0; i < 20; i++) {
			
				// Find a random place to teleport
		        double x = entity.getPosX() + (NostrumMagica.rand.nextDouble() - 0.5D) * radius;
		        double y = entity.getPosY() + (double)(NostrumMagica.rand.nextInt((int) radius) - (int) radius / 2.0);
		        double z = entity.getPosZ() + (NostrumMagica.rand.nextDouble() - 0.5D) * radius;
	
			    // Try to teleport
		        if (entity.attemptTeleport(x, y, z, false))
		        	break;
			}
			resultBuilder.applied |= true;
		}

		@Override
		public void apply(LivingEntity caster, World world, BlockPos block, float efficiency, SpellActionResult resultBuilder) {
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
			resultBuilder.applied |= true;
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
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
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			// If entity is special enchantable entity, try to use that
			if (entity instanceof IEnchantableEntity && ((IEnchantableEntity) entity).canEnchant(entity, element, level)) {
				resultBuilder.applied |= ((IEnchantableEntity) entity).attemptEnchant(entity, element, level);
			}
			
			// Different effect if non-player casts: just give magic buff
			if (!(caster instanceof PlayerEntity)) {
				int count = level + 1;
				double amt = 2 + level;
				caster.removeActivePotionEffect(NostrumEffects.magicBuff);
				NostrumMagica.magicEffectProxy.applyMagicBuff(entity, element, amt, count);
				entity.addPotionEffect(new EffectInstance(NostrumEffects.magicBuff, 60 * 20, 0));
				resultBuilder.applied |= true;
			}
			
			ItemStack inhand = entity.getHeldItemMainhand();
			boolean offhand = false;
			if (!isEnchantable(inhand)) {
				inhand = entity.getHeldItemOffhand();
				offhand = true;
			}
			
			ItemStack addedItem = ItemStack.EMPTY;
			boolean didEmpower = false;
			boolean consumeInput = false;
			
			// Main hand attempt
			if (!inhand.isEmpty()) {
				Item item = inhand.getItem();
				if (item instanceof IEnchantableItem) {
					IEnchantableItem.Result result = ((IEnchantableItem) item).attemptEnchant(inhand, entity, element, level);
					didEmpower = result.success;
					addedItem = result.resultItem;
					consumeInput = result.consumeInput;
				}
//				else if (NostrumTags.Items.InfusedGemVoid.contains(item)) {
//					int count = (int) Math.pow(2, level - 1);
//					addedItem = InfusedGemItem.getGem(element, count);
//				} else if (item instanceof EssenceItem) {
//					int count = level + 1;
//					double amt = 2 + level;
//					didEmpower = true;
//					caster.removeActivePotionEffect(NostrumEffects.magicBuff);
//					NostrumMagica.magicEffectProxy.applyMagicBuff(entity, element, amt, count);
//					entity.addPotionEffect(new EffectInstance(NostrumEffects.magicBuff, 60 * 20, 0));
//				}
			}
			
			if (addedItem.isEmpty() && !didEmpower) {
				//NostrumMagicaSounds.CAST_FAIL.play(entity);
			} else {
				if (entity instanceof PlayerEntity) {
					PlayerEntity p = (PlayerEntity) entity;
					
					if (consumeInput) {
						if (inhand.getCount() == 1) {
							if (offhand) {
								p.inventory.removeStackFromSlot(40);
							} else {
								p.inventory.removeStackFromSlot(p.inventory.currentItem);
							}
						} else {
							inhand.split(1);
						}
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
			
			// Apply enchant effect
			entity.addPotionEffect(new EffectInstance(ElementalEnchantEffect.GetForElement(this.element), (int) (20 * 15 * efficiency), level-1));
			resultBuilder.applied |= true;
			
		}

		@Override
		public void apply(LivingEntity caster, World world, BlockPos block, float efficiency, SpellActionResult resultBuilder) {
			return;
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class GrowEffect implements SpellEffect {
		
		private int count;
		
		public GrowEffect(int count) {
			this.count = count;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			if (entity instanceof AnimalEntity) {
				AnimalEntity animal = (AnimalEntity) entity;
				animal.addGrowth((int) (count * 500 * efficiency));
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				resultBuilder.applied |= true;
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		public void apply(LivingEntity caster, World world, BlockPos block, float efficiency, SpellActionResult resultBuilder) {
			if (world.isAirBlock(block)) {
				block = block.add(0, -1, 0);
			}
			
			// Since farmland is smaller than a block, standing on it means the block below you (at feet trigger) is
			// the block below the farmland. So try and step up if we're in that specific case.
			if (world.getBlockState(block.up()).getBlock() instanceof FarmlandBlock) {
				block = block.up();
			}
			
			if (world.getBlockState(block).getBlock() instanceof FarmlandBlock) {
				block = block.up();
			}
			
			if (world.isAirBlock(block)) {
				return;
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
			
			resultBuilder.applied |= worked;
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class BurnArmorEffect extends NegativeSpellEffect {
		
		private int level;
		
		public BurnArmorEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
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
			resultBuilder.applied |= true;
		}

		@Override
		public void apply(LivingEntity caster, World world, BlockPos block, float efficiency, SpellActionResult resultBuilder) {
			return;
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
		
	}
	
	private static class WallEffect implements SpellEffect {
		
		private int level;
		
		public WallEffect(int level) {
			this.level = level;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			apply(caster, entity.world, entity.getPosition().add(0, 1, 0), efficiency, resultBuilder);
		}

		@Override
		public void apply(LivingEntity caster, World world, BlockPos block, float efficiency, SpellActionResult resultBuilder) {
			if (!world.isAirBlock(block))
				block = block.add(0, 1, 0); // TODO don't move up one cause it messes up AoE effects and double-affects locations!
			
			if (!world.isAirBlock(block)) {
				NostrumMagicaSounds.CAST_FAIL.play(world, block.getX(), block.getY(), block.getZ());
			} else {
				NostrumMagicaSounds.DAMAGE_WIND.play(world, block.getX(), block.getY(), block.getZ());
				world.setBlockState(block, NostrumBlocks.magicWall.getState(level));
				resultBuilder.applied |= true;
			}
				
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
		}
		
	}

	private static class CursedIce implements SpellEffect {
		
		private int level;
		
		public CursedIce(int level) {
			this.level = level;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder) {
			apply(caster, entity.world, entity.getPosition().add(0, 1, 0), efficiency, resultBuilder);
		}

		@Override
		public void apply(LivingEntity caster, World world, BlockPos block, float efficiency, SpellActionResult resultBuilder) {
			world.setBlockState(block, NostrumBlocks.cursedIce.getState(level));
			NostrumMagicaSounds.DAMAGE_ICE.play(world, block.getX(), block.getY(), block.getZ());
			resultBuilder.applied |= true;
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
		}
	}
	
	private static class BreakEffect implements SpellEffect {

		private int level;
		
		public BreakEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder) {
			apply(caster, entity.world, entity.getPosition().add(0, -1, 0), eff, resultBuilder);
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
		public void apply(LivingEntity caster, World world, BlockPos block, float eff, SpellActionResult resultBuilder) {
			if (world.isAirBlock(block))
				return;
			
			if (DimensionUtils.IsSorceryDim(world)) {
				return;
			}
			
			BlockState state = world.getBlockState(block);
			if (state == null || state.getMaterial().isLiquid())
				return;
			
			boolean onlyStone = (level <= 1);
			if (onlyStone && caster instanceof PlayerEntity) {
				if (!Tags.Blocks.STONE.contains(state.getBlock())) {
					return;
				}
			}
			
			boolean usePickaxe = (level >= 3);
			float hardness = state.getBlockHardness(world, block);
			
			if (hardness >= 100f || hardness < 0f)
				return;
			
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
			
			resultBuilder.applied |= true;
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
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
	
	private static class HarvestEffect implements SpellEffect {

		// Level 1 harvests like normal.
		// Level 2+ harvests all connected.
		// Level 3 uses tool in the caster's hand
		private int level;
		
		public HarvestEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder) {
			apply(caster, entity.world, entity.getPosition().add(0, -1, 0), eff, resultBuilder);
		}
		
		protected boolean isTool(@Nullable PlayerEntity player, ItemStack stack) {
			if (stack.isEmpty()) {
				return false;
			}
			
			Set<ToolType> classes = stack.getItem().getToolTypes(stack);
			for (ToolType cla : classes) {
				// Required harvest level >= iron so throw-away levels like wood and stone don't count
				if (cla == ToolType.AXE
						&& stack.getItem().getHarvestLevel(stack, cla, player, null) >= 2) {
					return true;
				}
			}
			
			return false;
		}
		
		protected boolean harvestCropBlock(LivingEntity caster, World world, BlockPos block, @Nullable ItemStack tool) {
			return HarvestUtil.HarvestCrop(world, block);
		}
		
		protected boolean harvestCrop(LivingEntity caster, World world, BlockPos block, @Nullable ItemStack tool, Set<BlockPos> visitted) {
			if (visitted.contains(block)) {
				return false;
			}
			
			visitted.add(block);
			if (harvestCropBlock(caster, world, block, tool)) {
				// Try nearby crops now
				harvestCrop(caster, world, block.north(), tool, visitted);
				harvestCrop(caster, world, block.east(), tool, visitted);
				harvestCrop(caster, world, block.south(), tool, visitted);
				harvestCrop(caster, world, block.west(), tool, visitted);
				return true;
			} else {
				// Not crop or not fully grown
				return false;
			}
		}
		
		protected boolean harvestCrop(LivingEntity caster, World world, BlockPos block, @Nullable ItemStack tool, boolean doNearby) {
			if (doNearby) {
				return harvestCrop(caster, world, block, tool, new HashSet<>());
			} else {
				return harvestCropBlock(caster, world, block, tool);
			}
		}
		
		protected boolean harvestTreeBlock(LivingEntity caster, World world, BlockPos pos, @Nullable ItemStack tool) {
			if (tool != null) {
				((ServerPlayerEntity) caster).interactionManager.tryHarvestBlock(pos);
			} else {
				world.destroyBlock(pos, true);
			}
			return true;
		}
		
		protected boolean harvestTree(LivingEntity caster, World world, BlockPos pos, @Nullable ItemStack tool, boolean wholeTree) {
			final class LastNode {
				@Nullable BlockPos finalPos = null;
				int maxDepth = -1;
			};
			
			final LastNode node = new LastNode();
			final ITreeWalker walker;
			if (wholeTree) {
				walker = (walkWorld, walkPos, depth, isLeaves) -> {
					// harvest all non-leaves as we visit them
					if (!isLeaves) {
						harvestTreeBlock(caster, walkWorld, walkPos, tool);
					}
					return true;
					// Note: not setting finalPos;
				};
			} else {
				walker = (walkWorld, walkPos, depth, isLeaves) -> {
					// Take furthest away, based on depth.
					// Note it's depth first, so this may not be right?
					if (!isLeaves && depth > node.maxDepth) {
						node.maxDepth = depth;
						node.finalPos = walkPos;
					}
					return true;
				};
			}
			
			boolean walked = HarvestUtil.WalkTree(world, pos, walker);
			
			if (node.finalPos != null) {
				harvestTreeBlock(caster, world, node.finalPos, tool);
			}
			
			return walked;
		}

		@Override
		public void apply(LivingEntity caster, World world, BlockPos block, float eff, SpellActionResult resultBuilder) {
			if (world.isAirBlock(block))
				return;
			
			if (DimensionUtils.IsSorceryDim(world)) {
				return;
			}
			
			BlockState state = world.getBlockState(block);
			if (state == null || state.getMaterial().isLiquid())
				return;
			
			float hardness = state.getBlockHardness(world, block);
			
			if (hardness >= 100f || hardness < 0f)
				return; // unbreakable?
			
			ItemStack tool = null;
			if (level >= 3 && caster instanceof ServerPlayerEntity) {
				tool = caster.getHeldItemMainhand();
				if (!isTool((PlayerEntity) caster, tool)) {
					tool = caster.getHeldItemOffhand();
				}
				if (!isTool((PlayerEntity) caster, tool)) {
					tool = null;
				}
			}
			
			final boolean spread = level >= 2;
			
			if (HarvestUtil.canHarvestCrop(state)) {
				resultBuilder.applied |= harvestCrop(caster, world, block, tool, spread);
			} else if (HarvestUtil.canHarvestTree(state)) {
				resultBuilder.applied |= harvestTree(caster, world, block, tool, spread);
			} else if (HarvestUtil.canHarvestCrop(world.getBlockState(block.up()))) {
				resultBuilder.applied |= harvestCrop(caster, world, block.up(), tool, spread);
			} else {
				;
			}
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
		}
		
	}
	
	private List<SpellEffect> effects;
	private String nameKey;
	
	public SpellAction() {
		effects = new ArrayList<>(2);
	}
	
	/**
	 * Applies the contained effect(s) on the provided entity at the given efficiency.
	 * Returns whether the entity was affected. 
	 * @param entity
	 * @param efficiency
	 * @return
	 */
	public SpellActionResult apply(LivingEntity source, LivingEntity entity, float efficiency) {
		if (entity.world.isRemote)
			return SpellActionResult.FAIL;
		
		final LivingEntity ent = entity;
		SpellActionResult result = new SpellActionResult();
		
		SpellActionSummary summary = new SpellActionSummary(this, efficiency);
		NostrumMagica.playerListener.onMagicEffect(entity, source, summary);
		if (!summary.wasCancelled()) {
			for (SpellEffect e : effects) {
				final SpellEffect effect = e;
				
				if (!entity.getServer().isOnExecutionThread()) { // TODO I think?
					throw new RuntimeException("Wrong thread for spell effects!");
				}
				//entity.getServer().runAsync(() -> {
				effect.apply(source, ent, summary.getEfficiency(), result);
				//});
			}
		}
		
		return result;
	}
	
	/**
	 * Apply the contained effect(s) at the provided location.
	 * Returns whether the location was affected
	 * @param world
	 * @param pos
	 * @param efficiency
	 * @return
	 */
	public SpellActionResult apply(LivingEntity source, World world, BlockPos pos, float efficiency) {
		if (world.isRemote)
			return SpellActionResult.FAIL;
		
		final World w = world;
		final BlockPos b = pos;
		
		SpellActionResult result = new SpellActionResult();
		for (SpellEffect e : effects) {
			final SpellEffect effect = e;
			
			if (!world.getServer().isOnExecutionThread()) {
				throw new RuntimeException("Wrong thread for spell effects!");
			}
			
			//world.getMinecraftServer().runAsync(() -> {
				effect.apply(source, w, b, efficiency, result);
			//});
		}
		
		return result;
	}
	
//	/**
//	 * Check and return whether any obviously harmful effects are in this action
//	 * @return
//	 */
//	public boolean isHarmful() {
//		for (SpellEffect e : effects) {
//			if (e instanceof NegativeSpellEffect && ((NegativeSpellEffect) e).isHarmful()) {
//				return true;
//			}
//		}
//		
//		return false;
//	}
	
	public SpellActionProperties getProperties() {
		return new SpellActionProperties(this);
	}
	
	protected static final float getPhysicalAttributeBonus(LivingEntity caster) {
		// Get raw amount
		if (!caster.getAttributeManager().hasAttributeInstance(Attributes.ATTACK_DAMAGE)) {
			return 0f;
		}
		
		double amt = caster.getAttributeValue(Attributes.ATTACK_DAMAGE);
		amt -= 1; // Players always have +1 attack
		
		// Reduce any from main-hand weapon, since that's given assuming it's used to attack
		ItemStack held = caster.getHeldItemMainhand();
		if (!held.isEmpty()) {
			final Multimap<Attribute, AttributeModifier> heldAttribs = held.getAttributeModifiers(EquipmentSlotType.MAINHAND);
			if (heldAttribs != null && heldAttribs.containsKey(Attributes.ATTACK_DAMAGE)) {
				double extra = 0;
				for (AttributeModifier mod : heldAttribs.get(Attributes.ATTACK_DAMAGE)) {
					extra += mod.getAmount();
				}
				
				// Note that the physical master skill includes using some of this
				if (NostrumMagica.getMagicWrapper(caster).hasSkill(NostrumSkills.Physical_Master)) {
					amt -= (int) ((float) extra * .8f);
				} else {
					amt -= extra;
				}
			}
		}
		
		return (float) amt;
	}
	
	public static final float calcDamage(LivingEntity caster, LivingEntity target, float base, EMagicElement element) {
		float amt = 0f;
		
		if (target == null)
			return amt;
		
		INostrumMagic magic = NostrumMagica.getMagicWrapper(caster);
		System.out.println("Damage base: " + base);
		
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
			base += getPhysicalAttributeBonus(caster);
			base = applyArmor(target, base);
			// Physical is reduced by real armor but not affected by magic resist effects and attributes.
			// It still gains power from magic boost (above) AND the strength status effect/attack attribute AND is still reduces with magic reduction (below).
		} else {
		
			final int armor = target.getTotalArmorValue();
			final boolean undead = target.isEntityUndead();
			final boolean ender;
			final boolean light;
			final boolean flamy;
			
			if (target instanceof EndermanEntity || target instanceof EndermiteEntity
					|| target instanceof EntityDragon) {
				// Ender status and immunity can be turned off with the disrupt status effect
				EffectInstance effect = target.getActivePotionEffect(NostrumEffects.disruption);
				if (effect != null && effect.getDuration() > 0) {
					ender = false;
				} else {
					ender = true;
				}
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
			
			if (element == EMagicElement.FIRE && magic.hasSkill(NostrumSkills.Fire_Adept)) {
				base += 2;
			}
			
			EffectInstance resEffect = target.getActivePotionEffect(NostrumEffects.magicResist);
			if (resEffect != null) {
				base *= Math.pow(.75, resEffect.getAmplifier() + 1);
			}
			
			ModifiableAttributeInstance attr = target.getAttribute(NostrumAttributes.magicResist);
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
		ModifiableAttributeInstance attr = target.getAttribute(NostrumAttributes.GetReduceAttribute(element));
		if (attr != null && attr.getValue() != 0.0D) {
			base -= attr.getValue();
		}
		
		System.out.println("Calculated damage: " + base);
		return base;
	}
	
	public static final float applyArmor(LivingEntity target, float damage) {
            int i = 25 - target.getTotalArmorValue();
            float f = damage * (float)i;
            return f / 25.0F;
	}
	
	public static final boolean isEnchantable(ItemStack stack) {
		Item item = stack.getItem();
//		if (NostrumTags.Items.InfusedGemVoid.contains(stack.getItem())) {
//			return true;
//		} else if (item instanceof EssenceItem && ((EssenceItem) stack.getItem()).getElement() != EMagicElement.PHYSICAL) {
//			return true;
//		}
//		
//		return false;
		return !stack.isEmpty() && item instanceof IEnchantableItem && ((IEnchantableItem) item).canEnchant(stack);
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
	
//	public SpellAction geoblock(int level) {
//		effects.add(new GeoBlock(level));
//		return this;
//	}
	
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
	
	public SpellAction harvest(int level) {
		effects.add(new HarvestEffect(level));
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
