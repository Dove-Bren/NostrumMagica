package com.smanzana.nostrummagica.items;

import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams.TargetBehavior;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.entity.EntityAreaEffect;
import com.smanzana.nostrummagica.entity.EntityAreaEffect.IAreaEntityEffect;
import com.smanzana.nostrummagica.entity.EntityAreaEffect.IAreaLocationEffect;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.NostrumTameLightning;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.MagicDamageSource;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.utils.ItemStacks;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.SwordItem;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class AspectedWeapon extends SwordItem implements IReactiveEquipment {
	
	public static enum Type {
		NOVICE,
		ADEPT,
		MASTER;
		
		public final @Nullable Type getNext() {
			switch (this) {
			case NOVICE:
				return ADEPT;
			case ADEPT:
				return MASTER;
			case MASTER:
				return null;
			}
			
			return null;
		}
		
		public final @Nullable Type getPrev() {
			switch (this) {
			case NOVICE:
				return null;
			case ADEPT:
				return NOVICE;
			case MASTER:
				return ADEPT;
			}
			
			return null;
		}
	}
	
	protected static final String ID_PREFIX = "sword_";
	protected static final String ID_SUFFIX_NOVICE = "novice";
	protected static final String ID_SUFFIX_ADEPT = "adept";
	protected static final String ID_SUFFIX_MASTER = "master";
	
	public static final String ID_ICE_NOVICE = ID_PREFIX + "ice_" + ID_SUFFIX_NOVICE;
	public static final String ID_ICE_ADEPT = ID_PREFIX + "ice_" + ID_SUFFIX_ADEPT;
	public static final String ID_ICE_MASTER = ID_PREFIX + "ice_" + ID_SUFFIX_MASTER;
	
	public static final String ID_WIND_NOVICE = ID_PREFIX + "wind_" + ID_SUFFIX_NOVICE;
	public static final String ID_WIND_ADEPT = ID_PREFIX + "wind_" + ID_SUFFIX_ADEPT;
	public static final String ID_WIND_MASTER = ID_PREFIX + "wind_" + ID_SUFFIX_MASTER;
	
	public static final String ID_LIGHTNING_NOVICE = ID_PREFIX + "lightning_" + ID_SUFFIX_NOVICE;
	public static final String ID_LIGHTNING_ADEPT = ID_PREFIX + "lightning_" + ID_SUFFIX_ADEPT;
	public static final String ID_LIGHTNING_MASTER = ID_PREFIX + "lightning_" + ID_SUFFIX_MASTER;
	

	public static boolean isWeaponElement(EMagicElement element) {
		switch (element) {
		case EARTH:
		case ENDER:
		case FIRE:
		case PHYSICAL:
		default:
			return false;
		case WIND:
		case ICE:
		case LIGHTNING:
			return true;
		}
	}
	
	private static int calcDamage(EMagicElement element, Type type) {
		
		float mod;
		
		switch (element) {
		case WIND:
			mod = 1.2f;
			break;
		case LIGHTNING:
			mod = 0.9f;
			break;
		case ICE:
			mod = 1.1f;
			break;
		default:
			mod = 1.0f;
		}
		
		int bonus = 0;
		switch (type) {
		case NOVICE:
			bonus = 1;
			break;
		case ADEPT:
			bonus = 2;
			break;
		case MASTER:
			bonus = 3;
			break;
		}
		
		int base = 5 + (bonus * 2);
		
		return (int) ((float) base * mod);
	}
	
	private static float calcSwingSpeed(EMagicElement element, Type type) {
		final float amt;
		switch (element) {
		case ICE:
			if (type == Type.NOVICE) {
				amt = 2.6f; // Mace only slightly slower than sword
			} else if (type == Type.ADEPT) {
				amt = 3.3f; // Morning star slower than vanilla axe!
			} else {
				amt = 2.8f; // Scepter slower than mace, but not TOO slow
			}
			break;
		case LIGHTNING:
			if (type == Type.NOVICE) {
				amt = 1.5f; // Knife is very fast! 2.5 attacks per second (vanilla sword is 1.6)
			} else if (type == Type.ADEPT) {
				amt = 2.0f; // Dagger slower, but still faster than sword
			} else {
				amt = 1.6f; // Stiletto very fast! Lightning fast, even! lol
			}
			break;
		case WIND:
			if (type == Type.NOVICE) {
				amt = 2.4f; // Slasher is same as vanilla sword. Heavier, but powered by wind!
			} else if (type == Type.ADEPT) {
				amt = 2.6f; // Slave is a tad slower
			} else {
				amt = 2.1f; // Striker is very fast :)
			}
			break;
		default:
			amt = 2.4f;
		}
		
		return -amt;
	}
	
	private static int calcDurability(EMagicElement element, Type type) {
		int durability = 250; // Base
		switch (type) {
		case MASTER:
			durability *= 2;
			// fall through
		case ADEPT:
			durability *= 2;
			// fall through
		case NOVICE:
			; // No bonus above base
			// fall through
		default:
			break;
		}
		return durability;//200;
	}
	
	private static int typeScale(Type type) {
		switch (type) {
		case NOVICE:
			return 1;
		case ADEPT:
			return 2;
		case MASTER:
			return 3;
		}
		
		return 1;
	}
	
	protected static final UUID OFFHAND_ATTACK_SPEED_MODIFIER = UUID.fromString("B2879ABC-4180-1234-B01B-487954A3BAC4");
	
	private Type type;
	private EMagicElement element;
	
	public AspectedWeapon(EMagicElement element, Type type) {
		super(ItemTier.DIAMOND,
				(int) (calcDamage(element, type) - ItemTier.DIAMOND.getAttackDamage()), // Calc desired damage, and subtrace the amt diamond tier is gonna give
				calcSwingSpeed(element, type),
				NostrumItems.PropEquipment().maxDamage(calcDurability(element, type)));
		
		this.type = type;
		this.element = element;
	}
	
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot);

		if (slot == EquipmentSlotType.OFFHAND && element == EMagicElement.WIND) {
			double amt = typeScale(this.type)* .1;
			multimap.put(Attributes.ATTACK_SPEED.getName(), new AttributeModifier(OFFHAND_ATTACK_SPEED_MODIFIER, "Weapon modifier", amt, AttributeModifier.Operation.ADDITION));
		}

		return multimap;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

	@Override
	public SpellAction getTriggerAction(LivingEntity user, boolean offense, @Nonnull ItemStack stack) {
		if (!offense)
			return null;
		
		SpellAction action = null;
		switch (element) {
		case EARTH:
		case ENDER:
		case FIRE:
		case PHYSICAL:
			break;
		case WIND:
			// now just does on right click
//			if (NostrumMagica.rand.nextFloat() < 0.35f * level)
//				action = new SpellAction(user).push(5f, level);
			break;
		case LIGHTNING:
			if (NostrumMagica.rand.nextFloat() < 0.1f * typeScale(this.type))
				action = new SpellAction(user).lightning();
			break;
		case ICE:
			if (NostrumMagica.rand.nextFloat() < 0.5f * typeScale(this.type))
				action = new SpellAction(user).status(
						NostrumEffects.frostbite, 5 * 20, typeScale(this.type) > 2 ? typeScale(this.type) - 2 : 0);
			break;
		default:
			break;
		
		}
		
		return action;
	}

	@Override
	public boolean shouldTrigger(boolean offense, @Nonnull ItemStack stack) {
		return offense;
	}
	
	public static AspectedWeapon get(EMagicElement element, Type type) {
		AspectedWeapon item = null;
		
		switch (element) {
		case EARTH:
		case ENDER:
		case FIRE:
		case PHYSICAL:
			; // No weapon
			break;
		case ICE:
			switch (type) {
			case NOVICE:
				item = NostrumItems.enchantedWeaponIceNovice;
				break;
			case ADEPT:
				item = NostrumItems.enchantedWeaponIceAdept;
				break;
			case MASTER:
				item = NostrumItems.enchantedWeaponIceMaster;
				break;
			}
		case LIGHTNING:
			switch (type) {
			case NOVICE:
				item = NostrumItems.enchantedWeaponLightningNovice;
				break;
			case ADEPT:
				item = NostrumItems.enchantedWeaponLightningAdept;
				break;
			case MASTER:
				item = NostrumItems.enchantedWeaponLightningMaster;
				break;
			}
			break;
		case WIND:
			switch (type) {
			case NOVICE:
				item = NostrumItems.enchantedWeaponWindNovice;
				break;
			case ADEPT:
				item = NostrumItems.enchantedWeaponWindAdept;
				break;
			case MASTER:
				item = NostrumItems.enchantedWeaponWindMaster;
				break;
			}
			break;
		default:
			break;
		
		}
		
		return item;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		Vector3d dir = playerIn.getLookVec();
		dir = dir.add(0, -dir.y, 0).normalize();
		
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		
		if (element == EMagicElement.ICE) {
			
			if (playerIn.getCooledAttackStrength(0.5F) > .95) {
				
				if (!worldIn.isRemote) {
//					EntityAreaEffectCloud cloud = new EntityAreaEffect(worldIn, );
//	
//					dir = dir.scale(5f/(3f * 20f));
//					cloud.setOwner(playerIn);
//					cloud.setWaitTime(5);
//					cloud.setRadius(0.5f);
//					cloud.setRadiusPerTick((1f + level * .75f) / (20f * 3));
//					cloud.setDuration(20 * 3);
//					//cloud.setColor(0xFFFF0000);
//					//cloud.setParticle(ParticleTypes.SPELL);
//					cloud.addEffect(new PotionEffect(FrostbitePotion.instance(), 20 * 10));
//					worldIn.spawnEntityInWorld(cloud);
//					cloud.getMotion().x = dir.x;
//					cloud.getMotion().y = dir.y;
//					cloud.getMotion().z = dir.z;
					
					spawnIceCloud(worldIn, playerIn, new Vector3d(playerIn.getPosX() + dir.x, playerIn.getPosY() + .75, playerIn.getPosZ() + dir.z), dir, this.type);
					
					ItemStacks.damageItem(itemStackIn, playerIn, hand, 2);
				}
				
				playerIn.resetCooldown();
			}
			
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
		} else if (element == EMagicElement.WIND) {
			if (playerIn.getCooledAttackStrength(0.5F) > .95) {
				if (playerIn.isSneaking()) {
					if (!worldIn.isRemote) {
						spawnWalkingVortex(worldIn, playerIn, new Vector3d(playerIn.getPosX() + dir.x, playerIn.getPosY() + .75, playerIn.getPosZ() + dir.z), dir, this.type);
						ItemStacks.damageItem(itemStackIn, playerIn, hand, 1);
					}
					playerIn.resetCooldown();
					return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
				}
			}
			
		} else if (element == EMagicElement.LIGHTNING && MagicArmor.GetSetCount(playerIn, EMagicElement.LIGHTNING, MagicArmor.Type.TRUE) == 4) {
			if (playerIn.getCooledAttackStrength(0.5F) > .95) {
				// If full set, strike at targetting location (unless sneaking, then strike self)
				boolean used = false;
				if (playerIn.isSneaking()) {
					if (!worldIn.isRemote) {
						summonBoltOnSelf(playerIn);
					}
					used = true;
				} else if (playerIn.isPotionActive(NostrumEffects.lightningAttack)) {
					// This should be client-side... TODO do it on client and send via armor message?
					
					// Do quick mana check prior to actually doing raytrace. Redone inside helper func.
					INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
					if (attr != null && attr.getMana() >= 30) {
						if (!worldIn.isRemote) {
							final float maxDist = 50;
							RayTraceResult mop = RayTrace.raytrace(worldIn, playerIn, playerIn.getPositionVec().add(0, playerIn.getEyeHeight(), 0), playerIn.getLookVec(), maxDist, (ent) -> { return ent != playerIn;});
							if (mop != null && mop.getType() != RayTraceResult.Type.MISS) {
								final Vector3d at = (mop.getType() == RayTraceResult.Type.ENTITY ? RayTrace.entFromRaytrace(mop).getPositionVec() : mop.getHitVec());
								summonBoltAtTarget(playerIn, worldIn, at);
							}
						}
						used = true;
					}
				}
				if (used) {
					if (!worldIn.isRemote) {
						ItemStacks.damageItem(itemStackIn, playerIn, hand, 1);
					}
					playerIn.resetCooldown();
					return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
				}
			}
		}
			
        return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		final World worldIn = context.getWorld();
		final PlayerEntity playerIn = context.getPlayer();
		final BlockPos pos = context.getPos();
		Vector3d hitVec = context.getHitVec();
		ItemStack stack = context.getItem();
		final Hand hand = context.getHand();
		if (playerIn.getCooledAttackStrength(0.5F) > .95) {
			Vector3d dir = hitVec.subtract(playerIn.getPositionVec());
			dir = dir.add(0, -dir.y, 0);
			dir = dir.normalize();
			if (element == EMagicElement.WIND) {
				if (playerIn.isSneaking()) {
					if (!worldIn.isRemote) {
						spawnWalkingVortex(worldIn, playerIn, new Vector3d(playerIn.getPosX() + dir.x, playerIn.getPosY() + .75, playerIn.getPosZ() + dir.z), dir, this.type);
						ItemStacks.damageItem(stack, playerIn, hand, 1);
					}
					playerIn.resetCooldown();
					return ActionResultType.SUCCESS;
				}
			} else if (element == EMagicElement.ICE) {
				if (!worldIn.isRemote) { 
					spawnIceCloud(worldIn, playerIn, new Vector3d(pos.getX() + hitVec.x, pos.getY() + 1, pos.getZ() + hitVec.z), dir, this.type);
					ItemStacks.damageItem(stack, playerIn, hand, 2);
				}
				playerIn.resetCooldown();
				return ActionResultType.SUCCESS;
			} else if (element == EMagicElement.LIGHTNING && MagicArmor.GetSetCount(playerIn, EMagicElement.LIGHTNING, MagicArmor.Type.TRUE) == 4) {
				
				boolean used = false;
				if (playerIn.isSneaking()) {
					if (!worldIn.isRemote) {
						summonBoltOnSelf(playerIn);
					}
					used = true;
				} else if (playerIn.isPotionActive(NostrumEffects.lightningAttack)) {
					// This should be client-side... TODO do it on client and send via armor message?
					
					// Do quick mana check prior to actually doing raytrace. Redone inside helper func.
					INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
					if (attr != null && attr.getMana() >= 30) {
						if (!worldIn.isRemote) {
							final float maxDist = 50;
							RayTraceResult mop = RayTrace.raytrace(worldIn, playerIn, playerIn.getPositionVec().add(0, playerIn.getEyeHeight(), 0), playerIn.getLookVec(), maxDist, (ent) -> { return ent != playerIn;});
							if (mop != null && mop.getType() != RayTraceResult.Type.MISS) {
								final Vector3d at = (mop.getType() == RayTraceResult.Type.ENTITY ? RayTrace.entFromRaytrace(mop).getPositionVec() : mop.getHitVec());
								summonBoltAtTarget(playerIn, worldIn, at);
							}
						}
						used = true;
					}
				}
				if (used) {
					if (!worldIn.isRemote) {
						ItemStacks.damageItem(stack, playerIn, hand, 1);
					}
					playerIn.resetCooldown();
					return ActionResultType.SUCCESS;
				}
			}
		}
		return ActionResultType.PASS;
		
	}
	
	protected static void spawnIceCloud(World world, PlayerEntity caster, Vector3d at, Vector3d direction, Type weaponType) {
		direction = direction.scale(5f/(3f * 20f)); // 5 blocks over 3 seconds
		EntityAreaEffect cloud = new EntityAreaEffect(NostrumEntityTypes.areaEffect, world, at.x, at.y, at.z);
		cloud.setOwner(caster);
		cloud.setWaitTime(5);
		cloud.setRadius(0.5f);
		cloud.setRadiusPerTick((1f + typeScale(weaponType) * .75f) / (20f * 3)); // 1 (+ .75 per extra level) extra radius per 3 seconds
		cloud.setDuration((int) (20 * (3 + typeScale(weaponType) * .5f))); // 3 seconds + a half a second per extra level
		cloud.addEffect(new EffectInstance(NostrumEffects.frostbite, 20 * 10));
		cloud.addEffect((IAreaLocationEffect)(worldIn, pos) -> {
			BlockState state = worldIn.getBlockState(pos);
			if (state.getMaterial() == Material.WATER
					&& (state.getBlock() == Blocks.WATER)) {
				worldIn.setBlockState(pos, Blocks.ICE.getDefaultState());
			}
		});
		cloud.setVerticleStepping(true);
		cloud.setGravity(true, .1);
		//cloud.setWalksWater();
		world.addEntity(cloud);
		cloud.setMotion(direction);
	}
	
	protected static void spawnWalkingVortex(World world, PlayerEntity caster, Vector3d at, Vector3d direction, Type weaponType) {
		final int hurricaneCount = MagicArmor.GetSetCount(caster, EMagicElement.WIND, MagicArmor.Type.TRUE);
		direction = direction.scale(5f/(3f * 20f)); // 5 blocks over 10 seconds
		EntityAreaEffect cloud = new EntityAreaEffect(NostrumEntityTypes.areaEffect, world, at.x, at.y, at.z);
		cloud.setOwner(caster);
		cloud.setWaitTime(10);
		cloud.setRadius(.75f);
		//cloud.setRadiusPerTick((.25f + level * .5f) / (20f * 10));
		cloud.setDuration((int) (20 * (3 + typeScale(weaponType) * .5f))); // 3 seconds + a half a second per extra level
		cloud.addEffect((IAreaEntityEffect)(worldIn, entity) -> {
			if (entity.noClip || entity.hasNoGravity()) {
				return;
			}
			if (hurricaneCount == 4 && entity == caster) {
				// Get another tick (continue forever) from hurricane caster
				cloud.addTime(1, false);
				
				// Move in direction of player look
				Vector3d lookDir = Vector3d.fromPitchYaw(0, entity.rotationYaw).scale(5f/(3f * 20f)); // 5 blocks over 10 seconds;
				cloud.setWaddle(lookDir, 2);
				entity.getLookVec();
				
				// disable growing by falling else infinite-size!
				cloud.setRadiusPerFall(0);
				
				entity.fallDistance = 0; // Reset any fall distance
				
				// Only affect caster if they jump
				if (entity.onGround || entity.isSneaking()) {
					return;
				}
			}
			// upward effect
			final int period = 20;
			final float prog = ((float) (entity.ticksExisted % period) / (float) period);
			final double dy = (Math.sin(prog * 2 * Math.PI) + 1) / 2;
			final Vector3d target = new Vector3d(cloud.getPosX(), cloud.getPosY() + 2 + dy, cloud.getPosZ());
			final Vector3d diff = target.subtract(entity.getPositionVec());
			entity.setMotion(diff.x / 2,
					diff.y / 2,
					diff.z / 2
					);
			entity.velocityChanged = true;
			//entity.getPosY() = 2 + dy;
			//entity.setPositionAndUpdate(cloud.getPosX(), cloud.getPosY() + 2 + dy, cloud.getPosZ());
			
			// Hurricane vortexes also deal damage to non-friendlies!
			if (hurricaneCount >= 4 && entity.ticksExisted % 15 == 0) {
				if (entity instanceof LivingEntity && !NostrumMagica.IsSameTeam((LivingEntity)entity, caster)) {
					LivingEntity living = (LivingEntity) entity;
					entity.hurtResistantTime = 0;
					entity.attackEntityFrom(new MagicDamageSource(caster, EMagicElement.WIND),
							SpellAction.calcDamage(caster, living, .5f, EMagicElement.WIND));
					entity.hurtResistantTime = 0;
					
					 NostrumParticles.GLOW_ORB.spawn(living.getEntityWorld(), new NostrumParticles.SpawnParams(
							 10,
							 living.getPosX(), entity.getPosY() + entity.getHeight()/2f, entity.getPosZ(), entity.getWidth() * 2,
							 10, 5,
							 living.getEntityId())
							 .color(EMagicElement.WIND.getColor()));
				}
			}
		});
		cloud.setVerticleStepping(true);
		cloud.setGravity(true, 1);
		cloud.setRadiusPerFall(.1f);
		cloud.setWalksWater();
		cloud.setEffectDelay(0);
		cloud.setWaddle(direction, 1);
		
		cloud.setParticleData(ParticleTypes.MYCELIUM);
		cloud.setIgnoreRadius(true);
		cloud.addVFXFunc((worldIn, ticksExisted, cloudIn) -> {
			final int count = 5 + Math.max(0, (int)Math.floor(cloudIn.getRadius() / 4)); 
				AspectedWeapon.spawnWhirlwindParticle(worldIn, count, cloudIn.getPositionVec(), cloudIn, 0xA0C0EEC0, -.05f);
			//}
		});
		if (hurricaneCount >= 4) {
			cloud.setCustomParticle(ParticleTypes.SWEEP_ATTACK);
			//cloud.setCustomParticleParam1(10);
			cloud.setCustomParticleFrequency(.4f);
		}
		world.addEntity(cloud);
		cloud.setMotion(direction);
	}
	
	public static void spawnJumpVortex(World world, PlayerEntity caster, Vector3d at, Type weaponType) {
		EntityAreaEffect cloud = new EntityAreaEffect(NostrumEntityTypes.areaEffect, world, at.x, at.y, at.z);
		cloud.setOwner(caster);
		cloud.setWaitTime(0);
		cloud.setRadius(1f);
		//cloud.setRadiusPerTick((.25f + level * .5f) / (20f * 10));
		cloud.setDuration((int) (20 * (6 + typeScale(weaponType)))); // 6 seconds + a second per extra level
		cloud.addEffect((IAreaEntityEffect)(worldIn, entity) -> {
			if (entity.noClip || entity.hasNoGravity()) {
				return;
			}
			
			// Try and only affect jumping or falling entities
			final float minY = .1f;
			final float maxY = 2f;
			if (!entity.onGround && entity.getMotion().y > minY && entity.getMotion().y < maxY) {
				entity.setMotion(entity.getMotion().x, Math.min(maxY, entity.getMotion().y + .5f), entity.getMotion().z);
				entity.velocityChanged = true;
			} else if (entity.getMotion().y < 0) {
				entity.fallDistance = 0;
			}
		});
		cloud.setEffectDelay(0);
		
		cloud.setParticleData(ParticleTypes.MYCELIUM);
		cloud.setIgnoreRadius(true);
		cloud.addVFXFunc((worldIn, ticksExisted, cloudIn) -> {
			final int count = 5 + Math.max(0, (int)Math.floor(cloudIn.getRadius() / 4)); 
				AspectedWeapon.spawnWhirlwindParticle(worldIn, count, cloudIn.getPositionVec(), cloudIn, 0xA090EE90, -.1f);
			//}
		});
		world.addEntity(cloud);
		//cloud.getMotion().x = direction.x;
		//cloud.getMotion().y = direction.y;
		//cloud.getMotion().z = direction.z;
	}
	
	protected static boolean summonBoltOnSelf(LivingEntity entity) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr == null || attr.getMana() < 30) {
			return false;
		}
		
		if (!(entity.world instanceof ServerWorld)) {
			return false;
		}
		
		((ServerWorld)entity.world).addLightningBolt(
				new NostrumTameLightning(NostrumEntityTypes.tameLightning, entity.world, entity.getPosX(), entity.getPosY(), entity.getPosZ())
				);
		attr.addMana(-30);
		if (entity instanceof PlayerEntity) {
			NostrumMagica.instance.proxy.sendMana((PlayerEntity) entity);
		}
		return true;
	}
	
	protected static boolean summonBoltAtTarget(LivingEntity caster, World world, Vector3d pos) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		if (attr == null || attr.getMana() < 30) {
			return false;
		}
		
		if (!(world instanceof ServerWorld)) {
			return false;
		}
		
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
		
		MutableBlockPos cursor = new MutableBlockPos();
		Random rand = (caster == null ? new Random() : caster.getRNG());
		for (int i = 0; i < count; i++) {
			
			if (i == 0) {
				((ServerWorld) world).addLightningBolt(
						new NostrumTameLightning(NostrumEntityTypes.tameLightning, world, pos.x, pos.y, pos.z)
						);
			} else {
				// Apply random x/z offsets. Then step up to 4 to find surface
				cursor.setPos(
						pos.x + rand.nextInt(6) - 3,
						pos.y - 2,
						pos.z + rand.nextInt(6) - 3);
				
				// Find surface
				int dist = 0;
				while (dist++ < 4 && !world.isAirBlock(cursor)) {
					cursor.setY(cursor.getY() + 1);
				}
				
				if (world.isAirBlock(cursor)) {
					((ServerWorld) world).addLightningBolt(
						new NostrumTameLightning(NostrumEntityTypes.tameLightning, world, cursor.getX() + 0.5, cursor.getY(), cursor.getZ() + 0.5)
						);
				}
			}
		}
		
		attr.addMana(-30);
		if (caster instanceof PlayerEntity) {
			NostrumMagica.instance.proxy.sendMana((PlayerEntity) caster);
		}
		return true;
	}
	
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
		if (element == EMagicElement.WIND) {
			SpellAction fly = new SpellAction(playerIn);
			fly.push(5.0f, typeScale(this.type));
			fly.apply(target, 1.0f);
			ItemStacks.damageItem(stack, playerIn, hand, 2);
			return true;
		}

        return false;
	}
	
	public static void spawnWhirlwindParticle(World world, int count, Vector3d pos, EntityAreaEffect cloud, int color, float gravity) {
		NostrumParticles.GLOW_ORB.spawn(world, new NostrumParticles.SpawnParams(count, pos.x, pos.y, pos.z,
				cloud.getRadius(),
				cloud.getRemainingTicks() / 4, 20,
				cloud.getEntityId())
					//.gravity(-.1f)
					//.gravity(.65f)
					.gravity(gravity)
					.color(color)
					.dieOnTarget(false)
					.setTargetBehavior(TargetBehavior.ORBIT)
			);
	}
}
