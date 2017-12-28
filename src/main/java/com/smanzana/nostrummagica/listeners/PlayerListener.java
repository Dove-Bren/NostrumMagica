package com.smanzana.nostrummagica.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ManaMessage;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
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
		timeInfos = new HashMap<>();
		proximityInfos = new HashMap<>();
		positionInfos = new HashMap<>();
		damagedInfos = new HashMap<>();
		healthInfos = new HashMap<>();
		foodInfos = new HashMap<>();
		manaInfos = new HashMap<>();
		
		MinecraftForge.EVENT_BUS.register(this);
		tickCount = 0;
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
		if ((ent.lastTickPosX - ent.posX) >= 0.01f
				|| (ent.lastTickPosY - ent.posY >= 0.01f)
				|| (ent.lastTickPosZ - ent.posZ >= 0.01f)) {
			// Moved
			Iterator<Entry<IMagicListener, ProximityInfo>> it = proximityInfos.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IMagicListener, ProximityInfo> entry = it.next();
				if (entry.getValue() == null)
					continue;
				
				if (entry.getValue().world != ent.worldObj)
					continue;
				
				double dist = Math.abs(ent.getPositionVector().subtract(entry.getValue().position).lengthVector());
				if (dist <= entry.getValue().proximity)
					if (entry.getKey().onEvent(Event.PROXIMITY, ent))
						it.remove();
					
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
				
				float level = attr.getMana() / attr.getMaxMana();
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
					
					if (entry.getValue().entity.getPersistentID() != event.getEntityLiving().getPersistentID())
						continue;
					
					if (entry.getKey().onEvent(Event.DAMAGED, source))
						it.remove();
				}
			}
		}
		
		onHealth(event.getEntityLiving());
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
			if (diff == info.interval)
				if (entry.getKey().onEvent(Event.TIME, null))
					it.remove();
		}
	}
	
	private void regenMana(EntityPlayer player) {
		// Called 2 times a second
		INostrumMagic stats = NostrumMagica.getMagicWrapper(player);
		
		// TODO add regen speed upgrades?
		int mana = 1;
		stats.addMana(mana);
		EntityTracker tracker = ((WorldServer) player.worldObj).getEntityTracker();
		if (tracker == null)
			return;
		
		tracker.sendToTrackingAndSelf(player, NetworkHandler.getSyncChannel()
				.getPacketFrom(new ManaMessage(player, stats.getMana())));
	}
	
	@SubscribeEvent
	public void onConnect(PlayerLoggedInEvent event) {
		System.out.println("connect");
		if (event.player.worldObj.isRemote) {
			return;
		}
		System.out.println("Sync");
		
		NostrumMagica.proxy.syncPlayer((EntityPlayerMP) event.player);
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
		Spell spell = new Spell("Wind Cutter");
		spell.addPart(new SpellPart(
				ProjectileTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.WIND,
				1,
				null,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Roots");
		spell.addPart(new SpellPart(
				SelfTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.EARTH,
				1,
				EAlteration.INFLICT,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Transmute");
		spell.addPart(new SpellPart(
				SelfTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.PHYSICAL,
				1,
				EAlteration.ALTER,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Summon Physical");
		spell.addPart(new SpellPart(
				SelfTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.PHYSICAL,
				1,
				EAlteration.SUMMON,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Summon Zapper");
		spell.addPart(new SpellPart(
				ProjectileTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.LIGHTNING,
				1,
				EAlteration.SUMMON,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Summon Fire");
		spell.addPart(new SpellPart(
				ProjectileTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.FIRE,
				1,
				EAlteration.SUMMON,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Summon Earth");
		spell.addPart(new SpellPart(
				ProjectileTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.EARTH,
				1,
				EAlteration.SUMMON,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Summon Ice");
		spell.addPart(new SpellPart(
				ProjectileTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.ICE,
				1,
				EAlteration.SUMMON,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Raze");
		spell.addPart(new SpellPart(
				ProjectileTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				AoEShape.instance(),
				EMagicElement.FIRE,
				1,
				EAlteration.CONJURE,
				new SpellPartParam(2, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Pain");
		spell.addPart(new SpellPart(
				SelfTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.FIRE,
				1,
				null,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Harm");
		spell.addPart(new SpellPart(
				SelfTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.PHYSICAL,
				1,
				null,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Magic Shield");
		spell.addPart(new SpellPart(
				SelfTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.ICE,
				1,
				EAlteration.SUPPORT,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Physical Shield");
		spell.addPart(new SpellPart(
				SelfTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.EARTH,
				1,
				EAlteration.SUPPORT,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		spell = new Spell("Frostbite");
		spell.addPart(new SpellPart(
				SelfTrigger.instance(),
				new SpellPartParam(0, false)
				));
		spell.addPart(new SpellPart(
				SingleShape.instance(),
				EMagicElement.ICE,
				1,
				EAlteration.INFLICT,
				new SpellPartParam(0, false)
				));
		SpellTome.addSpell(tome, spell);
		
		BlockPos pos = e.getPos().add(0, 1, 0);
		e.getWorld().spawnEntityInWorld(new EntityItem(
				e.getWorld(),
				pos.getX() + .5f,
				(float) pos.getY(),
				pos.getZ() + .5f,
				tome
				));
	}
}
