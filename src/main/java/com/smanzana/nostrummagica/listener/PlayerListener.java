package com.smanzana.nostrummagica.listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.autodungeons.event.GetPlayerRegionSelectionEvent;
import com.smanzana.autodungeons.event.GetPlayerSelectionEvent;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.NostrumMagica.NostrumTeleportedOtherEvent;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.block.PortalBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.mirror.MirrorResearchSubscreen;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.enchantment.ManaRecoveryEnchantment;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.WolfTypeCapability;
import com.smanzana.nostrummagica.item.IReactiveEquipment;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.item.equipment.HookshotItem;
import com.smanzana.nostrummagica.item.equipment.ReagentBag;
import com.smanzana.nostrummagica.item.equipment.RuneBag;
import com.smanzana.nostrummagica.item.equipment.ThanoPendant;
import com.smanzana.nostrummagica.item.equipment.ThanosStaff;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.VanillaEffectSyncMessage;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellActionSummary;
import com.smanzana.nostrummagica.spell.SpellCastEvent;
import com.smanzana.nostrummagica.spell.SpellCasting;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.log.ISpellLogBuilder;
import com.smanzana.nostrummagica.tile.TeleportRuneTileEntity;
import com.smanzana.nostrummagica.util.Projectiles;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingVisibilityEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionColorCalculationEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionExpiryEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

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
		public Level world;
		
		// Proximity based
		public Vec3 position;
		public double proximity;
		
		public ProximityInfo(Level world, Vec3 position, double proximity) {
			this.world = world;
			this.position = position;
			this.proximity = proximity;
		}
	}
	
	private class PositionInfo {
		public Level world;
		
		// Tile based
		public Collection<BlockPos> blocks;
		
		public PositionInfo(Level world, Collection<BlockPos> blocks) {
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
			Level world, Vec3 pos, double range) {
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
			Level world, Collection<BlockPos> blocks) {
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
			Player player, int level, boolean higher) {
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
		if (ent.getLevel().isClientSide()) {
			return;
		}
		
		if (Math.abs(ent.getDeltaMovement().x) >= 0.01f
				|| Math.abs(ent.getDeltaMovement().y) >= 0.01f
				|| Math.abs(ent.getDeltaMovement().z) >= 0.01f) {
			// Moved
			Iterator<Entry<IGenericListener, ProximityInfo>> it = proximityInfos.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IGenericListener, ProximityInfo> entry = it.next();
				if (entry.getValue() == null)
					continue;
				
				if (entry.getValue().world != ent.level)
					continue;
				
				double dist = Math.abs(ent.position().subtract(entry.getValue().position).length());
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
				
				if (entry.getValue().world != ent.level)
					continue;
				
				BlockPos entpos = ent.blockPosition();
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
		
		if (ent instanceof Player) {
			Iterator<Entry<IGenericListener, FoodInfo>> it = foodInfos.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IGenericListener, FoodInfo> entry = it.next();
				if (entry.getValue() == null)
					continue;
				
				if (entry.getValue().entity.getUUID() != ent.getUUID())
					continue;
				
				int level = ((Player) ent).getFoodData().getFoodLevel();
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

				if (entry.getValue().entity.getUUID() != ent.getUUID())
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
		
		if (ent.getEffect(NostrumEffects.rooted) != null) {
			if (ent.getEffect(NostrumEffects.lightningCharge) != null
					|| ent.getEffect(NostrumEffects.lightningAttack) != null) {
				ent.removeEffect(NostrumEffects.lightningCharge);
				ent.removeEffect(NostrumEffects.lightningAttack);
				ent.removeEffect(NostrumEffects.rooted);
			}
		}
	}
	
	private void onHealth(LivingEntity ent) {
		Iterator<Entry<IGenericListener, HealthInfo>> it = healthInfos.entrySet().iterator();
		while (it.hasNext()) {
			Entry<IGenericListener, HealthInfo> entry = it.next();
			if (entry.getValue() == null)
				continue;

			if (entry.getValue().entity.getUUID() != ent.getUUID())
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
		LivingEntity ent = event.getEntityLiving(); // convenience
		if (ent.getLevel().isClientSide()) {
			return;
		}
		
		onHealth(ent);
	}
	
	@SubscribeEvent
	public void onAttack(LivingAttackEvent event) {
		if (event.isCanceled())
			return;
		
		if (event.getEntityLiving().getLevel().isClientSide()) {
			return;
		}
		
		final LivingEntity living = event.getEntityLiving();
		
		if (event.getSource().isFire()) {
			
			// lava set ignores fire damage (but not lava). True lava set ignores lava as well
			final boolean lavaSet = ElementalArmor.GetSetCount(living, EMagicElement.FIRE, ElementalArmor.Type.MASTER) == 4;
			final boolean isLava = event.getSource() == DamageSource.LAVA || event.getSource().getMsgId().equalsIgnoreCase("lava");
			if (lavaSet) {
				final int manaCost = 1; // / 4
				final INostrumMagic attr = NostrumMagica.getMagicWrapper(living);
				if (attr != null) {
					// true set requires mana to prevent lava damage, though
					if (!isLava || attr.getMana() >= manaCost) {
						event.setCanceled(true);
						if (isLava && living.tickCount % 4 == 0) {
							attr.addMana(-manaCost);
							if (living instanceof Player) {
								NostrumMagica.instance.proxy.sendMana((Player) living);
							}
						}
						return;
					}
				}
			}
			
			// Fire arcane wolves also ignore fire damage
			if (living instanceof ArcaneWolfEntity
					&& ((ArcaneWolfEntity) living).hasWolfCapability(WolfTypeCapability.LAVA_WALK)) {
				event.setCanceled(true);
				living.clearFire();
			}
			// Same for entities riding the wolf
			if (living.getVehicle() instanceof ArcaneWolfEntity
					&& ((ArcaneWolfEntity) living.getVehicle()).hasWolfCapability(WolfTypeCapability.LAVA_WALK)) {
				event.setCanceled(true);
				living.clearFire();
			}
		}
		
		if (event.getAmount() > 0f && event.getSource() instanceof EntityDamageSource && !((EntityDamageSource) event.getSource()).isThorns()) {
			Entity source = ((EntityDamageSource) event.getSource()).getEntity();
			
			if (source instanceof Projectile) {
				source = ((Projectile) source).getOwner(); //getShooter();
			}
			
			if (source instanceof LivingEntity) {

				LivingEntity livingTarget = living;
				LivingEntity livingSource = (LivingEntity) source;
				
				// Defense
				if (event.getAmount() > 0 && livingTarget != livingSource) {
					for (ItemStack stack : livingTarget.getAllSlots()) {
						if (stack.isEmpty() || !(stack.getItem() instanceof IReactiveEquipment))
							continue;
						
						IReactiveEquipment ench = (IReactiveEquipment) stack.getItem();
						if (ench.shouldTrigger(false, stack)) {
							SpellAction action = ench.getTriggerAction(livingTarget, false, stack);
							if (action != null)
								action.apply(livingTarget, livingSource, 1.0f, ISpellLogBuilder.Dummy);
						}
					}
					if (NostrumMagica.instance.curios.isEnabled() && livingTarget instanceof Player) {
						Container inv = NostrumMagica.instance.curios.getCurios((Player) livingTarget);
						if (inv != null) {
							for (int i = 0; i < inv.getContainerSize(); i++) {
								ItemStack stack = inv.getItem(i);
								if (stack.isEmpty() || !(stack.getItem() instanceof IReactiveEquipment))
									continue;
								
								IReactiveEquipment ench = (IReactiveEquipment) stack.getItem();
								if (ench.shouldTrigger(false, stack)) {
									SpellAction action = ench.getTriggerAction(livingTarget, false, stack);
									if (action != null)
										action.apply(livingTarget, livingSource, 1.0f, ISpellLogBuilder.Dummy);
								}
							}
						}
					}
				}
		
				// Offense
				for (ItemStack stack : livingSource.getAllSlots()) {
					if (stack.isEmpty() || !(stack.getItem() instanceof IReactiveEquipment))
						continue;
					
					IReactiveEquipment ench = (IReactiveEquipment) stack.getItem();
					if (ench.shouldTrigger(true, stack)) {
						SpellAction action = ench.getTriggerAction(livingSource, true, stack);
						if (action != null)
							action.apply(livingSource, livingTarget, 1.0f, ISpellLogBuilder.Dummy);
					}
				}
				if (NostrumMagica.instance.curios.isEnabled() && livingSource instanceof Player) {
					Container inv = NostrumMagica.instance.curios.getCurios((Player) livingSource);
					if (inv != null) {
						for (int i = 0; i < inv.getContainerSize(); i++) {
							ItemStack stack = inv.getItem(i);
							if (stack.isEmpty() || !(stack.getItem() instanceof IReactiveEquipment))
								continue;
							
							IReactiveEquipment ench = (IReactiveEquipment) stack.getItem();
							if (ench.shouldTrigger(true, stack)) {
								SpellAction action = ench.getTriggerAction(livingSource, true, stack);
								if (action != null)
									action.apply(livingSource, livingTarget, 1.0f, ISpellLogBuilder.Dummy);
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
		
		if (event.getEntityLiving().getLevel().isClientSide()) {
			return;
		}
		
		// Make hookshots not damage someone if you reach the wall
		if (event.getSource() == DamageSource.FLY_INTO_WALL) {
			LivingEntity ent = event.getEntityLiving();
			for (@Nonnull ItemStack held : new ItemStack[] {ent.getMainHandItem(), ent.getOffhandItem()}) {
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
		
		if (event.getSource().getEntity() != null) {
			LivingEntity source = null;
			
			// Projectiles can be from no entity
			if (event.getSource().isProjectile()) {
				source = Projectiles.getShooter(event.getSource().getEntity());
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
			} else if (event.getSource().getEntity() instanceof LivingEntity) {
				source = (LivingEntity) event.getSource().getEntity();
			}
			
			if (source != null) {
				Iterator<Entry<IGenericListener, DamagedInfo>> it = damagedInfos.entrySet().iterator();
				while (it.hasNext()) {
					Entry<IGenericListener, DamagedInfo> entry = it.next();
					if (entry.getValue() == null)
						continue;
					
					if (entry.getValue().entity.getUUID() != event.getEntityLiving().getUUID()) {
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
			for (Entity e : event.getEntityLiving().getCommandSenderWorld().getEntities(event.getEntity(), event.getEntity().getBoundingBox().inflate(10), (e) -> true)) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(e);
				if (e instanceof Player && attr != null && attr.hasSkill(NostrumSkills.Fire_Master)) {
					final LivingEntity source = event.getEntityLiving();
					regenMana((Player) e);
					NostrumParticles.FILLED_ORB.spawn(e.level, new SpawnParams(
							5, source.getX(), source.getY() + .75, source.getZ(), 0,
							40, 0,
							new TargetLocation(e, true)
							).setTargetBehavior(new ParticleTargetBehavior().orbitMode(true).dieWithTarget()).color(1f, .4f, .8f, 1f));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BreakEvent event) {
		if (event.isCanceled())
			return;
		
		if (event.getPlayer().getLevel().isClientSide()) {
			return;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(event.getPlayer());
		
		if (attr != null && attr.isUnlocked()) {
			if (event.getState().getBlock() instanceof ILoreTagged) {
				attr.giveBasicLore((ILoreTagged) event.getState().getBlock());
			} else if (event.getState().is(BlockTags.LEAVES)) {
				attr.giveBasicLore(LoreRegistry.Leaves.instance());
			}
		}
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.isCanceled())
			return;
		
		if (event.getEntityLiving().getLevel().isClientSide()) {
			return;
		}

		if (event.getSource() != null && event.getSource().getEntity() != null && event.getSource().getEntity() instanceof Player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(event.getSource().getEntity());
			
			if (attr != null && attr.isUnlocked()) {
				if (event.getEntityLiving() instanceof ILoreTagged) {
					attr.giveBasicLore((ILoreTagged) event.getEntityLiving());
				} else if (event.getEntityLiving() instanceof ILoreSupplier) {
					ILoreTagged tag = ((ILoreSupplier) event.getEntityLiving()).getLoreTag();
					if (tag != null) {
						attr.giveBasicLore(tag);
					}
				} else if (event.getEntityLiving().isInvertedHealAndHarm()) {
					attr.giveBasicLore(LoreRegistry.UndeadLore.instance());
				}
			}
			
		}
		
		if (event.getEntityLiving() instanceof Player) {
			NostrumMagica.instance.getSpellCooldownTracker(event.getEntityLiving().level)
				.clearCooldowns((Player) event.getEntityLiving());
		}
	}
	
	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		if (event.isCanceled() || event.isWasDeath()) {
			return;
		}
		
		if (event.getEntityLiving().getLevel().isClientSide()) {
			return;
		}
		
		// Could do the math, and then update every cooldown...
//		final int oldExisted = event.getOriginal().ticksExisted;
//		final int newExisted = event.getPlayer().ticksExisted;
		
		NostrumMagica.instance.getSpellCooldownTracker(event.getPlayer().level)
			.clearCooldowns(event.getPlayer());
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onPlayerDimensionChange(PlayerChangedDimensionEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		if (event.getEntityLiving().getLevel().isClientSide()) {
			return;
		}
		
		NostrumMagica.instance.getSpellCooldownTracker(event.getPlayer().level)
			.clearCooldowns(event.getPlayer());
	}
	
	@SubscribeEvent
	public void onMobDrop(LivingDropsEvent event) {
		if (event.getEntityLiving().getLevel().isClientSide()) {
			return;
		}
		
		if (event.getEntityLiving().isInvertedHealAndHarm()) {
			for (int i = 0; i <= event.getLootingLevel(); i++) {
				if (NostrumMagica.rand.nextFloat() <= 0.3f) {
					ItemEntity entity = new ItemEntity(event.getEntity().level,
							event.getEntity().getX(),
							event.getEntity().getY(),
							event.getEntity().getZ(),
							new ItemStack(NostrumItems.reagentGraveDust, 1));
					event.getDrops().add(entity);
				}
			}
				
		}
		if (event.getEntityLiving() instanceof Spider) {
			for (int i = 0; i <= event.getLootingLevel(); i++) {
				if (NostrumMagica.rand.nextFloat() <= 0.4f) {
					ItemEntity entity = new ItemEntity(event.getEntity().level,
							event.getEntity().getX(),
							event.getEntity().getY(),
							event.getEntity().getZ(),
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
		
		if (e.getEntityLiving().getLevel().isClientSide()) {
			return;
		}
		
		Player player = e.getPlayer();
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
		if (e.getEntityLiving().getLevel().isClientSide()) {
			return;
		}
		
		if (e.getAnimal() instanceof Wolf) {
			Player player = e.getTamer();
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr != null && !attr.hasLore(ArcaneWolfEntity.WolfTameLore.instance())) {
				attr.giveBasicLore(ArcaneWolfEntity.WolfTameLore.instance());
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityVisibilityCheck(LivingVisibilityEvent event) {
		if (event.getEntityLiving().getLevel().isClientSide()) {
			return;
		}
		
		if (event.getLookingEntity() != null && event.getLookingEntity() instanceof LivingEntity) {
			final MobEffectInstance instance = ((LivingEntity) event.getLookingEntity()).getEffect(NostrumEffects.mobBlindness);
			if (instance != null && instance.getDuration() > 0) {
				event.modifyVisibility(Math.max(0, .2 - (0.1 * instance.getAmplifier())));
			}
		}
	}
	
	protected boolean shouldIgnoreVacuum(Player player) {
		return !ModConfig.config.vacuumWhileSneaking()
				&& player.isShiftKeyDown();
	}
	
	@SubscribeEvent
	public void onPickup(EntityItemPickupEvent e) {
		if (e.isCanceled())
			return;
		
		if (e.getEntityLiving().getLevel().isClientSide()) {
			return;
		}
		
		if (!(e.getEntityLiving() instanceof Player))
			return; // It SAYS EntityItemPickup, so just in case...
		
		Player player = e.getPlayer();
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
			for (ItemStack item : player.getInventory().offhand) {
				// Silly but prefer offhand
				if (!item.isEmpty() && item.getItem() instanceof ReagentBag) {
					if (!shouldIgnoreVacuum(player) && ReagentBag.isVacuumEnabled(item)) {
						addedItem = ReagentBag.addItem(item, addedItem);
						if (addedItem.isEmpty() || addedItem.getCount() < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.level, player.getX(), player.getY(), player.getZ());
						}
						if (addedItem.isEmpty()) {
							e.setCanceled(true);
							e.getItem().discard();
							return;
						}
						originalSize = addedItem.getCount();
					}
				}
			}
			for (ItemStack item : player.getInventory().items) {
				if (!item.isEmpty() && item.getItem() instanceof ReagentBag) {
					if (!shouldIgnoreVacuum(player) && ReagentBag.isVacuumEnabled(item)) {
						addedItem = ReagentBag.addItem(item, addedItem);
						if (addedItem.isEmpty() || addedItem.getCount() < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.level, player.getX(), player.getY(), player.getZ());
						}
						if (addedItem.isEmpty()) {
							e.setCanceled(true);
							e.getItem().discard();
							return;
						}
						originalSize = addedItem.getCount();
					}
				}
			}
			
			Container curios = NostrumMagica.instance.curios.getCurios(player);
			if (curios != null) {
				for (int i = 0; i < curios.getContainerSize(); i++) {
					ItemStack equip = curios.getItem(i);
					if (equip.isEmpty()) {
						continue;
					}
					
					if (equip.getItem() instanceof ReagentBag) {
						addedItem = ReagentBag.addItem(equip, addedItem);
						if (addedItem.isEmpty() || addedItem.getCount() < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.level, player.getX(), player.getY(), player.getZ());
						}
						if (addedItem.isEmpty()) {
							e.setCanceled(true);
							e.getItem().discard();
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
			for (ItemStack item : player.getInventory().offhand) {
				// Silly but prefer offhand
				if (!item.isEmpty() && item.getItem() instanceof RuneBag) {
					if (!shouldIgnoreVacuum(player) && RuneBag.isVacuumEnabled(item)) {
						addedItem = RuneBag.addItem(item, addedItem);
						if (addedItem.isEmpty() || addedItem.getCount() < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.level, player.getX(), player.getY(), player.getZ());
						}
						if (addedItem.isEmpty()) {
							e.setCanceled(true);
							e.getItem().discard();
							return;
						}
						originalSize = addedItem.getCount();
					}
				}
			}
			for (ItemStack item : player.getInventory().items) {
				if (!item.isEmpty() && item.getItem() instanceof RuneBag) {
					if (!shouldIgnoreVacuum(player) && RuneBag.isVacuumEnabled(item)) {
						addedItem = RuneBag.addItem(item, addedItem);
						if (addedItem.isEmpty() || addedItem.getCount() < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.level, player.getX(), player.getY(), player.getZ());
						}
						if (addedItem.isEmpty()) {
							e.setCanceled(true);
							e.getItem().discard();
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
				
				// Crash here if workqueue stops being a minecraft server. I'm not sure of the RIGHT way of doing this.
				for (ServerLevel world : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
					if (world.players().isEmpty()) {
						continue;
					}
					
					for (Player player : world.players()) {
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
			
			PortalBlock.serverTick();
			TeleportRuneTileEntity.tickChargeMap();
			for (ServerLevel world : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
				ElementalArmor.ServerWorldTick(world);
			}
		} else if (event.phase == Phase.END) {
			for (ServerLevel world : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
				// Do cursed fire check
				world.getEntities(EntityTypeTest.forClass(LivingEntity.class), e -> e.getEffect(NostrumEffects.cursedFire) != null && e.isInWaterRainOrBubble())
					.forEach(e -> {
						e.removeEffect(NostrumEffects.cursedFire);
						world.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1f, 1f);
					});
				
				if (world.players().isEmpty()) {
					continue;
				}
				
				for (Player player : world.players()) {
					checkTickSkills(player);
				}
			}
			updateTrackedEntities();
			teleportedEntitiesThisTick.clear();
		}
	}
	
	private void checkTickSkills(Player entity) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr == null) {
			return;
		}
		
		// Physical gives shield when armor is reduced
		if (attr.hasSkill(NostrumSkills.Physical_Adept)) {
			if (entity.isAlive() && entity.getHealth() > 0f && entity.getArmorValue() < getLastTickArmor(entity)) {
				entity.addEffect(new MobEffectInstance(NostrumEffects.physicalShield, 20 * 15, 0));
			}
		}
		
		// Wind gives extra mana regen when moving
		if (attr.hasSkill(NostrumSkills.Wind_Master) && entity.tickCount % 10 == 0) {
			double distance = entity.position().distanceTo(getLastTickPos(entity));
			if (distance > 0.1 && distance < 3) {
				final int mult = Math.min(3, 1 + (int) (distance / .3));
				for (int i = 0; i < mult; i++) {
					regenMana(entity);
				}
			}
		}
	}
	
	private void regenMana(Player player) {
		regenMana(player, 1);
	} 
	
	private void regenMana(Player player, int base) {
		// Called 2 times a second
		INostrumMagic stats = NostrumMagica.getMagicWrapper(player);
		
		float bonus = 0f;
		
		for (ItemStack armor : player.getArmorSlots()) {
			int level = EnchantmentHelper.getItemEnchantmentLevel(ManaRecoveryEnchantment.instance(), armor);
			if (level > 0)
				bonus += level * .1f;
		}
		
		// Pull in character regen bonus
		bonus += (stats.getManaRegenModifier());
		bonus += (player.getAttribute(NostrumAttributes.manaRegen).getValue()/100.0);
		
		int mana = base + (int) (bonus);
		bonus = bonus - (int) bonus;
		if (bonus > 0f && NostrumMagica.rand.nextFloat() < bonus)
			mana++;
		
		stats.addMana(mana);
		NostrumMagica.instance.proxy.sendMana(player);
	}
	
	@SubscribeEvent
	public void onConnect(PlayerLoggedInEvent event) {
		if (event.getPlayer().level.isClientSide) {
			return;
		}
		
		NostrumMagica.instance.proxy.syncPlayer((ServerPlayer) event.getPlayer());
	}
	
	@SubscribeEvent
	public void onDisconnect(PlayerLoggedOutEvent event) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(event.getPlayer());
		if (attr != null)
			attr.clearFamiliars();
		
		if (event.getEntityLiving() instanceof Player) {
			// Make sure to do on both sides
			NostrumMagica.instance.getSpellCooldownTracker(event.getEntityLiving().level)
				.clearCooldowns(event.getPlayer());
		}
	}
	
//	@SubscribeEvent
//	public void onClientConnect(ClientConnectedToServerEvent event) {
//		this.clearAll();
//	}
	
	@SubscribeEvent
	public void onXPPickup(PlayerXpEvent.PickupXp event) {
		if (event.getEntityLiving().getLevel().isClientSide()) {
			return;
		}
		
		Player player = event.getPlayer();
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		int xp = event.getOrb().value;
		if (attr != null) {
			for (ItemStack item : player.getAllSlots()) {
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
			Container baubles = NostrumMagica.instance.curios.getCurios(player);
			if (xp != 0)	
			if (baubles != null) {
				for (int i = 0; i < baubles.getContainerSize(); i++) {
					ItemStack equip = baubles.getItem(i);
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
			for (ItemStack item : player.getInventory().items) {
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
		
		if (e.getEntity().level.isClientSide) {
			return;
		}
		
		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}
		
		LivingEntity living = (LivingEntity) e.getEntity();
		
		final boolean hasLightningSet = ElementalArmor.GetSetCount(living, EMagicElement.LIGHTNING, ElementalArmor.Type.MASTER) == 4;
		if (hasLightningSet) {
			// Alternate between buff and attack modes
			MobEffectInstance boostEffect = living.getEffect(NostrumEffects.lightningCharge);
			MobEffectInstance attackEffect = living.getEffect(NostrumEffects.lightningAttack);
			boolean tooSoon = (boostEffect == null ? (attackEffect == null ? 0 : attackEffect.getDuration()) : boostEffect.getDuration())
					> (20 * 30 - 5);
			
			if (!tooSoon) {
				if (boostEffect != null) {
					living.removeEffect(NostrumEffects.lightningCharge);
					living.addEffect(new MobEffectInstance(NostrumEffects.lightningAttack, 20 * 30, 0));
				} else {
					if (attackEffect != null) {
						living.removeEffect(NostrumEffects.lightningAttack);
					}
					living.addEffect(new MobEffectInstance(NostrumEffects.lightningCharge, 20 * 30, 0));
				}
			}
			
			e.setCanceled(true);
		}
		
		if (!e.isCanceled() && e.getEntity() instanceof LivingEntity) {
			// If any players nearby have lightning master skill, they can regain mana
			for (Entity ent : e.getEntity().getCommandSenderWorld().getEntities(e.getEntity(), e.getEntity().getBoundingBox().inflate(10), (ent) -> true)) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(ent);
				if (ent instanceof Player && attr != null) {
					int mana = 0;
					final LivingEntity source = (LivingEntity) e.getEntity();
					if (attr.hasSkill(NostrumSkills.Lightning_Master)) {
						mana += 5;
					}
					
					if (attr.hasSkill(NostrumSkills.Lightning_Corrupt) && ((LivingEntity) e.getEntity()).getEffect(NostrumEffects.magicRend) != null) {
						mana += 5;
					}
					
					if (mana > 0) {
						regenMana((Player) ent, mana);
						NostrumParticles.FILLED_ORB.spawn(ent.level, new SpawnParams(
								5, source.getX(), source.getY() + .75, source.getZ(), 0,
								40, 0,
								new TargetLocation(ent, true)
								).setTargetBehavior(new ParticleTargetBehavior().orbitMode(true).dieWithTarget()).color(1f, .4f, .8f, 1f));
					}
				}
			}
		}
		
	}
	
	@SubscribeEvent
	public void onLeftClick(LeftClickBlock e) {
		if (e.isCanceled()) {
			return;
		}
		
		Player player = e.getPlayer();
		if (player.level.isClientSide) {
			return;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null || attr.getMana() < 20) {
			return;
		}
		
		if (ElementalArmor.GetSetCount(player, EMagicElement.EARTH, ElementalArmor.Type.MASTER) != 4) {
			return;
		}
		
		if (ElementalArmor.DoEarthDig(player.level, player, e.getPos(), e.getFace())) {
			attr.addMana(-20);
			NostrumMagica.instance.proxy.sendMana(player);
			e.setCanceled(true);
		}
	}
	
	private int tryThanos(Player player, ItemStack item, int xp) {
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
	
	protected Map<Entity, Vec3> lastPosCache = new HashMap<>();
	protected Map<Entity, Vec3> lastMoveCache = new HashMap<>();
	protected Map<LivingEntity, Integer> lastArmorCache = new HashMap<>();
	
	protected void addEntity(Entity ent) {
		if (!lastPosCache.containsKey(ent)) {
			lastPosCache.put(ent, ent.position());
			lastMoveCache.put(ent, ent.getViewVector(.5f));
			
			if (ent instanceof LivingEntity) {
				LivingEntity living = (LivingEntity) ent;
				lastArmorCache.put(living, living.getArmorValue());
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
	public Vec3 getLastTickPos(Entity ent) {
		addEntity(ent);
		return lastPosCache.get(ent);
	}
	
	public Vec3 getLastMove(Entity ent) {
		addEntity(ent);
		return lastMoveCache.get(ent);
	}
	
	protected void updateTrackedEntities() {
		// Look at entities being tracked. If dead or removed, remove from tracking. Else stash their current positions.
		Iterator<Entry<Entity, Vec3>> it = lastPosCache.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Entity, Vec3> entry = it.next();
			if (entry.getKey() == null || !entry.getKey().isAlive()) {
				it.remove();
			} else {
				Vec3 last = entry.getValue();
				Vec3 cur = entry.getKey().position();
				entry.setValue(cur);
				if (last.distanceToSqr(cur) > .025) {
					// Update movement
					lastMoveCache.put(entry.getKey(), cur.subtract(last));
					lastPosCache.put(entry.getKey(), cur);
				}
				if (entry.getKey() instanceof LivingEntity) {
					LivingEntity living = (LivingEntity) entry.getKey();
					lastArmorCache.put(living, living.getArmorValue());
				}
			}
		}
	}
	
	public int getLastTickArmor(LivingEntity ent) {
		addEntity(ent);
		return lastArmorCache.get(ent);
	}
	
	private Set<LivingEntity> teleportedEntitiesThisTick = new HashSet<>();
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onTeleport(EntityTeleportEvent event) {
		if (event.isCanceled()) {
			return;
		}
		if (!(event.getEntity() instanceof LivingEntity)) {
			return;
		}
		if (event.getEntity().level.isClientSide()) {
			return;
		}
		if (event.getEntity() instanceof Player && ((Player) event.getEntity()).isCreative()) {
			return;
		}
		final LivingEntity ent = (LivingEntity) event.getEntity();
		if (teleportedEntitiesThisTick.contains(ent)) {
			return;
		}
		teleportedEntitiesThisTick.add(ent);
		INostrumMagic attr = NostrumMagica.getMagicWrapper(ent);
		if (attr != null) {
			if (attr.hasSkill(NostrumSkills.Ender_Adept)) {
				ent.heal(2f);
				if (ent instanceof Player) {
					((Player) ent).getFoodData().eat(2, 2);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onTeleport(NostrumTeleportedOtherEvent event) {
		final Entity ent = event.getEntity();
		final LivingEntity causingEntity = event.getCausingEntity();
		if (causingEntity != null && causingEntity != ent && causingEntity instanceof Player && ent instanceof LivingEntity) {
			INostrumMagic causerAttr = NostrumMagica.getMagicWrapper(causingEntity);
			if (causerAttr != null) {
				if (causerAttr.hasSkill(NostrumSkills.Ender_Master)) {
					regenMana((Player) causingEntity, 20);
					NostrumParticles.FILLED_ORB.spawn(ent.level, new SpawnParams(
							5, ent.getX(), ent.getY() + .75, ent.getZ(), 0,
							40, 0,
							new TargetLocation(causingEntity, true)
							).setTargetBehavior(new ParticleTargetBehavior().orbitMode(true).dieWithTarget()).color(1f, .4f, .8f, 1f));
				}
			}
		}
	}

	protected Map<LivingEntity, Spell> lastSpell = new HashMap<>();
	
	@SubscribeEvent
	public void onSpellCast(SpellCastEvent.Post event) {
		lastSpell.put(event.getCaster(), event.getSpell());
		
		// Let this be the thing that updates spell cooldowns
		if (!event.isChecking && event.getCastResult().succeeded && event.getCaster() instanceof Player && !event.getCaster().level.isClientSide() && !event.getCaster().isDeadOrDying()) {
			final int cooldown = SpellCasting.CalculateSpellCooldown(event.getCastResult());
			final int globalCooldown = SpellCasting.CalculateGlobalSpellCooldown(event.getCastResult());
			NostrumMagica.instance.getSpellCooldownTracker(event.getCaster().level).setSpellCooldown((Player) event.getCaster(), event.getSpell(), cooldown);
			NostrumMagica.instance.getSpellCooldownTracker(event.getCaster().level).setGlobalCooldown((Player) event.getCaster(), globalCooldown);
		}
	}
	
	public @Nullable Spell getLastSpell(LivingEntity caster) {
		return lastSpell.get(caster);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void overrideLastSpell(LivingEntity caster, Spell spell) {
		lastSpell.put(caster, spell);
	}
	
	@SubscribeEvent
	public void onEntityEffect(PotionColorCalculationEvent event) {
		// Called any time effects change or cache needs refreshing!
		;
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onEntityEffectAdded(PotionAddedEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		if (event.getEntityLiving() instanceof Player) {
			// Automatically handled
			return;
		}
		
		final LivingEntity ent = event.getEntityLiving();
		if (ent.level.isClientSide()) {
			return;
		}
		
		final @Nullable MobEffectInstance oldEffect = event.getOldPotionEffect();
		final MobEffectInstance newEffect = event.getPotionEffect();
		
		// Simulate merge. Copied in essence from LivingEntity#addPotionEffect()
		final MobEffectInstance mergedEffect;
		if (oldEffect == null) {
			mergedEffect = newEffect;
		} else {
			MobEffectInstance oldCopy = new MobEffectInstance(oldEffect);
			if (oldCopy.update(newEffect)) {
				mergedEffect = oldCopy;
			} else {
				mergedEffect = null;
			}
		}
		
		if (mergedEffect != null) {
			NetworkHandler.sendToAllTracking(new VanillaEffectSyncMessage(ent, mergedEffect), ent);
		}
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onEntityEffectRemoved(PotionRemoveEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		if (event.getEntityLiving() instanceof Player) {
			// Automatically handled
			return;
		}

		final LivingEntity ent = event.getEntityLiving();
		if (ent.level.isClientSide()) {
			return;
		}
		
		final MobEffectInstance removedEffect = event.getPotionEffect();
		if (removedEffect != null) {
			NetworkHandler.sendToAllTracking(new VanillaEffectSyncMessage(ent.getId(), removedEffect.getEffect()), ent);
		}
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onEntityEffectRemoved(PotionExpiryEvent event) {
		if (event.getEntityLiving() instanceof Player) {
			// Automatically handled
			return;
		}

		final LivingEntity ent = event.getEntityLiving();
		if (ent.level.isClientSide()) {
			return;
		}
		
		final MobEffectInstance removedEffect = event.getPotionEffect();
		if (removedEffect != null) {
			NetworkHandler.sendToAllTracking(new VanillaEffectSyncMessage(ent.getId(), removedEffect.getEffect()), ent);
		}
	}
	
	@SubscribeEvent
	public void onDungeonSelect(GetPlayerSelectionEvent event) {
		// Must be a position crystals in hand with low corner selected
		ItemStack main = event.getPlayer().getMainHandItem();
		if (!main.isEmpty() && main.getItem() instanceof PositionCrystal) {
			event.setSelection(PositionCrystal.getBlockPosition(main));
		}
	}
	
	@SubscribeEvent
	public void onDungeonSelect(GetPlayerRegionSelectionEvent event) {
		// Must be holding two position crystals in hands with corners selected
		ItemStack main = event.getPlayer().getMainHandItem();
		ItemStack offhand =  event.getPlayer().getOffhandItem();
		if ((main.isEmpty() || !(main.getItem() instanceof PositionCrystal) || PositionCrystal.getBlockPosition(main) == null)
			|| (offhand.isEmpty() || !(offhand.getItem() instanceof PositionCrystal) || PositionCrystal.getBlockPosition(offhand) == null)) {
			return;
		}
		
		event.setPos1(PositionCrystal.getBlockPosition(main));
		event.setPos2(PositionCrystal.getBlockPosition(offhand));
	}
}
