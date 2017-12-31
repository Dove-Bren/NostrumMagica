package com.smanzana.nostrummagica.spells.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A 'trigger' in a spell. Either some condition or some selection field (like 'touch',
 * 'projectile', 'beam').
 * @author Skyler
 *
 */
public abstract class SpellTrigger {
	
	private static Map<String, SpellTrigger> registry = new HashMap<>();
	
	public static void register(SpellTrigger shape) {
		registry.put(shape.getTriggerKey(), shape);
	}
	
	public static SpellTrigger get(String name) {
		return registry.get(name);
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
	public abstract SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw, SpellPartParam params);
	
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
	public abstract List<ItemStack> getReagents();
}
