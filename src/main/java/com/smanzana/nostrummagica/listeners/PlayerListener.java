package com.smanzana.nostrummagica.listeners;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.enchantments.EnchantmentManaRecovery;
import com.smanzana.nostrummagica.items.EnchantedEquipment;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ManaMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;

import net.minecraft.block.BlockBush;
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
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
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
	}
	
	public interface IMagicListener {
		/**
		 * Called for each event that is activated.
		 * @param type The event type this call matches
		 * @param entity The entity (null for Time events) involved. Damaged events
		 * set this to the entity that did the damaging.
		 * @return true to remove this listener so it doesn't receive anymore updates
		 */
		public boolean onEvent(Event type, EntityLivingBase entity);
	}

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
	
	private int tickCount;
	private Map<IMagicListener, TimeInfo> timeInfos;
	private Map<IMagicListener, ProximityInfo> proximityInfos;
	private Map<IMagicListener, PositionInfo> positionInfos;
	private Map<IMagicListener, DamagedInfo> damagedInfos;
	private Map<IMagicListener, HealthInfo> healthInfos;
	private Map<IMagicListener, FoodInfo> foodInfos;
	private Map<IMagicListener, ManaInfo> manaInfos;
	
	public PlayerListener() {
		timeInfos = new ConcurrentHashMap<>();
		proximityInfos = new ConcurrentHashMap<>();
		positionInfos = new ConcurrentHashMap<>();
		damagedInfos = new ConcurrentHashMap<>();
		healthInfos = new ConcurrentHashMap<>();
		foodInfos = new ConcurrentHashMap<>();
		manaInfos = new ConcurrentHashMap<>();
		
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
	public void registerTimer(IMagicListener listener, int delay, int interval) {
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
	public void registerProximity(IMagicListener listener, 
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
	public void registerPosition(IMagicListener listener,
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
	public void registerHit(IMagicListener listener,
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
	public void registerHealth(IMagicListener listener,
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
	public void registerFood(IMagicListener listener,
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
	public void registerMana(IMagicListener listener,
			EntityLivingBase entity, float level, boolean higher) {
		manaInfos.put(listener,
				new ManaInfo(entity, level, higher));
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		
		EntityLivingBase ent = event.getEntityLiving(); // convenience
		if (Math.abs(ent.motionX) >= 0.01f
				|| Math.abs(ent.motionY) >= 0.01f
				|| Math.abs(ent.motionZ) >= 0.01f) {
			// Moved
			Iterator<Entry<IMagicListener, ProximityInfo>> it = proximityInfos.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IMagicListener, ProximityInfo> entry = it.next();
				if (entry.getValue() == null)
					continue;
				
				if (entry.getValue().world != ent.worldObj)
					continue;
				
				double dist = Math.abs(ent.getPositionVector().subtract(entry.getValue().position).lengthVector());
				if (dist <= entry.getValue().proximity) {
					if (entry.getKey().onEvent(Event.PROXIMITY, ent))
						it.remove();
				}
					
			}
			
			Iterator<Entry<IMagicListener, PositionInfo>> it2 = positionInfos.entrySet().iterator();
			while (it2.hasNext()) {
				Entry<IMagicListener, PositionInfo> entry = it2.next();
				if (entry.getValue() == null)
					continue;
				
				if (entry.getValue().world != ent.worldObj)
					continue;
				
				BlockPos entpos = ent.getPosition();
				for (BlockPos p : entry.getValue().blocks) {
					if (p.equals(entpos))
						if (entry.getKey().onEvent(Event.POSITION, ent))
							it2.remove();
				}	
			}
		}
		
		if (ent instanceof EntityPlayer) {
			Iterator<Entry<IMagicListener, FoodInfo>> it = foodInfos.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IMagicListener, FoodInfo> entry = it.next();
				if (entry.getValue() == null)
					continue;
				
				if (entry.getValue().entity.getPersistentID() != ent.getPersistentID())
					continue;
				
				int level = ((EntityPlayer) ent).getFoodStats().getFoodLevel();
				int thresh = entry.getValue().threshold;
				
				if (entry.getValue().higher) {
					if (level >= thresh)
						if (entry.getKey().onEvent(Event.FOOD, ent))
							it.remove();
				} else {
					if (level <= thresh)
						if (entry.getKey().onEvent(Event.FOOD, ent))
							it.remove();
				}
			}
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(ent);
		if (attr != null) {
			Iterator<Entry<IMagicListener, ManaInfo>> it = manaInfos.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IMagicListener, ManaInfo> entry = it.next();
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
						if (entry.getKey().onEvent(Event.MANA, ent))
							it.remove();
				} else {
					if (level <= thresh)
						if (entry.getKey().onEvent(Event.MANA, ent))
							it.remove();
				}
			}
		}
	}
	
	private void onHealth(EntityLivingBase ent) {
		Iterator<Entry<IMagicListener, HealthInfo>> it = healthInfos.entrySet().iterator();
		while (it.hasNext()) {
			Entry<IMagicListener, HealthInfo> entry = it.next();
			if (entry.getValue() == null)
				continue;

			if (entry.getValue().entity.getPersistentID() != ent.getPersistentID())
				continue;
			
			float level = ent.getHealth() / ent.getMaxHealth();
			float thresh = entry.getValue().threshold;
			
			if (entry.getValue().higher) {
				if (level >= thresh)
					if (entry.getKey().onEvent(Event.HEALTH, ent))
						it.remove();
			} else {
				if (level <= thresh)
					if (entry.getKey().onEvent(Event.HEALTH, ent))
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
				Iterator<Entry<IMagicListener, DamagedInfo>> it = damagedInfos.entrySet().iterator();
				while (it.hasNext()) {
					Entry<IMagicListener, DamagedInfo> entry = it.next();
					if (entry.getValue() == null)
						continue;
					
					if (entry.getValue().entity.getPersistentID() != event.getEntityLiving().getPersistentID()) {
						continue;
					}
					
					if (entry.getKey().onEvent(Event.DAMAGED, source))
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
		
		if (event.getState().getBlock() instanceof BlockBush
				&& NostrumMagica.rand.nextFloat() <= 0.05f) {
			EntityItem entity = new EntityItem(event.getWorld(),
					event.getPos().getX() + 0.5,
					event.getPos().getY() + 0.5,
					event.getPos().getZ() + 0.5,
					ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1));
			event.getWorld().spawnEntityInWorld(entity);
		}
		
		if (event.getState().getBlock() instanceof BlockBush
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
		
		if (e.getItem().getEntityItem().getItem() instanceof ReagentItem) {
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
			
			if (addedItem == null || addedItem.stackSize < e.getItem().getEntityItem().stackSize) {
				e.setCanceled(true);
				e.getItem().setEntityItemStack(addedItem);
			}
			
		}
	}
	
	@SubscribeEvent
	public void onTick(ServerTickEvent event) {
		tickCount++;
		
		// Regain mana
		if (tickCount % 10 == 0) {
			for (World world : FMLCommonHandler.instance().getMinecraftServerInstance().worldServers) {
				for (EntityPlayer player : world.playerEntities) {
					regenMana(player);
				}
			}
		}
		
		Iterator<Entry<IMagicListener, TimeInfo>> it = timeInfos.entrySet().iterator();
		while (it.hasNext()) {
			Entry<IMagicListener, TimeInfo> entry = it.next();
			TimeInfo info = entry.getValue();
			if (info.delay > 0) {
				info.delay--;
				if (info.delay == 0) {
					if (entry.getKey().onEvent(Event.TIME, null))
						it.remove();
					else {
						info.startTick = tickCount;
						continue;
					}
				}
			}
			
			int diff = tickCount - info.startTick;
			if (diff % info.interval == 0)
				if (entry.getKey().onEvent(Event.TIME, null))
					it.remove();
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
	
	// TESTING
	@SubscribeEvent
	public void onTest(UseHoeEvent e) {
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(e.getEntityPlayer());
		if (attr != null) {
			attr.unlock();
		}
		
		if (e.getWorld().isRemote)
			return;
		
		ItemStack tome = new ItemStack(SpellTome.instance(), 1);
		
		// Create spell on server side.
		// Spawn tome with that spell in it
		Spell spell;
//		= new Spell("Wind Cutter");
//		spell.addPart(new SpellPart(
//				ProjectileTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.WIND,
//				1,
//				null,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
		spell = new Spell("Wind Palm");
		spell.addPart(new SpellPart(
				TouchTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.WIND,
				3,
				null,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Wind Beam Chain");
//		spell.addPart(new SpellPart(
//				BeamTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				ChainShape.instance(),
//				EMagicElement.WIND,
//				3,
//				null,
//				new SpellPartParam(3, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Transmute");
//		spell.addPart(new SpellPart(
//				SelfTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.PHYSICAL,
//				1,
//				EAlteration.ALTER,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Alter Fire I");
//		spell.addPart(new SpellPart(
//				SelfTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.FIRE,
//				1,
//				EAlteration.ALTER,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Alter Ice 3");
//		spell.addPart(new SpellPart(
//				SelfTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.ICE,
//				3,
//				EAlteration.ALTER,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Summon Zapper");
//		spell.addPart(new SpellPart(
//				DamagedTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.LIGHTNING,
//				1,
//				EAlteration.SUMMON,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Summon Wind");
//		spell.addPart(new SpellPart(
//				ProjectileTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.WIND,
//				1,
//				EAlteration.SUMMON,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Summon Ender");
//		spell.addPart(new SpellPart(
//				ProjectileTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.ENDER,
//				1,
//				EAlteration.SUMMON,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Magic Shield");
//		spell.addPart(new SpellPart(
//				ManaTrigger.instance(),
//				new SpellPartParam(0.5f, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.ICE,
//				1,
//				EAlteration.SUPPORT,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Physical Shield");
//		spell.addPart(new SpellPart(
//				HealthTrigger.instance(),
//				new SpellPartParam(0.5f, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.EARTH,
//				1,
//				EAlteration.SUPPORT,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Pull");
//		spell.addPart(new SpellPart(
//				SelfTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.LIGHTNING,
//				1,
//				EAlteration.SUPPORT,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Magic Wall I");
//		spell.addPart(new SpellPart(
//				ProjectileTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.WIND,
//				1,
//				EAlteration.CONJURE,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Geoblock");
//		spell.addPart(new SpellPart(
//				ProjectileTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.EARTH,
//				1,
//				EAlteration.CONJURE,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Cursed Ice");
//		spell.addPart(new SpellPart(
//				ProjectileTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.ICE,
//				1,
//				EAlteration.CONJURE,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Cursed Ice III");
//		spell.addPart(new SpellPart(
//				ProjectileTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.ICE,
//				3,
//				EAlteration.CONJURE,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
//		
//		spell = new Spell("Phase");
//		spell.addPart(new SpellPart(
//				ProjectileTrigger.instance(),
//				new SpellPartParam(0, false)
//				));
//		spell.addPart(new SpellPart(
//				SingleShape.instance(),
//				EMagicElement.ENDER,
//				1,
//				EAlteration.CONJURE,
//				new SpellPartParam(0, false)
//				));
//		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Grow");
		spell.addPart(new SpellPart(
				ProjectileTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.EARTH,
				1,
				EAlteration.CONJURE,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		ItemStack scroll = new ItemStack(SpellScroll.instance(), 1);
		SpellScroll.setSpell(scroll, spell);
		
		BlockPos pos = e.getPos().add(0, 1, 0);
		e.getWorld().spawnEntityInWorld(new EntityItem(
				e.getWorld(),
				pos.getX() + .5f,
				(float) pos.getY(),
				pos.getZ() + .5f,
				tome
				));
		e.getWorld().spawnEntityInWorld(new EntityItem(
				e.getWorld(),
				pos.getX() + .5f,
				(float) pos.getY(),
				pos.getZ() + .5f,
				scroll
				));
		
		pos.add(0, 5, 0);
		
		//NostrumDungeon.temp.spawn(e.getWorld(), new NostrumDungeon.DungeonExitPoint(pos, EnumFacing.NORTH));
		//(new ShrineRoom()).spawn(null, e.getWorld(), new NostrumDungeon.DungeonExitPoint(pos, EnumFacing.fromAngle(e.getEntityPlayer().rotationYaw)));
	}
}
