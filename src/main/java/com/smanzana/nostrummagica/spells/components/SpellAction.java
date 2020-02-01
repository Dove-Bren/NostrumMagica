package com.smanzana.nostrummagica.spells.components;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.baubles.items.ItemMagicBauble;
import com.smanzana.nostrummagica.baubles.items.ItemMagicBauble.ItemType;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.MagicWall;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.EntityGolem;
import com.smanzana.nostrummagica.entity.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.EntityGolemEnder;
import com.smanzana.nostrummagica.entity.EntityGolemFire;
import com.smanzana.nostrummagica.entity.EntityGolemIce;
import com.smanzana.nostrummagica.entity.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.EntityGolemWind;
import com.smanzana.nostrummagica.entity.NostrumTameLightning;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.potions.FamiliarPotion;
import com.smanzana.nostrummagica.potions.MagicBoostPotion;
import com.smanzana.nostrummagica.potions.MagicBuffPotion;
import com.smanzana.nostrummagica.potions.MagicResistPotion;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.SpellActionSummary;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeHell;
import net.minecraftforge.oredict.OreDictionary;

public class SpellAction {
	
	public static class MagicDamageSource extends EntityDamageSource {
		
		private EMagicElement element;
		
		public MagicDamageSource(Entity source, EMagicElement element) {
			super("nostrummagic", source);
			this.element = element;
			
			this.setDamageBypassesArmor();
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
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float eff);
		public void apply(EntityLivingBase caster, World world, BlockPos block, float eff);
	}
	
	private class DamageEffect implements SpellEffect {
		private float amount;
		private EMagicElement element;
		
		public DamageEffect(EMagicElement element, float amount) {
			this.amount = amount;
			this.element = element;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			float fin = calcDamage(caster, entity, amount * efficiency, element);
			source.setLastAttacker(entity);
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
			
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos, float efficiency) {
			; // Do nothing
		}
	}
	
	private class HealEffect implements SpellEffect {
		private float amount;
		
		public HealEffect(float amount) {
			this.amount = amount;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			entity.heal(amount * efficiency);
			
			NostrumMagicaSounds.STATUS_BUFF2.play(entity);
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos, float efficiency) {
			; // Do nothing
		}
	}
	
	private class HealFoodEffect implements SpellEffect {
		private int amount;
		
		public HealFoodEffect(int amount) {
			this.amount = amount;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			if (entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entity;
				player.getFoodStats().addStats((int) (amount * efficiency), 2);
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
			} else if (entity instanceof EntityAnimal && caster != null && 
					caster instanceof EntityPlayer) {
				((EntityAnimal) entity)
					.setInLove((EntityPlayer) caster);
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
			} else {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
			}
			
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos, float efficiency) {
			; // Do nothing
		}
	}
	
	private class HealManaEffect implements SpellEffect {
		private int amount;
		
		public HealManaEffect(int amount) {
			this.amount = amount;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			INostrumMagic magic = NostrumMagica.getMagicWrapper(entity);
			if (magic == null) {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
			} else {
				magic.addMana((int) (amount * efficiency));
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
			}
				
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos, float efficiency) {
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
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			entity.addPotionEffect(new PotionEffect(effect, (int) (duration * efficiency), amp));
			
			if (effect.isBadEffect()) {
				caster.setLastAttacker(entity);
				entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
				NostrumMagicaSounds.STATUS_DEBUFF2.play(entity);
			} else {
				NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			}
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos, float efficiency) {
			; // Do nothing
		}
	}
	
	private class DispelEffect implements SpellEffect {
		private int number; // -1 to clear all
		
		public DispelEffect(int number) {
			this.number = number; 
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
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
		public void apply(EntityLivingBase caster, World world, BlockPos pos, float efficiency) {
			; // Do nothing
		}
	}
	
	private class BlinkEffect implements SpellEffect {
		private float dist;
		
		public BlinkEffect(float dist) {
			this.dist = dist;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			
			if (caster != null && caster instanceof EntityPlayer) {
				// Look for lightning belt
				IInventory baubles = NostrumMagica.baubles.getBaubles((EntityPlayer) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getSizeInventory(); i++) {
						ItemStack stack = baubles.getStackInSlot(i);
						if (stack == null || !(stack.getItem() instanceof ItemMagicBauble)) {
							continue;
						}
						
						ItemType type = ItemMagicBauble.getTypeFromMeta(stack.getMetadata());
						if (type == ItemType.BELT_ENDER) {
							efficiency *= 2;
							break;
						}
					}
				}
			}
			
