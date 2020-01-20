package com.smanzana.nostrummagica.listeners;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.baubles.items.ItemMagicBauble;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.enchantments.EnchantmentManaRecovery;
import com.smanzana.nostrummagica.items.EnchantedEquipment;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.RuneBag;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.ThanoPendant;
import com.smanzana.nostrummagica.items.ThanosStaff;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ManaMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.SpellActionSummary;
import com.smanzana.nostrummagica.spells.components.SpellAction;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

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
		public boolean onEvent(Event type, EntityLivingBase entity, T data);
	}
	
	/**
	 * Listener that just doesn't use data
	 */
	public interface IGenericListener extends IMagicListener<Object> {};
	
	public static class SpellActionListenerData {
		/**
		 * Entity being affected
		 */
		public EntityLivingBase entity;
		
		/**
		 * Entity that cast the spell. Can be empty.
		 */
		@Nullable
		public EntityLivingBase caster;
		
		/**
		 * Information about the spell being applied
		 */
		public SpellActionSummary summary;
		
		public SpellActionListenerData(EntityLivingBase entity, @Nullable EntityLivingBase caster, SpellActionSummary summary) {
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
		public Vec3d position;
		public double proximity;
		
		public ProximityInfo(World world, Vec3d position, double proximity) {
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
		public EntityLivingBase entity;
		
		public DamagedInfo(EntityLivingBase entity) {
			this.entity = entity;
		}
	}
	
	private class HealthInfo {
		public EntityLivingBase entity;
		public float threshold; // percentage out of 1
		public boolean higher;
		public HealthInfo(EntityLivingBase entity, float threshold, boolean higher) {
			this.entity = entity;
			this.threshold = threshold;
			this.higher = higher;
		}
	}
	
	private class FoodInfo {
		public EntityLivingBase entity;
		public int threshold;
		public boolean higher;
		public FoodInfo(EntityLivingBase entity, int threshold, boolean higher) {
			this.entity = entity;
			this.threshold = threshold;
			this.higher = higher;
		}
	}
	
	private class ManaInfo {
		public EntityLivingBase entity;
		public float threshold;
		public boolean higher;
		public ManaInfo(EntityLivingBase entity, float threshold, boolean higher) {
			this.entity = entity;
			this.threshold = threshold;
			this.higher = higher;
		}
	}
	
	private class MagicEffectInfo {
		public EntityLivingBase entity;
		public MagicEffectInfo(@Nullable EntityLivingBase entity) {
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
			World world, Vec3d pos, double range) {
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
			EntityLivingBase entity) {
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
			EntityLivingBase entity, float level, boolean higher) {
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
			EntityPlayer player, int level, boolean higher) {
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
			EntityLivingBase entity, float level, boolean higher) {
		manaInfos.put(listener,
				new ManaInfo(entity, level, higher));
	}
	
	/**
	 * Listens for magical effects being applied (optionally to a specific entity).
	 * @param listener
	 * @param entity If provided, the entity to fire when effects are applied to. If left null, fired every time an effect is applied.
	 */
	public void registerMagicEffect(ISpellActionListener listener,
			@Nullable EntityLivingBase entity) {
		magicEffectInfos.put(listener,
				new MagicEffectInfo(entity));
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		
		EntityLivingBase ent = event.getEntityLiving(); // convenience
		if (Math.abs(ent.motionX) >= 0.01f
				|| Math.abs(ent.motionY) >= 0.01f
				|| Math.abs(ent.motionZ) >= 0.01f) {
			// Moved
			Iterator<Entry<IGenericListener, ProximityInfo>> it = proximityInfos.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IGenericListener, ProximityInfo> entry = it.next();
				if (entry.getValue() == null)
					continue;
				
				if (entry.getValue().world != ent.worldObj)
					continue;
				
				double dist = Math.abs(ent.getPositionVector().subtract(entry.getValue().position).lengthVector());
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
				
				if (entry.getValue().world != ent.worldObj)
					continue;
				
				BlockPos entpos = ent.getPosition();
				for (BlockPos p : entry.getValue().blocks) {
					if (p.equals(entpos))
						if (entry.getKey().onEvent(Event.POSITION, ent, null))
							it2.remove();
				}	
			}
		}
		
		if (ent instanceof EntityPlayer) {
			Iterator<Entry<IGenericListener, FoodInfo>> it = foodInfos.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IGenericListener, FoodInfo> entry = it.next();
				if (entry.getValue() == null)
					continue;
				
				if (entry.getValue().entity.getPersistentID() != ent.getPersistentID())
					continue;
				
				int level = ((EntityPlayer) ent).getFoodStats().getFoodLevel();
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

				if (entry.getValue().entity.getPersistentID() != ent.getPersistentID())
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
	}
	
	private void onHealth(EntityLivingBase ent) {
		Iterator<Entry<IGenericListener, HealthInfo>> it = healthInfos.entrySet().iterator();
		while (it.hasNext()) {
			Entry<IGenericListener, HealthInfo> entry = it.next();
			if (entry.getValue() == null)
				continue;

			if (entry.getValue().entity.getPersistentID() != ent.getPersistentID())
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
		
		if (event.getAmount() > 0f && event.getSource() instanceof EntityDamageSource) {
			Entity source = ((EntityDamageSource) event.getSource()).getSourceOfDamage();
			
			if (source instanceof EntityArrow) {
				source = ((EntityArrow) source).shootingEntity;
			} else if (source instanceof EntityFireball) {
				source = ((EntityFireball) source).shootingEntity;
			} else if (source instanceof EntityThrowable) {
				source = ((EntityThrowable) source).getThrower();
			}
			
			if (source instanceof EntityLivingBase) {

				EntityLivingBase livingTarget = event.getEntityLiving();
				EntityLivingBase livingSource = (EntityLivingBase) source;
				
				// Defense
				for (ItemStack stack : livingTarget.getEquipmentAndArmor()) {
					if (stack == null || !(stack.getItem() instanceof EnchantedEquipment))
						continue;
					
					EnchantedEquipment ench = (EnchantedEquipment) stack.getItem();
					if (ench.shouldTrigger(false)) {
						SpellAction action = ench.getTriggerAction(livingTarget, false);
						if (action != null)
							action.apply(livingSource, 1.0f);
					}
				}
		
				// Offense
				for (ItemStack stack : livingSource.getEquipmentAndArmor()) {
					if (stack == null || !(stack.getItem() instanceof EnchantedEquipment))
						continue;
					
					EnchantedEquipment ench = (EnchantedEquipment) stack.getItem();
					if (ench.shouldTrigger(true)) {
						SpellAction action = ench.getTriggerAction(livingSource, true);
						if (action != null)
							action.apply(livingTarget, 1.0f);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onDamage(LivingHurtEvent event) {
		if (event.getSource().getSourceOfDamage() != null) {
			EntityLivingBase source = null;
			
			// Projectiles can be from no entity
			if (event.getSource().isProjectile()) {
				
				Entity proj = event.getSource().getSourceOfDamage();
				Entity shooter;
				if (proj instanceof EntityArrow) {
					shooter = ((EntityArrow) proj).shootingEntity;
					if (shooter != null && shooter instanceof EntityLivingBase)
						source = (EntityLivingBase) shooter;
				} else if (proj instanceof EntityFireball) {
					source = ((EntityFireball) proj).shootingEntity;
				} else if (proj instanceof EntityThrowable) {
					source = ((EntityThrowable) proj).getThrower();
				}
			} else if (event.getSource().getSourceOfDamage() instanceof EntityLivingBase) {
				source = (EntityLivingBase) event.getSource().getSourceOfDamage();
			}
			
			if (source != null) {
				Iterator<Entry<IGenericListener, DamagedInfo>> it = damagedInfos.entrySet().iterator();
				while (it.hasNext()) {
					Entry<IGenericListener, DamagedInfo> entry = it.next();
					if (entry.getValue() == null)
						continue;
					
					if (entry.getValue().entity.getPersistentID() != event.getEntityLiving().getPersistentID()) {
						continue;
					}
					
					if (entry.getKey().onEvent(Event.DAMAGED, source, null))
						it.remove();
				}
			}
		}
		
		onHealth(event.getEntityLiving());
	}
	
	@SubscribeEvent
	public void onBlockBreak(BreakEvent event) {
		if (event.isCanceled())
			return;
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(event.getPlayer());
		
		if (attr != null && attr.isUnlocked()) {
			if (event.getState().getBlock() instanceof ILoreTagged) {
				attr.giveBasicLore((ILoreTagged) event.getState().getBlock());
				System.out.println("lore");
			} else if (null != LoreRegistry.getPreset(event.getState().getBlock()))
				attr.giveBasicLore(LoreRegistry.getPreset(event.getState().getBlock()));
		}
		
		if (event.getState().getMaterial() == Material.LEAVES
				&& NostrumMagica.rand.nextFloat() <= 0.2f) {
			EntityItem entity = new EntityItem(event.getWorld(),
					event.getPos().getX() + 0.5,
					event.getPos().getY() + 0.5,
					event.getPos().getZ() + 0.5,
					ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1));
			event.getWorld().spawnEntityInWorld(entity);
		}
		if (event.getState().getMaterial() == Material.WEB) {
			EntityItem entity = new EntityItem(event.getWorld(),
					event.getPos().getX() + 0.5,
					event.getPos().getY() + 0.5,
					event.getPos().getZ() + 0.5,
					ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1));
			event.getWorld().spawnEntityInWorld(entity);
		}
		
		if (event.getState().getBlock() instanceof BlockTallGrass
				&& NostrumMagica.rand.nextFloat() <= 0.05f) {
			EntityItem entity = new EntityItem(event.getWorld(),
					event.getPos().getX() + 0.5,
					event.getPos().getY() + 0.5,
					event.getPos().getZ() + 0.5,
					ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1));
			event.getWorld().spawnEntityInWorld(entity);
		}
		
		if (event.getState().getBlock() instanceof BlockTallGrass
				&& NostrumMagica.rand.nextFloat() <= 0.05f) {
			EntityItem entity = new EntityItem(event.getWorld(),
					event.getPos().getX() + 0.5,
					event.getPos().getY() + 0.5,
					event.getPos().getZ() + 0.5,
					ReagentItem.instance().getReagent(ReagentType.GINSENG, 1));
			event.getWorld().spawnEntityInWorld(entity);
		}
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.isCanceled())
			return;

		if (event.getSource() != null && event.getSource().getSourceOfDamage() != null && event.getSource().getSourceOfDamage() instanceof EntityPlayer) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(event.getSource().getEntity());
			
			if (attr != null && attr.isUnlocked()) {
				if (event.getEntityLiving() instanceof ILoreTagged) {
					attr.giveBasicLore((ILoreTagged) event.getEntityLiving());
				} else if (null != LoreRegistry.getPreset(event.getEntityLiving())) {
					attr.giveBasicLore(LoreRegistry.getPreset(event.getEntityLiving()));
				}
			}
			
		}
		
		if (event.getEntityLiving() instanceof EntityPlayer && !event.getEntityLiving().worldObj.isRemote) {
			// Scan for baubles, since Baubles doesn't call onUnequip when you die....
			IBaublesItemHandler baubles = BaublesApi.getBaublesHandler((EntityPlayer) event.getEntityLiving());
			for (int i = 0; i < baubles.getSlots(); i++) {
				ItemStack stack = baubles.getStackInSlot(i);
				if (stack != null && stack.getItem() instanceof ItemMagicBauble) {
					((ItemMagicBauble) stack.getItem()).onUnequipped(stack, event.getEntityLiving());
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onMobDrop(LivingDropsEvent event) {
		if (event.getEntityLiving().isEntityUndead()) {
			for (int i = 0; i <= event.getLootingLevel(); i++) {
				if (NostrumMagica.rand.nextFloat() <= 0.3f) {
					EntityItem entity = new EntityItem(event.getEntity().worldObj,
							event.getEntity().posX,
							event.getEntity().posY,
							event.getEntity().posZ,
							new ItemStack(ReagentItem.instance(), 1, ReagentType.GRAVE_DUST.getMeta()));
					event.getDrops().add(entity);
				}
			}
				
		}
		if (event.getEntityLiving() instanceof EntitySpider) {
			for (int i = 0; i <= event.getLootingLevel(); i++) {
				if (NostrumMagica.rand.nextFloat() <= 0.4f) {
					EntityItem entity = new EntityItem(event.getEntity().worldObj,
							event.getEntity().posX,
							event.getEntity().posY,
							event.getEntity().posZ,
							new ItemStack(ReagentItem.instance(), 1, ReagentType.SPIDER_SILK.getMeta()));
					event.getDrops().add(entity);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onCraft(PlayerEvent.ItemCraftedEvent e) {
		if (e.isCanceled())
			return;
		
		EntityPlayer player = e.player;
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		
		if (e.crafting.getItem() instanceof ILoreTagged && attr != null && attr.isUnlocked()) {
			attr.giveBasicLore((ILoreTagged) e.crafting.getItem());
		}
	}
	
	@SubscribeEvent
	public void onPickup(EntityItemPickupEvent e) {
		if (e.isCanceled())
			return;
		
		if (!(e.getEntityLiving() instanceof EntityPlayer))
			return; // It SAYS EntityItemPickup, so just in case...
		
		EntityPlayer player = e.getEntityPlayer();
		ItemStack addedItem = e.getItem().getEntityItem();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		
		if (addedItem.getItem() instanceof ILoreTagged && attr != null && attr.isUnlocked()) {
			attr.giveBasicLore((ILoreTagged) addedItem.getItem());
		}
		
		if (e.getItem().getEntityItem().getItem() instanceof ReagentItem
				&& !player.isSneaking()) {
			int originalSize = addedItem.stackSize;
			for (ItemStack item : player.inventory.offHandInventory) {
				// Silly but prefer offhand
				if (item != null && item.getItem() instanceof ReagentBag) {
					if (ReagentBag.isVacuumEnabled(item)) {
						addedItem = ReagentBag.addItem(item, addedItem);
						if (addedItem == null || addedItem.stackSize < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.worldObj, player.posX, player.posY, player.posZ);
						}
						if (addedItem == null) {
							e.setCanceled(true);
							e.getItem().setDead();
							return;
						}
						originalSize = addedItem.stackSize;
					}
				}
			}
			for (ItemStack item : player.inventory.mainInventory) {
				if (item != null && item.getItem() instanceof ReagentBag) {
					if (ReagentBag.isVacuumEnabled(item)) {
						addedItem = ReagentBag.addItem(item, addedItem);
						if (addedItem == null || addedItem.stackSize < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.worldObj, player.posX, player.posY, player.posZ);
						}
						if (addedItem == null) {
							e.setCanceled(true);
							e.getItem().setDead();
							return;
						}
						originalSize = addedItem.stackSize;
					}
				}
			}
			
			if (addedItem.stackSize < e.getItem().getEntityItem().stackSize) {
				e.setCanceled(true);
				e.getItem().setEntityItemStack(addedItem);
			}
			
		}
		
		if (e.getItem().getEntityItem().getItem() instanceof SpellRune
				&& !player.isSneaking()) {
			int originalSize = addedItem.stackSize;
			for (ItemStack item : player.inventory.offHandInventory) {
				// Silly but prefer offhand
				if (item != null && item.getItem() instanceof RuneBag) {
					if (RuneBag.isVacuumEnabled(item)) {
						addedItem = RuneBag.addItem(item, addedItem);
						if (addedItem == null || addedItem.stackSize < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.worldObj, player.posX, player.posY, player.posZ);
						}
						if (addedItem == null) {
							e.setCanceled(true);
							e.getItem().setDead();
							return;
						}
						originalSize = addedItem.stackSize;
					}
				}
			}
			for (ItemStack item : player.inventory.mainInventory) {
				if (item != null && item.getItem() instanceof RuneBag) {
					if (RuneBag.isVacuumEnabled(item)) {
						addedItem = RuneBag.addItem(item, addedItem);
						if (addedItem == null || addedItem.stackSize < originalSize) {
							NostrumMagicaSounds.UI_TICK.play(player.worldObj, player.posX, player.posY, player.posZ);
						}
						if (addedItem == null) {
							e.setCanceled(true);
							e.getItem().setDead();
							return;
						}
						originalSize = addedItem.stackSize;
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onConstruct(EntityConstructing event) {
		Entity ent = event.getEntity();
		if (ent instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase) ent;
			living.getAttributeMap().registerAttribute(AttributeMagicResist.instance());
		}
	}
	
	@SubscribeEvent
	public void onTick(ServerTickEvent event) {
		if (event.phase == Phase.START) {
			tickCount++;
			
			// Regain mana
			if (tickCount % 10 == 0) {
				for (World world : FMLCommonHandler.instance().getMinecraftServerInstance().worldServers) {
					if (world.playerEntities.isEmpty()) {
						continue;
					}
					
					for (EntityPlayer player : world.playerEntities) {
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
						if (entry.getKey().onEvent(Event.TIME, null, null))
							it.remove();
						else {
							info.startTick = tickCount;
							continue;
						}
					}
				}
				
				int diff = tickCount - info.startTick;
				if (diff % info.interval == 0)
					if (entry.getKey().onEvent(Event.TIME, null, null))
						it.remove();
			}
		}
	}
	
	private void regenMana(EntityPlayer player) {
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
		
		int mana = 1 + (int) (bonus);
		bonus = bonus - (int) bonus;
		if (bonus > 0f && NostrumMagica.rand.nextFloat() < bonus)
			mana++;
		
		stats.addMana(mana);
		EntityTracker tracker = ((WorldServer) player.worldObj).getEntityTracker();
		if (tracker == null)
			return;
		
		tracker.sendToTrackingAndSelf(player, NetworkHandler.getSyncChannel()
				.getPacketFrom(new ManaMessage(player, stats.getMana())));
	}
	
	@SubscribeEvent
	public void onConnect(PlayerLoggedInEvent event) {
		if (event.player.worldObj.isRemote) {
			return;
		}
		
		NostrumMagica.proxy.syncPlayer((EntityPlayerMP) event.player);
	}
	
	@SubscribeEvent
	public void onDisconnect(PlayerLoggedOutEvent event) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(event.player);
		if (attr != null)
			attr.clearFamiliars();
		
		if (event.player.worldObj.isRemote) {
			this.clearAll();
		}
	}
	
	@SubscribeEvent
	public void onXPPickup(PlayerPickupXpEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		int xp = event.getOrb().xpValue;
		if (attr != null) {
			for (ItemStack item : player.getEquipmentAndArmor()) {
				if (item == null)
					continue;
				int leftover = tryThanos(player, item, xp);
				if (leftover == 0) {
					break;
				} else if (leftover != xp) {
					xp = leftover;
				}
			}
			if (xp != 0)
			for (ItemStack item : player.inventory.mainInventory) {
				if (item == null)
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
	
	private int tryThanos(EntityPlayer player, ItemStack item, int xp) {
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
	public void onMagicEffect(EntityLivingBase entity, @Nullable EntityLivingBase caster, SpellActionSummary summary) {
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
	
}
