package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
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
		private ISpellState state; // The state to trigger
		
		public SpellShapeInstance(ISpellState state) {
			this.state = state;
		}
				
		protected void trigger(TriggerData data) {
			trigger(data, 1f, false);
		}
		
		protected void trigger(TriggerData data, float stageEfficiency, boolean forceSplit) {
			state.trigger(data.targets, data.world, data.locations, stageEfficiency, forceSplit);
		}
		
		protected ISpellState getState() {
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
		public final List<SpellLocation> locations;
		
		public TriggerData(List<LivingEntity> targets, World world, List<SpellLocation> locations) {
			this.targets = targets;
			this.locations = locations;
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
	 * @param location
	 * @param pitch
	 * @param yaw
	 * @param properties
	 * @param characteristics
	 * @return
	 */
	public abstract SpellShapeInstance createInstance(ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics);
	
	/**
	 * Possibly spawn visual fx for this shape
	 * @param world
	 * @param location
	 * @param harmful Whether the effects of the spell this shape is being cast as part of appear to be harmful
	 */
	protected void spawnShapeEffect(LivingEntity caster,
			@Nullable LivingEntity target, World world, SpellLocation location,
			SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		
		spawnDefaultShapeEffect(caster, target, world, location, properties, characteristics);
	}
	
	protected final void spawnDefaultShapeEffect(LivingEntity caster,
			@Nullable LivingEntity target, World world, SpellLocation location,
			SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		final float p = (supportedFloats() == null || supportedFloats().length == 0 ? 0 : (
				properties.level == 0f ? supportedFloats()[0] : properties.level));
		NostrumMagica.instance.proxy.spawnEffect(world, new SpellComponentWrapper(this),
				caster, null, target, location.hitPosition,
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
	 * @param player TODO
	 * @return
	 */
	public abstract boolean shouldTrace(PlayerEntity player, SpellShapePartProperties params);
	
	/**
	 * if {@link #shouldTrace(PlayerEntity, SpellPartProperties)} is true, how far to trace
	 * @param player TODO
	 * @param params
	 * @return
	 */
	public double getTraceRange(PlayerEntity player, SpellShapePartProperties params) {
		return 0;
	}
	
	/**
	 * Whether this shape supports previewing.
	 * If this returns true, addToPreview should add any preview materials in #addToPreview().
	 * @param params
	 * @return
	 */
	public abstract boolean supportsPreview(SpellShapePartProperties params);
	
	/**
	 * Add information about how this shape is expected to work if cast with the given arguments.
	 * This is intended to preview the actual shape of the spell instantly. Shapes that take time to actually run
	 * (like a projectile that is spawned and flies) should approximate the behavior.
	 * @param builder
	 * @param state
	 * @param world
	 * @param location
	 * @param pitch
	 * @param yaw
	 * @param properties
	 * @param characteristics
	 * @return whether to continue with the spell (return true), or if the spell is expected to fizzle here (return false)
	 */
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		return false;
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