			// Apply efficiency bonus
			float dist = this.dist * efficiency;
			
			Vec3d dest;
			Vec3d direction = entity.getLookVec().normalize();
			Vec3d source = entity.getPositionVector();
			source = source.addVector(0, entity.getEyeHeight(), 0);
			BlockPos bpos;
			Vec3d translation = new Vec3d(direction.xCoord * dist,
					direction.yCoord * dist,
					direction.zCoord * dist);
			
			// Find ideal dest (vect addition). Can we go there? Then go there.
			// Else step backwards and raycast forward in 1/5 increments.
			// See if place we hit is same spot as raycast. If so, fail and do again
			
			dest = source.add(translation);
			bpos = new BlockPos(dest.xCoord, dest.yCoord, dest.zCoord);
			if (isPassable(entity.worldObj, bpos)
				&& isPassable(entity.worldObj, bpos.add(0, 1, 0))) {
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
						from = new Vec3d(translation.xCoord * curDist,
								translation.yCoord * curDist,
								translation.zCoord * curDist);
						from = source.add(from);
					}
					
					RayTraceResult mop = entity.worldObj.rayTraceBlocks(from, endpoint, false);
					if (mop != null && mop.hitVec.distanceTo(from) > 0.5) {
						// We got one
						BlockPos pos = new BlockPos(mop.hitVec);
						if (isPassable(entity.worldObj, pos) && isPassable(entity.worldObj, pos.add(0, 1, 0))) {
							dest = mop.hitVec;
							break;
						}
					}
					
					i--;
				}
			}
			
