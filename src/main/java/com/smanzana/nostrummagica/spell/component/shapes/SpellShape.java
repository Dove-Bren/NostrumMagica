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
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
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
			state.trigger(data.targets, data.locations, stageEfficiency, forceSplit);
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
		public final List<SpellLocation> locations;
		
		public TriggerData(List<LivingEntity> targets, List<SpellLocation> locations) {
			this.targets = targets;
			this.locations = locations;
		}
	}
	
	private final String key;
	protected final SpellShapeProperties baseProperties;
	
	public SpellShape(String key) {
		this.key = key;
		this.baseProperties = new SpellShapeProperties();
		
		this.registerProperties();
	}
	
	protected void registerProperties() {
		
	}
	
	public String getShapeKey() {
		return key;
	}
	
	public abstract String getDisplayName();
	
	/**
	 * Spawn an instance to perform this spell shape's action.
	 * For exaple, get ready to spawn a projectile to fly through the air that will eventually 'trigger' the next part of the spell.
	 * @param state
	 * @param location
	 * @param pitch
	 * @param yaw
	 * @param properties
	 * @param characteristics
	 * @return
	 */
	public abstract SpellShapeInstance createInstance(ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics);
	
	/**
	 * Possibly spawn visual fx for this shape
	 * @param location
	 * @param harmful Whether the effects of the spell this shape is being cast as part of appear to be harmful
	 */
	protected void spawnShapeEffect(LivingEntity caster,
			@Nullable LivingEntity target, SpellLocation location, SpellShapeProperties properties,
			SpellCharacteristics characteristics) {
		
		spawnDefaultShapeEffect(caster, target, location, properties, characteristics);
	}
	
	protected final void spawnDefaultShapeEffect(LivingEntity caster,
			@Nullable LivingEntity target, SpellLocation location, SpellShapeProperties properties,
			SpellCharacteristics characteristics) {
//		final float p = (supportedFloats() == null || supportedFloats().length == 0 ? 0 : (
//				properties.level == 0f ? supportedFloats()[0] : properties.level));
		final float p = 1f; int unused;
		NostrumMagica.instance.proxy.spawnEffect(location.world, new SpellComponentWrapper(this),
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
	 * Array of itemstack costs for the above floats.
	 * Should be the same size as the array returned by supportedFloats()
	 * The idea is you return more valuable materials the higher the float.
	 * @param property TODO
	 * @return
	 */
	public <T> NonNullList<ItemStack> supportedFloatCosts(SpellShapeProperty<T> property) {
		return null;
	}
	
	/**
	 * How much using this shape should cost
	 * @param properties TODO
	 * @return
	 */
	public abstract int getManaCost(SpellShapeProperties properties);
	
	/**
	 * Return the weight cost of this shape. Should be 0+.
	 * @param properties TODO
	 * @return
	 */
	public abstract int getWeight(SpellShapeProperties properties);

	/**
	 * Whether spells that start with this shape should request tracing when players have
	 * the spell selected.
	 * @param player TODO
	 * @return
	 */
	public abstract boolean shouldTrace(PlayerEntity player, SpellShapeProperties params);
	
	/**
	 * if {@link #shouldTrace(PlayerEntity, SpellPartProperties)} is true, how far to trace
	 * @param player TODO
	 * @param params
	 * @return
	 */
	public double getTraceRange(PlayerEntity player, SpellShapeProperties params) {
		return 0;
	}
	
	/**
	 * Whether this shape supports previewing.
	 * If this returns true, addToPreview should add any preview materials in #addToPreview().
	 * @param params
	 * @return
	 */
	public abstract boolean supportsPreview(SpellShapeProperties params);
	
	/**
	 * Add information about how this shape is expected to work if cast with the given arguments.
	 * This is intended to preview the actual shape of the spell instantly. Shapes that take time to actually run
	 * (like a projectile that is spawned and flies) should approximate the behavior.
	 * @param builder
	 * @param state
	 * @param location
	 * @param pitch
	 * @param yaw
	 * @param properties
	 * @param characteristics
	 * @return whether to continue with the spell (return true), or if the spell is expected to fizzle here (return false)
	 */
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		return false;
	}
	
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(false, true, true);
	}

	public SpellShapeProperties getDefaultProperties() {
		return baseProperties.copy();
	}
	
	
}
