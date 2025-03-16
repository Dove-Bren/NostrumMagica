package com.smanzana.nostrummagica.item.equipment;

import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams.TargetBehavior;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.AreaEffectEntity;
import com.smanzana.nostrummagica.entity.AreaEffectEntity.IAreaEntityEffect;
import com.smanzana.nostrummagica.entity.AreaEffectEntity.IAreaLocationEffect;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.TameLightning;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.item.IReactiveEquipment;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellDamage;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.log.ISpellLogBuilder;
import com.smanzana.nostrummagica.util.ItemStacks;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.SwordItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

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
	
	private static final int WIND_COST = 15;
	

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
		super(Tiers.DIAMOND,
				(int) (calcDamage(element, type) - Tiers.DIAMOND.getAttackDamageBonus()), // Calc desired damage, and subtrace the amt diamond tier is gonna give
				calcSwingSpeed(element, type),
				NostrumItems.PropEquipment().durability(calcDurability(element, type)));
		
		this.type = type;
		this.element = element;
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
		Multimap<Attribute, AttributeModifier> multimap = super.getDefaultAttributeModifiers(slot);

		if (slot == EquipmentSlot.OFFHAND && element == EMagicElement.WIND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(multimap);
			double amt = typeScale(this.type)* .1;
			builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(OFFHAND_ATTACK_SPEED_MODIFIER, "Weapon modifier", amt, AttributeModifier.Operation.ADDITION));
			multimap = builder.build();
		}

		return multimap;
	}
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
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
				action = new SpellAction().lightning();
			break;
		case ICE:
			if (NostrumMagica.rand.nextFloat() < 0.5f * typeScale(this.type))
				action = new SpellAction().status(
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
			break;
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
	
//	@Override
//	public UseAction getUseAction(ItemStack stack) {
//		if (hasWindPropel(stack)) {
//			return UseAction.BOW;
//		}
//		return super.getUseAction(stack);
//	}
//	
//	@Override
//	public int getUseDuration(ItemStack stack) {
//		// How long to let the player keep 'using' it.
//		if (hasWindPropel(stack)) {
//			// We set it high so they can hold it forever.
//			// This value is copied from the bow.
//			return 270000;
//		}
//		return super.getUseDuration(stack);
//	}
//	
//	@Override
//	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
//		if (player instanceof PlayerEntity && isWindPropel(stack, (PlayerEntity) player)) {
//			doPropelTick(stack, (PlayerEntity) player);
//		}
//	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final @Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		Vec3 dir = playerIn.getLookAngle();
		dir = dir.add(0, -dir.y, 0).normalize();
		
		ItemStack itemStackIn = playerIn.getItemInHand(hand);
		
		if (element == EMagicElement.ICE) {
			
			if (playerIn.getAttackStrengthScale(0.5F) > .95) {
				
				if (!worldIn.isClientSide) {
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
					
					spawnIceCloud(worldIn, playerIn, new Vec3(playerIn.getX() + dir.x, playerIn.getY() + .75, playerIn.getZ() + dir.z), dir, this.type);
					
					ItemStacks.damageItem(itemStackIn, playerIn, hand, 2);
				}
				
				playerIn.resetAttackStrengthTicker();
			}
			
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStackIn);
		} else if (element == EMagicElement.WIND) {
			if (playerIn.getAttackStrengthScale(0.5F) > .95 && playerIn.isShiftKeyDown()) {
				if (!worldIn.isClientSide) {
					spawnWalkingVortex(worldIn, playerIn, new Vec3(playerIn.getX() + dir.x, playerIn.getY() + .75, playerIn.getZ() + dir.z), dir, this.type);
					ItemStacks.damageItem(itemStackIn, playerIn, hand, 1);
				}
				playerIn.resetAttackStrengthTicker();
				return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStackIn);
			} else if (isWindPropel(itemStackIn, playerIn)) {
				doPropel(itemStackIn,  playerIn);
				return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStackIn);
			}
			
		} else if (element == EMagicElement.LIGHTNING && ElementalArmor.GetSetCount(playerIn, EMagicElement.LIGHTNING, ElementalArmor.Type.MASTER) == 4) {
			if (playerIn.getAttackStrengthScale(0.5F) > .95) {
				// If full set, strike at targetting location (unless sneaking, then strike self)
				boolean used = false;
				if (playerIn.isShiftKeyDown()) {
					if (!worldIn.isClientSide) {
						summonBoltOnSelf(playerIn);
					}
					used = true;
				} else if (playerIn.hasEffect(NostrumEffects.lightningAttack)) {
					// This should be client-side... TODO do it on client and send via armor message?
					
					// Do quick mana check prior to actually doing raytrace. Redone inside helper func.
					if (attr != null && attr.getMana() >= 30) {
						if (!worldIn.isClientSide) {
							final float maxDist = 50;
							HitResult mop = RayTrace.raytrace(worldIn, playerIn, playerIn.position().add(0, playerIn.getEyeHeight(), 0), playerIn.getLookAngle(), maxDist, (ent) -> { return ent != playerIn;});
							if (mop != null && mop.getType() != HitResult.Type.MISS) {
								final Vec3 at = (mop.getType() == HitResult.Type.ENTITY ? RayTrace.entFromRaytrace(mop).position() : mop.getLocation());
								summonBoltAtTarget(playerIn, worldIn, at);
							}
						}
						used = true;
					}
				}
				if (used) {
					if (!worldIn.isClientSide) {
						ItemStacks.damageItem(itemStackIn, playerIn, hand, 1);
					}
					playerIn.resetAttackStrengthTicker();
					return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStackIn);
				}
			}
		}
			
        return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemStackIn);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Level worldIn = context.getLevel();
		final Player playerIn = context.getPlayer();
		final BlockPos pos = context.getClickedPos();
		Vec3 hitVec = context.getClickLocation();
		ItemStack stack = context.getItemInHand();
		final InteractionHand hand = context.getHand();
		if (playerIn.getAttackStrengthScale(0.5F) > .95) {
			Vec3 dir = hitVec.subtract(playerIn.position());
			dir = dir.add(0, -dir.y, 0);
			dir = dir.normalize();
			if (element == EMagicElement.WIND) {
				if (playerIn.isShiftKeyDown()) {
					if (!worldIn.isClientSide) {
						spawnWalkingVortex(worldIn, playerIn, new Vec3(playerIn.getX() + dir.x, playerIn.getY() + .75, playerIn.getZ() + dir.z), dir, this.type);
						ItemStacks.damageItem(stack, playerIn, hand, 1);
					}
					playerIn.resetAttackStrengthTicker();
					return InteractionResult.SUCCESS;
				}
			} else if (element == EMagicElement.ICE) {
				if (!worldIn.isClientSide) { 
					spawnIceCloud(worldIn, playerIn, new Vec3(pos.getX() + hitVec.x, pos.getY() + 1, pos.getZ() + hitVec.z), dir, this.type);
					ItemStacks.damageItem(stack, playerIn, hand, 2);
				}
				playerIn.resetAttackStrengthTicker();
				return InteractionResult.SUCCESS;
			} else if (element == EMagicElement.LIGHTNING && ElementalArmor.GetSetCount(playerIn, EMagicElement.LIGHTNING, ElementalArmor.Type.MASTER) == 4) {
				
				boolean used = false;
				if (playerIn.isShiftKeyDown()) {
					if (!worldIn.isClientSide) {
						summonBoltOnSelf(playerIn);
					}
					used = true;
				} else if (playerIn.hasEffect(NostrumEffects.lightningAttack)) {
					// This should be client-side... TODO do it on client and send via armor message?
					
					// Do quick mana check prior to actually doing raytrace. Redone inside helper func.
					INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
					if (attr != null && attr.getMana() >= 30) {
						if (!worldIn.isClientSide) {
							final float maxDist = 50;
							HitResult mop = RayTrace.raytrace(worldIn, playerIn, playerIn.position().add(0, playerIn.getEyeHeight(), 0), playerIn.getLookAngle(), maxDist, (ent) -> { return ent != playerIn;});
							if (mop != null && mop.getType() != HitResult.Type.MISS) {
								final Vec3 at = (mop.getType() == HitResult.Type.ENTITY ? RayTrace.entFromRaytrace(mop).position() : mop.getLocation());
								summonBoltAtTarget(playerIn, worldIn, at);
							}
						}
						used = true;
					}
				}
				if (used) {
					if (!worldIn.isClientSide) {
						ItemStacks.damageItem(stack, playerIn, hand, 1);
					}
					playerIn.resetAttackStrengthTicker();
					return InteractionResult.SUCCESS;
				}
			}
		}
		return InteractionResult.PASS;
		
	}
	
	protected static void spawnIceCloud(Level world, Player caster, Vec3 at, Vec3 direction, Type weaponType) {
		final int blizzardCount = ElementalArmor.GetSetCount(caster, EMagicElement.ICE, ElementalArmor.Type.MASTER);
		INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		direction = direction.scale(5f/(3f * 20f)); // 5 blocks over 3 seconds
		AreaEffectEntity cloud = new AreaEffectEntity(NostrumEntityTypes.areaEffect, world, at.x, at.y, at.z);
		cloud.setOwner(caster);
		cloud.setFixedColor(NostrumEffects.frostbite.getColor());
		cloud.setWaitTime(5);
		cloud.setRadius(0.5f);
		cloud.setRadiusPerTick((1f + typeScale(weaponType) * .75f) / (20f * 3)); // 1 (+ .75 per extra level) extra radius per 3 seconds
		cloud.setDuration((int) (20 * (3 + (typeScale(weaponType) * .5f) + (blizzardCount == 4 ? 6 : 0)))); // 3 seconds + a half a second per extra level
		cloud.addEffect((IAreaLocationEffect)(worldIn, pos) -> {
			BlockState state = worldIn.getBlockState(pos);
			if (state.getMaterial() == Material.WATER
					&& (state.getBlock() == Blocks.WATER)) {
				worldIn.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
			}
		});
		final boolean hasHeal = attr != null && attr.hasSkill(NostrumSkills.Ice_Weapon);
		final boolean hasHealBoost = attr != null && attr.hasSkill(NostrumSkills.Ice_Master);
		final boolean hasHealShield = attr != null && attr.hasSkill(NostrumSkills.Ice_Adept);
		cloud.addEffect((Level w, Entity ent) -> {
			if (hasHeal && ent != caster && ent instanceof LivingEntity && NostrumMagica.IsSameTeam(caster, (LivingEntity) ent)) {
				((LivingEntity) ent).heal(hasHealBoost ? 2f : 1f);
				((LivingEntity) ent).removeEffectNoUpdate(NostrumEffects.frostbite);
				
				if (hasHealShield && NostrumMagica.rand.nextInt(8) == 0) {
					((LivingEntity) ent).addEffect(new MobEffectInstance(NostrumEffects.magicShield, (int)((20 * 15) * 1f), 0));
				}
				NostrumParticles.FILLED_ORB.spawn(ent.level, new SpawnParams(
						10, ent.getX(), ent.getY() + ent.getBbHeight()/2, ent.getZ(), 4,
						30, 10,
						ent.getId()
						).color(NostrumEffects.frostbite.getColor()).dieOnTarget(true));
			} else if (ent instanceof LivingEntity) {
				((LivingEntity) ent).addEffect(new MobEffectInstance(NostrumEffects.frostbite, 20 * 10));
			}
		});
		cloud.setVerticleStepping(true);
		cloud.setGravity(true, .1);
		//cloud.setWalksWater();
		world.addFreshEntity(cloud);
		cloud.setDeltaMovement(direction);
	}
	
	protected static void spawnWalkingVortex(Level world, Player caster, Vec3 at, Vec3 direction, Type weaponType) {
		final int hurricaneCount = ElementalArmor.GetSetCount(caster, EMagicElement.WIND, ElementalArmor.Type.MASTER);
		direction = direction.scale(5f/(3f * 20f)); // 5 blocks over 10 seconds
		AreaEffectEntity cloud = new AreaEffectEntity(NostrumEntityTypes.areaEffect, world, at.x, at.y, at.z);
		cloud.setOwner(caster);
		cloud.setWaitTime(10);
		cloud.setRadius(.75f);
		//cloud.setRadiusPerTick((.25f + level * .5f) / (20f * 10));
		cloud.setDuration((int) (20 * (3 + typeScale(weaponType) * .5f))); // 3 seconds + a half a second per extra level
		cloud.addEffect((IAreaEntityEffect)(worldIn, entity) -> {
			if (entity.noPhysics || entity.isNoGravity()) {
				return;
			}
			if (hurricaneCount == 4 && entity == caster) {
				// Get another tick (continue forever) from hurricane caster
				cloud.addTime(1, false);
				
				// Move in direction of player look
				Vec3 lookDir = Vec3.directionFromRotation(0, entity.getYRot()).scale(5f/(3f * 20f)); // 5 blocks over 10 seconds;
				cloud.setWaddle(lookDir, 2);
				entity.getLookAngle();
				
				// disable growing by falling else infinite-size!
				cloud.setRadiusPerFall(0);
				
				entity.fallDistance = 0; // Reset any fall distance
				
				// Only affect caster if they jump
				if (entity.isOnGround() || entity.isShiftKeyDown()) {
					return;
				}
			}
			// upward effect
			final int period = 20;
			final float prog = ((float) (entity.tickCount % period) / (float) period);
			final double dy = (Math.sin(prog * 2 * Math.PI) + 1) / 2;
			final Vec3 target = new Vec3(cloud.getX(), cloud.getY() + 2 + dy, cloud.getZ());
			final Vec3 diff = target.subtract(entity.position());
			entity.setDeltaMovement(diff.x / 2,
					diff.y / 2,
					diff.z / 2
					);
			entity.hurtMarked = true;
			//entity.getPosY() = 2 + dy;
			//entity.setPositionAndUpdate(cloud.getPosX(), cloud.getPosY() + 2 + dy, cloud.getPosZ());
			
			// Hurricane vortexes also deal damage to non-friendlies!
			if (hurricaneCount >= 4 && entity.tickCount % 15 == 0) {
				if (entity instanceof LivingEntity && !NostrumMagica.IsSameTeam((LivingEntity)entity, caster)) {
					LivingEntity living = (LivingEntity) entity;
					entity.invulnerableTime = 0;
					SpellDamage.DamageEntity(living, EMagicElement.WIND, .5f, caster);
					entity.invulnerableTime = 0;
					
					 NostrumParticles.GLOW_ORB.spawn(living.getCommandSenderWorld(), new NostrumParticles.SpawnParams(
							 10,
							 living.getX(), entity.getY() + entity.getBbHeight()/2f, entity.getZ(), entity.getBbWidth() * 2,
							 10, 5,
							 living.getId())
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
		
		cloud.setParticle(ParticleTypes.MYCELIUM);
		cloud.setWaiting(true);
		cloud.addVFXFunc((worldIn, ticksExisted, cloudIn) -> {
			final int count = 5 + Math.max(0, (int)Math.floor(cloudIn.getRadius() / 4)); 
				AspectedWeapon.spawnWhirlwindParticle(worldIn, count, cloudIn.position(), cloudIn, 0xA0C0EEC0, -.05f);
			//}
		});
		if (hurricaneCount >= 4) {
			cloud.setCustomParticle(ParticleTypes.SWEEP_ATTACK);
			//cloud.setCustomParticleParam1(10);
			cloud.setCustomParticleFrequency(.4f);
		}
		world.addFreshEntity(cloud);
		cloud.setDeltaMovement(direction);
	}
	
	public static void spawnJumpVortex(Level world, Player caster, Vec3 at, Type weaponType) {
		AreaEffectEntity cloud = new AreaEffectEntity(NostrumEntityTypes.areaEffect, world, at.x, at.y, at.z);
		cloud.setOwner(caster);
		cloud.setWaitTime(0);
		cloud.setRadius(1f);
		//cloud.setRadiusPerTick((.25f + level * .5f) / (20f * 10));
		cloud.setDuration((int) (20 * (6 + typeScale(weaponType)))); // 6 seconds + a second per extra level
		cloud.addEffect((IAreaEntityEffect)(worldIn, entity) -> {
			if (entity.noPhysics || entity.isNoGravity()) {
				return;
			}
			
			// Try and only affect jumping or falling entities
			final float minY = .1f;
			final float maxY = 2f;
			if (!entity.isOnGround() && entity.getDeltaMovement().y > minY && entity.getDeltaMovement().y < maxY) {
				entity.setDeltaMovement(entity.getDeltaMovement().x, Math.min(maxY, entity.getDeltaMovement().y + .5f), entity.getDeltaMovement().z);
				entity.hurtMarked = true;
			} else if (entity.getDeltaMovement().y < 0) {
				entity.fallDistance = 0;
			}
		});
		cloud.setEffectDelay(0);
		
		cloud.setParticle(ParticleTypes.MYCELIUM);
		cloud.setWaiting(true);
		cloud.addVFXFunc((worldIn, ticksExisted, cloudIn) -> {
			final int count = 5 + Math.max(0, (int)Math.floor(cloudIn.getRadius() / 4)); 
				AspectedWeapon.spawnWhirlwindParticle(worldIn, count, cloudIn.position(), cloudIn, 0xA090EE90, -.1f);
			//}
		});
		world.addFreshEntity(cloud);
		//cloud.getMotion().x = direction.x;
		//cloud.getMotion().y = direction.y;
		//cloud.getMotion().z = direction.z;
	}
	
	protected static boolean summonBoltOnSelf(LivingEntity entity) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr == null || attr.getMana() < 30) {
			return false;
		}
		
		if (!(entity.level instanceof ServerLevel)) {
			return false;
		}
		
		((ServerLevel)entity.level).addFreshEntity(
				new TameLightning(NostrumEntityTypes.tameLightning, entity.level, entity.getX(), entity.getY(), entity.getZ())
				);
		attr.addMana(-30);
		if (entity instanceof Player) {
			NostrumMagica.instance.proxy.sendMana((Player) entity);
		}
		
		if (attr.hasSkill(NostrumSkills.Lightning_Weapon)) {
			// Explode magic rend
			// Spread
			for (Entity ent : entity.getCommandSenderWorld().getEntities(entity, entity.getBoundingBox().inflate(5), (ent) -> ent instanceof LivingEntity && !NostrumMagica.IsSameTeam((LivingEntity) ent, entity))) {
				((LivingEntity) ent).addEffect(new MobEffectInstance(NostrumEffects.magicRend, 20 * 15, 0));
				
				NostrumParticles.FILLED_ORB.spawn(entity.level, new SpawnParams(
						10, entity.getX(), entity.getY() + entity.getBbHeight()/2, entity.getZ(), 0,
						40, 10,
						ent.getId()
						).color(NostrumEffects.magicRend.getColor()).dieOnTarget(true));
			}
			
			NostrumParticles.FILLED_ORB.spawn(entity.level, new SpawnParams(
					50, entity.getX(), entity.getY() + entity.getBbHeight()/2, entity.getZ(), 0,
					30, 10,
					new Vec3(0, .1, 0), new Vec3(.2, .05, .2)
					).color(NostrumEffects.magicRend.getColor()).gravity(true));
			//NostrumMagicaSounds.MELT_METAL.play(event.getEntityLiving());
		}
		return true;
	}
	
	protected static boolean summonBoltAtTarget(LivingEntity caster, Level world, Vec3 pos) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		if (attr == null || attr.getMana() < 30) {
			return false;
		}
		
		if (!(world instanceof ServerLevel)) {
			return false;
		}
		
		int count = 1;
		if (caster != null && caster instanceof Player) {
			// Look for lightning belt
			Container baubles = NostrumMagica.instance.curios.getCurios((Player) caster);
			if (baubles != null) {
				for (int i = 0; i < baubles.getContainerSize(); i++) {
					ItemStack stack = baubles.getItem(i);
					if (stack.isEmpty() || stack.getItem() != NostrumCurios.lightningBelt) {
						continue;
					}
					
					count = caster.getRandom().nextInt(3) + 3;
					break;
				}
			}
		}
		
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		Random rand = (caster == null ? new Random() : caster.getRandom());
		for (int i = 0; i < count; i++) {
			
			if (i == 0) {
				((ServerLevel) world).addFreshEntity(
						new TameLightning(NostrumEntityTypes.tameLightning, world, pos.x, pos.y, pos.z)
						);
			} else {
				// Apply random x/z offsets. Then step up to 4 to find surface
				cursor.set(
						pos.x + rand.nextInt(6) - 3,
						pos.y - 2,
						pos.z + rand.nextInt(6) - 3);
				
				// Find surface
				int dist = 0;
				while (dist++ < 4 && !world.isEmptyBlock(cursor)) {
					cursor.setY(cursor.getY() + 1);
				}
				
				if (world.isEmptyBlock(cursor)) {
					((ServerLevel) world).addFreshEntity(
						new TameLightning(NostrumEntityTypes.tameLightning, world, cursor.getX() + 0.5, cursor.getY(), cursor.getZ() + 0.5)
						);
				}
			}
		}
		
		attr.addMana(-30);
		if (caster instanceof Player) {
			NostrumMagica.instance.proxy.sendMana((Player) caster);
		}
		return true;
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
		if (element == EMagicElement.WIND) {
			SpellAction fly = new SpellAction();
			fly.push(5.0f, typeScale(this.type));
			fly.apply(playerIn, target, 1.0f, ISpellLogBuilder.Dummy);
			ItemStacks.damageItem(stack, playerIn, hand, 2);
			return InteractionResult.SUCCESS;
		}

        return InteractionResult.PASS;
	}
	
	public static void spawnWhirlwindParticle(Level world, int count, Vec3 pos, AreaEffectEntity cloud, int color, float gravity) {
		NostrumParticles.GLOW_ORB.spawn(world, new NostrumParticles.SpawnParams(count, pos.x, pos.y, pos.z,
				cloud.getRadius(),
				cloud.getRemainingTicks() / 4, 20,
				cloud.getId())
					//.gravity(-.1f)
					//.gravity(.65f)
					.gravity(gravity)
					.color(color)
					.dieOnTarget(false)
					.setTargetBehavior(TargetBehavior.ORBIT)
			);
	}
	
	protected static boolean isWindPropel(ItemStack stack, Player player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return hasWindPropel(stack) && attr != null && attr.hasSkill(NostrumSkills.Wind_Weapon) && attr.getMana() >= WIND_COST;
	}
	
	protected static boolean hasWindPropel(ItemStack stack) {
		AspectedWeapon weapon = (AspectedWeapon) stack.getItem();
		return weapon.element == EMagicElement.WIND && weapon.type == Type.MASTER;
	}
	
	private static SpellAction propelAction = new SpellAction().propel(1);
	
	protected static void doPropel(ItemStack stack, Player player) {
		if (isWindPropel(stack, player)) {
			// Adjust velocity, ignoring downward velocity so players can use it to move even when falling down fast
			Vec3 motion = player.getDeltaMovement();
			if (motion.y < 0) {
				motion = new Vec3(motion.x, 0, motion.z);
			}
			if (motion.length() < 1.0 || player.isFallFlying()) {
				propelAction.apply(player, player, 1f, ISpellLogBuilder.Dummy);
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				attr.addMana(-WIND_COST);
				NostrumMagica.instance.proxy.sendMana(player);
			}
		} else {
			player.releaseUsingItem();
		}
	}
}
