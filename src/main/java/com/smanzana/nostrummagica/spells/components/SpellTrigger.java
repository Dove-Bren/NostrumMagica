package com.smanzana.nostrummagica.spells.components;

import java.util.List;

import com.smanzana.nostrummagica.spells.Spell.SpellState;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * A 'trigger' in a spell. Either some condition or some selection field (like 'touch',
 * 'projectile', 'beam').
 * @author Skyler
 *
 */
public abstract class SpellTrigger {

	/**
	 * Instantiated spell trigger in the world
	 * @author Skyler
	 *
	 */
	public abstract class SpellTriggerInstance {
		private SpellState state; // The state to trigger
		
		public SpellTriggerInstance(SpellState state) {
			this.state = state;
		}
		
		// TODO implementation figured out when to call trigger.
		
		protected void trigger(TriggerData data) {
			state.trigger(data.targets, data.others, data.world, data.pos);
		}
		
		protected SpellState getState() {
			return state;
		}
		
		/**
		 * Called after trigger stuff is set up in owning spell.
		 * @param caster
		 */
		public abstract void init(EntityLivingBase caster);
	}
	
	protected static class TriggerData {
		public List<EntityLivingBase> targets;
		public List<EntityLivingBase> others;
		public World world;
		public List<BlockPos> pos;
		
		public TriggerData(List<EntityLivingBase> targets, List<EntityLivingBase> others, World world, List<BlockPos> pos) {
			this.targets = targets;
			this.others = others;
			this.pos = pos;
		}
	}
	
	private String key;
	
	protected SpellTrigger(String key) {
		this.key = key;
	}
	
	public String getTriggerKey() {
		return key;
	}
	
	/**
	 * Construct a SpellTriggerInstance for this trigger for the given state.
	 * @param target
	 * @param targetPos
	 * @return
	 */
	public abstract SpellTriggerInstance instance(SpellState state, World world, Vec3 pos, float pitch, float yaw);
	
	/**
	 * 
	 * @param caster
	 * @param pos
	 * @param pitch
	 * @param yaw
	 * @return
	 */
	protected abstract TriggerData getTargetData(SpellState state, World world, Vec3 pos, float pitch, float yaw);
	
}
