package com.smanzana.nostrummagica.spells.components.shapes;

import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.SpellCharacteristics;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Shape that can immediately be resolved.
 * For example, touch is resolve instantly.
 * @author Skyler
 *
 */
public abstract class InstantShape extends SpellShape {

	public static class InstantShapeInstance extends SpellShapeInstance {

		private final InstantShape shape;
		private final World world;
		private final Vector3d pos;
		private final float pitch;
		private final float yaw;
		private final SpellCharacteristics characteristics;
		
		public InstantShapeInstance(InstantShape shape, SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellCharacteristics characteristics) {
			super(state);
			this.shape = shape;
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			// We are instant! Whoo!
			TriggerData data = shape.getTargetData(this.getState(), world, pos, pitch, yaw, this.characteristics);
			this.trigger(data);
		}
	}
	
	public InstantShape(String key) {
		super(key);
	}
	
	@Override
	public SpellShapeInstance createInstance(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		return new InstantShapeInstance(this, state, world, pos, pitch, yaw, characteristics);
	}
	
	/**
	 * 
	 * @param caster
	 * @param pos
	 * @param pitch
	 * @param yaw
	 * @return
	 */
	protected abstract TriggerData getTargetData(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellCharacteristics characteristics);
	
}
