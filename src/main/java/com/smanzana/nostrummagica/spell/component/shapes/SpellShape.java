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
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
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
	
	public BaseComponent getDisplayName() {
		return new TranslatableComponent("shape." + this.getShapeKey() + ".name");
	}
	
	public BaseComponent getDescription() {
		return new TranslatableComponent("shape." + this.getShapeKey() + ".desc");
	}
	
	/**
	 * Spawn an instance to perform this spell shape's action.
	 * For exaple, get ready to spawn a projectile to fly through the air that will eventually 'trigger' the next part of the spell.
	 * @param state
	 * @param entity the entity (if any) that the previous components of the spell have directly targetted. Can be null when previous spell stages
	 * 				 indicate world location hits, rather than entity hits
	 * @param location
	 * @param pitch
	 * @param yaw
	 * @param properties
	 * @param characteristics
	 * @return
	 */
	public abstract SpellShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics);
	
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
		NostrumMagica.instance.proxy.spawnSpellShapeVfx(location.world, this, properties,
				caster, null, target, location.hitPosition,
				characteristics);
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
	 * Array of itemstack costs for changing to the different values of the provided property.
	 * Should be the same size as the list of possible values of the property, or NULL.
	 * The idea is you return more valuable materials the more expensive the value should be to get.
	 * @param property which property's costs to query
	 * @return
	 */
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
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
	public abstract boolean shouldTrace(Player player, SpellShapeProperties params);
	
	/**
	 * if {@link #shouldTrace(PlayerEntity, SpellPartProperties)} is true, how far to trace
	 * @param player TODO
	 * @param params
	 * @return
	 */
	public double getTraceRange(Player player, SpellShapeProperties params) {
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
	 * @param entity TODO
	 * @param location
	 * @param pitch
	 * @param yaw
	 * @param properties
	 * @param characteristics
	 * @return whether to continue with the spell (return true), or if the spell is expected to fizzle here (return false)
	 */
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		return false;
	}
	
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(false, true, true);
	}

	public SpellShapeProperties getDefaultProperties() {
		return baseProperties.copy();
	}

	@SuppressWarnings("unchecked")
	public <T> SpellShapeProperty<T> getProperty(String propName) {
		SpellShapeProperty<T> prop = null;
		for (SpellShapeProperty<?> property : this.getDefaultProperties().getProperties()) {
			if (property.getName().equalsIgnoreCase(propName)) {
				prop = (SpellShapeProperty<T>) property;
				break;
			}
		}
		return prop;
	}

	public List<Component> getTooltip() {
		return List.of(
				getDisplayName().copy().withStyle(ChatFormatting.BOLD),
				getDescription()
			);
	}
	
	public boolean canIncant() {
		return true;
	}
	
	
}