			if (dest != null) {
				entity.setPositionAndUpdate(.5 + Math.floor(dest.xCoord), Math.floor(dest.yCoord), .5 + Math.floor(dest.zCoord));
				NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			}
		}
		
		private boolean isPassable(World world, BlockPos pos) {
			if (world.isAirBlock(pos))
				return true;
			
			IBlockState state = world.getBlockState(pos);
			
			if (state == null)
				return true;
			if (state.isTranslucent())
				return true;
			if (state.getMaterial().isLiquid())
				return true;
			
			return false;
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos, float efficiency) {
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
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			apply(caster, entity.worldObj, entity.getPosition(), efficiency);
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos, float efficiency) {

			// We abs the amp here, but check it belwo for pull and negate vector
			float magnitude = .35f * (Math.abs(amp) + 1.0f) * (float) Math.min(2.0f, Math.max(0.0f, 1.0f + Math.log(efficiency)));
			Vec3d center = new Vec3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
			NostrumMagicaSounds.DAMAGE_WIND.play(world, center.xCoord, center.yCoord, center.zCoord);
			
			for (Entity e : world.getEntitiesWithinAABBExcludingEntity(null, 
					new AxisAlignedBB(center.xCoord - range, center.yCoord - range, center.zCoord - range, center.xCoord + range, center.yCoord + range, center.zCoord + range)
					)) {
				double dist = e.getPositionVector().distanceTo(center); 
				if (dist <= range) {
					
					// If push, straight magnitude
					// If pull, cap magnitude so that it doesn't fly past player
					
					Vec3d force;
					Vec3d direction = e.getPositionVector().addVector(0, e.getEyeHeight(), 0).subtract(center).normalize();
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
							mod = (dist * .4) / mod;
							force = new Vec3d(
									force.xCoord * mod,
									force.yCoord * mod,
									force.zCoord * mod
									);
						}

						force = new Vec3d(
								force.xCoord * -1.0,
								force.yCoord * -1.0,
								force.zCoord * -1.0);
					}
					
					e.addVelocity(force.xCoord, force.yCoord, force.zCoord);
				}
			}
			
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
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
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
		public void apply(EntityLivingBase caster, World world, BlockPos pos, float efficiency) {
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
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			int duration = (int) (this.duration * efficiency);
			if (duration == 0)
				return; // Nope
			
			NostrumMagicaSounds.DAMAGE_FIRE.play(entity);
			
			caster.setLastAttacker(entity);
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
			
			entity.setFire((int) Math.ceil((float) duration / 20.0f));
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block, float efficiency) {
			IBlockState state = world.getBlockState(block);
			if (state != null && state.getBlock() instanceof Candle) {
				Candle.light(world, block, state);
				NostrumMagicaSounds.DAMAGE_FIRE.play(world,
						block.getX() + .5, block.getY(), block.getZ() + .5);
				return;
			}
			
			if (world.provider.getDimension() == ModConfig.config.sorceryDimensionIndex()) {
				return;
			}
			
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
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
			apply(caster, entity.worldObj, entity.getPosition(), efficiency);
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block, float efficiency) {
			
			int count = 1;
			
			if (caster != null && caster instanceof EntityPlayer) {
				// Look for lightning belt
				IInventory baubles = NostrumMagica.baubles.getBaubles((EntityPlayer) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getSizeInventory(); i++) {
						ItemStack stack = baubles.getStackInSlot(i);
						if (stack == null || !(stack.getItem() instanceof ItemMagicBauble)) {
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
				
				world.addWeatherEffect(
					new NostrumTameLightning(world, cursor.getX() + 0.5, cursor.getY(), cursor.getZ() + 0.5)
					);
			}
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
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
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
			
			apply(caster, world, pos, efficiency);
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block, float efficiency) {
			NostrumMagica.getMagicWrapper(caster).clearFamiliars();
			caster.removeActivePotionEffect(FamiliarPotion.instance());
			for (int i = 0; i < power; i++) {
				EntityGolem golem = spawnGolem(world);
				golem.setPosition(block.getX() + .5, block.getY(), block.getZ() + .5);
				world.spawnEntityInWorld(golem);
				golem.setOwnerId(caster.getPersistentID());
				NostrumMagica.getMagicWrapper(caster).addFamiliar(golem);
			}
			int time = (int) (20 * 60 * 2.5 * Math.pow(2, Math.max(0, power - 1)) * efficiency);
			caster.addPotionEffect(new PotionEffect(FamiliarPotion.instance(), time, 0) {
				@Override
				public boolean onUpdate(EntityLivingBase entityIn) {
					// heh snekky
					boolean ret = super.onUpdate(entityIn);
					if (ret) {
						// we're not being removed. Check familiars
						if (entityIn.worldObj.isRemote) {
							return true;
						}
						
						INostrumMagic attr = NostrumMagica.getMagicWrapper(entityIn);
						if (attr != null) {
							boolean active = false;
							if (!attr.getFamiliars().isEmpty()) {
								for (EntityLivingBase fam : attr.getFamiliars()) {
									if (fam.isEntityAlive()) {
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
				golem = new EntityGolemEnder(world);
				break;
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
	
	private static class SwapEffect implements SpellEffect {
		
		public SwapEffect() {
			
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			if (caster == null || entity == null)
				return;
			
			Vec3d pos = caster.getPositionVector();
			float pitch = caster.rotationPitch;
			float yaw = caster.rotationYawHead;
			
			if (caster instanceof EntityPlayer) {
				caster.setPositionAndRotation(caster.posX, caster.posY, caster.posZ, entity.rotationYawHead, entity.rotationPitch);
				caster.setPositionAndUpdate(
						entity.posX, entity.posY, entity.posZ);
			} else {
				caster.setPositionAndRotation(
						entity.posX, entity.posY, entity.posZ,
						entity.rotationPitch, entity.rotationYawHead
						);
			}
			
			if (entity instanceof EntityPlayer) {
				entity.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, yaw, pitch);
				entity.setPositionAndUpdate(pos.xCoord, pos.yCoord, pos.zCoord);
			} else {
				entity.setPositionAndRotation(pos.xCoord, pos.yCoord, pos.zCoord, yaw, pitch);
			}
			
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos, float efficiency) {
			caster.setPositionAndUpdate(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		}
	}
	
	private static class PropelEffect implements SpellEffect {
		
		int level;
		
		public PropelEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {

			Vec3d force = entity.getLookVec().addVector(0, 0.15, 0).normalize();
			float scale = 1f * (.5f * (level + 1)) * (float) (Math.max(0.0, Math.min(2.0, 1.0 - Math.log(efficiency))));
			
			force = new Vec3d(force.xCoord * scale, force.yCoord * scale, force.zCoord * scale);
			
			entity.motionX += force.xCoord;
			entity.motionY += force.yCoord;
			entity.motionZ += force.zCoord;
			entity.velocityChanged = true;
			
			NostrumMagicaSounds.DAMAGE_WIND.play(entity);
			
		}
		
		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos pos, float efficiency) {
			; // Doesn't mean anything
		}
	}
	
	private static class PhaseEffect implements SpellEffect {
		
		private int level;
		
		public PhaseEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			if (caster != entity && entity instanceof EntityLiving) {
				// Make sure they want to attack you if you do it
				entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
				entity.hurtResistantTime = 0;
			}
			
			double radius = 32.0 * level * efficiency;
			
			if (caster != null && caster instanceof EntityPlayer) {
				// Look for ender belt
				IInventory baubles = NostrumMagica.baubles.getBaubles((EntityPlayer) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getSizeInventory(); i++) {
						ItemStack stack = baubles.getStackInSlot(i);
						if (stack == null || !(stack.getItem() instanceof ItemMagicBauble)) {
							continue;
						}
						
						ItemType type = ItemMagicBauble.getTypeFromMeta(stack.getMetadata());
						if (type == ItemType.BELT_ENDER) {
							radius *= 2.0;
							break;
						}
					}
				}
			}
			
			NostrumMagicaSounds.DAMAGE_ENDER.play(entity);
			
			for (int i = 0; i < 20; i++) {
			
				// Find a random place to teleport
		        double x = entity.posX + (NostrumMagica.rand.nextDouble() - 0.5D) * radius;
		        double y = entity.posY + (double)(NostrumMagica.rand.nextInt((int) radius) - (int) radius / 2.0);
		        double z = entity.posZ + (NostrumMagica.rand.nextDouble() - 0.5D) * radius;
	
			    // Try to teleport
		        if (entity.attemptTeleport(x, y, z))
		        	break;
			}
			
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block, float efficiency) {
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
					entity = new EntityItem(world,
							x, y, z,
							new ItemStack(Items.ENDER_PEARL));
				} else if (NostrumMagica.rand.nextFloat() <= .3) {
					entity = new EntityEnderman(world);
				} else {
					entity = new EntityEndermite(world);
				}
				
				entity.setPosition(x + (NostrumMagica.rand.nextFloat() - .5),
									y,
									z + (NostrumMagica.rand.nextFloat() - .5));
				
				world.spawnEntityInWorld(entity);
			}
			
			NostrumMagicaSounds.DAMAGE_ENDER.play(world, block.getX(), block.getY(), block.getZ());
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
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			
			ItemStack inhand = entity.getHeldItemMainhand();
			boolean offhand = false;
			if (inhand == null) {
				inhand = entity.getHeldItemOffhand();
				offhand = true;
			}
			
			if (inhand == null)
				return;

			ItemStack addedItem = null;
			boolean didEmpower = false;
			
			Item item = inhand.getItem();
			if ((item instanceof InfusedGemItem && inhand.getMetadata() == 0)) {
				int count = (int) Math.pow(2, level - 1);
				addedItem = InfusedGemItem.instance().getGem(element, count);
			} else if (item instanceof EssenceItem) {
				int count = level + 1;
				double amt = 2 + level;
				didEmpower = true;
				caster.removeActivePotionEffect(MagicBuffPotion.instance());
				NostrumMagica.magicEffectProxy.applyMagicBuff(entity, element, amt, count);
				entity.addPotionEffect(new PotionEffect(MagicBuffPotion.instance(), 60 * 20, 0));
			}
			
			
			
			if (addedItem == null && !didEmpower) {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
			} else {
				if (entity instanceof EntityPlayer) {
					EntityPlayer p = (EntityPlayer) entity;
					if (inhand.stackSize == 1) {
						if (offhand) {
							p.inventory.removeStackFromSlot(40);
						} else {
							p.inventory.removeStackFromSlot(p.inventory.currentItem);
						}
					} else {
						inhand.splitStack(1);
					}
					if (addedItem != null) {
						((EntityPlayer) entity).inventory.addItemStackToInventory(addedItem);
					}
					
					
				} else {
					// EntityLiving has held item in slot 0
					if (addedItem != null) {
						entity.setHeldItem(EnumHand.MAIN_HAND, addedItem);
					}
				}
				NostrumMagicaSounds.CAST_CONTINUE.play(entity);
			}
			
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block, float efficiency) {
			;
		}
		
	}
	
	private static class GrowEffect implements SpellEffect {
		
		private int count;
		
		public GrowEffect(int count) {
			this.count = count;
		}

		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			if (entity instanceof EntityAnimal) {
				EntityAnimal animal = (EntityAnimal) entity;
				animal.addGrowth((int) (count * 500 * efficiency));
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
			}
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block, float efficiency) {
			if (world.isAirBlock(block))
				block = block.add(0, -1, 0);
			ItemStack junk = new ItemStack(Items.DYE, 10);
			for (int i = 0; i < count; i++)
				ItemDye.applyBonemeal(junk, world, block);
			
			NostrumMagicaSounds.STATUS_BUFF2.play(world, block.getX(), block.getY(), block.getZ());
		}
	}
	
	private static class BurnArmorEffect implements SpellEffect {
		
		private int level;
		
		public BurnArmorEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			int amount = (int) (20 * level * efficiency);
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
			caster.setLastAttacker(entity);
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block, float efficiency) {
			;
		}
		
	}
	
	private static class WallEffect implements SpellEffect {
		
		private int level;
		
		public WallEffect(int level) {
			this.level = level;
		}

		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			apply(caster, entity.worldObj, entity.getPosition().add(0, 1, 0), efficiency);
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block, float efficiency) {
			if (!world.isAirBlock(block))
				block = block.add(0, 1, 0);
			
			if (!world.isAirBlock(block)) {
				NostrumMagicaSounds.CAST_FAIL.play(world, block.getX(), block.getY(), block.getZ());
			} else {
				NostrumMagicaSounds.DAMAGE_WIND.play(world, block.getX(), block.getY(), block.getZ());
				world.setBlockState(block, MagicWall.instance().getState(level));
			}
				
		}
		
	}

	private static class CursedIce implements SpellEffect {
		
		private int level;
		
		public CursedIce(int level) {
			this.level = level;
		}

		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			apply(caster, entity.worldObj, entity.getPosition().add(0, 1, 0), efficiency);
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block, float efficiency) {
			
			world.setBlockState(block, com.smanzana.nostrummagica.blocks.CursedIce.instance().getState(level));
			NostrumMagicaSounds.DAMAGE_ICE.play(world, block.getX(), block.getY(), block.getZ());
			
		}
	}
	
	private static class GeoBlock implements SpellEffect {
		
		private int level;
		
		public GeoBlock(int level) {
			this.level = level;
		}

		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
			apply(caster, entity.worldObj, entity.getPosition().add(0, -1, 0), efficiency);
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block, float efficiency) {
			
			Block result;
			float temp = world.getBiome(block).getFloatTemperature(block);
			// < 0 exists for icy places
			// .1 to .2 has somethign to do with snow
			// desert is 2.0
			// Plains are just below 1
			if (world.getBiome(block) instanceof BiomeHell) {
				if (level == 1)
					result = Blocks.NETHERRACK;
				else if (level == 2)
					result = Blocks.LAVA;
				else
					if (NostrumMagica.rand.nextFloat() < 0.3f)
						result = Blocks.QUARTZ_ORE;
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
						result = Blocks.QUARTZ_ORE;
					else
						result = Blocks.GLOWSTONE;
			}
			
			world.setBlockState(block, result.getDefaultState());
			NostrumMagicaSounds.DAMAGE_FIRE.play(world, block.getX(), block.getY(), block.getZ());
			
		}
	}
	
	private static class BreakEffect implements SpellEffect {

		private static List<ItemStack> StoneItems = null;
		
		private static List<ItemStack> GetStone() {
			if (StoneItems == null) {
				StoneItems = OreDictionary.getOres("stone");
			}
			
			return StoneItems;
		}
		
		private int level;
		
		public BreakEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(EntityLivingBase caster, EntityLivingBase entity, float eff) {
			apply(caster, entity.worldObj, entity.getPosition().add(0, -1, 0), eff);
		}

		@Override
		public void apply(EntityLivingBase caster, World world, BlockPos block, float eff) {
			if (world.isAirBlock(block))
				return;
			
			if (world.provider.getDimension() == ModConfig.config.sorceryDimensionIndex()) {
				return;
			}
			
			IBlockState state = world.getBlockState(block);
			if (state == null || state.getMaterial().isLiquid())
				return;
			
			boolean onlyStone = (level <= 1);
			if (onlyStone && caster instanceof EntityPlayer) {
				if (!OreDictionary.containsMatch(false, GetStone(), state.getBlock().getPickBlock(state, null, world, block, (EntityPlayer) caster))) {
					return;
				}
			}
			
			float hardness = state.getBlockHardness(world, block);
			
			
			if (this.level < 3) {
				int harvestLevel = state.getBlock().getHarvestLevel(state);
				if (harvestLevel > 1)
					return;
				
				if (hardness >= 10f || hardness < 0f)
					return;
			}
			
			if (hardness >= 100f || hardness < 0f)
				return;
			
			if (caster instanceof EntityPlayerMP) {
				((EntityPlayerMP) caster).interactionManager.tryHarvestBlock(block);
			} else {
				world.destroyBlock(block, true);
			}
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
//		public void apply(EntityLivingBase caster, EntityLivingBase entity, float efficiency) {
//			ItemStack inhand = entity.getHeldItemMainhand();
//			boolean offhand = false;
//			if (inhand == null) {
//				inhand = entity.getHeldItemOffhand();
//				offhand = true;
//			}
//			
//			if (inhand == null)
//				return;
//			
//			Item item = inhand.getItem();
//			 else {
//				NostrumMagicaSounds.CAST_FAIL.play(entity);
//			}
//		}
//
//		@Override
//		public void apply(EntityLivingBase caster, World world, BlockPos block, float efficiency) {
//			; // No effect
//		}
//		
//	}
	
	private EntityLivingBase source;
	private List<SpellEffect> effects;
	private String nameKey;
	
	public SpellAction(EntityLivingBase source) {
		this.source = source;
		effects = new LinkedList<>();
	}
	
	public void apply(EntityLivingBase entity, float efficiency) {
		if (entity.worldObj.isRemote)
			return;
		
		final EntityLivingBase ent = entity;
		
		SpellActionSummary summary = new SpellActionSummary(this, efficiency);
		
		NostrumMagica.playerListener.onMagicEffect(entity, source, summary);
		if (!summary.wasCancelled()) {
			for (SpellEffect e : effects) {
				final SpellEffect effect = e;
				entity.getServer().addScheduledTask(new Runnable() {

					@Override
					public void run() {
						effect.apply(source, ent, summary.getEfficiency());
					}
					
				});
			}
		}
	}
	
	public void apply(World world, BlockPos pos, float efficiency) {
		if (world.isRemote)
			return;
		
		final World w = world;
		final BlockPos b = pos;
		
		for (SpellEffect e : effects) {
			final SpellEffect effect = e;
			world.getMinecraftServer().addScheduledTask(new Runnable() {

				@Override
				public void run() {
					effect.apply(source, w, b, efficiency);
				}
				
			});
		}
	}
	
	public static final float calcDamage(EntityLivingBase caster, EntityLivingBase target, float base, EMagicElement element) {
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
		
		PotionEffect boostEffect = caster.getActivePotionEffect(MagicBoostPotion.instance());
		if (boostEffect != null) {
			base *= Math.pow(1.5, boostEffect.getAmplifier() + 1);
		}
		
		PotionEffect resEffect = target.getActivePotionEffect(MagicResistPotion.instance());
		if (resEffect != null) {
			base *= Math.pow(.75, resEffect.getAmplifier() + 1);
		}
		
		IAttributeInstance attr = target.getEntityAttribute(AttributeMagicResist.instance());
		if (attr != null && attr.getAttributeValue() != 0.0D) {
			base *= Math.max(0.0D, Math.min(2.0D, 1.0D - (attr.getAttributeValue() / 100.0D)));
		}
			
		switch (element) {
		case ENDER:
			if (ender) return 0.0f; // does not affect ender
			return base * 1.2f; // return raw damage (+20%) not affected by armor
		case LIGHTNING:
			return base * (.75f + ((float) armor / 20f)); // double in power for every 20 armor
		case FIRE:
			return base * (undead ? 1.5f : 1f); // 1.5x damage against undead. Regular otherwise
		case EARTH:
			return base; // raw damage. Not affected by armor
		case ICE:
			return base * (undead ? .6f : 1.3f); // More affective against everything except undead
		case WIND:
			return base * (light ? 1.8f : .8f); // 180% against light (endermen included) enemies
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
