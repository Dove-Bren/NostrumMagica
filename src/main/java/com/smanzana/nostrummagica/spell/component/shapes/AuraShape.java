package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listener.PlayerListener.Event;
import com.smanzana.nostrummagica.listener.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.BooleanSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.FloatSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.Entities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.NonNullList;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.Lazy;

/**
 * Shape that can immediately be resolved.
 * For example, touch is resolve instantly.
 * @author Skyler
 *
 */
public class AuraShape extends AreaShape {

	public static class AuraTriggerInstance extends SpellShapeInstance implements IGenericListener {

		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 20) / TICK_RATE; // 20 seconds

		private final LivingEntity origin;
		private final float radius;
		private final Level world;
		private final boolean includeAllies;
		private final SpellCharacteristics characteristics;
		
		private int aliveCycles;
		private boolean dead;
		private final Map<LivingEntity, Integer> affected;
		
		public AuraTriggerInstance(ISpellState state, LivingEntity entity, float radius, boolean includeAllies, SpellCharacteristics characteristics) {
			super(state);
			this.radius = radius;
			this.origin = entity;
			this.world = entity.getCommandSenderWorld();
			this.includeAllies = includeAllies;
			this.characteristics = characteristics;
			
			dead = false;
			aliveCycles = 0;
			affected = new HashMap<>();
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			// Register timer for life and for effects
			NostrumMagica.playerListener.registerTimer(this, 0, TICK_RATE);
			
			doEffect();
		}
		
		protected boolean canAffect(LivingEntity entity) {
			return entity != null
					&& entity.isAlive()
					&& (
							includeAllies
							|| !NostrumMagica.IsSameTeam(entity, origin)
							);
		}
		
		protected boolean isInArea(LivingEntity entity) {
			return origin.distanceTo(entity) <= radius;
		}

		protected void doEffect() {
			if (origin != null && origin.isAlive())
			for (int i = 0; i < radius + 1; i++) {
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						1,
						origin.getX(),
						origin.getY() + (origin.getEyeHeight() / 2), // technically correct but visually sucky cause 50% will be underground
						origin.getZ(),
						.1,
						30, 0, // lifetime + jitter
						Vec3.ZERO, (new Vec3(.2, .2, .2)).scale(radius / 4)
						).color(characteristics.element.getColor())
						);
				NostrumParticles.LIGHTNING_STATIC.spawn(world, new SpawnParams(
						2,
						origin.getX(),
						origin.getY() + (origin.getEyeHeight() / 2), // technically correct but visually sucky cause 50% will be underground
						origin.getZ(),
						radius,
						20, 0, // lifetime + jitter
						new Vec3(0, -.025, 0), new Vec3(0, .05, 0)
						).color(characteristics.element.getColor()));
			}
		}

		@Override
		public boolean onEvent(Event type, LivingEntity entity, Object empty) {
			if (dead)
				return true;
			
			if (type == Event.TIME) {
				
				if (!origin.isAlive() || !DimensionUtils.InDimension(origin, world)) {
					this.dead = true;
					return true;
				}
				
				doEffect();
				
				aliveCycles++;
				if (aliveCycles >= NUM_TICKS) { // 20 seconds
					this.dead = true;
					return true;
				}
				
				// Check all entities in the world
				for (LivingEntity e : Entities.GetEntities((ServerLevel) world, (e) -> {return canAffect(e) && isInArea(e);})) {
					if (visitEntity(e)) {
						TriggerData data = new TriggerData(
								Lists.newArrayList(e),
								null
								);
						this.trigger(data, 1f, true);
					}
				}
				
				return false;
			}
			
			return false;
		}
		
		/**
		 * Check if entity should experirence effects.
		 * Also tracks time when returned true to slow down effects.
		 * @param entity
		 * @return
		 */
		protected boolean visitEntity(LivingEntity entity) {
			if (entity == null || !entity.isAlive()) {
				return false;
			}
			
			Integer last = affected.get(entity);
			if (last == null
					|| (last + 40 < entity.tickCount)
					) {
				affected.put(entity, entity.tickCount);
				return true;
			}
			return false;
		}
	}
	
	private static final String ID = "aura";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY,
			ReagentItem.CreateStack(ReagentType.SKY_ASH, 1),
			ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1),
			ReagentItem.CreateStack(ReagentType.CRYSTABLOOM, 1),
			ReagentItem.CreateStack(ReagentType.MANI_DUST, 1)));
	
	public static final SpellShapeProperty<Boolean> AFFECT_ALLIES = new BooleanSpellShapeProperty("hit_allies");
	public static final SpellShapeProperty<Float> RADIUS = new FloatSpellShapeProperty("radius", 2f, 3f, 5f, 10f);
	
	protected AuraShape(String key) {
		super(key);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		baseProperties.addProperty(AFFECT_ALLIES, false)
			.addProperty(RADIUS);
	}
	
	public AuraShape() {
		this(ID);
	}
	
	protected float getRadius(SpellShapeProperties properties) {
		return properties.getValue(RADIUS);
	}
	
	protected boolean includeAllies(SpellShapeProperties properties) {
		return properties.getValue(AFFECT_ALLIES);
	}
	
	@Override
	public SpellShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		return new AuraTriggerInstance(state, state.getSelf(),
				getRadius(params),
				includeAllies(params),
				characteristics);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.GUNPOWDER);
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (costs == null) {
			costs = NonNullList.of(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.DRAGON_BREATH),
				new ItemStack(NostrumItems.resourceSpriteCore, 1),
				new ItemStack(NostrumItems.crystalLarge, 1)
				);
		}
		
		if (property == RADIUS) {
			return costs;
		} else {
			return super.getPropertyItemRequirements(property);
		}
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		final float range = getRadius(properties);
		return 50 + 100 * ((int) range / 5); // 50, 50, 150, 250
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		final float range = getRadius(properties);
		return range < 5 ? 2 : 3;
	}

	@Override
	public boolean shouldTrace(Player player, SpellShapeProperties params) {
		return false;
	}
	
	@Override
	public boolean affectsBlocks(SpellShapeProperties properties) {
		return false;
	}
	
	@Override
	public boolean affectsEntities(SpellShapeProperties properties) {
		return true;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return super.getAttributes(params);
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final float range = this.getRadius(getDefaultProperties());
		builder.add(new SpellShapePreviewComponent.Disk(location.hitPosition.add(0, .5, 0), range));
		
		List<LivingEntity> ents = location.world.getEntities(state.getSelf(), Shapes.block().bounds().move(location.hitPosition).inflate(range + 1), (ent) -> 
			ent instanceof LivingEntity && location.hitPosition.distanceTo(ent.position()) <= range
		).stream().map(ent -> (LivingEntity) ent).collect(Collectors.toList());
		if (ents != null && !ents.isEmpty()) {
			state.trigger(ents, null);
		}
		
		return !ents.isEmpty();
	}
	
	public SpellShapeProperties makeProps(float radius, boolean includeAllies) {
		return this.getDefaultProperties()
				.setValue(AFFECT_ALLIES, includeAllies)
				.setValue(RADIUS, radius);
	}
}
