package com.smanzana.nostrummagica.listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.block.NostrumPortal;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.mirror.MirrorResearchSubscreen;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.enchantment.EnchantmentManaRecovery;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.WolfTypeCapability;
import com.smanzana.nostrummagica.item.IReactiveEquipment;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.item.armor.MagicArmor;
import com.smanzana.nostrummagica.item.equipment.HookshotItem;
import com.smanzana.nostrummagica.item.equipment.ReagentBag;
import com.smanzana.nostrummagica.item.equipment.RuneBag;
import com.smanzana.nostrummagica.item.equipment.ThanoPendant;
import com.smanzana.nostrummagica.item.equipment.ThanosStaff;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellActionSummary;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.tile.TeleportRuneTileEntity;
import com.smanzana.nostrummagica.util.Projectiles;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

/**
 * I lied. It's actually the one and only listener. It listens to time, too. And
 * entities in general. And soul music.
 * @author Skyler
 *
 */
public class PlayerListener {
	
	public enum Event {
		TIME,
		PROXIMITY,
		POSITION,
		DAMAGED,
		HEALTH,
		FOOD,
		MANA,
		MAGIC_EFFECT,
	}
	
	public interface IMagicListener<T> {
		/**
		 * Called for each event that is activated.
		 * @param type The event type this call matches
		 * @param entity The entity (null for Time events) involved. Damaged events
		 * set this to the entity that did the damaging.
		 * @return true to remove this listener so it doesn't receive anymore updates
		 */
		public boolean onEvent(Event type, LivingEntity entity, T data);
	}
	
	/**
	 * Listener that just doesn't use data
	 */
	public interface IGenericListener extends IMagicListener<Object> {};
	
	public static class SpellActionListenerData {
		/**
		 * Entity being affected
		 */
		public LivingEntity entity;
		
		/**
		 * Entity that cast the spell. Can be empty.
		 */
		@Nullable
		public LivingEntity caster;
		
		/**
		 * Information about the spell being applied
		 */
		public SpellActionSummary summary;
		
		public SpellActionListenerData(LivingEntity entity, @Nullable LivingEntity caster, SpellActionSummary summary) {
			this.entity = entity;
			this.caster = caster;
			this.summary = summary;
		}
	}
	
	/**
	 * Listener that receives information about a spell/spell effect
	 */
	public interface ISpellActionListener extends IMagicListener<SpellActionListenerData> {};

	private class TimeInfo {
		public int startTick;
		public int interval;
		public int delay; // 0 for no delay
		
		public TimeInfo(int startTick, int delay, int interval) {
			this.interval = interval;
			this.startTick = startTick;
			this.delay = delay;
		}
	}
	
	private class ProximityInfo {
		public World world;
		
		// Proximity based
		public Vector3d position;
		public double proximity;
		
		public ProximityInfo(World world, Vector3d position, double proximity) {
			this.world = world;
			this.position = position;
			this.proximity = proximity;
		}
	}
	
	private class PositionInfo {
		public World world;
		
		// Tile based
		public Collection<BlockPos> blocks;
		
		public PositionInfo(World world, Collection<BlockPos> blocks) {
			this.world = world;
			this.blocks = blocks;
		}
	}
	
	private class DamagedInfo {
		public LivingEntity entity;
		
		public DamagedInfo(LivingEntity entity) {
			this.entity = entity;
		}
	}
	
	private class HealthInfo {
		public LivingEntity entity;
		public float threshold; // percentage out of 1
		public boolean higher;
		public HealthInfo(LivingEntity entity, float threshold, boolean higher) {
			this.entity = entity;
			this.threshold = threshold;
			this.higher = higher;
		}
	}
	
	private class FoodInfo {
		public LivingEntity entity;
		public int threshold;
		public boolean higher;
		public FoodInfo(LivingEntity entity, int threshold, boolean higher) {
			this.entity = entity;
			this.threshold = threshold;
			this.higher = higher;
		}
	}
	
	private class ManaInfo {
		public LivingEntity entity;
		public float threshold;
		public boolean higher;
		public ManaInfo(LivingEntity entity, float threshold, boolean higher) {
			this.entity = entity;
			this.threshold = threshold;
			this.higher = higher;
		}
	}
	
	private class MagicEffectInfo {
		public LivingEntity entity;
		public MagicEffectInfo(@Nullable LivingEntity entity) {
			this.entity = entity;
		}
	}
	
	private int tickCount;
	private Map<IGenericListener, TimeInfo> timeInfos;
	private Map<IGenericListener, ProximityInfo> proximityInfos;
	private Map<IGenericListener, PositionInfo> positionInfos;
	private Map<IGenericListener, DamagedInfo> damagedInfos;
	private Map<IGenericListener, HealthInfo> healthInfos;
	private Map<IGenericListener, FoodInfo> foodInfos;
	private Map<IGenericListener, ManaInfo> manaInfos;
	private Map<ISpellActionListener, MagicEffectInfo> magicEffectInfos;
	
	public PlayerListener() {
		timeInfos = new ConcurrentHashMap<>();
		proximityInfos = new ConcurrentHashMap<>();
		positionInfos = new ConcurrentHashMap<>();
		damagedInfos = new ConcurrentHashMap<>();
		healthInfos = new ConcurrentHashMap<>();
		foodInfos = new ConcurrentHashMap<>();
		manaInfos = new ConcurrentHashMap<>();
		magicEffectInfos = new ConcurrentHashMap<>();
		
		MinecraftForge.EVENT_BUS.register(this);
		tickCount = 0;
	}
	
