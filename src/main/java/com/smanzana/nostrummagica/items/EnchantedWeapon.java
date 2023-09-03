package com.smanzana.nostrummagica.items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams.EntityBehavior;
import com.smanzana.nostrummagica.entity.EntityAreaEffect;
import com.smanzana.nostrummagica.entity.EntityAreaEffect.IAreaEntityEffect;
import com.smanzana.nostrummagica.entity.EntityAreaEffect.IAreaLocationEffect;
import com.smanzana.nostrummagica.entity.NostrumTameLightning;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble.ItemType;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.potions.LightningAttackPotion;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.MagicDamageSource;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;

public class EnchantedWeapon extends ItemSword implements EnchantedEquipment {

	private static Map<EMagicElement, Map<Integer, EnchantedWeapon>> items;
	
	public static final void registerWeapons(final IForgeRegistry<Item> registry) {
		items = new EnumMap<EMagicElement, Map<Integer, EnchantedWeapon>>(EMagicElement.class);
		
		for (EMagicElement element : EMagicElement.values()) {
			if (isWeaponElement(element)) {
				items.put(element, new HashMap<Integer, EnchantedWeapon>());
				for (int i = 0; i < 3; i++) {
					ResourceLocation location = new ResourceLocation(NostrumMagica.MODID, "sword_" + element.name().toLowerCase() + (i + 1));
					EnchantedWeapon weapon =  new EnchantedWeapon(location.getResourcePath(), element, i + 1);
					weapon.setUnlocalizedName(location.getResourcePath());
					weapon.setRegistryName(location);
					registry.register(weapon);
					items.get(element).put(i + 1, weapon);
				}
			}
		}
	}
	
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
	
	private static int calcDamage(EMagicElement element, int level) {
		
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
		
		int base = 5 + (level * 2);
		
		return (int) ((float) base * mod);
	}
	
	protected static final UUID OFFHAND_ATTACK_SPEED_MODIFIER = UUID.fromString("B2879ABC-4180-1234-B01B-487954A3BAC4");
	
	private int level;
	private int damage;
	private EMagicElement element;
	
	private String modelID;
	
