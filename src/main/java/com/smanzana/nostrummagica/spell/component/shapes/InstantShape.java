package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.List;

import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;

import net.minecraft.entity.LivingEntity;
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
		private final SpellLocation location;
		private final float pitch;
		private final float yaw;
		private final SpellShapePartProperties params;
		private final SpellCharacteristics characteristics;
		
		public InstantShapeInstance(InstantShape shape, ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
			super(state);
			this.shape = shape;
			this.world = world;
			this.location = location;
			this.pitch = pitch;
			this.yaw = yaw;
			this.params = params;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			// We are instant! Whoo!
			TriggerData data = shape.getTargetData(this.getState(), world, location, pitch, yaw, params, this.characteristics);
			this.trigger(data);
		}
	}
	
	public InstantShape(String key) {
		super(key);
	}
	
	@Override
	public SpellShapeInstance createInstance(ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		return new InstantShapeInstance(this, state, world, location, pitch, yaw, params, characteristics);
	}
	
	/**
	 * 
	 * @param location
	 * @param pitch
	 * @param yaw
	 * @param params TODO
	 * @param caster
	 * @return
	 */
	protected abstract TriggerData getTargetData(ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics);
	
	@Override
	public boolean supportsPreview(SpellShapePartProperties params) {
		return true;
	}
	
	protected boolean previewBlockHits(SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		return true;
	}
	
	protected boolean previewEntityHits(SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		TriggerData data = this.getTargetData(state, world, location, pitch, yaw, properties, characteristics);
		
		List<LivingEntity> entityHits = (this.previewEntityHits(properties, characteristics) ? data.targets : null);
		List<SpellLocation> blockHits = (this.previewBlockHits(properties, characteristics) ? data.locations : null);
		
		state.trigger(entityHits, data.world, blockHits);
		return (entityHits != null && !entityHits.isEmpty()) || (blockHits != null && !blockHits.isEmpty());
	}
	
}
