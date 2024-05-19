package com.smanzana.nostrummagica.spells.components.triggers;

import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.SpellPartProperties;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Trigger that's cast instantly. AoE spells that can immediately find
 * their targets
 * @author Skyler
 *
 */
public abstract class InstantTrigger extends SpellTrigger {
	
	public class InstantTriggerInstance extends SpellTrigger.SpellTriggerInstance {

		private World world;
		private Vector3d pos;
		private float pitch;
		private float yaw;
		
		public InstantTriggerInstance(SpellState state, World world, Vector3d pos, float pitch, float yaw) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
		}
		
		@Override
		public void init(LivingEntity caster) {
			// We are instant! Whoo!
			TriggerData data = getTargetData(this.getState(), world, pos, pitch, yaw);
			this.trigger(data);
		}
	}
	
	public InstantTrigger(String key) {
		super(key);
	}
	
	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellPartProperties params) {
		return new InstantTriggerInstance(state, world, pos, pitch, yaw);
	}
	
	/**
	 * 
	 * @param caster
	 * @param pos
	 * @param pitch
	 * @param yaw
	 * @return
	 */
	protected abstract TriggerData getTargetData(SpellState state, World world, Vector3d pos, float pitch, float yaw);
	
}
