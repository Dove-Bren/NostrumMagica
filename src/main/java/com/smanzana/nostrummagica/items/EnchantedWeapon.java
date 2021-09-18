package com.smanzana.nostrummagica.items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.entity.EntityAreaEffect;
import com.smanzana.nostrummagica.entity.EntityAreaEffect.IAreaEntityEffect;
import com.smanzana.nostrummagica.entity.EntityAreaEffect.IAreaLocationEffect;
import com.smanzana.nostrummagica.entity.NostrumTameLightning;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble.ItemType;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.potions.LightningAttackPotion;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
		Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

		if (slot == EntityEquipmentSlot.MAINHAND) {
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
		} else if (slot == EntityEquipmentSlot.OFFHAND && element == EMagicElement.WIND) {
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
	public SpellAction getTriggerAction(EntityLivingBase user, boolean offense, ItemStack stack) {
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
	public boolean shouldTrigger(boolean offense, ItemStack stack) {
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
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		Vec3d dir = playerIn.getLookVec();
		dir = dir.addVector(0, -dir.y, 0).normalize();
		
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
//					cloud.motionX = dir.x;
//					cloud.motionY = dir.y;
//					cloud.motionZ = dir.z;
					
					spawnIceCloud(worldIn, playerIn, new Vec3d(playerIn.posX + dir.x, playerIn.posY + .75, playerIn.posZ + dir.z), dir, level);
					
					itemStackIn.damageItem(2, playerIn);
				}
				
				playerIn.resetCooldown();
			}
			
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		} else if (element == EMagicElement.WIND) {
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
//					cloud.motionX = dir.x;
//					cloud.motionY = dir.y;
//					cloud.motionZ = dir.z;
					
					spawnVortex(worldIn, playerIn, new Vec3d(playerIn.posX + dir.x, playerIn.posY + .75, playerIn.posZ + dir.z), dir, level);
					
					itemStackIn.damageItem(2, playerIn);
				}
				
				playerIn.resetCooldown();
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		} else if (element == EMagicElement.LIGHTNING && EnchantedArmor.GetSetCount(playerIn, EMagicElement.LIGHTNING, 3) == 4) {
			if (playerIn.getCooledAttackStrength(0.5F) > .95) {
				// If full set, strike at targetting location (unless sneaking, then strike self)
				if (!worldIn.isRemote) {
					boolean used = false;
					if (playerIn.isSneaking()) {
						used = summonBoltOnSelf(playerIn);
					} else if (playerIn.isPotionActive(LightningAttackPotion.instance())) {
						// This should be client-side... TODO do it on client and send via armor message?
						
						// Do quick mana check prior to actually doing raytrace. Redone inside helper func.
						INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
						if (attr != null && attr.getMana() >= 30) {
							final float maxDist = 50;
							RayTraceResult mop = RayTrace.raytrace(worldIn, playerIn.getPositionVector().addVector(0, playerIn.eyeHeight, 0), playerIn.getLookVec(), maxDist, (ent) -> { return ent != playerIn;});
							if (mop != null && mop.typeOfHit != RayTraceResult.Type.MISS) {
								final Vec3d at = (mop.typeOfHit == RayTraceResult.Type.ENTITY ? mop.entityHit.getPositionVector() : mop.hitVec);
								used = summonBoltAtTarget(playerIn, worldIn, at);
							}
						}
					}
					if (used) {
						itemStackIn.damageItem(1, playerIn);
					}
				}
				
				playerIn.resetCooldown();
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		}
			
        return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = playerIn.getHeldItem(hand);
		if (playerIn.getCooledAttackStrength(0.5F) > .95) {
			Vec3d dir = new Vec3d(pos).addVector(hitX, 0, hitZ).subtract(playerIn.getPositionVector());
			dir = dir.addVector(0, -dir.y, 0);
			dir = dir.normalize();
			if (element == EMagicElement.WIND) {
				if (!worldIn.isRemote) {
					spawnVortex(worldIn, playerIn, new Vec3d(pos.getX() + hitX, pos.getY() + 1, pos.getZ() + hitZ), dir, level);
					stack.damageItem(3, playerIn);
				}
				playerIn.resetCooldown();
				return EnumActionResult.SUCCESS;
			} else if (element == EMagicElement.ICE) {
				if (!worldIn.isRemote) { 
					spawnIceCloud(worldIn, playerIn, new Vec3d(pos.getX() + hitX, pos.getY() + 1, pos.getZ() + hitZ), dir, level);
					stack.damageItem(3, playerIn);
				}
				playerIn.resetCooldown();
				return EnumActionResult.SUCCESS;
			} else if (element == EMagicElement.LIGHTNING && EnchantedArmor.GetSetCount(playerIn, EMagicElement.LIGHTNING, 3) == 4) {
				if (!worldIn.isRemote) {
					boolean used = false;
					if (playerIn.isSneaking()) {
						used = summonBoltOnSelf(playerIn);
					} else if (playerIn.isPotionActive(LightningAttackPotion.instance())) {
						// This should be client-side... TODO do it on client and send via armor message?
						
						// Do quick mana check prior to actually doing raytrace. Redone inside helper func.
						INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
						if (attr != null && attr.getMana() >= 30) {
							final float maxDist = 50;
							RayTraceResult mop = RayTrace.raytrace(worldIn, playerIn.getPositionVector().addVector(0, playerIn.eyeHeight, 0), playerIn.getLookVec(), maxDist, (ent) -> { return ent != playerIn;});
							if (mop != null && mop.typeOfHit != RayTraceResult.Type.MISS) {
								final Vec3d at = (mop.typeOfHit == RayTraceResult.Type.ENTITY ? mop.entityHit.getPositionVector() : mop.hitVec);
								used = summonBoltAtTarget(playerIn, worldIn, at);
							}
						}
					}
					if (used) {
						stack.damageItem(1, playerIn);
					}
				}
				playerIn.resetCooldown();
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
		
	}
	
	protected static void spawnIceCloud(World world, EntityPlayer caster, Vec3d at, Vec3d direction, int level) {
		direction = direction.scale(5f/(3f * 20f)); // 5 blocks over 3 seconds
		EntityAreaEffect cloud = new EntityAreaEffect(world, at.x, at.y, at.z);
		cloud.setOwner(caster);
		cloud.setWaitTime(5);
		cloud.setRadius(0.5f);
		cloud.setRadiusPerTick((1f + level * .75f) / (20f * 3)); // 1 (+ .75 per extra level) extra radius per 3 seconds
		cloud.setDuration((int) (20 * (3 + level * .5f))); // 3 seconds + a half a second per extra level
		cloud.addEffect(new PotionEffect(FrostbitePotion.instance(), 20 * 10));
		cloud.addEffect((IAreaLocationEffect)(worldIn, pos) -> {
			IBlockState state = worldIn.getBlockState(pos);
			if (state.getMaterial() == Material.WATER
					&& (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER)) {
				worldIn.setBlockState(pos, Blocks.ICE.getDefaultState());
			}
		});
		cloud.setVerticleStepping(true);
		cloud.setGravity(true, .1);
		//cloud.setWalksWater();
		world.spawnEntity(cloud);
		cloud.motionX = direction.x;
		cloud.motionY = direction.y;
		cloud.motionZ = direction.z;
	}
	
	protected static void spawnVortex(World world, EntityPlayer caster, Vec3d at, Vec3d direction, int level) {
		final int hurricaneCount = EnchantedArmor.GetSetCount(caster, EMagicElement.WIND, 3);
		direction = direction.scale(5f/(3f * 20f)); // 5 blocks over 10 seconds
		EntityAreaEffect cloud = new EntityAreaEffect(world, at.x, at.y, at.z);
		cloud.setOwner(caster);
		cloud.setWaitTime(10);
		cloud.setRadius(.5f);
		cloud.setRadiusPerTick((.25f + level * .5f) / (20f * 10));
		cloud.setDuration((int) (20 * (3 + level * .5f))); // 3 seconds + a half a second per extra level
		cloud.addEffect((IAreaEntityEffect)(worldIn, entity) -> {
			if (entity.noClip || entity.hasNoGravity()) {
				return;
			}
			if (hurricaneCount == 4 && entity == caster) {
				// Only affect caster if they jump
				if (entity.onGround) {
					return;
				}
			}
			// upward effect
			final int period = 20;
			final float prog = ((float) (entity.ticksExisted % period) / (float) period);
			final double dy = (Math.sin(prog * 2 * Math.PI) + 1) / 2;
			final Vec3d target = new Vec3d(cloud.posX, cloud.posY + 2 + dy, cloud.posZ);
			final Vec3d diff = target.subtract(entity.getPositionVector());
			entity.motionX = diff.x / 2;
			entity.motionY = diff.y / 2;
			entity.motionZ = diff.z / 2;
			entity.velocityChanged = true;
			//entity.posY = 2 + dy;
			//entity.setPositionAndUpdate(cloud.posX, cloud.posY + 2 + dy, cloud.posZ);
		});
		cloud.setVerticleStepping(true);
		cloud.setGravity(true, 1);
		cloud.setRadiusPerFall(.1f);
		cloud.setWalksWater();
		cloud.setEffectDelay(0);
		cloud.setWaddle(direction, 1);
		cloud.setParticle(EnumParticleTypes.SWEEP_ATTACK);
		cloud.setParticleParam1(10);
		world.spawnEntity(cloud);
		cloud.motionX = direction.x;
		cloud.motionY = direction.y;
		cloud.motionZ = direction.z;
	}
	
	protected static boolean summonBoltOnSelf(EntityLivingBase entity) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr == null || attr.getMana() < 30) {
			return false;
		}
		
		entity.world.addWeatherEffect(
				new NostrumTameLightning(entity.world, entity.posX, entity.posY, entity.posZ)
				);
		attr.addMana(-30);
		if (entity instanceof EntityPlayer) {
			NostrumMagica.proxy.sendMana((EntityPlayer) entity);
		}
		return true;
	}
	
	protected static boolean summonBoltAtTarget(EntityLivingBase caster, World world, Vec3d pos) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		if (attr == null || attr.getMana() < 30) {
			return false;
		}
		
		int count = 1;
		if (caster != null && caster instanceof EntityPlayer) {
			// Look for lightning belt
			IInventory baubles = NostrumMagica.baubles.getBaubles((EntityPlayer) caster);
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
		if (caster instanceof EntityPlayer) {
			NostrumMagica.proxy.sendMana((EntityPlayer) caster);
		}
		return true;
	}
	
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		if (element == EMagicElement.WIND) {
			SpellAction fly = new SpellAction(playerIn);
			fly.push(5.0f, level);
			fly.apply(target, 1.0f);
			stack.damageItem(2, playerIn);
			return true;
		}

        return false;
	}
	
}
