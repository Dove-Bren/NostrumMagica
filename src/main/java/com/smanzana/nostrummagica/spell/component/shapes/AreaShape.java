package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listener.PlayerListener.Event;
import com.smanzana.nostrummagica.listener.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Abstract trigger that waits for entities to be in an area and then applies the spell to them.
 * May apply multiple times.
 * @author Skyler
 *
 */
public abstract class AreaShape extends SpellShape {
	
	public abstract class AreaShapeInstance extends SpellShape.SpellShapeInstance implements IGenericListener {
		
		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 20) / TICK_RATE; // 20 seconds
		private static final int GROUND_TICK_CYCLES = (20 * 4) / TICK_RATE;

		protected final World world;
		protected final Vector3d pos;
		protected final int tickRate;
		protected final int duration;
		private final boolean continuous;
		private final boolean affectsGround;
		
		private final float radiusHint;
		
		private int aliveCycles;
		private boolean dead;
		private Map<LivingEntity, Integer> affected; // maps to time last effect visited
		
		public AreaShapeInstance(ISpellState state, World world, Vector3d pos, int tickRate, int duration, float radiusHint, boolean continuous, boolean affectsGround, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.radiusHint = radiusHint;
			this.continuous = continuous;
			this.tickRate = tickRate;
			this.duration = duration;
			this.affectsGround = affectsGround;
			
			dead = false;
			affected = new HashMap<>();
			aliveCycles = 0;
		}
		
		protected abstract boolean isInArea(LivingEntity entity);
		protected abstract boolean isInArea(World world, BlockPos pos);
		
		@Override
		public void spawn(LivingEntity caster) {
			NostrumMagica.playerListener.registerProximity(this, world, pos, radiusHint);
			
			// Register timer for life and for effects
			NostrumMagica.playerListener.registerTimer(this, 0, TICK_RATE);
			
			doEffect();
		}

		@Override
		public boolean onEvent(Event type, LivingEntity entity, Object empty) {
			if (dead)
				return true;
			
			if (type == Event.TIME) {
				
				doEffect();
				
				if (affectsGround && aliveCycles % GROUND_TICK_CYCLES == 0) {
					// check all blocks in -radius,-radius,-radius <-> radius,radius,radius
					BlockPos.Mutable cursor = new BlockPos.Mutable();
					List<SpellLocation> list = Lists.newArrayList();
					for (int i = -(int)radiusHint; i <= radiusHint; i++)
					for (int j = -(int)radiusHint; j <= radiusHint; j++)
					for (int k = -(int)radiusHint; k <= radiusHint; k++) {
						cursor.setPos(pos.x + i, pos.y + j, pos.z + k);
						if (this.isInArea(world, cursor)) {
							list.add(new SpellLocation(cursor.toImmutable()));
						}
					}
					TriggerData data = new TriggerData(
							null,
							world,
							list
							);
					this.trigger(data, 1f, true);
				}
				
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
							null,
							null
							);
					this.trigger(data, 1f, true);
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
		protected boolean visitEntity(LivingEntity entity) {
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

	
	protected AreaShape(String key) {
		super(key);
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(true, true, true);
	}
}
