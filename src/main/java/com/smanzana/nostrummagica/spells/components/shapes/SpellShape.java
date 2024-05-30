package com.smanzana.nostrummagica.spells.components.shapes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.SpellCharacteristics;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Area that is hit in a spell
 * @author Skyler
 *
 */
public abstract class SpellShape {
	
	private static Map<String, SpellShape> registry = new HashMap<>();
	
	protected static void register(SpellShape shape) {
		registry.put(shape.getShapeKey(), shape);
	}
	
	public static SpellShape get(String name) {
		return registry.get(name);
	}
	
	public static Collection<String> getAllNames() {
		return registry.keySet();
	}
	
	public static Collection<SpellShape> getAllShapes() {
		return registry.values();
	}
	
	public static void fireRegisterEvent() {
		FMLJavaModLoadingContext.get().getModEventBus().post(new RegisterSpellShapeEvent(SpellShape::register));
	}
	
	public static final class RegisterSpellShapeEvent extends Event implements IModBusEvent {
		
		public static interface ISpellShapeRegistry {
			public void register(SpellShape shape);
		}
		
		protected final ISpellShapeRegistry registry;
		
		protected RegisterSpellShapeEvent(ISpellShapeRegistry registry) {
			this.registry = registry;
		}
		
		public ISpellShapeRegistry getRegistry() {
			return this.registry;
		}
		
	}
	
	public static final class SpellShapeAttributes {
		public final boolean terminal;
		public final boolean selectsEntities;
		public final boolean selectsBlocks;
		
		public SpellShapeAttributes(boolean terminal, boolean selectsEntities, boolean selectsBlocks) {
			this.terminal = terminal;
			this.selectsEntities = selectsEntities;
			this.selectsBlocks = selectsBlocks;
		}
	}
	
	public static abstract class SpellShapeInstance {
		private SpellState state; // The state to trigger
		
		public SpellShapeInstance(SpellState state) {
			this.state = state;
		}
				
		protected void trigger(TriggerData data) {
			trigger(data, false);
		}
		
		protected void trigger(TriggerData data, boolean forceSplit) {
			state.trigger(data.targets, data.world, data.pos, forceSplit);
		}
		
		protected SpellState getState() {
			return state;
		}
		
		/**
		 * Called after trigger stuff is set up in owning spell.
		 * Spawn whatever's needed and start performing the shape's function.
		 * For example, spawn the projectile and wait for it to hit something.
		 * @param caster
		 */
		public abstract void spawn(LivingEntity caster);
	}
	
	protected static class TriggerData {
		public final List<LivingEntity> targets;
		public final World world;
		public final List<BlockPos> pos;
		
		public TriggerData(List<LivingEntity> targets, World world, List<BlockPos> pos) {
			this.targets = targets;
			this.pos = pos;
			this.world = world;
		}
	}
	
	private String key;
	
	public SpellShape(String key) {
		this.key = key;
	}
	
	public String getShapeKey() {
		return key;
	}
	
	public abstract String getDisplayName();
	
	/**
	 * Spawn an instance to perform this spell shape's action.
	 * For exaple, get ready to spawn a projectile to fly through the air that will eventually 'trigger' the next part of the spell.
	 * @param state
	 * @param world
	 * @param pos
	 * @param pitch
	 * @param yaw
	 * @param properties
	 * @param characteristics
	 * @return
	 */
	public abstract SpellShapeInstance createInstance(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics);
	
//	public void perform(SpellAction action,
//						SpellPartProperties param,
//						LivingEntity target,
//						World world,
//						BlockPos pos,
//						float efficiency,
//						List<LivingEntity> affectedEnts,
//						List<BlockPos> affectedPos) {
//		
//		if (target != null && (world == null || pos == null)) {
//			world = target.world;
//			Vector3d vec = target.getPositionVec();
//			pos = new BlockPos(vec.x, vec.y, vec.z);
//		}
//		
//		List<LivingEntity> entTargets = getTargets(param, target, world, pos);
//		if (entTargets != null && !entTargets.isEmpty())
//		for (LivingEntity ent : entTargets) {
//			if (ent != null) {
//				if (action.apply(ent, efficiency)) {
//					affectedEnts.add(ent);
//				}
//			}
//		}
//		
//		List<BlockPos> blockTargets = getTargetLocations(param, target, world, pos);
//		if (blockTargets != null && !blockTargets.isEmpty())
//		for (BlockPos bp : blockTargets) {
//			if (bp != null) {
//				if (action.apply(world, bp, efficiency)) {
//					affectedPos.add(bp);
//				}
//			}
//		}
//	}
	
	/**
	 * Possibly spawn visual fx for this shape
	 * @param world
	 * @param pos
	 * @param harmful Whether the effects of the spell this shape is being cast as part of appear to be harmful
	 */
	protected void spawnShapeEffect(LivingEntity caster,
			@Nullable LivingEntity target, World world, Vector3d pos,
			SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		
		spawnDefaultShapeEffect(caster, target, world, pos, properties, characteristics);
//		
//		
//		// One more for the shape itself
//		final @Nullable LivingEntity centerEnt = (targets == null || targets.isEmpty() ? null : targets.get(0));
//		final @Nullable BlockPos centerBP = (positions == null || positions.isEmpty() ? null : positions.get(0));
//		if (centerEnt != null || centerBP != null) {
//			final Vector3d centerPos = (centerEnt == null ? new Vector3d(centerBP.getX() + .5, centerBP.getY(), centerBP.getZ() + .5) : centerEnt.getPositionVec().add(0, centerEnt.getHeight() / 2, 0));
//			final float p= (shape.supportedFloats() == null || shape.supportedFloats().length == 0 ? 0 : (
//					param.level == 0f ? shape.supportedFloats()[0] : param.level));
//			
//		}
	}
	
	protected final void spawnDefaultShapeEffect(LivingEntity caster,
			@Nullable LivingEntity target, World world, Vector3d pos,
			SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		final float p = (supportedFloats() == null || supportedFloats().length == 0 ? 0 : (
				properties.level == 0f ? supportedFloats()[0] : properties.level));
		NostrumMagica.instance.proxy.spawnEffect(world, new SpellComponentWrapper(this),
				caster, null, target, pos,
				new SpellComponentWrapper(characteristics.element), characteristics.harmful, p);
	}
	
	/**
	 * Return a list of reagents required.
	 * Both type and count of the itemstacks will be respected.
	 * @return
	 */
	public abstract NonNullList<ItemStack> getReagents();
	
	/**
	 * Get the item used in the crafting recipe for a spell rune of this shape
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
	 * How much using this shape should cost
	 * @return
	 */
	public abstract int getManaCost();
	
	/**
	 * Return the weight cost of this shape. Should be 0+.
	 * @return
	 */
	public abstract int getWeight();

	/**
	 * Whether spells that start with this shape should request tracing when players have
	 * the spell selected.
	 * @return
	 */
	public abstract boolean shouldTrace(SpellShapePartProperties params);
	
	/**
	 * if {@link #shouldTrace(SpellPartProperties)} is true, how far to trace
	 * @param params
	 * @return
	 */
	public double getTraceRange(SpellShapePartProperties params) {
		return 0;
	}
	
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(false, true, true);
	}

	public SpellShapePartProperties getDefaultProperties() {
		final float val;
		if (this.supportedFloats() != null) {
			val = supportedFloats()[0];
		} else {
			val = 0f;
		}
		return new SpellShapePartProperties(val, false);
	}
	
	
}
