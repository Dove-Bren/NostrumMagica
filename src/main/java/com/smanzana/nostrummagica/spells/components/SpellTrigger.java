package com.smanzana.nostrummagica.spells.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * A 'trigger' in a spell. Either some condition or some selection field (like 'touch',
 * 'projectile', 'beam').
 * @author Skyler
 *
 */
public abstract class SpellTrigger {
	
	private static Map<String, SpellTrigger> registry = new HashMap<>();
	
	public static void register(SpellTrigger trigger) {
		registry.put(trigger.getTriggerKey(), trigger);
	}
	
	public static SpellTrigger get(String name) {
		return registry.get(name);
	}
	
	public static Collection<SpellTrigger> getAllTriggers() {
		return registry.values();
	}

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
				
		protected void trigger(TriggerData data) {
			trigger(data, false);
		}
		
		protected void trigger(TriggerData data, boolean forceSplit) {
			state.trigger(data.targets, data.others, data.world, data.pos, forceSplit);
		}
		
		protected SpellState getState() {
			return state;
		}
		
		/**
		 * Called after trigger stuff is set up in owning spell.
		 * @param caster
		 */
		public abstract void init(LivingEntity caster);
	}
	
	protected static class TriggerData {
		public List<LivingEntity> targets;
		public List<LivingEntity> others;
		public World world;
		public List<BlockPos> pos;
		
		public TriggerData(List<LivingEntity> targets, List<LivingEntity> others, World world, List<BlockPos> pos) {
			this.targets = targets;
			this.others = others;
			this.pos = pos;
			this.world = world;
		}
	}
	
	private String key;
	
	protected SpellTrigger(String key) {
		this.key = key;
	}
	
	public String getTriggerKey() {
		return key;
	}
	
	public abstract String getDisplayName();
	
	/**
	 * Construct a SpellTriggerInstance for this trigger for the given state.
	 * @param target
	 * @param targetPos
	 * @return
	 */
	public abstract SpellTriggerInstance instance(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellPartParam params);
	
	/**
	 * 
	 * @return
	 */
	public abstract int getManaCost();
	
	/**
	 * Return a list of reagents required.
	 * Both type and count of the itemstacks will be respected.
	 * @return
	 */
	public abstract NonNullList<ItemStack> getReagents();

	/**
	 * Return the unique item used when crafting this rune.
	 * Must be unique between all triggers or you risk not being able to craft
	 * your trigger rune!
	 * @return
	 */
	public abstract ItemStack getCraftItem();
	

	
	/**
	 * Whether this shape supports a boolean switch in its SpellPartParam
	 * @return
	 */
	public abstract boolean supportsBoolean();
	
	/**
	 * Display name for the boolean option.
	 * @return
	 */
	public abstract String supportedBooleanName();
	
	/**
	 * If this shape supports float values in its SpellPartParams, which floats are
	 * accepted.
	 * @return
	 */
	public abstract float[] supportedFloats();
	
	/**
	 * Array of itemstack costs for the above floats.
	 * Should be the same size as the array returned by supportedFloats()
	 * The idea is you return more valuable materials the higher the float.
	 * @return
	 */
	public abstract NonNullList<ItemStack> supportedFloatCosts();
	
	/**
	 * Display name for the float option. Should be translated already
	 */
	public abstract String supportedFloatName();
	
	/**
	 * Return the weight cost of this trigger. Should be 0+.
	 * @return
	 */
	public abstract int getWeight();
}
