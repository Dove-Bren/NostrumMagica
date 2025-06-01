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
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

/**
 * Abstract trigger that waits for entities to be in a (static) area and then applies the spell to them.
 * May apply multiple times.
 * @author Skyler
 *
 */
public abstract class AreaShape extends SpellShape implements ISelectableShape {
	
	public abstract class AreaShapeInstance extends SpellShape.SpellShapeInstance implements IGenericListener {
		
//		private static final int TICK_RATE = 5;
//		private static final int NUM_TICKS = (20 * 20) / TICK_RATE; // 20 seconds
//		private static final int GROUND_TICK_CYCLES = (20 * 4) / TICK_RATE;
		
		private static final int GROUND_TICK_CYCLES = 4; // make this configurable! This means every 4 (tickRate) intervals affects the ground

		protected final Level world;
		protected final Vec3 pos;
		protected final int tickRate;
		protected final int duration;
		private final boolean continuous;
		protected final boolean affectsGround;
		protected final boolean affectsEnts;
		
		private final float radiusHint;
		
		protected int aliveCycles;
		private boolean dead;
		private Map<LivingEntity, Integer> affected; // maps to time last effect visited
		
		public AreaShapeInstance(ISpellState state, Level world, Vec3 pos, int tickRate, int duration, float radiusHint, boolean continuous, boolean affectsEnts, boolean affectsGround, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.radiusHint = radiusHint;
			this.continuous = continuous;
			this.tickRate = tickRate;
			this.duration = duration;
			this.affectsGround = affectsGround;
			this.affectsEnts = affectsEnts;
			
			dead = false;
			affected = new HashMap<>();
			aliveCycles = 0;
		}
		
		protected abstract boolean isInArea(LivingEntity entity);
		protected abstract boolean isInArea(Level world, BlockPos pos);
		
		@Override
		public void spawn(LivingEntity caster) {
			NostrumMagica.playerListener.registerProximity(this, world, pos, radiusHint);
			
			// Register timer for life and for effects
			NostrumMagica.playerListener.registerTimer(this, 0, this.tickRate);
			
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
					BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
					List<SpellLocation> list = Lists.newArrayList();
					for (int i = -(int)radiusHint; i <= radiusHint; i++)
					for (int j = -(int)radiusHint; j <= radiusHint; j++)
					for (int k = -(int)radiusHint; k <= radiusHint; k++) {
						cursor.set(pos.x + i, pos.y + j, pos.z + k);
						if (this.isInArea(world, cursor)) {
							list.add(new SpellLocation(world, cursor.immutable()));
						}
					}
					TriggerData data = new TriggerData(
							null,
							list
							);
					this.trigger(data, 1f, true);
				}
				
				aliveCycles++;
				if (aliveCycles >= duration) { // 20 seconds
					this.dead = true;
					return true;
				}
				
				return false;
			}
			
			
			// Else we've already been set. Check if actually inside wall, and then try to trigger
			if (affectsEnts && this.isInArea(entity)) {
				if (visitEntity(entity)) {
					TriggerData data = new TriggerData(
							Lists.newArrayList(entity),
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
					|| (continuous && last + 20 < entity.tickCount)
					) {
				affected.put(entity, entity.tickCount);
				return true;
			}
			return false;
		}
	}

	
	protected AreaShape(String key) {
		super(key);
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, this.affectsEntities(params), this.affectsBlocks(params));
	}
}