	public void clearAll() {
		timeInfos.clear();
		proximityInfos.clear();
		positionInfos.clear();
		damagedInfos.clear();
		healthInfos.clear();
		foodInfos.clear();
		manaInfos.clear();
		magicEffectInfos.clear();
		MirrorResearchSubscreen.ResetSeenCache();
	}
	
	/**
	 * Registered a timed-based listener.
	 * The listener will get a TIME callback when:
	 * delay has expired or
	 * on an interval (from after delay has expired)
	 * @param listener
	 * @param delay 0 means no delay
	 * @param interval
	 */
	public void registerTimer(IGenericListener listener, int delay, int interval) {
		timeInfos.put(listener,
				new TimeInfo(tickCount, delay, interval));
	}
	
	/**
	 * Listen for entities moving within proximity of the given position.
	 * Matches any living entity (not all entities -- arrows won't be matched!)
	 * @param listener
	 * @param world
	 * @param pos
	 * @param range negative just won't work. :)
	 */
	public void registerProximity(IGenericListener listener, 
			World world, Vector3d pos, double range) {
		proximityInfos.put(listener,
				new ProximityInfo(world, pos, range));
	}
	
	/**
	 * Listens for living entities that step in the provided collection
	 * of blocks.
	 * @param listener
	 * @param world
	 * @param blocks
	 */
	public void registerPosition(IGenericListener listener,
			World world, Collection<BlockPos> blocks) {
		positionInfos.put(listener,
				new PositionInfo(world, blocks));
	}
	
	/**
	 * Listen for an entity being hit by another living entity.
	 * Does not trigger on arrows shot from dispensers or other projectiles
	 * with no tied living shooter
	 * @param listener
	 * @param entity
	 */
	public void registerHit(IGenericListener listener,
			LivingEntity entity) {
		damagedInfos.put(listener,
				new DamagedInfo(entity));
	}
	
	/**
	 * Listens for some health condition.
	 * @param listener
	 * @param entity The entity to listen to
	 * @param level The level that's critical. THIS IS BETWEEN 0 and 1! It's a fraction!
	 * @param higher if true, triggers when health is >=. Otherwise, triggers when health <=
	 */
	public void registerHealth(IGenericListener listener,
			LivingEntity entity, float level, boolean higher) {
		healthInfos.put(listener,
				new HealthInfo(entity, level, higher));
	}
	
	/**
	 * Listens for some critical food level
	 * @param listener
	 * @param player
	 * @param level The level to listen for. This is in food points
	 * @param higher If true, triggers when food is greater than or equal to. Otherwise, LTE
	 */
	public void registerFood(IGenericListener listener,
			PlayerEntity player, int level, boolean higher) {
		foodInfos.put(listener,
				new FoodInfo(player, level, higher));
	}
	
	/**
	 * Listens for changes in mana level. Triggers when it drops below or above
	 * the indicated level.
	 * @param listener
	 * @param entity The entity. If the entity has no mana, never ever triggers
	 * @param level The fraction to listen for. Between 0 and 1 (mana / maxmana)
	 * @param higher If true, triggers when actual >= level. Otherwise, <=
	 */
	public void registerMana(IGenericListener listener,
			LivingEntity entity, float level, boolean higher) {
		manaInfos.put(listener,
				new ManaInfo(entity, level, higher));
	}
	
	/**
	 * Listens for magical effects being applied (optionally to a specific entity).
	 * @param listener
	 * @param entity If provided, the entity to fire when effects are applied to. If left null, fired every time an effect is applied.
	 */
	public void registerMagicEffect(ISpellActionListener listener,
			@Nullable LivingEntity entity) {
		magicEffectInfos.put(listener,
				new MagicEffectInfo(entity));
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		
		LivingEntity ent = event.getEntityLiving(); // convenience
		if (Math.abs(ent.getMotion().x) >= 0.01f
				|| Math.abs(ent.getMotion().y) >= 0.01f
				|| Math.abs(ent.getMotion().z) >= 0.01f) {
			// Moved
			Iterator<Entry<IGenericListener, ProximityInfo>> it = proximityInfos.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IGenericListener, ProximityInfo> entry = it.next();
				if (entry.getValue() == null)
					continue;
				
				if (entry.getValue().world != ent.world)
					continue;
				
				double dist = Math.abs(ent.getPositionVec().subtract(entry.getValue().position).length());
				if (dist <= entry.getValue().proximity) {
					if (entry.getKey().onEvent(Event.PROXIMITY, ent, null))
						it.remove();
				}
					
			}
			
			Iterator<Entry<IGenericListener, PositionInfo>> it2 = positionInfos.entrySet().iterator();
			while (it2.hasNext()) {
				Entry<IGenericListener, PositionInfo> entry = it2.next();
				if (entry.getValue() == null)
					continue;
				
				if (entry.getValue().world != ent.world)
					continue;
				
				BlockPos entpos = ent.getPosition();
				// entry can be removed but block set cannot
				List<BlockPos> blockListCopy = Lists.newArrayList(entry.getValue().blocks);
				for (BlockPos p : blockListCopy) {
					if (p.equals(entpos))
						if (entry.getKey().onEvent(Event.POSITION, ent, null)) {
							it2.remove();
							break;
						}
				}	
			}
		}
		
