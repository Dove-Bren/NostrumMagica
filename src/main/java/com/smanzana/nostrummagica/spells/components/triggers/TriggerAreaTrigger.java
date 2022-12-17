package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Abstract trigger that waits for entities to be in an area and then applies the spell to them.
 * May apply multiple times.
 * @author Skyler
 *
 */
public abstract class TriggerAreaTrigger extends SpellTrigger {
	
	public abstract class TriggerAreaTriggerInstance extends SpellTrigger.SpellTriggerInstance implements IGenericListener {
		
		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 20) / TICK_RATE; // 20 seconds

		protected final World world;
		protected final Vec3d pos;
		protected final int tickRate;
		protected final int duration;
		private final boolean continuous;
		
		private final float radiusHint;
		
		private int aliveCycles;
		private boolean dead;
		private Map<EntityLivingBase, Integer> affected; // maps to time last effect visited
		
		public TriggerAreaTriggerInstance(SpellState state, World world, Vec3d pos, int tickRate, int duration, float radiusHint, boolean continuous) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.radiusHint = radiusHint;
			this.continuous = continuous;
			this.tickRate = tickRate;
			this.duration = duration;
			
			dead = false;
			affected = new HashMap<>();
			aliveCycles = 0;
		}
		
		protected abstract boolean isInArea(EntityLivingBase entity);
		
		@Override
		public void init(EntityLivingBase caster) {
			// We are instant! Whoo!
			
			NostrumMagica.playerListener.registerProximity(this, world, pos, radiusHint);
			
			// Register timer for life and for effects
			NostrumMagica.playerListener.registerTimer(this, 0, TICK_RATE);
			
			doEffect();
		}

		@Override
		public boolean onEvent(Event type, EntityLivingBase entity, Object empty) {
			if (dead)
				return true;
			
			if (type == Event.TIME) {
				
				doEffect();
				
				aliveCycles++;
				if (aliveCycles >= NUM_TICKS) { // 20 seconds
					this.dead = true;
					return true;
				}
				
				return false;
			}
			
			
			// Else we've already been set. Check if actually inside wall, and then try to trigger
			if (this.isInArea(entity)) {
				if (visitEntity(entity)) {
					TriggerData data = new TriggerData(
							Lists.newArrayList(entity),
							Lists.newArrayList(this.getState().getSelf()),
							null,
							null
							);
					this.trigger(data, true);
				}
			}
			return false;
		}
		
		protected abstract void doEffect();
		
		/**
		 * Check if entity should experirence effects.
		 * Also tracks time when returned true to slow down effects.
		 * @param entity
		 * @return
		 */
		protected boolean visitEntity(EntityLivingBase entity) {
			if (entity == null) {
				return false;
			}
			
			Integer last = affected.get(entity);
			if (last == null
					|| (continuous && last + 20 < entity.ticksExisted)
					) {
				affected.put(entity, entity.ticksExisted);
				return true;
			}
			return false;
		}
	}

	
	protected TriggerAreaTrigger(String key) {
		super(key);
	}
}