	public EnchantedWeapon(String modelID, EMagicElement element, int level) {
		super(ToolMaterial.DIAMOND);
		
		this.level = level;
		this.element = element;
		this.modelID = modelID;
		
		this.setMaxDamage(400);
		this.setCreativeTab(NostrumMagica.creativeTab);
		
		this.damage = calcDamage(element, level);
	}
	
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot) {
		Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

		if (slot == EquipmentSlotType.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damage, 0));
			
			final double amt;
			switch (element) {
			case ICE:
				if (level == 1) {
					amt = 2.6; // Mace only slightly slower than sword
				} else if (level == 2) {
					amt = 3.3; // Morning star slower than vanilla axe!
				} else {
					amt = 2.8; // Scepter slower than mace, but not TOO slow
				}
				break;
			case LIGHTNING:
				if (level == 1) {
					amt = 1.5; // Knife is very fast! 2.5 attacks per second (vanilla sword is 1.6)
				} else if (level == 2) {
					amt = 2.0; // Dagger slower, but still faster than sword
				} else {
					amt = 1.6; // Stiletto very fast! Lightning fast, even! lol
				}
				break;
			case WIND:
				if (level == 1) {
					amt = 2.4; // Slasher is same as vanilla sword. Heavier, but powered by wind!
				} else if (level == 2) {
					amt = 2.6; // Slave is a tad slower
				} else {
					amt = 2.1; // Striker is very fast :)
				}
				break;
			default:
				amt = 2.4;
			}
			
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -amt, 0));
		} else if (slot == EquipmentSlotType.OFFHAND && element == EMagicElement.WIND) {
			final double amt = level * 0.1;
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(OFFHAND_ATTACK_SPEED_MODIFIER, "Weapon modifier", amt, 0));
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
			if (NostrumMagica.rand.nextFloat() < 0.1f * level)
				action = new SpellAction(user).lightning();
			break;
		case ICE:
			if (NostrumMagica.rand.nextFloat() < 0.5f * level)
				action = new SpellAction(user).status(
						FrostbitePotion.instance(), 5 * 20, level > 2 ? level - 2 : 0);
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
	
	public static EnchantedWeapon get(EMagicElement element, int level) {
		if (items.containsKey(element))
			return items.get(element).get(level);
		
		return null;
	}
	
	public static List<EnchantedWeapon> getAll() {
		List<EnchantedWeapon> list = new LinkedList<>();
		
		for (EMagicElement element : EMagicElement.values())
		if (isWeaponElement(element)) {
			list.addAll(items.get(element).values());
		}
		
		return list;
	}
	
	public String getModelID() {
		return modelID;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		Vec3d dir = playerIn.getLookVec();
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
//					//cloud.setParticle(EnumParticleTypes.SPELL);
//					cloud.addEffect(new PotionEffect(FrostbitePotion.instance(), 20 * 10));
//					worldIn.spawnEntityInWorld(cloud);
//					cloud.getMotion().x = dir.x;
//					cloud.getMotion().y = dir.y;
//					cloud.getMotion().z = dir.z;
					
					spawnIceCloud(worldIn, playerIn, new Vec3d(playerIn.posX + dir.x, playerIn.posY + .75, playerIn.posZ + dir.z), dir, level);
					
					itemStackIn.damageItem(2, playerIn);
				}
				
				playerIn.resetCooldown();
			}
			
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
		} else if (element == EMagicElement.WIND) {
			if (playerIn.getCooledAttackStrength(0.5F) > .95) {
				if (playerIn.isSneaking()) {
					if (!worldIn.isRemote) {
						spawnWalkingVortex(worldIn, playerIn, new Vec3d(playerIn.posX + dir.x, playerIn.posY + .75, playerIn.posZ + dir.z), dir, level);
						itemStackIn.damageItem(1, playerIn);
					}
					playerIn.resetCooldown();
					return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
				}
			}
			
		} else if (element == EMagicElement.LIGHTNING && EnchantedArmor.GetSetCount(playerIn, EMagicElement.LIGHTNING, 3) == 4) {
			if (playerIn.getCooledAttackStrength(0.5F) > .95) {
				// If full set, strike at targetting location (unless sneaking, then strike self)
				boolean used = false;
				if (playerIn.isSneaking()) {
					if (!worldIn.isRemote) {
						summonBoltOnSelf(playerIn);
					}
					used = true;
				} else if (playerIn.isPotionActive(LightningAttackPotion.instance())) {
					// This should be client-side... TODO do it on client and send via armor message?
					
					// Do quick mana check prior to actually doing raytrace. Redone inside helper func.
					INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
					if (attr != null && attr.getMana() >= 30) {
						if (!worldIn.isRemote) {
							final float maxDist = 50;
							RayTraceResult mop = RayTrace.raytrace(worldIn, playerIn.getPositionVector().addVector(0, playerIn.eyeHeight, 0), playerIn.getLookVec(), maxDist, (ent) -> { return ent != playerIn;});
							if (mop != null && mop.typeOfHit != RayTraceResult.Type.MISS) {
								final Vec3d at = (mop.typeOfHit == RayTraceResult.Type.ENTITY ? mop.entityHit.getPositionVector() : mop.hitVec);
								summonBoltAtTarget(playerIn, worldIn, at);
							}
						}
						used = true;
					}
				}
				if (used) {
					if (!worldIn.isRemote) {
						itemStackIn.damageItem(1, playerIn);
					}
					playerIn.resetCooldown();
					return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
				}
			}
		}
			
        return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
	}
	
	@Override
	public ActionResultType onItemUse(PlayerEntity playerIn, World worldIn, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = playerIn.getHeldItem(hand);
		if (playerIn.getCooledAttackStrength(0.5F) > .95) {
			Vec3d dir = new Vec3d(pos).addVector(hitX, 0, hitZ).subtract(playerIn.getPositionVector());
			dir = dir.add(0, -dir.y, 0);
			dir = dir.normalize();
			if (element == EMagicElement.WIND) {
				if (playerIn.isSneaking()) {
					if (!worldIn.isRemote) {
						spawnWalkingVortex(worldIn, playerIn, new Vec3d(playerIn.posX + dir.x, playerIn.posY + .75, playerIn.posZ + dir.z), dir, level);
						stack.damageItem(1, playerIn);
					}
					playerIn.resetCooldown();
					return ActionResultType.SUCCESS;
				}
			} else if (element == EMagicElement.ICE) {
				if (!worldIn.isRemote) { 
					spawnIceCloud(worldIn, playerIn, new Vec3d(pos.getX() + hitX, pos.getY() + 1, pos.getZ() + hitZ), dir, level);
					stack.damageItem(2, playerIn);
				}
				playerIn.resetCooldown();
				return ActionResultType.SUCCESS;
			} else if (element == EMagicElement.LIGHTNING && EnchantedArmor.GetSetCount(playerIn, EMagicElement.LIGHTNING, 3) == 4) {
				
				boolean used = false;
				if (playerIn.isSneaking()) {
					if (!worldIn.isRemote) {
						summonBoltOnSelf(playerIn);
					}
					used = true;
				} else if (playerIn.isPotionActive(LightningAttackPotion.instance())) {
					// This should be client-side... TODO do it on client and send via armor message?
					
					// Do quick mana check prior to actually doing raytrace. Redone inside helper func.
					INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
					if (attr != null && attr.getMana() >= 30) {
						if (!worldIn.isRemote) {
							final float maxDist = 50;
							RayTraceResult mop = RayTrace.raytrace(worldIn, playerIn.getPositionVector().addVector(0, playerIn.eyeHeight, 0), playerIn.getLookVec(), maxDist, (ent) -> { return ent != playerIn;});
							if (mop != null && mop.typeOfHit != RayTraceResult.Type.MISS) {
								final Vec3d at = (mop.typeOfHit == RayTraceResult.Type.ENTITY ? mop.entityHit.getPositionVector() : mop.hitVec);
								summonBoltAtTarget(playerIn, worldIn, at);
							}
						}
						used = true;
					}
				}
				if (used) {
					if (!worldIn.isRemote) {
						stack.damageItem(1, playerIn);
					}
					playerIn.resetCooldown();
					return ActionResultType.SUCCESS;
				}
			}
		}
		return ActionResultType.PASS;
		
	}
	
	protected static void spawnIceCloud(World world, PlayerEntity caster, Vec3d at, Vec3d direction, int level) {
		direction = direction.scale(5f/(3f * 20f)); // 5 blocks over 3 seconds
		EntityAreaEffect cloud = new EntityAreaEffect(world, at.x, at.y, at.z);
		cloud.setOwner(caster);
		cloud.setWaitTime(5);
		cloud.setRadius(0.5f);
		cloud.setRadiusPerTick((1f + level * .75f) / (20f * 3)); // 1 (+ .75 per extra level) extra radius per 3 seconds
		cloud.setDuration((int) (20 * (3 + level * .5f))); // 3 seconds + a half a second per extra level
		cloud.addEffect(new PotionEffect(FrostbitePotion.instance(), 20 * 10));
		cloud.addEffect((IAreaLocationEffect)(worldIn, pos) -> {
			BlockState state = worldIn.getBlockState(pos);
			if (state.getMaterial() == Material.WATER
					&& (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER)) {
				worldIn.setBlockState(pos, Blocks.ICE.getDefaultState());
			}
		});
		cloud.setVerticleStepping(true);
		cloud.setGravity(true, .1);
		//cloud.setWalksWater();
		world.spawnEntity(cloud);
		cloud.getMotion().x = direction.x;
		cloud.getMotion().y = direction.y;
		cloud.getMotion().z = direction.z;
	}
	
	protected static void spawnWalkingVortex(World world, PlayerEntity caster, Vec3d at, Vec3d direction, int level) {
		final int hurricaneCount = EnchantedArmor.GetSetCount(caster, EMagicElement.WIND, 3);
		direction = direction.scale(5f/(3f * 20f)); // 5 blocks over 10 seconds
		EntityAreaEffect cloud = new EntityAreaEffect(world, at.x, at.y, at.z);
		cloud.setOwner(caster);
		cloud.setWaitTime(10);
		cloud.setRadius(.75f);
		//cloud.setRadiusPerTick((.25f + level * .5f) / (20f * 10));
		cloud.setDuration((int) (20 * (3 + level * .5f))); // 3 seconds + a half a second per extra level
		cloud.addEffect((IAreaEntityEffect)(worldIn, entity) -> {
			if (entity.noClip || entity.hasNoGravity()) {
				return;
			}
			if (hurricaneCount == 4 && entity == caster) {
				// Get another tick (continue forever) from hurricane caster
				cloud.addTime(1, false);
				
				// Move in direction of player look
				Vec3d lookDir = Vec3d.fromPitchYaw(0, entity.rotationYaw).scale(5f/(3f * 20f)); // 5 blocks over 10 seconds;
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
			final Vec3d target = new Vec3d(cloud.posX, cloud.posY + 2 + dy, cloud.posZ);
			final Vec3d diff = target.subtract(entity.getPositionVector());
			entity.getMotion().x = diff.x / 2;
			entity.getMotion().y = diff.y / 2;
			entity.getMotion().z = diff.z / 2;
			entity.velocityChanged = true;
			//entity.posY = 2 + dy;
			//entity.setPositionAndUpdate(cloud.posX, cloud.posY + 2 + dy, cloud.posZ);
			
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
							 living.posX, entity.posY + entity.height/2f, entity.posZ, entity.width * 2,
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
		
		cloud.setParticle(EnumParticleTypes.SUSPENDED);
		cloud.setIgnoreRadius(true);
		cloud.addVFXFunc((worldIn, ticksExisted, cloudIn) -> {
			final int count = 5 + Math.max(0, (int)Math.floor(cloudIn.getRadius() / 4)); 
				EnchantedWeapon.spawnWhirlwindParticle(worldIn, count, cloudIn.getPositionVector(), cloudIn, 0xA0C0EEC0, -.05f);
			//}
		});
		if (hurricaneCount >= 4) {
			cloud.setCustomParticle(EnumParticleTypes.SWEEP_ATTACK);
			cloud.setCustomParticleParam1(10);
			cloud.setCustomParticleFrequency(.4f);
		}
		world.spawnEntity(cloud);
		cloud.getMotion().x = direction.x;
		cloud.getMotion().y = direction.y;
		cloud.getMotion().z = direction.z;
	}
	
	public static void spawnJumpVortex(World world, PlayerEntity caster, Vec3d at, int level) {
		EntityAreaEffect cloud = new EntityAreaEffect(world, at.x, at.y, at.z);
		cloud.setOwner(caster);
		cloud.setWaitTime(0);
		cloud.setRadius(1f);
		//cloud.setRadiusPerTick((.25f + level * .5f) / (20f * 10));
		cloud.setDuration((int) (20 * (6 + level))); // 6 seconds + a second per extra level
		cloud.addEffect((IAreaEntityEffect)(worldIn, entity) -> {
			if (entity.noClip || entity.hasNoGravity()) {
				return;
			}
			
			// Try and only affect jumping or falling entities
			final float minY = .1f;
			final float maxY = 2f;
			if (!entity.onGround && entity.getMotion().y > minY && entity.getMotion().y < maxY) {
				entity.getMotion().y = Math.min(maxY, entity.getMotion().y + .5f);
				entity.velocityChanged = true;
			} else if (entity.getMotion().y < 0) {
				entity.fallDistance = 0;
			}
		});
		cloud.setEffectDelay(0);
		
		cloud.setParticle(EnumParticleTypes.SUSPENDED);
		cloud.setIgnoreRadius(true);
		cloud.addVFXFunc((worldIn, ticksExisted, cloudIn) -> {
			final int count = 5 + Math.max(0, (int)Math.floor(cloudIn.getRadius() / 4)); 
				EnchantedWeapon.spawnWhirlwindParticle(worldIn, count, cloudIn.getPositionVector(), cloudIn, 0xA090EE90, -.1f);
			//}
		});
		world.spawnEntity(cloud);
		//cloud.getMotion().x = direction.x;
		//cloud.getMotion().y = direction.y;
		//cloud.getMotion().z = direction.z;
	}
	
	protected static boolean summonBoltOnSelf(LivingEntity entity) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr == null || attr.getMana() < 30) {
			return false;
		}
		
		entity.world.addWeatherEffect(
				new NostrumTameLightning(entity.world, entity.posX, entity.posY, entity.posZ)
				);
		attr.addMana(-30);
		if (entity instanceof PlayerEntity) {
			NostrumMagica.proxy.sendMana((PlayerEntity) entity);
		}
		return true;
	}
	
	protected static boolean summonBoltAtTarget(LivingEntity caster, World world, Vec3d pos) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		if (attr == null || attr.getMana() < 30) {
			return false;
		}
		
		int count = 1;
		if (caster != null && caster instanceof PlayerEntity) {
			// Look for lightning belt
			IInventory baubles = NostrumMagica.baubles.getBaubles((PlayerEntity) caster);
			if (baubles != null) {
				for (int i = 0; i < baubles.getSizeInventory(); i++) {
					ItemStack stack = baubles.getStackInSlot(i);
					if (stack.isEmpty() || !(stack.getItem() instanceof ItemMagicBauble)) {
						continue;
					}
					
					ItemType type = ItemMagicBauble.getTypeFromMeta(stack.getMetadata());
					if (type == ItemType.BELT_LIGHTNING) {
						count = caster.getRNG().nextInt(3) + 3;
						break;
					}
				}
			}
		}
		
		MutableBlockPos cursor = new MutableBlockPos();
		Random rand = (caster == null ? new Random() : caster.getRNG());
		for (int i = 0; i < count; i++) {
			
			if (i == 0) {
				world.addWeatherEffect(
						new NostrumTameLightning(world, pos.x, pos.y, pos.z)
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
					world.addWeatherEffect(
						new NostrumTameLightning(world, cursor.getX() + 0.5, cursor.getY(), cursor.getZ() + 0.5)
						);
				}
			}
		}
		
		attr.addMana(-30);
		if (caster instanceof PlayerEntity) {
			NostrumMagica.proxy.sendMana((PlayerEntity) caster);
		}
		return true;
	}
	
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
		if (element == EMagicElement.WIND) {
			SpellAction fly = new SpellAction(playerIn);
			fly.push(5.0f, level);
			fly.apply(target, 1.0f);
			stack.damageItem(2, playerIn);
			return true;
		}

        return false;
	}
	
	public static void spawnWhirlwindParticle(World world, int count, Vec3d pos, EntityAreaEffect cloud, int color, float gravity) {
		NostrumParticles.GLOW_ORB.spawn(world, new NostrumParticles.SpawnParams(count, pos.x, pos.y, pos.z,
				cloud.getRadius(),
				cloud.getRemainingTicks() / 4, 20,
				cloud.getEntityId())
					//.gravity(-.1f)
					//.gravity(.65f)
					.gravity(gravity)
					.color(color)
					.dieOnTarget(false)
					.setEntityBehavior(EntityBehavior.ORBIT)
			);
	}
}