		if (ent instanceof PlayerEntity) {
			Iterator<Entry<IGenericListener, FoodInfo>> it = foodInfos.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IGenericListener, FoodInfo> entry = it.next();
				if (entry.getValue() == null)
					continue;
				
				if (entry.getValue().entity.getUniqueID() != ent.getUniqueID())
					continue;
				
				int level = ((PlayerEntity) ent).getFoodStats().getFoodLevel();
				int thresh = entry.getValue().threshold;
				
				if (entry.getValue().higher) {
					if (level >= thresh)
						if (entry.getKey().onEvent(Event.FOOD, ent, null))
							it.remove();
				} else {
					if (level <= thresh)
						if (entry.getKey().onEvent(Event.FOOD, ent, null))
							it.remove();
				}
			}
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(ent);
		if (attr != null) {
			Iterator<Entry<IGenericListener, ManaInfo>> it = manaInfos.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IGenericListener, ManaInfo> entry = it.next();
				if (entry.getValue() == null)
					continue;
				
				if (attr.getMaxMana() == 0)
					continue;

				if (entry.getValue().entity.getUniqueID() != ent.getUniqueID())
					continue;
				
				float level = (float) attr.getMana() / (float) attr.getMaxMana();
				float thresh = entry.getValue().threshold;
				
				if (entry.getValue().higher) {
					if (level >= thresh)
						if (entry.getKey().onEvent(Event.MANA, ent, null))
							it.remove();
				} else {
					if (level <= thresh)
						if (entry.getKey().onEvent(Event.MANA, ent, null))
							it.remove();
				}
			}
		}
		
		if (ent.getActivePotionEffect(NostrumEffects.rooted) != null) {
			if (ent.getActivePotionEffect(NostrumEffects.lightningCharge) != null
					|| ent.getActivePotionEffect(NostrumEffects.lightningAttack) != null) {
				ent.removePotionEffect(NostrumEffects.lightningCharge);
				ent.removePotionEffect(NostrumEffects.lightningAttack);
				ent.removePotionEffect(NostrumEffects.rooted);
			}
		}
	}
	
	private void onHealth(LivingEntity ent) {
		Iterator<Entry<IGenericListener, HealthInfo>> it = healthInfos.entrySet().iterator();
		while (it.hasNext()) {
			Entry<IGenericListener, HealthInfo> entry = it.next();
			if (entry.getValue() == null)
				continue;

			if (entry.getValue().entity.getUniqueID() != ent.getUniqueID())
				continue;
			
			float level = ent.getHealth() / ent.getMaxHealth();
			float thresh = entry.getValue().threshold;
			
			if (entry.getValue().higher) {
				if (level >= thresh)
					if (entry.getKey().onEvent(Event.HEALTH, ent, null))
						it.remove();
			} else {
				if (level <= thresh)
					if (entry.getKey().onEvent(Event.HEALTH, ent, null))
						it.remove();
			}
		}
	}

	@SubscribeEvent
	public void onHeal(LivingHealEvent event) {
		onHealth(event.getEntityLiving());
	}
	
	@SubscribeEvent
	public void onAttack(LivingAttackEvent event) {
		if (event.isCanceled())
			return;
		
		final LivingEntity living = event.getEntityLiving();
		
		if (event.getSource().isFireDamage()) {
			
			// lava set ignores fire damage (but not lava). True lava set ignores lava as well
			final boolean lavaSet = MagicArmor.GetSetCount(living, EMagicElement.FIRE, MagicArmor.Type.MASTER) == 4;
			final boolean trueSet = MagicArmor.GetSetCount(living, EMagicElement.FIRE, MagicArmor.Type.TRUE) == 4;
			final boolean isLava = event.getSource() == DamageSource.LAVA || event.getSource().getDamageType().equalsIgnoreCase("lava");
			if (lavaSet || trueSet) {
				final int manaCost = 1; // / 4
				final INostrumMagic attr = NostrumMagica.getMagicWrapper(living);
				if (attr != null) {
					// true set requires mana to prevent lava damage, though
					if (!isLava || attr.getMana() >= manaCost) {
						event.setCanceled(true);
						if (isLava && living.ticksExisted % 4 == 0) {
							attr.addMana(-manaCost);
							if (living instanceof PlayerEntity) {
								NostrumMagica.instance.proxy.sendMana((PlayerEntity) living);
							}
						}
						return;
					}
				}
			}
			
			// Fire arcane wolves also ignore fire damage
			if (living instanceof EntityArcaneWolf
					&& ((EntityArcaneWolf) living).hasWolfCapability(WolfTypeCapability.LAVA_WALK)) {
				event.setCanceled(true);
				living.extinguish();
			}
			// Same for entities riding the wolf
			if (living.getRidingEntity() instanceof EntityArcaneWolf
					&& ((EntityArcaneWolf) living.getRidingEntity()).hasWolfCapability(WolfTypeCapability.LAVA_WALK)) {
				event.setCanceled(true);
				living.extinguish();
			}
		}
		
		if (event.getAmount() > 0f && event.getSource() instanceof EntityDamageSource && !((EntityDamageSource) event.getSource()).getIsThornsDamage()) {
			Entity source = ((EntityDamageSource) event.getSource()).getTrueSource();
			
			if (source instanceof ProjectileEntity) {
				source = ((ProjectileEntity) source).func_234616_v_(); //getShooter();
			}
			
			if (source instanceof LivingEntity) {

				LivingEntity livingTarget = living;
				LivingEntity livingSource = (LivingEntity) source;
				
				// Defense
				if (event.getAmount() > 0 && livingTarget != livingSource) {
					for (ItemStack stack : livingTarget.getEquipmentAndArmor()) {
						if (stack.isEmpty() || !(stack.getItem() instanceof IReactiveEquipment))
							continue;
						
						IReactiveEquipment ench = (IReactiveEquipment) stack.getItem();
						if (ench.shouldTrigger(false, stack)) {
							SpellAction action = ench.getTriggerAction(livingTarget, false, stack);
							if (action != null)
								action.apply(livingTarget, livingSource, 1.0f);
						}
					}
					if (NostrumMagica.instance.curios.isEnabled() && livingTarget instanceof PlayerEntity) {
						IInventory inv = NostrumMagica.instance.curios.getCurios((PlayerEntity) livingTarget);
						if (inv != null) {
							for (int i = 0; i < inv.getSizeInventory(); i++) {
								ItemStack stack = inv.getStackInSlot(i);
								if (stack.isEmpty() || !(stack.getItem() instanceof IReactiveEquipment))
									continue;
								
								IReactiveEquipment ench = (IReactiveEquipment) stack.getItem();
								if (ench.shouldTrigger(false, stack)) {
									SpellAction action = ench.getTriggerAction(livingTarget, false, stack);
									if (action != null)
										action.apply(livingTarget, livingSource, 1.0f);
								}
							}
						}
					}
				}
		
				// Offense
				for (ItemStack stack : livingSource.getEquipmentAndArmor()) {
					if (stack.isEmpty() || !(stack.getItem() instanceof IReactiveEquipment))
						continue;
					
					IReactiveEquipment ench = (IReactiveEquipment) stack.getItem();
					if (ench.shouldTrigger(true, stack)) {
						SpellAction action = ench.getTriggerAction(livingSource, true, stack);
						if (action != null)
							action.apply(livingSource, livingTarget, 1.0f);
					}
				}
				if (NostrumMagica.instance.curios.isEnabled() && livingSource instanceof PlayerEntity) {
					IInventory inv = NostrumMagica.instance.curios.getCurios((PlayerEntity) livingSource);
					if (inv != null) {
						for (int i = 0; i < inv.getSizeInventory(); i++) {
							ItemStack stack = inv.getStackInSlot(i);
							if (stack.isEmpty() || !(stack.getItem() instanceof IReactiveEquipment))
								continue;
							
							IReactiveEquipment ench = (IReactiveEquipment) stack.getItem();
							if (ench.shouldTrigger(true, stack)) {
								SpellAction action = ench.getTriggerAction(livingSource, true, stack);
								if (action != null)
									action.apply(livingSource, livingTarget, 1.0f);
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onDamage(LivingHurtEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		// Make hookshots not damage someone if you reach the wall
		if (event.getSource() == DamageSource.FLY_INTO_WALL) {
			LivingEntity ent = event.getEntityLiving();
			for (@Nonnull ItemStack held : new ItemStack[] {ent.getHeldItemMainhand(), ent.getHeldItemOffhand()}) {
				if (held.isEmpty()) {
					continue;
				}
				
				if (held.getItem() instanceof HookshotItem) {
					// Could check if we're actually being pulled here. Going to be lazy...
					if (HookshotItem.IsExtended(held)) {
						event.setCanceled(true);
						return;
					}
				}
			}
		}
		
		if (event.getSource().getTrueSource() != null) {
			LivingEntity source = null;
			
			// Projectiles can be from no entity
			if (event.getSource().isProjectile()) {
				source = Projectiles.getShooter(event.getSource().getTrueSource());
//				Entity proj = event.getSource().getTrueSource();
//				Entity shooter;
//				if (proj instanceof AbstractArrowEntity) {
//					shooter = ((AbstractArrowEntity) proj).shootingEntity;
//					if (shooter != null && shooter instanceof LivingEntity)
//						source = (LivingEntity) shooter;
//				} else if (proj instanceof EntityFireball) {
//					source = ((EntityFireball) proj).shootingEntity;
//				} else if (proj instanceof EntityThrowable) {
//					source = ((EntityThrowable) proj).getThrower();
//				}
			} else if (event.getSource().getTrueSource() instanceof LivingEntity) {
				source = (LivingEntity) event.getSource().getTrueSource();
			}
			
			if (source != null) {
				Iterator<Entry<IGenericListener, DamagedInfo>> it = damagedInfos.entrySet().iterator();
				while (it.hasNext()) {
					Entry<IGenericListener, DamagedInfo> entry = it.next();
					if (entry.getValue() == null)
						continue;
					
					if (entry.getValue().entity.getUniqueID() != event.getEntityLiving().getUniqueID()) {
						continue;
					}
					
					if (entry.getKey().onEvent(Event.DAMAGED, source, null))
						it.remove();
				}
			}
		}
		
		onHealth(event.getEntityLiving());
		
		if (event.getSource() == DamageSource.ON_FIRE) {
			// If any players nearby have fire master skill, they can regain mana
			for (Entity e : event.getEntityLiving().getEntityWorld().getEntitiesInAABBexcluding(event.getEntity(), event.getEntity().getBoundingBox().grow(10), (e) -> true)) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(e);
				if (e instanceof PlayerEntity && attr != null && attr.hasSkill(NostrumSkills.Fire_Master)) {
					final LivingEntity source = event.getEntityLiving();
					regenMana((PlayerEntity) e);
					NostrumParticles.FILLED_ORB.spawn(e.world, new SpawnParams(
							5, source.getPosX(), source.getPosY() + .75, source.getPosZ(), 0,
							40, 0,
							e.getEntityId()
							).color(1f, .4f, .8f, 1f).dieOnTarget(true));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BreakEvent event) {
		if (event.isCanceled())
			return;
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(event.getPlayer());
		
		if (attr != null && attr.isUnlocked()) {
			if (event.getState().getBlock() instanceof ILoreTagged) {
				attr.giveBasicLore((ILoreTagged) event.getState().getBlock());
			} else if (null != LoreRegistry.getPreset(event.getState().getBlock()))
				attr.giveBasicLore(LoreRegistry.getPreset(event.getState().getBlock()));
		}
		
//		if (event.getState().getBlock() instanceof BlockTallGrass
//				&& NostrumMagica.rand.nextFloat() <= 0.05f) {
//			ItemEntity entity = new ItemEntity(event.getWorld(),
//					event.getPos().getX() + 0.5,
//					event.getPos().getY() + 0.5,
//					event.getPos().getZ() + 0.5,
//					ReagentItem.CreateStack(ReagentType.MANDRAKE_ROOT, 1));
//			event.getWorld().spawnEntityInWorld(entity);
//		}
//		
//		if (event.getState().getBlock() instanceof BlockTallGrass
//				&& NostrumMagica.rand.nextFloat() <= 0.05f) {
//			ItemEntity entity = new ItemEntity(event.getWorld(),
//					event.getPos().getX() + 0.5,
//					event.getPos().getY() + 0.5,
//					event.getPos().getZ() + 0.5,
//					ReagentItem.CreateStack(ReagentType.GINSENG, 1));
//			event.getWorld().spawnEntityInWorld(entity);
//		}
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.isCanceled())
			return;

		if (event.getSource() != null && event.getSource().getTrueSource() != null && event.getSource().getTrueSource() instanceof PlayerEntity) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(event.getSource().getTrueSource());
			
			if (attr != null && attr.isUnlocked()) {
				if (event.getEntityLiving() instanceof ILoreTagged) {
					attr.giveBasicLore((ILoreTagged) event.getEntityLiving());
				} else if (event.getEntityLiving() instanceof ILoreSupplier) {
					ILoreTagged tag = ((ILoreSupplier) event.getEntityLiving()).getLoreTag();
					if (tag != null) {
						attr.giveBasicLore(tag);
					}
				} else if (null != LoreRegistry.getPreset(event.getEntityLiving())) {
					attr.giveBasicLore(LoreRegistry.getPreset(event.getEntityLiving()));
				}
			}
			
		}
		
		if (event.getEntityLiving() instanceof PlayerEntity && !event.getEntityLiving().world.isRemote) {
			//  TODO test this! Does this work now?
//			// Scan for baubles, since Baubles doesn't call onUnequip when you die....
//			if (NostrumMagica.instance.curios.isEnabled() && !event.getEntityLiving().world.getGameRules().getBoolean("keepInventory")) {
//				NostrumMagica.instance.curios.forEachCurio(event.getEntityLiving(), (stack) -> {
//					if (stack.getItem() instanceof INostrumCurio) {
//						((INostrumCurio) stack.getItem()).onUnequipped(stack, event.getEntityLiving());
//					}
//					return false;
//				});
//			}
		}
	}
	
	@SubscribeEvent
	public void onMobDrop(LivingDropsEvent event) {
		if (event.getEntityLiving().isEntityUndead()) {
			for (int i = 0; i <= event.getLootingLevel(); i++) {
				if (NostrumMagica.rand.nextFloat() <= 0.3f) {
					ItemEntity entity = new ItemEntity(event.getEntity().world,
							event.getEntity().getPosX(),
							event.getEntity().getPosY(),
							event.getEntity().getPosZ(),
							new ItemStack(NostrumItems.reagentGraveDust, 1));
					event.getDrops().add(entity);
				}
			}
				
		}
		if (event.getEntityLiving() instanceof SpiderEntity) {
			for (int i = 0; i <= event.getLootingLevel(); i++) {
				if (NostrumMagica.rand.nextFloat() <= 0.4f) {
					ItemEntity entity = new ItemEntity(event.getEntity().world,
							event.getEntity().getPosX(),
							event.getEntity().getPosY(),
							event.getEntity().getPosZ(),
							new ItemStack(NostrumItems.reagentSpiderSilk, 1));
					event.getDrops().add(entity);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onCraft(PlayerEvent.ItemCraftedEvent e) {
		if (e.isCanceled())
			return;
		
		PlayerEntity player = e.getPlayer();
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		
		if (attr != null && attr.isUnlocked()) {
			if (e.getCrafting().getItem() instanceof ILoreTagged) {
				attr.giveBasicLore((ILoreTagged) e.getCrafting().getItem());
			} else if (e.getCrafting().getItem() instanceof BlockItem &&
					((BlockItem)e.getCrafting().getItem()).getBlock() instanceof ILoreTagged) {
				attr.giveBasicLore((ILoreTagged) ((BlockItem) e.getCrafting().getItem()).getBlock());
			}
		}
	}
	
	@SubscribeEvent
	public void onTame(AnimalTameEvent e) {
		if (e.getAnimal() instanceof WolfEntity) {
			PlayerEntity player = e.getTamer();
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr != null && !attr.hasLore(EntityArcaneWolf.WolfTameLore.instance())) {
				attr.giveBasicLore(EntityArcaneWolf.WolfTameLore.instance());
			}
		}
	}
	
	protected boolean shouldIgnoreVacuum(PlayerEntity player) {
		return !ModConfig.config.vacuumWhileSneaking()
				&& player.isSneaking();
	}
	
	@SubscribeEvent
	public void onPickup(EntityItemPickupEvent e) {
		if (e.isCanceled())
			return;
		
		if (!(e.getEntityLiving() instanceof PlayerEntity))
			return; // It SAYS EntityItemPickup, so just in case...
		
		PlayerEntity player = e.getPlayer();
		ItemStack addedItem = e.getItem().getItem();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		
		if (attr != null && attr.isUnlocked()) {
			if (addedItem.getItem() instanceof ILoreTagged) {
				attr.giveBasicLore((ILoreTagged) addedItem.getItem());
			} else if (addedItem.getItem() instanceof BlockItem &&
					((BlockItem)addedItem.getItem()).getBlock() instanceof ILoreTagged) {
				attr.giveBasicLore((ILoreTagged) ((BlockItem) addedItem.getItem()).getBlock());
			}
		}
		
		if (e.getItem().getItem().getItem() instanceof ReagentItem) {
			int originalSize = addedItem.getCount();
			for (ItemStack item : player.inventory.offHandInventory) {
				// Silly but prefer offhand
				if (!item.isEmpty() && item.getItem() instanceof ReagentBag) {
					if (!shouldIgnoreVacuum(player) && ReagentBag.isVacuumEnabled(item)) {
						addedItem = ReagentBag.addItem(item, addedItem);
						if (addedItem.isEmpty() || addedItem.getCount() < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.world, player.getPosX(), player.getPosY(), player.getPosZ());
						}
						if (addedItem.isEmpty()) {
							e.setCanceled(true);
							e.getItem().remove();
							return;
						}
						originalSize = addedItem.getCount();
					}
				}
			}
			for (ItemStack item : player.inventory.mainInventory) {
				if (!item.isEmpty() && item.getItem() instanceof ReagentBag) {
					if (!shouldIgnoreVacuum(player) && ReagentBag.isVacuumEnabled(item)) {
						addedItem = ReagentBag.addItem(item, addedItem);
						if (addedItem.isEmpty() || addedItem.getCount() < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.world, player.getPosX(), player.getPosY(), player.getPosZ());
						}
						if (addedItem.isEmpty()) {
							e.setCanceled(true);
							e.getItem().remove();
							return;
						}
						originalSize = addedItem.getCount();
					}
				}
			}
			
			IInventory curios = NostrumMagica.instance.curios.getCurios(player);
			if (curios != null) {
				for (int i = 0; i < curios.getSizeInventory(); i++) {
					ItemStack equip = curios.getStackInSlot(i);
					if (equip.isEmpty()) {
						continue;
					}
					
					if (equip.getItem() instanceof ReagentBag) {
						addedItem = ReagentBag.addItem(equip, addedItem);
						if (addedItem.isEmpty() || addedItem.getCount() < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.world, player.getPosX(), player.getPosY(), player.getPosZ());
						}
						if (addedItem.isEmpty()) {
							e.setCanceled(true);
							e.getItem().remove();
							return;
						}
						originalSize = addedItem.getCount();
					}
				}
			}
			
			if (addedItem.getCount() < e.getItem().getItem().getCount()) {
				e.setCanceled(true);
				e.getItem().setItem(addedItem);
			}
			
		}
		
		if (e.getItem().getItem().getItem() instanceof SpellRune) {
			int originalSize = addedItem.getCount();
			for (ItemStack item : player.inventory.offHandInventory) {
				// Silly but prefer offhand
				if (!item.isEmpty() && item.getItem() instanceof RuneBag) {
					if (!shouldIgnoreVacuum(player) && RuneBag.isVacuumEnabled(item)) {
						addedItem = RuneBag.addItem(item, addedItem);
						if (addedItem.isEmpty() || addedItem.getCount() < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.world, player.getPosX(), player.getPosY(), player.getPosZ());
						}
						if (addedItem.isEmpty()) {
							e.setCanceled(true);
							e.getItem().remove();
							return;
						}
						originalSize = addedItem.getCount();
					}
				}
			}
			for (ItemStack item : player.inventory.mainInventory) {
				if (!item.isEmpty() && item.getItem() instanceof RuneBag) {
					if (!shouldIgnoreVacuum(player) && RuneBag.isVacuumEnabled(item)) {
						addedItem = RuneBag.addItem(item, addedItem);
						if (addedItem.isEmpty() || addedItem.getCount() < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.world, player.getPosX(), player.getPosY(), player.getPosZ());
						}
						if (addedItem.isEmpty()) {
							e.setCanceled(true);
							e.getItem().remove();
							return;
						}
						originalSize = addedItem.getCount();
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onTick(ServerTickEvent event) {
		if (event.phase == Phase.START) {
			tickCount++;
			
			// Regain mana
			if (tickCount % 10 == 0) {
				
				for (ServerWorld world : LogicalSidedProvider.INSTANCE.<MinecraftServer>get(LogicalSide.SERVER).getWorlds()) {
					if (world.getPlayers().isEmpty()) {
						continue;
					}
					
					for (PlayerEntity player : world.getPlayers()) {
						regenMana(player);
					}
				}
			}
			
			Iterator<Entry<IGenericListener, TimeInfo>> it = timeInfos.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IGenericListener, TimeInfo> entry = it.next();
				TimeInfo info = entry.getValue();
				if (info.delay > 0) {
					info.delay--;
					if (info.delay == 0) {
						if (entry.getKey().onEvent(Event.TIME, null, null)) {
							it.remove();
							continue;
						} else {
							info.startTick = tickCount;
							continue;
						}
					}
				}
				
				if (info.interval > 0 && info.delay == 0) {
					int diff = tickCount - info.startTick;
					if (diff % (info.interval == 0 ? 1 : info.interval) == 0)
						if (entry.getKey().onEvent(Event.TIME, null, null))
							it.remove();
				}
			}
			
			NostrumPortal.tick();
			TeleportRuneTileEntity.tickChargeMap();
			for (ServerWorld world : LogicalSidedProvider.INSTANCE.<MinecraftServer>get(LogicalSide.SERVER).getWorlds()) {
				MagicArmor.ServerWorldTick(world);
			}
		} else if (event.phase == Phase.END) {
			for (ServerWorld world : LogicalSidedProvider.INSTANCE.<MinecraftServer>get(LogicalSide.SERVER).getWorlds()) {
				if (world.getPlayers().isEmpty()) {
					continue;
				}
				
				for (PlayerEntity player : world.getPlayers()) {
					checkTickSkills(player);
				}
			}
			updateTrackedEntities();
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if (event.phase == Phase.START) {
			if (!Minecraft.getInstance().isIntegratedServerRunning() && Minecraft.getInstance().player != null) {
				NostrumPortal.tick();
				//TeleportRune.tick();
			}
		}
	}
	
	private void checkTickSkills(PlayerEntity entity) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr == null) {
			return;
		}
		
		// Physical gives shield when armor is reduced
		if (attr.hasSkill(NostrumSkills.Physical_Adept)) {
			if (entity.isAlive() && entity.getHealth() > 0f && entity.getTotalArmorValue() < getLastTickArmor(entity)) {
				entity.addPotionEffect(new EffectInstance(NostrumEffects.physicalShield, 20 * 15, 0));
			}
		}
		
		// Wind gives extra mana regen when moving
		if (attr.hasSkill(NostrumSkills.Wind_Master) && entity.ticksExisted % 10 == 0) {
			double distance = entity.getPositionVec().distanceTo(getLastTickPos(entity));
			if (distance > 0.1 && distance < 3) {
				final int mult = Math.min(3, 1 + (int) (distance / .3));
				for (int i = 0; i < mult; i++) {
					regenMana(entity);
				}
			}
		}
	}
	
	private void regenMana(PlayerEntity player) {
		// Called 2 times a second
		INostrumMagic stats = NostrumMagica.getMagicWrapper(player);
		
		float bonus = 0f;
		
		for (ItemStack armor : player.getArmorInventoryList()) {
			int level = EnchantmentHelper.getEnchantmentLevel(EnchantmentManaRecovery.instance(), armor);
			if (level > 0)
				bonus += level * .1f;
		}
		
		// Pull in character regen bonus
		bonus += (stats.getManaRegenModifier());
		bonus += (player.getAttribute(NostrumAttributes.manaRegen).getValue()/100.0);
		
		int mana = 1 + (int) (bonus);
		bonus = bonus - (int) bonus;
		if (bonus > 0f && NostrumMagica.rand.nextFloat() < bonus)
			mana++;
		
		stats.addMana(mana);
		NostrumMagica.instance.proxy.sendMana(player);
	}
	
	@SubscribeEvent
	public void onConnect(PlayerLoggedInEvent event) {
		if (event.getPlayer().world.isRemote) {
			return;
		}
		
		NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) event.getPlayer());
	}
	
	@SubscribeEvent
	public void onDisconnect(PlayerLoggedOutEvent event) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(event.getPlayer());
		if (attr != null)
			attr.clearFamiliars();
	}
	
//	@SubscribeEvent
//	public void onClientConnect(ClientConnectedToServerEvent event) {
//		this.clearAll();
//	}
	
	@SubscribeEvent
	public void onXPPickup(PlayerXpEvent.PickupXp event) {
		PlayerEntity player = event.getPlayer();
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		int xp = event.getOrb().xpValue;
		if (attr != null) {
			for (ItemStack item : player.getEquipmentAndArmor()) {
				if (item.isEmpty())
					continue;
				int leftover = tryThanos(player, item, xp);
				if (leftover == 0) {
					break;
				} else if (leftover != xp) {
					xp = leftover;
				}
			}
			// Possibly use baubles
			IInventory baubles = NostrumMagica.instance.curios.getCurios(player);
			if (xp != 0)	
			if (baubles != null) {
				for (int i = 0; i < baubles.getSizeInventory(); i++) {
					ItemStack equip = baubles.getStackInSlot(i);
					if (equip.isEmpty()) {
						continue;
					}
					
					int leftover = tryThanos(player, equip, xp);
					if (leftover == 0) {
						break;
					} else if (leftover != xp) {
						xp = leftover;
					}
				}
			}
			if (xp != 0)
			for (ItemStack item : player.inventory.mainInventory) {
				if (item.isEmpty())
					continue;
				int leftover = tryThanos(player, item, xp);
				if (leftover == 0) {
					break;
				} else if (leftover != xp) {
					xp = leftover;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLightning(EntityStruckByLightningEvent e) {
		if (e.isCanceled()) {
			return;
		}
		
		if (e.getEntity().world.isRemote) {
			return;
		}
		
		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}
		
		LivingEntity living = (LivingEntity) e.getEntity();
		
		final boolean hasLightningSet = MagicArmor.GetSetCount(living, EMagicElement.LIGHTNING, MagicArmor.Type.TRUE) == 4;
		if (hasLightningSet) {
			// Alternate between buff and attack modes
			EffectInstance boostEffect = living.getActivePotionEffect(NostrumEffects.lightningCharge);
			EffectInstance attackEffect = living.getActivePotionEffect(NostrumEffects.lightningAttack);
			boolean tooSoon = (boostEffect == null ? (attackEffect == null ? 0 : attackEffect.getDuration()) : boostEffect.getDuration())
					> (20 * 30 - 5);
			
			if (!tooSoon) {
				if (boostEffect != null) {
					living.removePotionEffect(NostrumEffects.lightningCharge);
					living.addPotionEffect(new EffectInstance(NostrumEffects.lightningAttack, 20 * 30, 0));
				} else {
					if (attackEffect != null) {
						living.removePotionEffect(NostrumEffects.lightningAttack);
					}
					living.addPotionEffect(new EffectInstance(NostrumEffects.lightningCharge, 20 * 30, 0));
				}
			}
			
			e.setCanceled(true);
		}
		
	}
	
	@SubscribeEvent
	public void onLeftClick(LeftClickBlock e) {
		if (e.isCanceled()) {
			return;
		}
		
		PlayerEntity player = e.getPlayer();
		if (player.world.isRemote) {
			return;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null || attr.getMana() < 20) {
			return;
		}
		
		if (MagicArmor.GetSetCount(player, EMagicElement.EARTH, MagicArmor.Type.TRUE) != 4) {
			return;
		}
		
		if (MagicArmor.DoEarthDig(player.world, player, e.getPos(), e.getFace())) {
			attr.addMana(-20);
			NostrumMagica.instance.proxy.sendMana(player);
			e.setCanceled(true);
		}
	}
	
	private int tryThanos(PlayerEntity player, ItemStack item, int xp) {
		if (item.getItem() instanceof ThanosStaff) {
			return ThanosStaff.addXP(item, xp);
		} else if (item.getItem() instanceof ThanoPendant) {
			return ThanoPendant.thanosAddXP(item, xp);
		}
		return xp;
	}
	
	/**
	 * Signals to magic effect listeners.
	 * TODO: Make an actual event if users expand?
	 * @param entity
	 * @param caster
	 * @param summary
	 */
	public void onMagicEffect(LivingEntity entity, @Nullable LivingEntity caster, SpellActionSummary summary) {
		Iterator<Entry<ISpellActionListener, MagicEffectInfo>> it = magicEffectInfos.entrySet().iterator();
		while (it.hasNext()) {
			Entry<ISpellActionListener, MagicEffectInfo> entry = it.next();
			MagicEffectInfo info = entry.getValue();
			
			if (info.entity == null || info.entity.equals(entity)) {
				if (entry.getKey().onEvent(Event.MAGIC_EFFECT, entity, new SpellActionListenerData(entity, caster, summary)))
					it.remove();
			}
		}
	}
	
	protected Map<Entity, Vector3d> lastPosCache = new HashMap<>();
	protected Map<Entity, Vector3d> lastMoveCache = new HashMap<>();
	protected Map<LivingEntity, Integer> lastArmorCache = new HashMap<>();
	
	protected void addEntity(Entity ent) {
		if (!lastPosCache.containsKey(ent)) {
			lastPosCache.put(ent, ent.getPositionVec());
			lastMoveCache.put(ent, ent.getLook(.5f));
			
			if (ent instanceof LivingEntity) {
				LivingEntity living = (LivingEntity) ent;
				lastArmorCache.put(living, living.getTotalArmorValue());
			}
		}
	}
	
	/**
	 * Returns the position of an entity at the end of server ticking the last time it happened.
	 * This can serve as 'what position did the entity end up at last tick'.
	 * If entity is not tracked, returns its current position as a best-guess.
	 * @param ent
	 * @return
	 */
	public Vector3d getLastTickPos(Entity ent) {
		addEntity(ent);
		return lastPosCache.get(ent);
	}
	
	public Vector3d getLastMove(Entity ent) {
		addEntity(ent);
		return lastMoveCache.get(ent);
	}
	
	protected void updateTrackedEntities() {
		// Look at entities being tracked. If dead or removed, remove from tracking. Else stash their current positions.
		Iterator<Entry<Entity, Vector3d>> it = lastPosCache.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Entity, Vector3d> entry = it.next();
			if (entry.getKey() == null || !entry.getKey().isAlive()) {
				it.remove();
			} else {
				Vector3d last = entry.getValue();
				Vector3d cur = entry.getKey().getPositionVec();
				entry.setValue(cur);
				if (last.squareDistanceTo(cur) > .025) {
					// Update movement
					lastMoveCache.put(entry.getKey(), cur.subtract(last));
					lastPosCache.put(entry.getKey(), cur);
				}
				if (entry.getKey() instanceof LivingEntity) {
					LivingEntity living = (LivingEntity) entry.getKey();
					lastArmorCache.put(living, living.getTotalArmorValue());
				}
			}
		}
	}
	
	public int getLastTickArmor(LivingEntity ent) {
		addEntity(ent);
		return lastArmorCache.get(ent);
	}
}
